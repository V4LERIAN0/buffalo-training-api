let currentUser = null;
let activeMembership = null;
let payments = [];

const OWNER_WHATSAPP_PHONE = "50371293959";

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
    document.getElementById("reloadPaymentsButton").addEventListener("click", loadPaymentHistory);
    document.getElementById("requestWompiButton").addEventListener("click", requestWompiPaymentLink);
}

async function loadEverything() {
    await loadActiveMembership();
    await loadPaymentHistory();
}

async function loadActiveMembership() {
    const membershipContent = document.getElementById("membershipContent");
    const paymentActionCard = document.getElementById("paymentActionCard");

    membershipContent.innerHTML = "<p>Loading membership...</p>";
    paymentActionCard.classList.add("hidden");

    try {
        activeMembership = await apiRequest(`/memberships/athlete/${currentUser.id}/active`);

        renderActiveMembership(activeMembership);
        paymentActionCard.classList.remove("hidden");
    } catch (error) {
        activeMembership = null;

        membershipContent.innerHTML = `
            <div class="empty-state">
                <h2>No active membership found</h2>
                <p>
                    You currently do not have an active membership assigned.
                    Please contact Buffalo Training staff to activate your membership.
                </p>
            </div>
        `;

        showMessage(error.message || "No active membership found.", "error");
    }
}

function renderActiveMembership(membership) {
    const membershipContent = document.getElementById("membershipContent");

    const statusClass = getMembershipBadgeClass(membership.status);
    const daysText = buildDaysRemainingText(membership);

    membershipContent.innerHTML = `
        <div class="membership-summary">
            <div class="membership-main">
                <h2>${escapeHtml(membership.planName || "Membership Plan")}</h2>

                <p>
                    <span class="badge ${statusClass}">
                        ${escapeHtml(membership.status || "-")}
                    </span>
                    ${membership.active === false ? `<span class="badge red">Inactive</span>` : ""}
                </p>

                <p>${escapeHtml(membership.planDescription || "No plan description available.")}</p>
            </div>

            <div class="membership-details-grid">
                <div class="detail-box">
                    <h3>Due Date</h3>
                    <p>${formatDate(membership.dueDate || membership.endDate || membership.expirationDate)}</p>
                    <small>${escapeHtml(daysText)}</small>
                </div>

                <div class="detail-box">
                    <h3>Base Payment Date</h3>
                    <p>${formatDate(membership.basePaymentDate)}</p>
                    <small>This date stays fixed for monthly payment logic.</small>
                </div>

                <div class="detail-box">
                    <h3>Start Date</h3>
                    <p>${formatDate(membership.startDate)}</p>
                    <small>Membership activation date.</small>
                </div>

                <div class="detail-box">
                    <h3>Grace Period</h3>
                    <p>${membership.gracePeriodDays ?? "-"} days</p>
                    <small>Extra days allowed after due date.</small>
                </div>
            </div>

            ${renderMembershipWarning(membership)}
        </div>
    `;
}

function renderMembershipWarning(membership) {
    if (membership.status === "ACTIVE") {
        return `
            <div class="message success">
                Your membership is active. You can reserve classes, check in and submit scores.
            </div>
        `;
    }

    if (membership.status === "EXPIRING_SOON") {
        return `
            <div class="message info">
                Your membership is close to expiring. Consider requesting your payment link soon.
            </div>
        `;
    }

    if (membership.status === "EXPIRED") {
        return `
            <div class="message error">
                Your membership is expired. Some athlete actions may be blocked until payment is registered.
            </div>
        `;
    }

    return "";
}

