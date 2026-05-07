let allClassSessions = [];
let allCoaches = [];

document.addEventListener("DOMContentLoaded", () => {
    setupInitialDates();
    setupEvents();
    loadCoaches();
    loadClassSessions();
});

function setupInitialDates() {
    const today = new Date().toISOString().split("T")[0];

    document.getElementById("classDate").value = today;
    document.getElementById("filterDate").value = today;
}

function setupEvents() {
    document.getElementById("classSessionForm").addEventListener("submit", saveClassSession);
    document.getElementById("clearFormButton").addEventListener("click", clearForm);
    document.getElementById("reloadButton").addEventListener("click", loadClassSessions);
    document.getElementById("filterDateButton").addEventListener("click", loadClassSessionsByDate);
    document.getElementById("loadAllButton").addEventListener("click", loadClassSessions);
    document.getElementById("closeReservationsButton").addEventListener("click", closeReservationsCard);
}

async function loadCoaches() {
    const coachSelect = document.getElementById("coachId");

    try {
        const users = await apiRequest("/users");

        allCoaches = users.filter(user =>
            user.roleName === "COACH" &&
            user.status === "ACTIVE"
        );

        if (!allCoaches.length) {
            coachSelect.innerHTML = `<option value="">No active coaches found</option>`;
            return;
        }

        coachSelect.innerHTML = `
            <option value="">Select coach</option>
            ${allCoaches.map(coach => `
                <option value="${coach.id}">
                    ${escapeHtml(coach.firstName)} ${escapeHtml(coach.lastName)} - ${escapeHtml(coach.email)}
                </option>
            `).join("")}
        `;
    } catch (error) {
        coachSelect.innerHTML = `<option value="">Could not load coaches</option>`;
        showMessage(error.message || "Could not load coaches.", "error");
    }
}

async function saveClassSession(event) {
    event.preventDefault();

    const classSessionId = document.getElementById("classSessionId").value;
    const payload = buildClassSessionPayload();

    try {
        if (classSessionId) {
            await apiRequest(`/class-sessions/${classSessionId}`, {
                method: "PUT",
                body: JSON.stringify(payload)
            });

            showMessage("Class session updated successfully.", "success");
        } else {
            await apiRequest("/class-sessions", {
                method: "POST",
                body: JSON.stringify(payload)
            });

            showMessage("Class session created successfully.", "success");
        }

        clearForm();
        await loadClassSessionsByDate();
    } catch (error) {
        showMessage(error.message || "Could not save class session.", "error");
    }
}

function buildClassSessionPayload() {
    return {
        className: document.getElementById("className").value.trim(),
        description: document.getElementById("description").value.trim(),
        classDate: document.getElementById("classDate").value,
        startTime: document.getElementById("startTime").value,
        endTime: document.getElementById("endTime").value,
        capacity: Number(document.getElementById("capacity").value),
        coachId: Number(document.getElementById("coachId").value),
        notes: document.getElementById("notes").value.trim()
    };
}

async function loadClassSessions() {
    const tableBody = document.getElementById("classSessionsTableBody");

    tableBody.innerHTML = `
        <tr>
            <td colspan="7">Loading class sessions...</td>
        </tr>
    `;

    try {
        allClassSessions = await apiRequest("/class-sessions");
        renderClassSessions(allClassSessions);
    } catch (error) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="7">Could not load class sessions: ${escapeHtml(error.message)}</td>
            </tr>
        `;
    }
}

async function loadClassSessionsByDate() {
    const date = document.getElementById("filterDate").value;

    if (!date) {
        showMessage("Please select a date.", "error");
        return;
    }

    const tableBody = document.getElementById("classSessionsTableBody");

    tableBody.innerHTML = `
        <tr>
            <td colspan="7">Loading class sessions for ${escapeHtml(date)}...</td>
        </tr>
    `;

    try {
        allClassSessions = await apiRequest(`/class-sessions/date/${date}`);
        renderClassSessions(allClassSessions);
    } catch (error) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="7">Could not load class sessions: ${escapeHtml(error.message)}</td>
            </tr>
        `;
    }
}

