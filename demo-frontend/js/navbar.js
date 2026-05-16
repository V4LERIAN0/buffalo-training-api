function getPageTitleFromDocument() {
    return document.body.dataset.pageTitle || document.title || "Buffalo Training";
}

function injectNavbar() {
    if (document.querySelector(".navbar")) {
        return;
    }

    const mount = document.getElementById("navbarMount");
    const pageTitle = getPageTitleFromDocument();

    const navbar = document.createElement("div");
    navbar.className = "navbar";
    navbar.innerHTML = `
        <div class="navbar-title">${pageTitle}</div>
        <div>
            <span id="userBox"></span>
            <button id="logoutButton" class="secondary" type="button">Logout</button>
        </div>
    `;

    if (mount) {
        mount.replaceWith(navbar);
    } else {
        document.body.prepend(navbar);
    }
}

if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", injectNavbar);
} else {
    injectNavbar();
}
