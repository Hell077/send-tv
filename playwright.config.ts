import { defineConfig, devices } from "@playwright/test";

export default defineConfig({
    testDir: "./sender/tests",
    workers: 1,
    fullyParallel: false,
    use: { baseURL: "http://127.0.0.1:18765", trace: "off", screenshot: "off" },
    projects: [
        { name: "chromium", use: { ...devices["Desktop Chrome"] } },
        { name: "webkit", use: { ...devices["iPhone 13"] } },
    ],
    webServer: {
        command: "./gradlew :tv:senderTestServer --console=plain",
        url: "http://127.0.0.1:18765/v1/info",
        reuseExistingServer: false,
        timeout: 120_000,
    },
});