function renderClassSessions(classSessions) {
    const tableBody = document.getElementById("classSessionsTableBody");

    if (!classSessions.length) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="7">No class sessions found.</td>
            </tr>
        `;
        return;
    }

    const sorted = [...classSessions].sort((a, b) => {
        const dateCompare = String(a.classDate).localeCompare(String(b.classDate));
        if (dateCompare !== 0) return dateCompare;
        return String(a.startTime).localeCompare(String(b.startTime));
    });

    tableBody.innerHTML = sorted.map(session => `
        <tr>
            <td>${escapeHtml(session.classDate)}</td>
            <td>${formatTime(session.startTime)} - ${formatTime(session.endTime)}</td>
            <td>
                <strong>${escapeHtml(session.className)}</strong>
                <br>
                <small>${escapeHtml(session.description || "-")}</small>
            </td>
            <td>${escapeHtml(session.coachName || `Coach #${session.coachId}`)}</td>
            <td>
                ${session.reservedSpots}/${session.capacity}
                <br>
                <small>${session.availableSpots} available</small>
            </td>
            <td>
                <span class="badge ${getStatusBadgeClass(session.status)}">
                    ${escapeHtml(session.status)}
                </span>
            </td>
            <td>
                <div class="row-actions">
                    <button type="button" class="secondary" onclick="editClassSession(${session.id})">Edit</button>
                    <button type="button" class="secondary" onclick="viewReservations(${session.id})">Reservations</button>
                    <button type="button" class="secondary" onclick="changeClassStatus(${session.id}, 'OPEN')">Open</button>
                    <button type="button" class="secondary" onclick="changeClassStatus(${session.id}, 'CANCELLED')">Cancel</button>
                    <button type="button" class="secondary" onclick="changeClassStatus(${session.id}, 'COMPLETED')">Complete</button>
                    <button type="button" class="danger" onclick="deleteClassSession(${session.id})">Delete</button>
                </div>
            </td>
        </tr>
    `).join("");
}

function editClassSession(id) {
    const session = allClassSessions.find(item => Number(item.id) === Number(id));

    if (!session) {
        showMessage("Class session not found.", "error");
        return;
    }

    document.getElementById("formTitle").textContent = "Edit Class Session";
    document.getElementById("classSessionId").value = session.id;
    document.getElementById("className").value = session.className || "";
    document.getElementById("description").value = session.description || "";
    document.getElementById("classDate").value = session.classDate || "";
    document.getElementById("startTime").value = trimSeconds(session.startTime);
    document.getElementById("endTime").value = trimSeconds(session.endTime);
    document.getElementById("capacity").value = session.capacity || "";
    document.getElementById("coachId").value = session.coachId || "";
    document.getElementById("notes").value = session.notes || "";

    window.scrollTo({ top: 0, behavior: "smooth" });
}

async function changeClassStatus(id, status) {
    const confirmed = confirm(`Change this class session to ${status}?`);

    if (!confirmed) return;

    try {
        await apiRequest(`/class-sessions/${id}/status`, {
            method: "PATCH",
            body: JSON.stringify({ status })
        });

        showMessage(`Class session changed to ${status}.`, "success");
        await reloadCurrentView();
    } catch (error) {
        showMessage(error.message || "Could not update class status.", "error");
    }
}

async function deleteClassSession(id) {
    const confirmed = confirm("Delete this class session? Use CANCELLED if you want to preserve it historically.");

    if (!confirmed) return;

    try {
        await apiRequest(`/class-sessions/${id}`, {
            method: "DELETE"
        });

        showMessage("Class session deleted successfully.", "success");
        await reloadCurrentView();
    } catch (error) {
        showMessage(error.message || "Could not delete class session.", "error");
    }
}

async function viewReservations(classSessionId) {
    const reservationsCard = document.getElementById("reservationsCard");
    const reservationsTitle = document.getElementById("reservationsTitle");
    const reservationsTableBody = document.getElementById("reservationsTableBody");

    const session = allClassSessions.find(item => Number(item.id) === Number(classSessionId));

    reservationsCard.classList.remove("hidden");
    reservationsTitle.textContent = session
        ? `Reservations - ${session.className} (${session.classDate} ${formatTime(session.startTime)})`
        : "Class Reservations";

    reservationsTableBody.innerHTML = `
        <tr>
            <td colspan="5">Loading reservations...</td>
        </tr>
    `;

    try {
        const reservations = await apiRequest(`/reservations/class-session/${classSessionId}`);

        if (!reservations.length) {
            reservationsTableBody.innerHTML = `
                <tr>
                    <td colspan="5">No reservations for this class yet.</td>
                </tr>
            `;
            return;
        }

        reservationsTableBody.innerHTML = reservations.map(reservation => `
            <tr>
                <td>${escapeHtml(reservation.athleteName || `Athlete #${reservation.athleteId}`)}</td>
                <td>${escapeHtml(reservation.athleteEmail || "-")}</td>
                <td>
                    <span class="badge ${getReservationBadgeClass(reservation.status)}">
                        ${escapeHtml(reservation.status)}
                    </span>
                </td>
                <td>${formatDateTime(reservation.reservedAt)}</td>
                <td>${escapeHtml(reservation.notes || "-")}</td>
            </tr>
        `).join("");
    } catch (error) {
        reservationsTableBody.innerHTML = `
            <tr>
                <td colspan="5">Could not load reservations: ${escapeHtml(error.message)}</td>
            </tr>
        `;
    }
}

function closeReservationsCard() {
    document.getElementById("reservationsCard").classList.add("hidden");
}

async function reloadCurrentView() {
    const date = document.getElementById("filterDate").value;

    if (date) {
        await loadClassSessionsByDate();
    } else {
        await loadClassSessions();
    }
}

function clearForm() {
    document.getElementById("formTitle").textContent = "Create Class Session";
    document.getElementById("classSessionForm").reset();
    document.getElementById("classSessionId").value = "";
    setupInitialDates();
}

function getStatusBadgeClass(status) {
    if (status === "OPEN") return "green";
    if (status === "FULL") return "orange";
    if (status === "CANCELLED") return "red";
    if (status === "COMPLETED") return "blue";
    return "";
}

function getReservationBadgeClass(status) {
    if (status === "ACTIVE") return "green";
    if (status === "CANCELLED") return "red";
    if (status === "NO_SHOW") return "orange";
    if (status === "ATTENDED") return "blue";
    return "";
}

function trimSeconds(time) {
    if (!time) return "";
    return String(time).substring(0, 5);
}

function formatTime(time) {
    return trimSeconds(time);
}

function formatDateTime(value) {
    if (!value) return "-";

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;

    return date.toLocaleString();
}

function showMessage(message, type = "info") {
    const messageBox = document.getElementById("messageBox");

    messageBox.textContent = message;
    messageBox.className = `message ${type}`;

    setTimeout(() => {
        messageBox.className = "message hidden";
        messageBox.textContent = "";
    }, 5000);
}

function escapeHtml(value) {
    if (value === null || value === undefined) return "";

    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}