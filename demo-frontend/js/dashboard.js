document.addEventListener("DOMContentLoaded", () => {
    const user = requireAuth();

    if (!user) {
        return;
    }

    const userBox = document.getElementById("userBox");

    if (userBox) {
        userBox.innerHTML = `
            <strong>${user.firstName} ${user.lastName}</strong>
            <span class="badge blue">${user.roleName}</span>
        `;
    }

    const logoutButton = document.getElementById("logoutButton");

    if (logoutButton) {
        logoutButton.addEventListener("click", logout);
    }
});