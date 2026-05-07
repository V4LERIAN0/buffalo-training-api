let currentUser = null;
let currentWod = null;

document.addEventListener("DOMContentLoaded", () => {
    currentUser = getCurrentUser();

    if (!currentUser || !currentUser.id) {
        showMessage("Could not find logged-in athlete. Please login again.", "error");
        return;
    }

    setupInitialDate();
    setupEvents();
    loadPublishedWodByDate();
});

function setupEvents() {
    document.getElementById("loadWodButton").addEventListener("click", loadPublishedWodByDate);
    document.getElementById("variantSelect").addEventListener("change", handleVariantChange);
    document.getElementById("submitScoreButton").addEventListener("click", openScoreModal);
    document.getElementById("closeScoreModalButton").addEventListener("click", closeScoreModal);
    document.getElementById("cancelScoreModalButton").addEventListener("click", closeScoreModal);
    document.getElementById("scoreForm").addEventListener("submit", submitScore);
}

function setupInitialDate() {
    const today = new Date();
    document.getElementById("wodDateInput").value = today.toISOString().split("T")[0];
}

async function loadPublishedWodByDate() {
    const date = document.getElementById("wodDateInput").value;
    const wodSummary = document.getElementById("wodSummary");
    const leaderboardCard = document.getElementById("leaderboardCard");

    if (!date) {
        showMessage("Please select a date.", "error");
        return;
    }

    wodSummary.innerHTML = "<p>Loading WOD...</p>";
    leaderboardCard.classList.add("hidden");

    try {
        currentWod = await apiRequest(`/wods/date/${date}/published`);

        renderWodSummary(currentWod);
        setupVariantSelect(currentWod.variants || []);

        leaderboardCard.classList.remove("hidden");
        await loadLeaderboardForSelectedVariant();
    } catch (error) {
        currentWod = null;

        wodSummary.innerHTML = `
            <div class="empty-state">
                <h3>No published WOD found</h3>
                <p>There is no published WOD for ${escapeHtml(date)} yet.</p>
            </div>
        `;

        showMessage(error.message || "No published WOD found for this date.", "error");
    }
}

function renderWodSummary(wod) {
    const wodSummary = document.getElementById("wodSummary");

    wodSummary.className = "summary-box";

    wodSummary.innerHTML = `
        <h2>${escapeHtml(wod.title || "Untitled WOD")}</h2>
        <p>
            <span class="badge blue">${escapeHtml(wod.wodType || "-")}</span>
            <span class="badge green">${escapeHtml(wod.status || "-")}</span>
            <span class="badge">${escapeHtml(wod.wodDate || "-")}</span>
        </p>
        <p>${escapeHtml(wod.description || "No description provided.")}</p>
    `;
}

function setupVariantSelect(variants) {
    const variantSelect = document.getElementById("variantSelect");

    if (!variants.length) {
        variantSelect.innerHTML = `<option value="">No variants available</option>`;
        return;
    }

    variantSelect.innerHTML = variants.map(variant => `
        <option value="${variant.id}">
            ${escapeHtml(formatVariantName(variant))} - ${escapeHtml(variant.scoreType)}
        </option>
    `).join("");
}

async function handleVariantChange() {
    await loadLeaderboardForSelectedVariant();
}

async function loadLeaderboardForSelectedVariant() {
    const selectedVariant = getSelectedVariant();
    const leaderboardBody = document.getElementById("leaderboardBody");
    const leaderboardSubtitle = document.getElementById("leaderboardSubtitle");

    if (!selectedVariant) {
        leaderboardBody.innerHTML = `
            <tr>
                <td colspan="5">Select a variant to load leaderboard.</td>
            </tr>
        `;
        return;
    }

    leaderboardSubtitle.textContent = `${formatVariantName(selectedVariant)} · ${selectedVariant.scoreType}`;

    leaderboardBody.innerHTML = `
        <tr>
            <td colspan="5">Loading leaderboard...</td>
        </tr>
    `;

    try {
        const leaderboard = await apiRequest(`/wod-scores/leaderboard/variant/${selectedVariant.id}`);
        renderLeaderboard(leaderboard);
    } catch (error) {
        leaderboardBody.innerHTML = `
            <tr>
                <td colspan="5">Could not load leaderboard: ${escapeHtml(error.message)}</td>
            </tr>
        `;
    }
}

