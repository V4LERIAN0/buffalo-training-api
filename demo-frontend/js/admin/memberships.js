document.addEventListener("DOMContentLoaded", () => {
    const currentUser = requireAuth();

    if (!currentUser) {
        return;
    }

    if (currentUser.roleName !== "ADMIN") {
        alert("Only ADMIN users can access this page.");
        window.location.href = "../login.html";
        return;
    }

    let selectedAthlete = null;
    let activeMembership = null;

    const athleteSelect = document.getElementById("athleteSelect");
    const loadAthleteButton = document.getElementById("loadAthleteButton");
    const athleteMessage = document.getElementById("athleteMessage");

    const membershipCard = document.getElementById("membershipCard");
    const activeMembershipBox = document.getElementById("activeMembershipBox");

    const assignMembershipCard = document.getElementById("assignMembershipCard");
    const assignMembershipForm = document.getElementById("assignMembershipForm");
    const assignMembershipMessage = document.getElementById("assignMembershipMessage");

    const planSelect = document.getElementById("planSelect");

    const paymentCard = document.getElementById("paymentCard");
    const paymentForm = document.getElementById("paymentForm");
    const paymentMessage = document.getElementById("paymentMessage");

    const paymentHistoryCard = document.getElementById("paymentHistoryCard");
    const paymentsTableBody = document.getElementById("paymentsTableBody");
    const refreshPaymentsButton = document.getElementById("refreshPaymentsButton");

    function showElement(element) {
        element.classList.remove("hidden");
    }

    function hideElement(element) {
        element.classList.add("hidden");
    }

    initializeDefaults();
    loadAthletes();
    loadActivePlans();

    loadAthleteButton.addEventListener("click", loadSelectedAthleteData);
    assignMembershipForm.addEventListener("submit", assignMembership);
    paymentForm.addEventListener("submit", registerPayment);
    refreshPaymentsButton.addEventListener("click", loadPaymentHistory);

    function initializeDefaults() {
        const today = new Date().toISOString().slice(0, 10);

        document.getElementById("startDate").value = today;
        document.getElementById("paymentDate").value = today;
    }

    async function loadAthletes() {
        try {
            const users = await apiRequest("/users");
            const athletes = users.filter(user => user.roleName === "ATHLETE");

            if (!athletes.length) {
                athleteSelect.innerHTML = `<option value="">No athletes found</option>`;
                return;
            }

            athleteSelect.innerHTML = `
                <option value="">Select athlete</option>
                ${athletes.map(athlete => `
                    <option value="${athlete.id}">
                        ${athlete.firstName} ${athlete.lastName} - ${athlete.email}
                    </option>
                `).join("")}
            `;
        } catch (error) {
            athleteSelect.innerHTML = `<option value="">Error loading athletes</option>`;
            showMessage(athleteMessage, error.message, "error");
        }
    }

    async function loadActivePlans() {
        try {
            const plans = await apiRequest("/membership-plans/active");

            if (!plans.length) {
                planSelect.innerHTML = `<option value="">No active plans found</option>`;
                return;
            }

            planSelect.innerHTML = `
                <option value="">Select plan</option>
                ${plans.map(plan => `
                    <option value="${plan.id}" data-price="${plan.price}">
                        ${plan.name} - $${Number(plan.price).toFixed(2)} - ${plan.billingCycle}
                    </option>
                `).join("")}
            `;
        } catch (error) {
            planSelect.innerHTML = `<option value="">Error loading plans</option>`;
        }
    }

    async function loadSelectedAthleteData() {
        clearAllMessages();

        const athleteId = athleteSelect.value;

        if (!athleteId) {
            showMessage(athleteMessage, "Please select an athlete.", "error");
            return;
        }

        try {
            selectedAthlete = await apiRequest(`/users/${athleteId}`);

            await loadActiveMembership();
            await loadPaymentHistory();

            showElement(paymentHistoryCard);
        } catch (error) {
            showMessage(athleteMessage, error.message, "error");
        }
    }

    async function loadActiveMembership() {
        activeMembership = null;

        try {
            activeMembership = await apiRequest(`/memberships/athlete/${selectedAthlete.id}/active`);

            showElement(membershipCard);
            hideElement(assignMembershipCard);
            showElement(paymentCard);

            renderActiveMembership(activeMembership);

            document.getElementById("amount").value = Number(activeMembership.planPrice).toFixed(2);

        } catch (error) {
            showElement(membershipCard);
            hideElement(paymentCard);
            showElement(assignMembershipCard);

            activeMembershipBox.innerHTML = `
                <p class="error">No active membership found for this athlete.</p>
                <p>You can assign a new membership below.</p>
            `;
        }
    }

    function renderActiveMembership(membership) {
        activeMembershipBox.innerHTML = `
            <p><strong>Athlete:</strong> ${membership.athleteName}</p>
            <p><strong>Plan:</strong> ${membership.planName} - $${Number(membership.planPrice).toFixed(2)}</p>
            <p><strong>Start Date:</strong> ${membership.startDate}</p>
            <p><strong>Due Date:</strong> ${membership.dueDate}</p>
            <p><strong>Base Payment Date:</strong> ${membership.basePaymentDate}</p>
            <p><strong>Status:</strong> ${renderMembershipStatusBadge(membership.status)}</p>
            <p><strong>Active:</strong> ${membership.active ? "Yes" : "No"}</p>
            <p><strong>Notes:</strong> ${membership.notes || "-"}</p>

            <button type="button" class="danger" onclick="cancelActiveMembership(${membership.id})">
                Cancel Membership
            </button>
        `;
    }

    async function assignMembership(event) {
        event.preventDefault();
        clearAllMessages();

        if (!selectedAthlete) {
            showMessage(assignMembershipMessage, "Please select an athlete first.", "error");
            return;
        }

        const payload = {
            athleteId: selectedAthlete.id,
            planId: Number(planSelect.value),
            startDate: document.getElementById("startDate").value,
            notes: document.getElementById("membershipNotes").value.trim() || null
        };

        try {
            await apiRequest("/memberships", {
                method: "POST",
                body: JSON.stringify(payload)
            });

            showMessage(assignMembershipMessage, "Membership assigned successfully.", "success");

            assignMembershipForm.reset();
            initializeDefaults();

            await loadActiveMembership();
            await loadPaymentHistory();
        } catch (error) {
            showMessage(assignMembershipMessage, error.message, "error");
        }
    }

    async function registerPayment(event) {
        event.preventDefault();
        clearAllMessages();

        if (!activeMembership) {
            showMessage(paymentMessage, "No active membership selected.", "error");
            return;
        }

        const payload = {
            membershipId: activeMembership.id,
            registeredByUserId: currentUser.id,
            amount: Number(document.getElementById("amount").value),
            paymentDate: document.getElementById("paymentDate").value,
            paymentMethod: document.getElementById("paymentMethod").value,
            referenceCode: document.getElementById("referenceCode").value.trim() || null,
            notes: document.getElementById("paymentNotes").value.trim() || null
        };

        try {
            const response = await apiRequest("/payments/membership", {
                method: "POST",
                body: JSON.stringify(payload)
            });

            showMessage(paymentMessage, response.message, "success");

            activeMembership = response.updatedMembership;
            renderActiveMembership(activeMembership);

            paymentForm.reset();
            initializeDefaults();
            document.getElementById("amount").value = Number(activeMembership.planPrice).toFixed(2);

            await loadPaymentHistory();
        } catch (error) {
            showMessage(paymentMessage, error.message, "error");
        }
    }

    async function loadPaymentHistory() {
        if (!selectedAthlete) {
            return;
        }

        showElement(paymentHistoryCard);

        paymentsTableBody.innerHTML = `
            <tr>
                <td colspan="7">Loading payment history...</td>
            </tr>
        `;

        try {
            const payments = await apiRequest(`/payments/athlete/${selectedAthlete.id}`);

            if (!payments.length) {
                paymentsTableBody.innerHTML = `
                    <tr>
                        <td colspan="7">No payments found.</td>
                    </tr>
                `;
                return;
            }

            paymentsTableBody.innerHTML = payments.map(payment => `
                <tr>
                    <td>${payment.id}</td>
                    <td>${payment.paymentDate}</td>
                    <td>$${Number(payment.amount).toFixed(2)}</td>
                    <td>${renderPaymentMethodBadge(payment.paymentMethod)}</td>
                    <td>${renderPaymentStatusBadge(payment.status)}</td>
                    <td>${payment.referenceCode || "-"}</td>
                    <td>${payment.registeredByName}</td>
                </tr>
            `).join("");

        } catch (error) {
            paymentsTableBody.innerHTML = `
                <tr>
                    <td colspan="7" class="error">${error.message}</td>
                </tr>
            `;
        }
    }

    window.cancelActiveMembership = async function(membershipId) {
        clearAllMessages();

        if (!confirm("Are you sure you want to cancel this membership?")) {
            return;
        }

        try {
            await apiRequest(`/memberships/${membershipId}/cancel`, {
                method: "PATCH"
            });

            showMessage(athleteMessage, "Membership cancelled successfully.", "success");

            await loadActiveMembership();
            await loadPaymentHistory();
        } catch (error) {
            showMessage(athleteMessage, error.message, "error");
        }
    };

    function clearAllMessages() {
        [athleteMessage, assignMembershipMessage, paymentMessage].forEach(box => {
            box.className = "";
            box.textContent = "";
        });
    }

    function showMessage(element, message, type) {
        element.className = type;
        element.textContent = message;
    }

    function renderMembershipStatusBadge(status) {
        if (status === "ACTIVE") {
            return `<span class="badge green">ACTIVE</span>`;
        }

        if (status === "EXPIRING_SOON") {
            return `<span class="badge orange">EXPIRING SOON</span>`;
        }

        if (status === "EXPIRED") {
            return `<span class="badge red">EXPIRED</span>`;
        }

        if (status === "CANCELLED") {
            return `<span class="badge">CANCELLED</span>`;
        }

        return `<span class="badge">${status}</span>`;
    }

    function renderPaymentMethodBadge(method) {
        if (method === "CASH") {
            return `<span class="badge green">CASH</span>`;
        }

        if (method === "WOMPI_LINK") {
            return `<span class="badge blue">WOMPI</span>`;
        }

        if (method === "BANK_TRANSFER") {
            return `<span class="badge orange">TRANSFER</span>`;
        }

        return `<span class="badge">${method}</span>`;
    }

    function renderPaymentStatusBadge(status) {
        if (status === "APPROVED") {
            return `<span class="badge green">APPROVED</span>`;
        }

        if (status === "PENDING") {
            return `<span class="badge orange">PENDING</span>`;
        }

        if (status === "REJECTED" || status === "CANCELLED") {
            return `<span class="badge red">${status}</span>`;
        }

        return `<span class="badge">${status}</span>`;
    }
});