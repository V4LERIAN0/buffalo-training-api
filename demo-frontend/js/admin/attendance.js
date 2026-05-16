let currentUser = null;
let classSessions = [];
let attendanceRecords = [];
let athletes = [];

document.addEventListener("DOMContentLoaded", () => {
    currentUser = getCurrentUser();

    if (!currentUser || !currentUser.id) {
        showMessage("Could not find logged-in admin/coach. Please login again.", "error");
        return;
    }

    setupInitialDate();
    setupEvents();
    loadClassesByDate();
    loadAthletes();
});

function setupInitialDate() {
    const today = new Date().toISOString().split("T")[0];
    document.getElementById("classDateFilter").value = today;
}

function setupEvents() {
    document.getElementById("loadClassesButton").addEventListener("click", loadClassesByDate);
    document.getElementById("loadAttendanceButton").addEventListener("click", loadAttendanceForSelectedClass);
    document.getElementById("reloadAttendanceButton").addEventListener("click", loadAttendanceForSelectedClass);
    document.getElementById("classSessionSelect").addEventListener("change", syncSelectedClass);
    document.getElementById("manualAttendanceForm").addEventListener("submit", registerManualAttendance);
}

async function loadClassesByDate() {
    const date = document.getElementById("classDateFilter").value;

    if (!date) {
        showMessage("Please select a date.", "error");
        return;
    }

    try {
        classSessions = await apiRequest(`/class-sessions/date/${date}`);
        renderClassSessionSelects(classSessions);

        if (classSessions.length) {
            await loadAttendanceForSelectedClass();
        } else {
            clearAttendanceTable("No classes found for this date.");
        }
    } catch (error) {
        showMessage(error.message || "Could not load classes.", "error");
        clearAttendanceTable("Could not load classes.");
    }
}

function renderClassSessionSelects(sessions) {
    const classSessionSelect = document.getElementById("classSessionSelect");
    const manualClassSessionId = document.getElementById("manualClassSessionId");

    if (!sessions.length) {
        classSessionSelect.innerHTML = `<option value="">No classes found</option>`;
        manualClassSessionId.innerHTML = `<option value="">No classes found</option>`;
        document.getElementById("selectedClassText").textContent = "No class selected.";
        return;
    }

    const options = sessions.map(session => `
        <option value="${session.id}">
            ${escapeHtml(session.className)} · ${escapeHtml(session.classDate)} · ${formatTime(session.startTime)}-${formatTime(session.endTime)}
        </option>
    `).join("");

    classSessionSelect.innerHTML = options;
    manualClassSessionId.innerHTML = options;

    syncSelectedClass();
}

function syncSelectedClass() {
    const selectedSession = getSelectedClassSession();

    const selectedClassText = document.getElementById("selectedClassText");

    if (!selectedSession) {
        selectedClassText.textContent = "No class selected.";
        return;
    }

    selectedClassText.textContent =
        `${selectedSession.className} · ${selectedSession.classDate} · ${formatTime(selectedSession.startTime)}-${formatTime(selectedSession.endTime)}`;

    document.getElementById("manualClassSessionId").value = selectedSession.id;
}

