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

    const form = document.getElementById("membershipPlanForm");
    const plansTableBody = document.getElementById("plansTableBody");
    const messageBox = document.getElementById("planFormMessage");
    const refreshButton = document.getElementById("refreshPlansButton");
    const cancelEditButton = document.getElementById("cancelEditButton");

    const formTitle = document.getElementById("formTitle");
    const submitButton = document.getElementById("submitButton");

    loadPlans();

    refreshButton.addEventListener("click", loadPlans);
    cancelEditButton.addEventListener("click", resetForm);

    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        clearMessage();

        const editingPlanId = document.getElementById("editingPlanId").value;

        if (editingPlanId) {
            await updatePlan(editingPlanId);
        } else {
            await createPlan();
        }
    });

    async function createPlan() {
        const payload = buildPayload();

        try {
            await apiRequest("/membership-plans", {
                method: "POST",
                body: JSON.stringify(payload)
            });

            showSuccess("Membership plan created successfully.");
            resetForm();
            loadPlans();
        } catch (error) {
            showError(error.message);
        }
    }

    async function updatePlan(planId) {
        const payload = buildPayload();

        try {
            await apiRequest(`/membership-plans/${planId}`, {
                method: "PUT",
                body: JSON.stringify(payload)
            });

            showSuccess("Membership plan updated successfully.");
            resetForm();
            loadPlans();
        } catch (error) {
            showError(error.message);
        }
    }

    async function loadPlans() {
        plansTableBody.innerHTML = `
            <tr>
                <td colspan="8">Loading membership plans...</td>
            </tr>
        `;

        try {
            const plans = await apiRequest("/membership-plans");

            if (!plans.length) {
                plansTableBody.innerHTML = `
                    <tr>
                        <td colspan="8">No membership plans found.</td>
                    </tr>
                `;
                return;
            }

            plansTableBody.innerHTML = plans.map(plan => `
                <tr>
                    <td>${plan.id}</td>
                    <td>
                        <strong>${plan.name}</strong><br>
                        <small>${plan.description || "-"}</small>
                    </td>
                    <td>$${Number(plan.price).toFixed(2)}</td>
                    <td>${plan.durationDays} days</td>
                    <td>${renderBillingBadge(plan.billingCycle)}</td>
                    <td>${plan.gracePeriodDays} days</td>
                    <td>${renderActiveBadge(plan.active)}</td>
                    <td>
                        <button type="button" onclick="editPlan(${plan.id})">Edit</button>
                        ${renderStatusAction(plan)}
                    </td>
                </tr>
            `).join("");
        } catch (error) {
            plansTableBody.innerHTML = `
                <tr>
                    <td colspan="8" class="error">${error.message}</td>
                </tr>
            `;
        }
    }

    window.editPlan = async function(planId) {
        clearMessage();

        try {
            const plan = await apiRequest(`/membership-plans/${planId}`);

            document.getElementById("editingPlanId").value = plan.id;
            document.getElementById("name").value = plan.name || "";
            document.getElementById("description").value = plan.description || "";
            document.getElementById("price").value = plan.price || "";
            document.getElementById("durationDays").value = plan.durationDays || "";
            document.getElementById("billingCycle").value = plan.billingCycle || "";
            document.getElementById("gracePeriodDays").value = plan.gracePeriodDays ?? 0;

            formTitle.textContent = `Edit Membership Plan #${plan.id}`;
            submitButton.textContent = "Update Plan";
            cancelEditButton.style.display = "inline-block";

            window.scrollTo({ top: 0, behavior: "smooth" });
        } catch (error) {
            showError(error.message);
        }
    };

    window.changePlanStatus = async function(planId, active) {
        clearMessage();

        const actionText = active ? "activate" : "deactivate";

        if (!confirm(`Are you sure you want to ${actionText} this membership plan?`)) {
            return;
        }

        try {
            await apiRequest(`/membership-plans/${planId}/status`, {
                method: "PATCH",
                body: JSON.stringify({ active })
            });

            showSuccess(`Membership plan ${active ? "activated" : "deactivated"} successfully.`);
            loadPlans();
        } catch (error) {
            showError(error.message);
        }
    };

    function buildPayload() {
        return {
            name: getValue("name"),
            description: getValue("description") || null,
            price: Number(document.getElementById("price").value),
            durationDays: Number(document.getElementById("durationDays").value),
            billingCycle: document.getElementById("billingCycle").value,
            gracePeriodDays: Number(document.getElementById("gracePeriodDays").value)
        };
    }

    function resetForm() {
        form.reset();

        document.getElementById("editingPlanId").value = "";
        document.getElementById("durationDays").value = 30;
        document.getElementById("gracePeriodDays").value = 3;

        formTitle.textContent = "Create Membership Plan";
        submitButton.textContent = "Create Plan";
        cancelEditButton.style.display = "none";

        clearMessage();
    }

    function renderStatusAction(plan) {
        if (plan.active) {
            return `<button type="button" class="danger" onclick="changePlanStatus(${plan.id}, false)">Deactivate</button>`;
        }

        return `<button type="button" onclick="changePlanStatus(${plan.id}, true)">Activate</button>`;
    }

    function renderActiveBadge(active) {
        if (active) {
            return `<span class="badge green">ACTIVE</span>`;
        }

        return `<span class="badge red">INACTIVE</span>`;
    }

    function renderBillingBadge(billingCycle) {
        if (billingCycle === "MONTHLY") {
            return `<span class="badge blue">MONTHLY</span>`;
        }

        if (billingCycle === "DAY_BASED") {
            return `<span class="badge orange">DAY_BASED</span>`;
        }

        return `<span class="badge">${billingCycle || "-"}</span>`;
    }

    function getValue(id) {
        return document.getElementById(id).value.trim();
    }

    function clearMessage() {
        messageBox.className = "";
        messageBox.textContent = "";
    }

    function showSuccess(message) {
        messageBox.className = "success";
        messageBox.textContent = message;
    }

    function showError(message) {
        messageBox.className = "error";
        messageBox.textContent = message;
    }
});