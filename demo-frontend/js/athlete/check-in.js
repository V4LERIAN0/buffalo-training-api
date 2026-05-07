let currentUser = null;
let myReservations = [];
let myAttendance = [];

document.addEventListener("DOMContentLoaded", () => {
    currentUser = getCurrentUser();

    if (!currentUser || !currentUser.id) {
        showMessage("Could not find logged-in athlete. Please login again.", "error");
        return;
    }

    setupEvents();
    loadEverything();
});

function setupEvents() {
    document.getElementById("reloadButton").addEventListener("click", loadEverything);
    document.getElementById("reloadAttendanceButton").addEventListener("click", loadMyAttendance);
}

async function loadEverything() {
    await loadMyReservations();
    await loadMyAttendance();
}

async function loadMyReservations() {
    const tableBody = document.getElementById("reservationsTableBody");

    tableBody.innerHTML = `
        <tr>
            <td colspan="6">Loading reservations...</td>
        </tr>
    `;

    try {
        myReservations = await apiRequest(`/reservations/athlete/${currentUser.id}`);
        renderReservations(myReservations);
    } catch (error) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="6">Could not load reservations: ${escapeHtml(error.message)}</td>
            </tr>
        `;
    }
}

function renderReservations(reservations) {
    const tableBody = document.getElementById("reservationsTableBody");

    const activeOrRelevantReservations = reservations.filter(reservation =>
        reservation.status === "ACTIVE" || reservation.status === "ATTENDED"
    );

    if (!activeOrRelevantReservations.length) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="6">You do not have active reservations available for check-in.</td>
            </tr>
        `;
        return;
    }

    tableBody.innerHTML = activeOrRelevantReservations.map(reservation => {
        const alreadyAttended = reservation.status === "ATTENDED";

        return `
            <tr>
                <td>${escapeHtml(reservation.classDate || "-")}</td>
                <td>${formatTime(reservation.startTime)} - ${formatTime(reservation.endTime)}</td>
                <td>${escapeHtml(reservation.className || `Class #${reservation.classSessionId || "-"}`)}</td>
                <td>${escapeHtml(reservation.coachName || `Coach #${reservation.coachId || "-"}`)}</td>
                <td>
                    <span class="badge ${getReservationBadgeClass(reservation.status)}">
                        ${escapeHtml(reservation.status)}
                    </span>
                </td>
                <td>
                    ${
                        alreadyAttended
                            ? `<span class="badge blue">Checked in</span>`
                            : `<button type="button" onclick="checkIn(${reservation.id})">Check in</button>`
                    }
                </td>
            </tr>
        `;
    }).join("");
}

async function checkIn(reservationId) {
    const confirmed = confirm("Check in to this class?");

    if (!confirmed) return;

    try {
        await postCheckIn(reservationId);

        showMessage("Check-in registered successfully.", "success");
        await loadEverything();
    } catch (error) {
        showMessage(error.message || "Could not complete check-in.", "error");
    }
}

async function postCheckIn(reservationId) {
    try {
        return await apiRequest("/attendance/check-in", {
            method: "POST",
            body: JSON.stringify({
                reservationId: Number(reservationId)
            })
        });
    } catch (firstError) {
        console.warn("Check-in with reservationId only failed. Trying with athleteId too.", firstError);

        return await apiRequest("/attendance/check-in", {
            method: "POST",
            body: JSON.stringify({
                reservationId: Number(reservationId),
                athleteId: Number(currentUser.id)
            })
        });
    }
}

async function loadMyAttendance() {
    const tableBody = document.getElementById("attendanceTableBody");

    tableBody.innerHTML = `
        <tr>
            <td colspan="4">Loading attendance...</td>
        </tr>
    `;

    try {
        myAttendance = await apiRequest(`/attendance/athlete/${currentUser.id}`);
        renderMyAttendance(myAttendance);
    } catch (error) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="4">Could not load attendance: ${escapeHtml(error.message)}</td>
            </tr>
        `;
    }
}

function renderMyAttendance(attendanceRecords) {
    const tableBody = document.getElementById("attendanceTableBody");

    if (!attendanceRecords.length) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="4">No attendance records yet.</td>
            </tr>
        `;
        return;
    }

    tableBody.innerHTML = attendanceRecords.map(record => `
        <tr>
            <td>${formatDateTime(record.checkInTime || record.createdAt || record.attendanceDate || record.fechaCheckin)}</td>
            <td>${escapeHtml(record.className || `Class #${record.classSessionId || "-"}`)}</td>
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
        </tr>
    `).join("");
}

function getReservationBadgeClass(status) {
    if (status === "ACTIVE") return "green";
    if (status === "ATTENDED") return "blue";
    if (status === "CANCELLED") return "red";
    if (status === "NO_SHOW") return "orange";
    return "";
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