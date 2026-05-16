const API_BASE = window.API_BASE_URL || "http://localhost:8080/api";

const variantTemplates = [
    {
        label: "Male RX",
        genderCategory: "MALE",
        executionLevel: "RX"
    },
    {
        label: "Female RX",
        genderCategory: "FEMALE",
        executionLevel: "RX"
    },
    {
        label: "Male Scaled",
        genderCategory: "MALE",
        executionLevel: "SCALED"
    },
    {
        label: "Female Scaled",
        genderCategory: "FEMALE",
        executionLevel: "SCALED"
    }
];

const scoreTypes = [
    "TIME",
    "REPS",
    "WEIGHT",
    "ROUNDS_REPS",
    "DISTANCE",
    "CALORIES",
    "NO_SCORE"
];

let allWods = [];

document.addEventListener("DOMContentLoaded", () => {
    setupLogoutButton();
    setupDefaultCreator();
    renderVariantInputs();
    setupEventListeners();
    loadWods();
});

function setupEventListeners() {
    const wodForm = document.getElementById("wodForm");
    const resetFormBtn = document.getElementById("resetFormBtn");
    const reloadWodsBtn = document.getElementById("reloadWodsBtn");
    const statusFilter = document.getElementById("statusFilter");
    const dateFilter = document.getElementById("dateFilter");

    wodForm.addEventListener("submit", handleSubmitWod);
    resetFormBtn.addEventListener("click", resetForm);
    reloadWodsBtn.addEventListener("click", loadWods);

    statusFilter.addEventListener("change", renderWodsTable);
    dateFilter.addEventListener("change", renderWodsTable);
}

function setupLogoutButton() {
    const logoutBtn = document.getElementById("logoutBtn");

    if (!logoutBtn) return;

    logoutBtn.addEventListener("click", () => {
        localStorage.removeItem("currentUser");
        localStorage.removeItem("loggedUser");
        localStorage.removeItem("user");
        localStorage.removeItem("authUser");
        localStorage.removeItem("buffaloUser");
        window.location.href = "../login.html";
    });
}

function setupDefaultCreator() {
    const createdByUserIdInput = document.getElementById("createdByUserId");
    const currentUser = getCurrentUser();

    if (currentUser && currentUser.id) {
        createdByUserIdInput.value = currentUser.id;
    }
}

function getCurrentUser() {
    const possibleKeys = [
        "currentUser",
        "loggedUser",
        "user",
        "authUser",
        "buffaloUser"
    ];

    for (const key of possibleKeys) {
        const value = localStorage.getItem(key);

        if (!value) continue;

        try {
            return JSON.parse(value);
        } catch (error) {
            console.warn(`Could not parse localStorage key: ${key}`, error);
        }
    }

    return null;
}

function renderVariantInputs(existingVariants = []) {
    const variantsGrid = document.getElementById("variantsGrid");
    variantsGrid.innerHTML = "";

    variantTemplates.forEach((template, index) => {
        const existing = findExistingVariant(existingVariants, template);

        const scoreType = existing?.scoreType || defaultScoreTypeByVariant(template);
        const mainWorkout = existing?.mainWorkout || "";
        const notes = existing?.notes || "";

        const card = document.createElement("div");
        card.className = "variant-card";

        card.innerHTML = `
            <h4>${template.label}</h4>

            <input type="hidden" id="variantGender${index}" value="${template.genderCategory}">
            <input type="hidden" id="variantLevel${index}" value="${template.executionLevel}">

            <div class="form-group">
                <label for="variantScoreType${index}">Score Type</label>
                <select id="variantScoreType${index}">
                    ${scoreTypes.map(type => `
                        <option value="${type}" ${type === scoreType ? "selected" : ""}>
                            ${type}
                        </option>
                    `).join("")}
                </select>
            </div>

            <div class="form-group">
                <label for="variantMainWorkout${index}">Main Workout</label>
                <textarea 
                    id="variantMainWorkout${index}" 
                    rows="5" 
                    placeholder="Workout for ${template.label}"
                >${escapeTextarea(mainWorkout)}</textarea>
            </div>

            <div class="form-group">
                <label for="variantNotes${index}">Notes</label>
                <textarea 
                    id="variantNotes${index}" 
                    rows="3" 
                    placeholder="Scaling notes or category-specific notes"
                >${escapeTextarea(notes)}</textarea>
            </div>
        `;

        variantsGrid.appendChild(card);
    });
}

function findExistingVariant(existingVariants, template) {
    return existingVariants.find(variant =>
        variant.genderCategory === template.genderCategory &&
        variant.executionLevel === template.executionLevel
    );
}

