export {};

type PairTicket = {
    requestId: string;
    pollToken: string;
    expiresIn: number;
    verificationCode: string;
};
type PairStatus = {
    status: "pending" | "approved" | "denied";
    accessToken?: string;
};
type Operation = {
    operationId: string;
    status: "accepted" | "launching" | "launched" | "failed";
    errorCode?: string;
};

const element = <T extends HTMLElement>(id: string) => {
    const found = document.getElementById(id);
    if (!found) throw new Error("Missing interface element");
    return found as T;
};
const status = element("status");
const error = element("error");
const pairingPanel = element("pairing");
const senderPanel = element("sender");
const pendingPanel = element("pending");
const pairButton = element<HTMLButtonElement>("pair");
const sendButton = element<HTMLButtonElement>("send");
const nameInput = element<HTMLInputElement>("name");
const urlInput = element<HTMLInputElement>("url");
const remember = element<HTMLInputElement>("remember");
const challenge = new URLSearchParams(location.hash.slice(1)).get("challenge");
history.replaceState(null, "", location.pathname);

let token: string | null = null;
let storageKey = "";
let generation = 0;
let ticket: PairTicket | null = null;
let submission: { url: string; id: string } | null = null;

const messages: Record<string, string> = {
    INVALID_URL:
        "Enter a valid HTTP or HTTPS URL without credentials, spaces, or more than 4096 characters.",
    INVALID_DEVICE_NAME:
        "Enter a device name of 1–48 characters without surrounding spaces or hidden formatting.",
    INVALID_CHALLENGE:
        "This QR code expired or was already used. Scan the current code on your TV.",
    UNAUTHORIZED:
        "Access expired or was revoked. Scan the current TV QR code and pair again.",
    NO_BROWSER:
        "No compatible browser installed on the TV. Install one, return to SendTV, and retry.",
    RETURN_TO_RECEIVER:
        "Return to SendTV on your TV, then retry. Android restricts launches from background apps.",
    RECEIVER_STOPPED: "Start SendTV on your TV and reconnect.",
    RATE_LIMITED: "Too many requests. Wait a minute, then retry.",
    DEVICE_LIMIT:
        "The TV has reached its device/request limit. Revoke an old device or wait for pending requests to expire.",
    LAUNCH_FAILED:
        "Android could not open the link. Return to SendTV and check your TV browser.",
    REQUEST_CONFLICT:
        "The request changed during retry. Edit the link and try again.",
};

class ApiError extends Error {
    constructor(readonly code: string) {
        super(
            messages[code] ??
                "The receiver could not complete this request. Please retry.",
        );
    }
}

async function api<T>(
    path: string,
    method = "GET",
    body?: unknown,
    credential?: string,
): Promise<T> {
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 7000);
    try {
        const headers: Record<string, string> = {};
        if (body !== undefined) headers["Content-Type"] = "application/json";
        if (credential) headers.Authorization = `Bearer ${credential}`;
        const response = await fetch(`/v1${path}`, {
            method,
            headers,
            body: body === undefined ? undefined : JSON.stringify(body),
            credentials: "omit",
            cache: "no-store",
            signal: controller.signal,
        });
        if (response.status === 204) return undefined as T;
        const data: unknown = await response.json();
        if (!response.ok) {
            const code =
                typeof data === "object" &&
                data !== null &&
                "code" in data &&
                typeof data.code === "string"
                    ? data.code
                    : "UNKNOWN";
            throw new ApiError(code);
        }
        return data as T;
    } finally {
        clearTimeout(timeout);
    }
}

function credentials(value: string | null, persist = false) {
    token = value;
    try {
        localStorage.removeItem(storageKey);
        sessionStorage.removeItem(storageKey);
        if (value)
            (persist ? localStorage : sessionStorage).setItem(
                storageKey,
                value,
            );
    } catch {
        /* A private-mode storage failure does not prevent this tab from sending. */
    }
}

function render(message: string, waiting = false, busy = false) {
    status.textContent = message;
    pairingPanel.hidden = !!token || waiting;
    senderPanel.hidden = !token;
    pendingPanel.hidden = !waiting;
    pairButton.disabled = busy || !challenge;
    sendButton.disabled = busy;
    element<HTMLButtonElement>("retry").disabled = busy;
}

function fail(failure: unknown) {
    if (failure instanceof ApiError && failure.code === "UNAUTHORIZED")
        credentials(null);
    render(token ? "Connected" : "Pairing required");
    error.textContent =
        failure instanceof ApiError
            ? failure.message
            : "TV unreachable. Check that SendTV is running on the same Wi-Fi or Ethernet network, then retry.";
}

const wait = () => new Promise<void>((resolve) => setTimeout(resolve, 1000));
const requestId = () =>
    Array.from(crypto.getRandomValues(new Uint8Array(16)), (byte) =>
        byte.toString(16).padStart(2, "0"),
    ).join("");

