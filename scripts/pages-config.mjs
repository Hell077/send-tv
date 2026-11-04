export function pagesConfig(env = process.env) {
    const repository = env.GITHUB_REPOSITORY ?? "";
    const validRepository = /^[A-Za-z0-9_.-]+\/[A-Za-z0-9_.-]+$/.test(
        repository,
    );
    const github = validRepository
        ? `https://github.com/${repository}`
        : undefined;
    const [owner, name] = repository.split("/");
    const inferred = validRepository
        ? `https://${owner}.github.io/${name.toLowerCase() === `${owner}.github.io`.toLowerCase() ? "" : `${name}/`}`
        : undefined;
    const url = env.SITE_URL
        ? new URL(env.SITE_URL)
        : inferred
          ? new URL(inferred)
          : undefined;
    if (
        url &&
        (url.protocol !== "https:" ||
            url.username ||
            url.password ||
            url.search ||
            url.hash)
    )
        throw new Error(
            "SITE_URL must be an HTTPS public site URL without credentials, query, or fragment",
        );
    return {
        site: url?.origin,
        base: url ? `${url.pathname.replace(/\/$/, "")}/` : "/",
        github,
    };
}
