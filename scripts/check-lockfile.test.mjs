import { test } from 'node:test';
import assert from 'node:assert/strict';
import { validateLockfile } from './check-lockfile.mjs';

const manifest = { devDependencies: { tool: '1.0.0' } };
const valid = () => ({ lockfileVersion: 3, packages: { '': manifest, 'node_modules/tool': { version: '1.0.0' } } });
test('accepts portable declared dependency records', () => { assert.doesNotThrow(() => validateLockfile(manifest, valid())); });
test('rejects temporary absolute/relative paths, missing packages and mismatched roots', () => {
  const temporary = valid();
  temporary.packages['../../private/tmp/build/node_modules/tool'] = temporary.packages['node_modules/tool'];
  delete temporary.packages['node_modules/tool'];
  assert.throws(() => validateLockfile(manifest, temporary));
  const missing = valid();
  delete missing.packages['node_modules/tool'];
  assert.throws(() => validateLockfile(manifest, missing));
  assert.throws(() => validateLockfile({ devDependencies: { tool: '2.0.0' } }, valid()));
});