async function loadAttendanceForSelectedClass() {
    const selectedSession = getSelectedClassSession();

    if (!selectedSession) {
        clearAttendanceTable("Select a class session to load attendance.");
        return;
    }

    const tableBody = document.getElementById("attendanceTableBody");

    tableBody.innerHTML = `
        <tr>
            <td colspan="6">Loading attendance...</td>
        </tr>
    `;

    try {
        attendanceRecords = await apiRequest(`/attendance/class-session/${selectedSession.id}`);
        renderAttendance(attendanceRecords);
    } catch (error) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="6">Could not load attendance: ${escapeHtml(error.message)}</td>
            </tr>
        `;
    }
}

function renderAttendance(records) {
    const tableBody = document.getElementById("attendanceTableBody");

    if (!records.length) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="6">No attendance records for this class yet.</td>
            </tr>
        `;
        return;
    }

    tableBody.innerHTML = records.map(record => `
        <tr>
            <td>${escapeHtml(record.athleteName || `Athlete #${record.athleteId || "-"}`)}</td>
            <td>${escapeHtml(record.className || `Class #${record.classSessionId || "-"}`)}</td>
            <td>${formatDateTime(record.checkInTime || record.createdAt || record.attendanceDate || record.fechaCheckin)}</td>
            <td>
                <span class="badge">
                    ${escapeHtml(record.checkInMethod || record.method || "-")}
                </span>
            </td>
            <td>
                <span class="badge ${getAttendanceBadgeClass(record.status)}">
                    ${escapeHtml(record.status || "-")}
                </span>
            </td>
            <td>
                <div class="row-actions">
                    ${
                        record.status !== "VALIDATED" && record.status !== "CANCELLED"
                            ? `<button type="button" class="secondary" onclick="validateAttendance(${record.id})">Validate</button>`
                            : ""
                    }

                    ${
                        record.status !== "CANCELLED"
                            ? `<button type="button" class="danger" onclick="cancelAttendance(${record.id})">Cancel</button>`
                            : ""
                    }
                </div>
            </td>
        </tr>
    `).join("");
}

async function validateAttendance(attendanceId) {
    const confirmed = confirm("Validate this attendance record?");

    if (!confirmed) return;

    try {
        await apiRequest(`/attendance/${attendanceId}/validate`, {
            method: "PATCH",
            body: JSON.stringify({
                validatedByUserId: Number(currentUser.id),
                notes: ""
            })
        });

        showMessage("Attendance validated successfully.", "success");
        await loadAttendanceForSelectedClass();
    } catch (error) {
        showMessage(error.message || "Could not validate attendance.", "error");
    }
}

async function cancelAttendance(attendanceId) {
    const confirmed = confirm("Cancel this attendance record?");

    if (!confirmed) return;

    try {
        await apiRequest(`/attendance/${attendanceId}/cancel`, {
            method: "PATCH"
        });

        showMessage("Attendance cancelled successfully.", "success");
        await loadAttendanceForSelectedClass();
    } catch (error) {
        showMessage(error.message || "Could not cancel attendance.", "error");
    }
}

async function loadAthletes() {
    const manualAthleteId = document.getElementById("manualAthleteId");

    try {
        const users = await apiRequest("/users");

        athletes = users.filter(user =>
            user.roleName === "ATHLETE" &&
            user.status === "ACTIVE"
        );

        if (!athletes.length) {
            manualAthleteId.innerHTML = `<option value="">No active athletes found</option>`;
            return;
        }

        manualAthleteId.innerHTML = `
            <option value="">Select athlete</option>
            ${athletes.map(athlete => `
                <option value="${athlete.id}">
                    ${escapeHtml(athlete.firstName)} ${escapeHtml(athlete.lastName)} - ${escapeHtml(athlete.email)}
                </option>
            `).join("")}
        `;
    } catch (error) {
        manualAthleteId.innerHTML = `<option value="">Could not load athletes</option>`;
        showMessage(error.message || "Could not load athletes.", "error");
    }
}

async function registerManualAttendance(event) {
    event.preventDefault();

    const classSessionId = Number(document.getElementById("manualClassSessionId").value);
    const athleteId = Number(document.getElementById("manualAthleteId").value);
    const notes = document.getElementById("manualNotes").value.trim();

    if (!classSessionId || !athleteId) {
        showMessage("Please select a class session and athlete.", "error");
        return;
    }

    try {
        await postManualAttendance(classSessionId, athleteId, notes);

        showMessage("Manual attendance registered successfully.", "success");
        document.getElementById("manualAttendanceForm").reset();

        document.getElementById("manualClassSessionId").value = classSessionId;

        await loadAttendanceForSelectedClass();
    } catch (error) {
        showMessage(error.message || "Could not register manual attendance.", "error");
    }
}

async function postManualAttendance(classSessionId, athleteId, notes) {
    return await apiRequest("/attendance/manual", {
        method: "POST",
        body: JSON.stringify({
            classSessionId: Number(classSessionId),
            athleteId: Number(athleteId),
            registeredByUserId: Number(currentUser.id),
            notes: notes
        })
    });
}

function getSelectedClassSession() {
    const selectedId = Number(document.getElementById("classSessionSelect").value);

    return classSessions.find(session => Number(session.id) === selectedId);
}

function clearAttendanceTable(message) {
    document.getElementById("attendanceTableBody").innerHTML = `
        <tr>
            <td colspan="6">${escapeHtml(message)}</td>
        </tr>
    `;
}

function getAttendanceBadgeClass(status) {
    if (status === "CHECKED_IN") return "orange";
    if (status === "VALIDATED") return "green";
    if (status === "MANUAL") return "blue";
    if (status === "CANCELLED") return "red";
    return "";
}

function formatTime(time) {
    if (!time) return "-";
    return String(time).substring(0, 5);
}

function formatDateTime(value) {
    if (!value) return "-";

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return String(value);

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