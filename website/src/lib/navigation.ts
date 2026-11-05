export const guides = [
    ["getting-started", "Getting started"],
    ["troubleshooting", "Troubleshooting"],
    ["architecture", "Architecture & privacy"],
    ["api", "Receiver API"],
    ["faq", "FAQ"],
    ["cli", "CLI — planned for V2"],
] as const;

export const href = (path: string) =>
    `${import.meta.env.BASE_URL}${path.replace(/^\//, "")}`;
