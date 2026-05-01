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

    const userForm = document.getElementById("userForm");
    const usersTableBody = document.getElementById("usersTableBody");
    const userFormMessage = document.getElementById("userFormMessage");
    const refreshUsersButton = document.getElementById("refreshUsersButton");
    const cancelEditButton = document.getElementById("cancelEditButton");

    const formTitle = document.getElementById("formTitle");
    const submitButton = document.getElementById("submitButton");
    const passwordGroup = document.getElementById("passwordGroup");
    const roleGroup = document.getElementById("roleGroup");

    loadUsers();

    refreshUsersButton.addEventListener("click", loadUsers);
    cancelEditButton.addEventListener("click", resetForm);

    userForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        clearMessage();

        const editingUserId = document.getElementById("editingUserId").value;

        if (editingUserId) {
            await updateUser(editingUserId);
        } else {
            await createUser();
        }
    });

    async function createUser() {
        const payload = {
            firstName: getValue("firstName"),
            lastName: getValue("lastName"),
            email: getValue("email"),
            phone: getValue("phone") || null,
            password: document.getElementById("password").value,
            roleName: document.getElementById("roleName").value,
            gender: document.getElementById("gender").value || null,
            profilePhotoUrl: getValue("profilePhotoUrl") || null
        };

        try {
            await apiRequest("/users", {
                method: "POST",
                body: JSON.stringify(payload)
            });

            showSuccess("User created successfully.");
            resetForm();
            loadUsers();
        } catch (error) {
            showError(error.message);
        }
    }

    async function updateUser(userId) {
        const payload = {
            firstName: getValue("firstName"),
            lastName: getValue("lastName"),
            email: getValue("email"),
            phone: getValue("phone") || null,
            gender: document.getElementById("gender").value || null,
            profilePhotoUrl: getValue("profilePhotoUrl") || null
        };

        try {
            await apiRequest(`/users/${userId}`, {
                method: "PUT",
                body: JSON.stringify(payload)
            });

            showSuccess("User updated successfully.");
            resetForm();
            loadUsers();
        } catch (error) {
            showError(error.message);
        }
    }

    async function loadUsers() {
        usersTableBody.innerHTML = `
            <tr>
                <td colspan="7">Loading users...</td>
            </tr>
        `;

        try {
            const users = await apiRequest("/users");

            if (!users.length) {
                usersTableBody.innerHTML = `
                    <tr>
                        <td colspan="7">No users found.</td>
                    </tr>
                `;
                return;
            }

            usersTableBody.innerHTML = users.map(user => `
                <tr>
                    <td>${user.id}</td>
                    <td>${user.firstName} ${user.lastName}</td>
                    <td>${user.email}</td>
                    <td>${user.phone || "-"}</td>
                    <td>${renderRoleBadge(user.roleName)}</td>
                    <td>${renderStatusBadge(user.status)}</td>
                    <td>
    <button type="button" onclick="editUser(${user.id})">Edit</button>
    <button type="button" class="secondary" onclick="resetUserPassword(${user.id}, '${user.email}')">Reset Password</button>
    ${renderStatusActions(user)}
</td>
                </tr>
            `).join("");
        } catch (error) {
            usersTableBody.innerHTML = `
                <tr>
                    <td colspan="7" class="error">${error.message}</td>
                </tr>
            `;
        }
    }

    window.editUser = async function(userId) {
        clearMessage();

        try {
            const user = await apiRequest(`/users/${userId}`);

            document.getElementById("editingUserId").value = user.id;
            document.getElementById("firstName").value = user.firstName || "";
            document.getElementById("lastName").value = user.lastName || "";
            document.getElementById("email").value = user.email || "";
            document.getElementById("phone").value = user.phone || "";
            document.getElementById("gender").value = user.gender || "";
            document.getElementById("profilePhotoUrl").value = user.profilePhotoUrl || "";

            formTitle.textContent = `Edit User #${user.id}`;
            submitButton.textContent = "Update User";
            cancelEditButton.style.display = "inline-block";

            passwordGroup.style.display = "none";
            roleGroup.style.display = "none";

            window.scrollTo({ top: 0, behavior: "smooth" });
        } catch (error) {
            showError(error.message);
        }
    };

    window.changeUserStatus = async function(userId, status) {
        clearMessage();

        const confirmMessage = `Are you sure you want to change this user status to ${status}?`;

        if (!confirm(confirmMessage)) {
            return;
        }

        try {
            await apiRequest(`/users/${userId}/status`, {
                method: "PATCH",
                body: JSON.stringify({ status })
            });

            showSuccess(`User status changed to ${status}.`);
            loadUsers();
        } catch (error) {
            showError(error.message);
        }
    };

    window.resetUserPassword = async function(userId, email) {
    clearMessage();

    const currentUser = getCurrentUser();

    const newPassword = prompt(`Enter new temporary password for ${email}:`);

    if (!newPassword) {
        return;
    }

    if (newPassword.length < 6) {
        showError("Password must be at least 6 characters.");
        return;
    }

    try {
        await apiRequest(`/users/${userId}/password/reset`, {
            method: "PATCH",
            body: JSON.stringify({
                adminUserId: currentUser.id,
                newPassword: newPassword
            })
        });

        showSuccess(`Password reset successfully for ${email}.`);
    } catch (error) {
        showError(error.message);
    }
};

    function renderStatusActions(user) {
        if (user.status === "ACTIVE") {
            return `
                <button type="button" class="secondary" onclick="changeUserStatus(${user.id}, 'INACTIVE')">Deactivate</button>
                <button type="button" class="danger" onclick="changeUserStatus(${user.id}, 'SUSPENDED')">Suspend</button>
            `;
        }

        if (user.status === "INACTIVE" || user.status === "SUSPENDED") {
            return `
                <button type="button" onclick="changeUserStatus(${user.id}, 'ACTIVE')">Reactivate</button>
            `;
        }

        return "";
    }

    function resetForm() {
        userForm.reset();

        document.getElementById("editingUserId").value = "";

        formTitle.textContent = "Create User";
        submitButton.textContent = "Create User";
        cancelEditButton.style.display = "none";

        passwordGroup.style.display = "block";
        roleGroup.style.display = "block";

        clearMessage();
    }

    function getValue(id) {
        return document.getElementById(id).value.trim();
    }

    function clearMessage() {
        userFormMessage.className = "";
        userFormMessage.textContent = "";
    }

    function showSuccess(message) {
        userFormMessage.className = "success";
        userFormMessage.textContent = message;
    }

    function showError(message) {
        userFormMessage.className = "error";
        userFormMessage.textContent = message;
    }

    function renderRoleBadge(roleName) {
        if (roleName === "ADMIN") {
            return `<span class="badge red">ADMIN</span>`;
        }

        if (roleName === "COACH") {
            return `<span class="badge blue">COACH</span>`;
        }

        if (roleName === "ATHLETE") {
            return `<span class="badge green">ATHLETE</span>`;
        }

        return `<span class="badge">${roleName}</span>`;
    }

    function renderStatusBadge(status) {
        if (status === "ACTIVE") {
            return `<span class="badge green">ACTIVE</span>`;
        }

        if (status === "INACTIVE") {
            return `<span class="badge orange">INACTIVE</span>`;
        }

        if (status === "SUSPENDED") {
            return `<span class="badge red">SUSPENDED</span>`;
        }

        return `<span class="badge">${status}</span>`;
    }
});