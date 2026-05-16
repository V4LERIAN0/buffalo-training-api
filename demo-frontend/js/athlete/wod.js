let currentWod = null;

document.addEventListener("DOMContentLoaded", () => {
    setupInitialDate();
    setupEvents();
    loadPublishedWodByDate();
});

function setupEvents() {
    document.getElementById("loadWodButton").addEventListener("click", loadPublishedWodByDate);
}

function setupInitialDate() {
    const today = new Date();
    document.getElementById("wodDateInput").value = today.toISOString().split("T")[0];
}

async function loadPublishedWodByDate() {
    const date = document.getElementById("wodDateInput").value;
    const wodContent = document.getElementById("wodContent");

    if (!date) {
        showMessage("Please select a date.", "error");
        return;
    }

    wodContent.innerHTML = "<p>Loading WOD...</p>";

    try {
        currentWod = await apiRequest(`/wods/date/${date}/published`);
        renderWod(currentWod);
    } catch (error) {
        currentWod = null;

        wodContent.innerHTML = `
            <div class="empty-state">
                <h3>No published WOD found</h3>
                <p>There is no published WOD for ${escapeHtml(date)} yet.</p>
            </div>
        `;

        showMessage(error.message || "No published WOD found for this date.", "error");
    }
}

function renderWod(wod) {
    const wodContent = document.getElementById("wodContent");
    const variants = wod.variants || [];

    wodContent.innerHTML = `
        <div class="wod-detail">
            <h2>${escapeHtml(wod.title || "Untitled WOD")}</h2>

            <p>
                <span class="badge blue">${escapeHtml(wod.wodType || "-")}</span>
                <span class="badge green">${escapeHtml(wod.status || "-")}</span>
                <span class="badge">${escapeHtml(wod.wodDate || "-")}</span>
            </p>

            ${renderSection("Description", wod.description)}
            ${renderSection("Warm Up", wod.warmUp)}
            ${renderSection("Skill / Strength", wod.skillOrStrength)}
            ${renderSection("Movement Notes", wod.movementNotes)}
            ${renderVideo(wod.videoUrl)}

            <hr>

            <h2>Variants</h2>

            <div class="variant-list">
                ${
                    variants.length === 0
                        ? "<p>No variants registered for this WOD.</p>"
                        : variants.map(renderVariantCard).join("")
                }
            </div>
        </div>
    `;
}

function renderSection(title, content) {
    if (!content) return "";

    return `
        <div class="info-section">
            <h3>${escapeHtml(title)}</h3>
            <p>${formatMultiline(content)}</p>
        </div>
    `;
}

function renderVideo(videoUrl) {
    if (!videoUrl) return "";

    return `
        <div class="info-section">
            <h3>Video</h3>
            <p>
                <a href="${escapeAttribute(videoUrl)}" target="_blank" rel="noopener noreferrer">
                    Open movement video
                </a>
            </p>
        </div>
    `;
}

function renderVariantCard(variant) {
    return `
        <div class="variant-card">
            <h3>${escapeHtml(formatVariantName(variant))}</h3>

            <p>
                <span class="badge orange">${escapeHtml(variant.scoreType || "-")}</span>
            </p>

            <div class="info-section">
                <h4>Main Workout</h4>
                <p>${formatMultiline(variant.mainWorkout || "-")}</p>
            </div>

            ${
                variant.notes
                    ? `
                        <div class="info-section">
                            <h4>Notes</h4>
                            <p>${formatMultiline(variant.notes)}</p>
                        </div>
                    `
                    : ""
            }
        </div>
    `;
}

function formatVariantName(variant) {
    const gender = variant.genderCategory === "MALE" ? "Male" : "Female";
    const level = variant.executionLevel === "RX" ? "RX" : "Scaled";
    return `${gender} ${level}`;
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

function formatMultiline(value) {
    if (!value) return "";
    return escapeHtml(value).replaceAll("\n", "<br>");
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

function escapeAttribute(value) {
    return escapeHtml(value).replaceAll("`", "&#096;");
}