document.addEventListener("DOMContentLoaded", () => {
    const loginForm = document.getElementById("loginForm");
    const errorBox = document.getElementById("errorBox");

    loginForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        errorBox.textContent = "";

        const email = document.getElementById("email").value.trim();
        const password = document.getElementById("password").value;

        try {
            const response = await apiRequest("/auth/login", {
                method: "POST",
                body: JSON.stringify({
                    email,
                    password
                })
            });

            saveCurrentUser(response.user);
            redirectByRole(response.user);
        } catch (error) {
            errorBox.textContent = error.message;
        }
    });
});