async function loadPaymentHistory() {
    const tableBody = document.getElementById("paymentsTableBody");

    tableBody.innerHTML = `
        <tr>
            <td colspan="6">Loading payments...</td>
        </tr>
    `;

    try {
        payments = await apiRequest(`/payments/athlete/${currentUser.id}`);
        renderPayments(payments);
    } catch (error) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="6">Could not load payment history: ${escapeHtml(error.message)}</td>
            </tr>
        `;
    }
}

function renderPayments(paymentList) {
    const tableBody = document.getElementById("paymentsTableBody");

    if (!paymentList.length) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="6">No payments registered yet.</td>
            </tr>
        `;
        return;
    }

    const sortedPayments = [...paymentList].sort((a, b) => {
        const dateA = new Date(a.paymentDate || a.createdAt || 0).getTime();
        const dateB = new Date(b.paymentDate || b.createdAt || 0).getTime();
        return dateB - dateA;
    });

    tableBody.innerHTML = sortedPayments.map(payment => `
        <tr>
            <td>${formatDateTime(payment.paymentDate || payment.createdAt)}</td>
            <td>${formatMoney(payment.amount)}</td>
            <td>${escapeHtml(payment.paymentMethod || "-")}</td>
            <td>
                <span class="badge ${getPaymentBadgeClass(payment.status)}">
                    ${escapeHtml(payment.status || "-")}
                </span>
            </td>
            <td>${escapeHtml(payment.reference || payment.paymentReference || "-")}</td>
            <td>${escapeHtml(payment.notes || payment.observations || "-")}</td>
        </tr>
    `).join("");
}

function requestWompiPaymentLink() {
    if (!currentUser) {
        showMessage("Could not find logged-in athlete.", "error");
        return;
    }

    if (OWNER_WHATSAPP_PHONE === "50300000000") {
        const confirmed = confirm(
            "The owner WhatsApp number is still using the placeholder 50300000000. Open WhatsApp anyway?"
        );

        if (!confirmed) return;
    }

    const fullName = `${currentUser.firstName || ""} ${currentUser.lastName || ""}`.trim();

    const planName = activeMembership?.planName || "my membership";
    const dueDate = activeMembership?.dueDate || activeMembership?.endDate || activeMembership?.expirationDate || "my due date";

    const message = `
Hola, soy ${fullName || "un atleta de Buffalo Training"}.

Quisiera solicitar el link de pago por Wompi para mi membresía.

Plan: ${planName}
Fecha de vencimiento: ${dueDate}

Quedo pendiente, gracias.
    `.trim();

    const encodedMessage = encodeURIComponent(message);
    const whatsappUrl = `https://wa.me/${OWNER_WHATSAPP_PHONE}?text=${encodedMessage}`;

    window.open(whatsappUrl, "_blank");
}

function buildDaysRemainingText(membership) {
    const dueDateRaw = membership.dueDate || membership.endDate || membership.expirationDate;

    if (!dueDateRaw) return "No due date available.";

    const today = new Date();
    const dueDate = new Date(`${dueDateRaw}T00:00:00`);

    if (Number.isNaN(dueDate.getTime())) return "Could not calculate remaining days.";

    today.setHours(0, 0, 0, 0);

    const diffMs = dueDate.getTime() - today.getTime();
    const diffDays = Math.ceil(diffMs / (1000 * 60 * 60 * 24));

    if (diffDays > 1) return `${diffDays} days remaining.`;
    if (diffDays === 1) return "1 day remaining.";
    if (diffDays === 0) return "Due today.";
    if (diffDays === -1) return "1 day overdue.";

    return `${Math.abs(diffDays)} days overdue.`;
}

function getMembershipBadgeClass(status) {
    if (status === "ACTIVE") return "green";
    if (status === "EXPIRING_SOON") return "orange";
    if (status === "EXPIRED") return "red";
    if (status === "CANCELLED") return "red";
    return "";
}

function getPaymentBadgeClass(status) {
    if (status === "APPROVED") return "green";
    if (status === "PENDING") return "orange";
    if (status === "REJECTED") return "red";
    if (status === "CANCELLED") return "red";
    return "";
}

function formatMoney(value) {
    if (value === null || value === undefined || value === "") return "-";

    const numberValue = Number(value);

    if (Number.isNaN(numberValue)) return `$${value}`;

    return `$${numberValue.toFixed(2)}`;
}

function formatDate(value) {
    if (!value) return "-";

    const date = new Date(`${value}T00:00:00`);

    if (Number.isNaN(date.getTime())) return String(value);

    return date.toLocaleDateString();
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