function defaultScoreTypeByVariant(template) {
    return "TIME";
}

function escapeTextarea(value) {
    if (value === null || value === undefined) return "";

    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;");
}

async function handleSubmitWod(event) {
    event.preventDefault();

    const wodId = document.getElementById("wodId").value;
    const payload = buildWodPayload();

    if (!payload.title || !payload.wodDate || !payload.wodType || !payload.status || !payload.createdByUserId) {
        showMessage("Please fill in all required WOD fields.", "error");
        return;
    }

    try {
        if (wodId) {
            await apiRequest(`/wods/${wodId}`, {
                method: "PUT",
                body: JSON.stringify(payload)
            });

            showMessage("WOD updated successfully.", "success");
        } else {
            await apiRequest("/wods", {
                method: "POST",
                body: JSON.stringify(payload)
            });

            showMessage("WOD created successfully.", "success");
        }

        resetForm();
        await loadWods();
    } catch (error) {
        console.error(error);
        showMessage(error.message || "Could not save WOD.", "error");
    }
}

function buildWodPayload() {
    return {
        title: getValue("title"),
        description: getValue("description"),
        wodDate: getValue("wodDate"),
        wodType: getValue("wodType"),
        status: getValue("status"),
        warmUp: getValue("warmUp"),
        skillOrStrength: getValue("skillOrStrength"),
        videoUrl: getValue("videoUrl"),
        movementNotes: getValue("movementNotes"),
        createdByUserId: Number(getValue("createdByUserId")),
        variants: buildVariantsPayload()
    };
}

function buildVariantsPayload() {
    return variantTemplates.map((template, index) => {
        return {
            genderCategory: getValue(`variantGender${index}`),
            executionLevel: getValue(`variantLevel${index}`),
            scoreType: getValue(`variantScoreType${index}`),
            mainWorkout: getValue(`variantMainWorkout${index}`),
            notes: getValue(`variantNotes${index}`)
        };
    }).filter(variant => variant.mainWorkout.trim() !== "");
}

function getValue(id) {
    const element = document.getElementById(id);
    return element ? element.value.trim() : "";
}

async function loadWods() {
    const tableBody = document.getElementById("wodsTableBody");

    tableBody.innerHTML = `
        <tr>
            <td colspan="6">Loading WODs...</td>
        </tr>
    `;

    try {
        allWods = await apiRequest("/wods");
        renderWodsTable();
    } catch (error) {
        console.error(error);

        tableBody.innerHTML = `
            <tr>
                <td colspan="6">Could not load WODs: ${escapeHtml(error.message)}</td>
            </tr>
        `;

        showMessage(error.message || "Could not load WODs.", "error");
    }
}