function renderLeaderboard(leaderboard) {
    const leaderboardBody = document.getElementById("leaderboardBody");
    const entries = leaderboard.entries || [];

    if (!entries.length) {
        leaderboardBody.innerHTML = `
            <tr>
                <td colspan="5">No scores have been submitted for this variant yet.</td>
            </tr>
        `;
        return;
    }

    leaderboardBody.innerHTML = entries.map(entry => {
        const score = entry.score || {};

        return `
            <tr>
                <td>${entry.position ?? "-"}</td>
                <td>${escapeHtml(score.athleteName || `Athlete #${score.athleteId || "-"}`)}</td>
                <td><strong>${escapeHtml(score.scoreText || "-")}</strong></td>
                <td>
                    <span class="badge ${score.status === "ACTIVE" ? "green" : "orange"}">
                        ${escapeHtml(score.status || "-")}
                    </span>
                </td>
                <td>${escapeHtml(score.notes || "-")}</td>
            </tr>
        `;
    }).join("");
}

function openScoreModal() {
    const selectedVariant = getSelectedVariant();

    if (!selectedVariant) {
        showMessage("Please select a variant first.", "error");
        return;
    }

    const normalizedScoreType = normalizeScoreType(selectedVariant.scoreType);

    document.getElementById("scoreModalSubtitle").textContent =
        `${formatVariantName(selectedVariant)} · ${normalizedScoreType}`;

    renderScoreFields(normalizedScoreType);

    const modal = document.getElementById("scoreModal");
    modal.classList.remove("hidden");
    modal.setAttribute("aria-hidden", "false");
}

function closeScoreModal() {
    const modal = document.getElementById("scoreModal");

    modal.classList.add("hidden");
    modal.setAttribute("aria-hidden", "true");

    document.getElementById("scoreForm").reset();

    document.getElementById("scoreFieldsContainer").innerHTML = `
        <p class="helper-text">Score fields will appear after selecting a variant.</p>
    `;
}

function renderScoreFields(scoreType) {
    const container = document.getElementById("scoreFieldsContainer");
    const normalizedScoreType = normalizeScoreType(scoreType);

    if (normalizedScoreType === "TIME") {
        container.innerHTML = `
            <div class="form-group">
                <label for="timeMinutes">Minutes</label>
                <input type="number" id="timeMinutes" min="0" step="1" required placeholder="Example: 10">
            </div>

            <div class="form-group">
                <label for="timeSecondsInput">Seconds</label>
                <input type="number" id="timeSecondsInput" min="0" max="59" step="1" required placeholder="Example: 0">
            </div>

            <p class="helper-text">For a time like 10:00, enter 10 minutes and 0 seconds.</p>
        `;
        return;
    }

    if (normalizedScoreType === "REPS") {
        container.innerHTML = `
            <div class="form-group">
                <label for="reps">Total reps</label>
                <input type="number" id="reps" min="0" step="1" required placeholder="Example: 145">
            </div>
        `;
        return;
    }

    if (normalizedScoreType === "WEIGHT") {
        container.innerHTML = `
            <div class="form-group">
                <label for="weight">Weight</label>
                <input type="number" id="weight" min="0" step="0.01" required placeholder="Example: 225">
            </div>

            <p class="helper-text">Use the unit defined by the coach, usually lb.</p>
        `;
        return;
    }

    if (normalizedScoreType === "ROUNDS_REPS") {
        container.innerHTML = `
            <div class="form-group">
                <label for="rounds">Rounds</label>
                <input type="number" id="rounds" min="0" step="1" required placeholder="Example: 7">
            </div>

            <div class="form-group">
                <label for="reps">Extra reps</label>
                <input type="number" id="reps" min="0" step="1" required placeholder="Example: 12">
            </div>

            <p class="helper-text">For 7 rounds + 12 reps, enter 7 and 12.</p>
        `;
        return;
    }

    if (normalizedScoreType === "DISTANCE") {
        container.innerHTML = `
            <div class="form-group">
                <label for="distance">Distance</label>
                <input type="number" id="distance" min="0" step="0.01" required placeholder="Example: 1000">
            </div>

            <p class="helper-text">Use the unit defined by the coach, usually meters.</p>
        `;
        return;
    }

    if (normalizedScoreType === "CALORIES") {
        container.innerHTML = `
            <div class="form-group">
                <label for="calories">Calories</label>
                <input type="number" id="calories" min="0" step="1" required placeholder="Example: 85">
            </div>
        `;
        return;
    }

    container.innerHTML = `
        <div class="form-group">
            <label for="scoreTextOnly">Score / Completion note</label>
            <input type="text" id="scoreTextOnly" required placeholder="Example: Completed">
        </div>
    `;
}

function normalizeScoreType(scoreType) {
    return String(scoreType || "")
        .trim()
        .toUpperCase()
        .replaceAll("-", "_")
        .replaceAll(" ", "_");
}

async function submitScore(event) {
    event.preventDefault();

    const selectedVariant = getSelectedVariant();

    if (!currentWod || !selectedVariant) {
        showMessage("Please load a WOD and select a variant first.", "error");
        return;
    }

    const payload = buildScorePayload(selectedVariant);
    console.log("Submitting score payload:", payload);

    try {
        await apiRequest("/wod-scores", {
            method: "POST",
            body: JSON.stringify(payload)
        });

        showMessage("Score submitted successfully.", "success");
        closeScoreModal();
        await loadLeaderboardForSelectedVariant();
    } catch (error) {
        showMessage(error.message || "Could not submit score.", "error");
    }
}

function buildScorePayload(variant) {
    const scoreType = normalizeScoreType(variant.scoreType);
    const notes = document.getElementById("scoreNotes").value.trim();

    const payload = {
        wodVariantId: Number(variant.id),
        athleteId: Number(currentUser.id),
        scoreText: "",
        notes: notes
    };

    if (scoreType === "TIME") {
        const minutes = Number(document.getElementById("timeMinutes").value);
        const seconds = Number(document.getElementById("timeSecondsInput").value);

        const totalSeconds = minutes * 60 + seconds;

        payload.timeSeconds = totalSeconds;
        payload.scoreText = formatSecondsAsTime(totalSeconds);

        return payload;
    }

    if (scoreType === "REPS") {
        const reps = Number(document.getElementById("reps").value);

        payload.reps = reps;
        payload.scoreText = `${reps} reps`;

        return payload;
    }

    if (scoreType === "WEIGHT") {
        const weight = Number(document.getElementById("weight").value);

        payload.weight = weight;
        payload.scoreText = `${weight} lb`;

        return payload;
    }

    if (scoreType === "ROUNDS_REPS") {
        const rounds = Number(document.getElementById("rounds").value);
        const reps = Number(document.getElementById("reps").value);

        payload.rounds = rounds;
        payload.reps = reps;
        payload.scoreText = `${rounds} rounds + ${reps} reps`;

        return payload;
    }

    if (scoreType === "DISTANCE") {
        const distance = Number(document.getElementById("distance").value);

        payload.distance = distance;
        payload.scoreText = `${distance} m`;

        return payload;
    }

    if (scoreType === "CALORIES") {
        const calories = Number(document.getElementById("calories").value);

        payload.calories = calories;
        payload.scoreText = `${calories} cal`;

        return payload;
    }

    const scoreTextOnly = document.getElementById("scoreTextOnly").value.trim();

    payload.scoreText = scoreTextOnly || "Completed";

    return payload;
}

function getSelectedVariant() {
    if (!currentWod || !Array.isArray(currentWod.variants)) return null;

    const selectedId = Number(document.getElementById("variantSelect").value);

    return currentWod.variants.find(variant => Number(variant.id) === selectedId);
}

function formatSecondsAsTime(totalSeconds) {
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;

    return `${minutes}:${String(seconds).padStart(2, "0")}`;
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

function escapeHtml(value) {
    if (value === null || value === undefined) return "";

    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}