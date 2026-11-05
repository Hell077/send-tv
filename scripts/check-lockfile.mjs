import { readFileSync } from 'node:fs';
import { pathToFileURL } from 'node:url';

export function validateLockfile(manifest, lock) {
  if (lock.lockfileVersion !== 3 || !lock.packages?.['']) throw new Error('Expected npm v3 lockfile');
  for (const path of Object.keys(lock.packages)) {
    if (path !== '' && !path.startsWith('node_modules/')) throw new Error('Lockfile contains a non-portable package path');
  }
  for (const section of ['dependencies', 'devDependencies']) {
    const requested = manifest[section] ?? {};
    const locked = lock.packages[''][section] ?? {};
    if (JSON.stringify(Object.entries(requested).sort()) !== JSON.stringify(Object.entries(locked).sort())) throw new Error('Lockfile root dependencies differ from package.json');
    for (const name of Object.keys(requested)) {
      if (!lock.packages[`node_modules/${name}`]) throw new Error('Lockfile lacks a declared package');
    }
  }
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  validateLockfile(JSON.parse(readFileSync('package.json', 'utf8')), JSON.parse(readFileSync('package-lock.json', 'utf8')));
  console.log('Dependency lockfile is portable and matches package.json');
}