element<HTMLFormElement>("pair-form").addEventListener("submit", (event) => {
    event.preventDefault();
    if (!challenge || pairButton.disabled) return;
    const current = ++generation;
    error.textContent = "";
    render("Connecting…", false, true);
    void (async () => {
        try {
            const pending = await api<PairTicket>("/pair", "POST", {
                challenge,
                deviceName: nameInput.value,
            });
            if (current !== generation) {
                await api(
                    `/pair/${pending.requestId}`,
                    "DELETE",
                    undefined,
                    pending.pollToken,
                );
                return;
            }
            ticket = pending;
            element("verification").textContent =
                `Compare code ${pending.verificationCode} on the TV.`;
            render("Waiting for TV approval", true, true);
            const expires = Date.now() + pending.expiresIn * 1000;
            while (current === generation && Date.now() < expires) {
                await wait();
                if (current !== generation) return;
                const result = await api<PairStatus>(
                    `/pair/${pending.requestId}`,
                    "GET",
                    undefined,
                    pending.pollToken,
                );
                if (current !== generation) return;
                if (result.status === "denied") {
                    ticket = null;
                    render("Pairing denied");
                    error.textContent =
                        "Pairing was denied or cancelled. Scan the current TV QR code to try again.";
                    pairButton.disabled = true;
                    return;
                }
                if (result.status === "approved" && result.accessToken) {
                    credentials(result.accessToken, remember.checked);
                    ticket = null;
                    render("Connected");
                    urlInput.focus();
                    return;
                }
            }
            if (current === generation) throw new ApiError("INVALID_CHALLENGE");
        } catch (failure) {
            if (current === generation) fail(failure);
        }
    })();
});

element("cancel").addEventListener("click", () => {
    generation++;
    const pending = ticket;
    ticket = null;
    render("Pairing cancelled. Scan the current TV QR code to retry.");
    pairButton.disabled = true;
    if (pending)
        void api(
            `/pair/${pending.requestId}`,
            "DELETE",
            undefined,
            pending.pollToken,
        ).catch(() => {
            error.textContent =
                "Cancellation could not reach the TV. The pending request will expire within 60 seconds.";
        });
});

element<HTMLFormElement>("send-form").addEventListener("submit", (event) => {
    event.preventDefault();
    if (!token || sendButton.disabled) return;
    const value = urlInput.value;
    const current = ++generation;
    const credential = token;
    error.textContent = "";
    if (!submission || submission.url !== value)
        submission = { url: value, id: requestId() };
    const id = submission.id;
    render("Sending…", false, true);
    void (async () => {
        try {
            const accepted = await api<Operation>(
                "/open",
                "POST",
                { url: value, requestId: id },
                credential,
            );
            const deadline = Date.now() + 30_000;
            while (current === generation && Date.now() < deadline) {
                const result = await api<Operation>(
                    `/operations/${accepted.operationId}`,
                    "GET",
                    undefined,
                    credential,
                );
                if (current !== generation) return;
                if (result.status === "launched") {
                    submission = null;
                    render(
                        "Sent to TV. Android accepted the handoff; this does not confirm the page loaded.",
                    );
                    return;
                }
                if (result.status === "failed") {
                    submission = null;
                    throw new ApiError(result.errorCode ?? "LAUNCH_FAILED");
                }
                await wait();
            }
            if (current === generation) {
                render("Handoff status timed out");
                error.textContent =
                    "The TV may have opened the link. Retry checks the same operation without launching a duplicate.";
            }
        } catch (failure) {
            if (current === generation) fail(failure);
        }
    })();
});

element("forget").addEventListener("click", () => {
    generation++;
    credentials(null);
    submission = null;
    render(
        "This browser forgot its credential. Revoke it on the TV to disable any copied credentials.",
    );
});
element("paste").addEventListener("click", () => {
    void navigator.clipboard
        ?.readText()
        .then((value) => {
            urlInput.value = value;
        })
        .catch(() => {
            error.textContent =
                "Clipboard access is unavailable on local HTTP. Use your browser’s Paste command.";
        });
    if (!navigator.clipboard)
        error.textContent =
            "Use your browser’s Paste command in the website address field.";
});
element("retry").addEventListener("click", () => {
    void connect();
});

async function connect() {
    error.textContent = "";
    render("Connecting…", false, true);
    try {
        const info = await api<{
            deviceId: string;
            name: string;
            protocolVersion: number;
        }>("/info");
        if (info.protocolVersion !== 1) throw new ApiError("UNKNOWN");
        storageKey = `sendtv.credential.${info.deviceId}`;
        element("receiver").textContent = info.name;
        try {
            token =
                localStorage.getItem(storageKey) ??
                sessionStorage.getItem(storageKey);
        } catch {
            token = null;
        }
        render(
            token
                ? "Connected"
                : challenge
                  ? "Ready to pair"
                  : "Scan the QR code on your TV to pair.",
        );
        if (token) urlInput.focus();
    } catch (failure) {
        fail(failure);
    }
}

void connect();
