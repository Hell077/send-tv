import { expect, test, type Page } from "@playwright/test";
import { readFile } from "node:fs/promises";
import AxeBuilder from "@axe-core/playwright";

async function scan(page: Page) {
    const challenge = await readFile("build/test-challenge", "utf8");
    await page.goto(`/#challenge=${challenge}`);
    await expect(page.locator("#receiver")).toHaveText("Test Living Room TV");
    await expect(page).toHaveURL("http://127.0.0.1:18765/");
}

async function pair(page: Page) {
    await scan(page);
    await page.getByLabel("This device’s name").fill("Test phone");
    await page.getByRole("button", { name: "Request TV approval" }).click();
    await expect(page.getByRole("status")).toHaveText(
        "Waiting for TV approval",
    );
    await expect(page.getByRole("status")).toHaveText("Connected", {
        timeout: 8000,
    });
}

test("a direct visit requires a TV QR and explains HTTP risk", async ({
    page,
}) => {
    await page.goto("/");
    await expect(
        page.getByRole("button", { name: "Request TV approval" }),
    ).toBeDisabled();
    await expect(
        page.getByText("Local HTTP does not encrypt", { exact: false }),
    ).toBeVisible();
});

test("a new QR can arrive in an already open sender tab", async ({ page }) => {
    await page.goto("/");
    await expect(
        page.getByRole("button", { name: "Request TV approval" }),
    ).toBeDisabled();
    const challenge = await readFile("build/test-challenge", "utf8");
    await page.goto(`/#challenge=${challenge}`);
    await expect(
        page.getByRole("button", { name: "Request TV approval" }),
    ).toBeEnabled();
    await expect(page).toHaveURL("http://127.0.0.1:18765/");
});

test("pairs through the real API and reports handoff without claiming page load", async ({
    page,
}) => {
    await page.setViewportSize({ width: 320, height: 844 });
    await pair(page);
    await page.getByLabel("Website address").fill("https://example.com/");
    await page.getByRole("button", { name: "Open on TV" }).click();
    await expect(page.getByRole("status")).toContainText(
        "Android accepted the handoff",
    );
    expect(
        await page.evaluate(() => document.documentElement.scrollWidth),
    ).toBeLessThanOrEqual(320);
    await page.reload();
    await expect(page.getByRole("status")).toHaveText("Connected");
    await page.getByRole("button", { name: "Forget this browser" }).click();
    await expect(page.locator("#pairing")).toBeVisible();
});

test("invalid URLs, missing browser, and background limits show recovery", async ({
    page,
}) => {
    await pair(page);
    for (const [url, message] of [
        ["javascript:alert(1)", "Enter a valid HTTP or HTTPS URL"],
        ["https://example.com/no-browser", "No compatible browser installed"],
        ["https://example.com/background", "Return to SendTV on your TV"],
    ]) {
        await page.getByLabel("Website address").fill(url);
        await page.getByRole("button", { name: "Open on TV" }).click();
        await expect(page.getByRole("alert")).toContainText(message);
        await expect(
            page.getByRole("button", { name: "Open on TV" }),
        ).toBeEnabled();
    }
});

test("denial never enables the sender", async ({ page }) => {
    await scan(page);
    await page.getByLabel("This device’s name").fill("Denied test device");
    await page.getByRole("button", { name: "Request TV approval" }).click();
    await expect(page.getByRole("status")).toHaveText("Pairing denied", {
        timeout: 8000,
    });
    await expect(page.locator("#sender")).toBeHidden();
});

test("paired sender passes automated WCAG AA checks", async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 });
    await pair(page);
    const results = await new AxeBuilder({ page })
        .withTags(["wcag2a", "wcag2aa", "wcag21aa", "wcag22aa"])
        .analyze();
    expect(results.violations).toEqual([]);
});

test("Russian preference survives reload and language changes preserve pairing and input", async ({
    browser,
}) => {
    const context = await browser.newContext({
        locale: "ru-RU",
        viewport: { width: 320, height: 844 },
    });
    const page = await context.newPage();
    try {
        await scan(page);
        await expect(page.locator("html")).toHaveAttribute("lang", "ru");
        await expect(
            page.getByText("Локальный HTTP не шифрует", { exact: false }),
        ).toBeVisible();
        await page.getByLabel("Имя этого устройства").fill("Тестовый телефон");
        await page
            .getByRole("button", { name: "Запросить разрешение на ТВ" })
            .click();
        await expect(page.getByRole("status")).toHaveText(
            "Ожидание разрешения на ТВ",
        );
        const code = (await page.locator("#verification").innerText()).match(
            /\d{6}/,
        )?.[0];
        expect(code).toBeTruthy();
        await page.locator("#language").selectOption("en");
        await expect(page.locator("#verification")).toContainText(
            `Compare code ${code}`,
        );
        await expect(page.locator("#name")).toHaveValue("Тестовый телефон");
        await page.locator("#language").selectOption("ru");
        await expect(page.getByRole("status")).toHaveText("Подключено", {
            timeout: 8000,
        });
        await expect(page.locator("#receiver")).toHaveText(
            "Test Living Room TV",
        );
        await page.getByLabel("Адрес сайта").fill("javascript:alert(1)");
        await page.getByRole("button", { name: "Открыть на ТВ" }).click();
        await expect(page.getByRole("alert")).toContainText(
            "Введите корректный HTTP-",
        );
        await page.locator("#language").selectOption("en");
        await expect(page.getByRole("alert")).toContainText(
            "Enter a valid HTTP or HTTPS URL",
        );
        await expect(page.locator("#url")).toHaveValue("javascript:alert(1)");
        await expect(page.locator("#receiver")).toHaveText(
            "Test Living Room TV",
        );
        await page.locator("#language").selectOption("ru");
        await page.reload();
        await expect(page.getByRole("status")).toHaveText("Подключено");
        expect(
            await page.evaluate(() => document.documentElement.scrollWidth),
        ).toBeLessThanOrEqual(320);
        expect(
            (
                await new AxeBuilder({ page })
                    .withTags(["wcag2a", "wcag2aa", "wcag21aa", "wcag22aa"])
                    .analyze()
            ).violations,
        ).toEqual([]);
    } finally {
        await context.close();
    }
});

test("unsupported browser language and malformed saved preference fall back to English", async ({
    browser,
}) => {
    const context = await browser.newContext({ locale: "ja-JP" });
    const page = await context.newPage();
    try {
        await page.addInitScript(() =>
            localStorage.setItem("sendtv.language", "unsupported"),
        );
        await page.goto("/");
        await expect(page.locator("html")).toHaveAttribute("lang", "en");
        await expect(
            page.getByRole("button", { name: "Request TV approval" }),
        ).toBeDisabled();
        await page.locator("#language").selectOption("ru");
        await page.setViewportSize({ width: 390, height: 844 });
        await page.screenshot({
            path: `build/sender-qa/${test.info().project.name}-ru-unpaired.png`,
            fullPage: true,
        });
    } finally {
        await context.close();
    }
});
