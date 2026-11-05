import eslint from "@eslint/js";
import tseslint from "typescript-eslint";

export default tseslint.config(
    { ignores: ["build/**", "**/build/**", "node_modules/**"] },
    eslint.configs.recommended,
    tseslint.configs.recommended,
    {
        files: ["sender/src/**/*.ts"],
        languageOptions: {
            globals: Object.fromEntries(
                [
                    "document",
                    "location",
                    "history",
                    "localStorage",
                    "sessionStorage",
                    "fetch",
                    "AbortController",
                    "setTimeout",
                    "clearTimeout",
                    "crypto",
                    "navigator",
                    "URLSearchParams",
                    "HTMLElement",
                    "HTMLButtonElement",
                    "HTMLFormElement",
                    "HTMLInputElement",
                    "Record",
                ].map((name) => [name, "readonly"]),
            ),
        },
    },
);
