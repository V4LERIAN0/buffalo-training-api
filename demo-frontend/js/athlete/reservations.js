let currentUser = null;
let classSessions = [];
let myReservations = [];

document.addEventListener("DOMContentLoaded", () => {
    currentUser = getCurrentUser();

    if (!currentUser || !currentUser.id) {
        showMessage("Could not find logged-in user. Please login again.", "error");
        return;
    }

    setupInitialDate();
    setupEvents();
    loadClassesByDate();
    loadMyReservations();
});

function setupInitialDate() {
    const today = new Date().toISOString().split("T")[0];
    document.getElementById("classDateFilter").value = today;
}

function setupEvents() {
    document.getElementById("loadClassesButton").addEventListener("click", loadClassesByDate);
    document.getElementById("reloadReservationsButton").addEventListener("click", loadMyReservations);
}

async function loadClassesByDate() {
    const date = document.getElementById("classDateFilter").value;
    const tableBody = document.getElementById("classesTableBody");

    if (!date) {
        showMessage("Please select a date.", "error");
        return;
    }

    tableBody.innerHTML = `
        <tr>
            <td colspan="6">Loading classes...</td>
        </tr>
    `;

    try {
        classSessions = await apiRequest(`/class-sessions/date/${date}`);
        renderClasses(classSessions);
    } catch (error) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="6">Could not load classes: ${escapeHtml(error.message)}</td>
            </tr>
        `;
    }
}

function renderClasses(classes) {
    const tableBody = document.getElementById("classesTableBody");

    if (!classes.length) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="6">No classes found for this date.</td>
            </tr>
        `;
        return;
    }

    const sorted = [...classes].sort((a, b) => String(a.startTime).localeCompare(String(b.startTime)));

    tableBody.innerHTML = sorted.map(session => {
        const alreadyReserved = hasActiveReservationForClass(session.id);
        const canReserve = session.status === "OPEN" && session.availableSpots > 0 && !alreadyReserved;

        return `
            <tr>
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
                    <span class="badge ${getClassBadgeClass(session.status)}">
                        ${escapeHtml(session.status)}
                    </span>
                </td>
                <td>
                    ${
                        alreadyReserved
                            ? `<span class="badge green">Reserved</span>`
                            : canReserve
                                ? `<button type="button" onclick="reserveClass(${session.id})">Reserve</button>`
                                : `<button type="button" disabled>Unavailable</button>`
                    }
                </td>
            </tr>
        `;
    }).join("");
}

async function reserveClass(classSessionId) {
    const session = classSessions.find(item => Number(item.id) === Number(classSessionId));

    const confirmed = confirm(
        session
            ? `Reserve ${session.className} at ${formatTime(session.startTime)}?`
            : "Reserve this class?"
    );

    if (!confirmed) return;

    try {
        await apiRequest("/reservations", {
            method: "POST",
            body: JSON.stringify({
                classSessionId: Number(classSessionId),
                athleteId: Number(currentUser.id),
                notes: ""
            })
        });

        showMessage("Reservation created successfully.", "success");

        await loadMyReservations();
        await loadClassesByDate();
    } catch (error) {
        showMessage(error.message || "Could not create reservation.", "error");
    }
}

async function loadMyReservations() {
    const tableBody = document.getElementById("myReservationsTableBody");

    tableBody.innerHTML = `
        <tr>
            <td colspan="6">Loading your reservations...</td>
        </tr>
    `;

    try {
        myReservations = await apiRequest(`/reservations/athlete/${currentUser.id}`);
        renderMyReservations(myReservations);
    } catch (error) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="6">Could not load reservations: ${escapeHtml(error.message)}</td>
            </tr>
        `;
    }
}

function renderMyReservations(reservations) {
    const tableBody = document.getElementById("myReservationsTableBody");

    if (!reservations.length) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="6">You do not have reservations yet.</td>
            </tr>
        `;
        return;
    }

    tableBody.innerHTML = reservations.map(reservation => `
        <tr>
            <td>${escapeHtml(reservation.classDate)}</td>
            <td>${formatTime(reservation.startTime)} - ${formatTime(reservation.endTime)}</td>
            <td>${escapeHtml(reservation.className)}</td>
            <td>${escapeHtml(reservation.coachName || `Coach #${reservation.coachId}`)}</td>
            <td>
                <span class="badge ${getReservationBadgeClass(reservation.status)}">
                    ${escapeHtml(reservation.status)}
                </span>
            </td>
            <td>
                ${
                    reservation.status === "ACTIVE"
                        ? `<button type="button" class="danger" onclick="cancelReservation(${reservation.id})">Cancel</button>`
                        : `<button type="button" disabled>No action</button>`
                }
            </td>
        </tr>
    `).join("");
}

async function cancelReservation(reservationId) {
    const confirmed = confirm("Cancel this reservation?");

    if (!confirmed) return;

    try {
        await apiRequest(`/reservations/${reservationId}/cancel`, {
            method: "PATCH"
        });

        showMessage("Reservation cancelled successfully.", "success");

        await loadMyReservations();
        await loadClassesByDate();
    } catch (error) {
        showMessage(error.message || "Could not cancel reservation.", "error");
    }
}

function hasActiveReservationForClass(classSessionId) {
    return myReservations.some(reservation =>
        Number(reservation.classSessionId) === Number(classSessionId) &&
        reservation.status === "ACTIVE"
    );
}

function getClassBadgeClass(status) {
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

function formatTime(time) {
    if (!time) return "-";
    return String(time).substring(0, 5);
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