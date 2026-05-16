const API_BASE_URL = "http://localhost:8080/api";

async function apiRequest(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`;

    const defaultHeaders = {
        "Content-Type": "application/json"
    };

    const config = {
        ...options,
        headers: {
            ...defaultHeaders,
            ...(options.headers || {})
        }
    };

    try {
        const response = await fetch(url, config);

        const contentType = response.headers.get("content-type");
        const hasJson = contentType && contentType.includes("application/json");

        const data = hasJson ? await response.json() : null;

        if (!response.ok) {
            const message = data?.message || "Request failed";
            throw new Error(message);
        }

        return data;
    } catch (error) {
        console.error("API error:", error);
        throw error;
    }
}

function getCurrentUser() {
    const userJson = localStorage.getItem("currentUser");
    return userJson ? JSON.parse(userJson) : null;
}

function saveCurrentUser(user) {
    localStorage.setItem("currentUser", JSON.stringify(user));
}

function getLoginPath() {
    const path = window.location.pathname.replaceAll("\\", "/");

    if (path.includes("/pages/admin/") || path.includes("/pages/coach/") || path.includes("/pages/athlete/")) {
        return "../login.html";
    }

    if (path.includes("/pages/")) {
        return "login.html";
    }

    return "pages/login.html";
}

function logout() {
    localStorage.removeItem("currentUser");
    window.location.href = getLoginPath();
}

function requireAuth() {
    const user = getCurrentUser();

    if (!user) {
        window.location.href = getLoginPath();
        return null;
    }

    return user;
}

function redirectByRole(user) {
    if (user.roleName === "ADMIN") {
        window.location.href = "admin/dashboard.html";
    } else if (user.roleName === "COACH") {
        window.location.href = "coach/dashboard.html";
    } else if (user.roleName === "ATHLETE") {
        window.location.href = "athlete/dashboard.html";
    } else {
        alert("Unknown role: " + user.roleName);
    }
}