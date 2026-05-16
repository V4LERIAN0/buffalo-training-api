document.addEventListener("DOMContentLoaded", () => {
    const currentUser = requireAuth();

    if (!currentUser) {
        return;
    }

    const form = document.getElementById("changePasswordForm");
    const messageBox = document.getElementById("passwordMessage");
    const backLink = document.getElementById("backToDashboardLink");

    configureBackLink(currentUser);

    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        messageBox.className = "";
        messageBox.textContent = "";

        const currentPassword = document.getElementById("currentPassword").value;
        const newPassword = document.getElementById("newPassword").value;
        const confirmPassword = document.getElementById("confirmPassword").value;

        if (newPassword !== confirmPassword) {
            messageBox.className = "error";
            messageBox.textContent = "New passwords do not match.";
            return;
        }

        try {
            await apiRequest(`/users/${currentUser.id}/password/change`, {
                method: "PATCH",
                body: JSON.stringify({
                    currentPassword,
                    newPassword
                })
            });

            messageBox.className = "success";
            messageBox.textContent = "Password changed successfully. Please use your new password next time.";

            form.reset();
        } catch (error) {
            messageBox.className = "error";
            messageBox.textContent = error.message;
        }
    });

    function configureBackLink(user) {
        if (user.roleName === "ADMIN") {
            backLink.href = "admin/dashboard.html";
        } else if (user.roleName === "COACH") {
            backLink.href = "coach/dashboard.html";
        } else if (user.roleName === "ATHLETE") {
            backLink.href = "athlete/dashboard.html";
        } else {
            backLink.href = "login.html";
        }
    }
});