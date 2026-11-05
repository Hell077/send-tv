import { readFile, mkdtemp, mkdir, symlink, rm } from "node:fs/promises";
import { spawn } from "node:child_process";
import { tmpdir } from "node:os";
import { join, resolve, dirname } from "node:path";

const html = await readFile("build/site/index.html", "utf8");
const canonical = html.match(/rel="canonical" href="([^"]+)"/);
const base = canonical ? new URL(canonical[1]).pathname : "/";
const temporary = await mkdtemp(join(tmpdir(), "sendtv-site-preview-"));
let directory = resolve("build/site");
if (base !== "/") {
    const mount = join(temporary, base.replace(/\/$/, ""));
    await mkdir(dirname(mount), { recursive: true });
    await symlink(directory, mount, "dir");
    directory = temporary;
}
const child = spawn(
    "python3",
    [
        "-m",
        "http.server",
        "18767",
        "--bind",
        "127.0.0.1",
        "--directory",
        directory,
    ],
    { stdio: "inherit" },
);
for (const signal of ["SIGTERM", "SIGINT"])
    process.on(signal, () => {
        child.kill(signal);
    });
child.on("exit", async (code) => {
    await rm(temporary, { recursive: true, force: true });
    process.exit(code ?? 0);
});
