import { test, expect } from "@playwright/test";
import AxeBuilder from "@axe-core/playwright";

for (const width of [390, 768, 1440]) {
    test(`landing is readable without horizontal overflow at ${width}px`, async ({
        page,
    }) => {
        await page.setViewportSize({ width, height: 900 });
        await page.goto("./");
        await expect(page.getByRole("heading", { level: 1 })).toHaveText(
            "A link on your phone.A page on your TV.",
        );
        await expect(
            page.getByText("V1 is under verification.", { exact: false }),
        ).toBeVisible();
        expect(
            await page.evaluate(() => document.documentElement.scrollWidth),
        ).toBeLessThanOrEqual(width);
        await page.screenshot({
            path: `build/site-qa/${test.info().project.name}-${width}.png`,
            fullPage: true,
        });
    });
}

test("documentation navigation, guide filtering, mobile menu and FAQ work statically", async ({
    page,
}) => {
    await page.goto("./docs/getting-started/");
    await expect(
        page.getByRole("heading", { name: "Getting started", exact: true }),
    ).toBeVisible();
    await page.getByLabel("Find a guide").fill("Troubleshooting");
    await expect(
        page
            .getByRole("navigation", { name: "Documentation" })
            .getByRole("link"),
    ).toHaveCount(1);
    await page
        .getByRole("link", { name: "Troubleshooting", exact: true })
        .click();
    await expect(
        page.getByRole("heading", {
            name: "Receiver unreachable",
            exact: true,
        }),
    ).toBeVisible();
    await page.goto("./");
    await page
        .getByText("Does this work on any smart TV?", { exact: true })
        .click();
    await expect(
        page.getByText("No. V1 requires an installed SendTV receiver", {
            exact: false,
        }),
    ).toBeVisible();
    await page.setViewportSize({ width: 390, height: 844 });
    await page.getByText("Menu", { exact: true }).click();
    await page
        .getByRole("navigation", { name: "Mobile primary" })
        .getByRole("link", { name: "Docs", exact: true })
        .click();
    await expect(
        page.getByRole("heading", { name: "Getting started", exact: true }),
    ).toBeVisible();
});

test("landing and guides pass automated WCAG AA checks", async ({ page }) => {
    for (const path of ["./", "./docs/getting-started/"]) {
        await page.goto(path);
        const results = await new AxeBuilder({ page })
            .withTags(["wcag2a", "wcag2aa", "wcag21aa", "wcag22aa"])
            .analyze();
        expect(results.violations).toEqual([]);
    }
});

test("Russian routes preserve guide identity, security copy, navigation and mobile layout", async ({
    page,
}) => {
    await page.setViewportSize({ width: 390, height: 844 });
    await page.goto("./docs/getting-started/");
    await page.getByRole("link", { name: "Русский", exact: true }).click();
    await expect(page.locator("html")).toHaveAttribute("lang", "ru");
    await expect(page.getByRole("heading", { level: 1 })).toHaveText(
        "Начало работы",
    );
    await expect(
        page.getByText("HTTP не шифрует коды", { exact: false }),
    ).toBeVisible();
    await expect(
        page.getByRole("button", { name: "Копировать код" }).first(),
    ).toBeVisible();
    await page.getByLabel("Найти руководство").fill("неполадок");
    await page
        .getByRole("navigation", { name: "Документация", exact: true })
        .getByRole("link", { name: "Устранение неполадок" })
        .click();
    await expect(
        page.getByRole("heading", { name: "Приёмник недоступен", exact: true }),
    ).toBeVisible();
    await page.getByRole("link", { name: "English", exact: true }).click();
    await expect(page.getByRole("heading", { level: 1 })).toHaveText(
        "Troubleshooting",
    );
    await page.goto("./ru/");
    await expect(page.locator("html")).toHaveAttribute("lang", "ru");
    await expect(page.getByRole("heading", { level: 1 })).toHaveText(
        "Ссылка на телефоне.Страница на телевизоре.",
    );
    expect(
        await page.evaluate(() => document.documentElement.scrollWidth),
    ).toBeLessThanOrEqual(390);
    expect(
        (
            await new AxeBuilder({ page })
                .withTags(["wcag2a", "wcag2aa", "wcag21aa", "wcag22aa"])
                .analyze()
        ).violations,
    ).toEqual([]);
    await page.screenshot({
        path: `build/site-qa/${test.info().project.name}-ru-390.png`,
        fullPage: true,
    });
});