function renderWodsTable() {
    const tableBody = document.getElementById("wodsTableBody");
    const statusFilter = document.getElementById("statusFilter").value;
    const dateFilter = document.getElementById("dateFilter").value;

    let filteredWods = [...allWods];

    if (statusFilter !== "ALL") {
        filteredWods = filteredWods.filter(wod => wod.status === statusFilter);
    }

    if (dateFilter) {
        filteredWods = filteredWods.filter(wod => wod.wodDate === dateFilter);
    }

    filteredWods.sort((a, b) => {
        const dateA = a.wodDate || "";
        const dateB = b.wodDate || "";
        return dateB.localeCompare(dateA);
    });

    if (filteredWods.length === 0) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="6">No WODs found.</td>
            </tr>
        `;
        return;
    }

    tableBody.innerHTML = filteredWods.map(wod => {
        const variantCount = Array.isArray(wod.variants) ? wod.variants.length : 0;

        return `
            <tr>
                <td>${escapeHtml(wod.wodDate || "-")}</td>
                <td>
                    <strong>${escapeHtml(wod.title || "-")}</strong>
                    <br>
                    <small>${escapeHtml(shortText(wod.description || "", 80))}</small>
                </td>
                <td>${escapeHtml(wod.wodType || "-")}</td>
                <td>
                    <span class="status-badge ${statusClass(wod.status)}">
                        ${escapeHtml(wod.status || "-")}
                    </span>
                </td>
                <td>${variantCount}</td>
                <td class="actions-cell">
                    <button type="button" class="secondary-btn small-btn" onclick="editWod(${wod.id})">
                        Edit
                    </button>

                    <button type="button" class="primary-btn small-btn" onclick="changeWodStatus(${wod.id}, 'PUBLISHED')">
                        Publish
                    </button>

                    <button type="button" class="secondary-btn small-btn" onclick="changeWodStatus(${wod.id}, 'ARCHIVED')">
                        Archive
                    </button>

                    <button type="button" class="danger-btn small-btn" onclick="deleteWod(${wod.id})">
                        Delete
                    </button>
                </td>
            </tr>
        `;
    }).join("");
}

function statusClass(status) {
    if (status === "PUBLISHED") return "status-active";
    if (status === "DRAFT") return "status-pending";
    if (status === "ARCHIVED") return "status-inactive";
    return "";
}

function editWod(wodId) {
    const wod = allWods.find(item => Number(item.id) === Number(wodId));

    if (!wod) {
        showMessage("Could not find selected WOD.", "error");
        return;
    }

    document.getElementById("formTitle").textContent = "Edit WOD";
    document.getElementById("wodId").value = wod.id || "";
    document.getElementById("title").value = wod.title || "";
    document.getElementById("description").value = wod.description || "";
    document.getElementById("wodDate").value = wod.wodDate || "";
    document.getElementById("wodType").value = wod.wodType || "AMRAP";
    document.getElementById("status").value = wod.status || "DRAFT";
    document.getElementById("warmUp").value = wod.warmUp || "";
    document.getElementById("skillOrStrength").value = wod.skillOrStrength || "";
    document.getElementById("videoUrl").value = wod.videoUrl || "";
    document.getElementById("movementNotes").value = wod.movementNotes || "";

    const creatorId =
        wod.createdByUserId ||
        wod.createdBy?.id ||
        getCurrentUser()?.id ||
        "";

    document.getElementById("createdByUserId").value = creatorId;

    renderVariantInputs(wod.variants || []);

    window.scrollTo({
        top: 0,
        behavior: "smooth"
    });
}

async function changeWodStatus(wodId, newStatus) {
    const confirmed = confirm(`Are you sure you want to change this WOD to ${newStatus}?`);

    if (!confirmed) return;

    try {
        await patchWodStatus(wodId, newStatus);

        showMessage(`WOD changed to ${newStatus}.`, "success");
        await loadWods();
    } catch (error) {
        console.error(error);
        showMessage(error.message || `Could not change WOD status to ${newStatus}.`, "error");
    }
}

async function patchWodStatus(wodId, newStatus) {
    try {
        return await apiRequest(`/wods/${wodId}/status`, {
            method: "PATCH",
            body: JSON.stringify({ status: newStatus })
        });
    } catch (firstError) {
        console.warn("Object status patch failed. Trying raw string body.", firstError);

        return await apiRequest(`/wods/${wodId}/status`, {
            method: "PATCH",
            body: JSON.stringify(newStatus)
        });
    }
}

async function deleteWod(wodId) {
    const confirmed = confirm(
        "Are you sure you want to delete this WOD? Use Archive instead if you want to keep it historically visible."
    );

    if (!confirmed) return;

    try {
        await apiRequest(`/wods/${wodId}`, {
            method: "DELETE"
        });

        showMessage("WOD deleted successfully.", "success");
        await loadWods();
    } catch (error) {
        console.error(error);
        showMessage(error.message || "Could not delete WOD.", "error");
    }
}

function resetForm() {
    document.getElementById("formTitle").textContent = "Create WOD";
    document.getElementById("wodForm").reset();
    document.getElementById("wodId").value = "";

    setupDefaultCreator();
    renderVariantInputs();
}

async function apiRequest(path, options = {}) {
    const response = await fetch(`${API_BASE}${path}`, {
        headers: {
            "Content-Type": "application/json",
            ...(options.headers || {})
        },
        ...options
    });

    const contentType = response.headers.get("content-type") || "";
    const hasJson = contentType.includes("application/json");

    let data = null;

    if (hasJson) {
        data = await response.json();
    } else {
        const text = await response.text();
        data = text ? { message: text } : null;
    }

    if (!response.ok) {
        const message =
            data?.message ||
            data?.error ||
            data?.detail ||
            `Request failed with status ${response.status}`;

        throw new Error(message);
    }

    return data;
}

function showMessage(message, type = "info") {
    const messageBox = document.getElementById("messageBox");

    messageBox.textContent = message;
    messageBox.className = `message-box ${type}`;

    setTimeout(() => {
        messageBox.className = "message-box hidden";
        messageBox.textContent = "";
    }, 6000);
}

function shortText(text, maxLength) {
    if (!text) return "";

    if (text.length <= maxLength) return text;

    return `${text.substring(0, maxLength)}...`;
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