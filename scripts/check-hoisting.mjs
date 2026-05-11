/**
 * Ensures critical shared packages are always hoisted to root node_modules.
 * Exits with code 1 if any blacklisted package is found duplicated in a workspace.
 *
 * Usage: node scripts/check-hoisting.mjs [--verbose]
 */

import { readdirSync, existsSync, readFileSync } from 'node:fs';
import { join, resolve } from 'node:path';

const ROOT = resolve(import.meta.dirname, '..');
const verbose = process.argv.includes('--verbose');

/**
 * Packages allowed to exist in workspace-local node_modules (exceptions).
 * Key must match the workspace path string as it appears in root package.json workspaces.
 */
const ALLOW_LOCAL = {
  'uaa-authentication-a12-client': [
    '@com.mgmtp.a12.uaa/uaa-authentication-client',
  ],
};


/**
 * Patterns/names that MUST be hoisted (shared single copy at root).
 * If any matching package exists in a workspace node_modules/, the check fails.
 * Entries can be exact package names or RegExp patterns.
 */
const BLACKLIST = [
  // All A12 packages
  /^@com\.mgmtp\.a12\./,
  // React
  'react',
  'react-dom',
  'react-redux',
  // Redux
  'redux',
  'redux-saga',
  'typed-redux-saga',
  // Build tooling
  'typescript',
  'typedoc',
  'webpack',
  'webpack-cli',
  'webpack-dev-server',
  // Testing
  'vitest',
  'jsdom',
  // Shared libs
  'styled-components',
];

const rootPkg = JSON.parse(readFileSync(join(ROOT, 'package.json'), 'utf-8'));
const workspaces = rootPkg.workspaces ?? [];

let failures = 0;

for (const ws of workspaces) {
  const wsNodeModules = join(ROOT, ws, 'node_modules');
  if (!existsSync(wsNodeModules)) {
    if (verbose) {
      console.log(`${ws}: no local node_modules (fully hoisted)`);
    }
    continue;
  }

  const localPackages = listPackages(wsNodeModules);
  const allowed = new Set(ALLOW_LOCAL[ws] ?? []);

  const violations = localPackages.filter((pkg) => {
    if (allowed.has(pkg)) return false;

    return BLACKLIST.some((rule) =>
        rule instanceof RegExp ? rule.test(pkg) : rule === pkg
    );
  }

);

  if (violations.length === 0) {
    if (verbose) {
      console.log(`${ws}: all critical packages hoisted`);
    }
    continue;
  }

  failures += violations.length;
  console.log(`\n${ws}: ${violations.length} critical package(s) NOT hoisted`);
  for (const pkg of violations) {
    const localVer = readVersion(join(wsNodeModules, pkg));
    const rootVer = readVersion(join(ROOT, 'node_modules', pkg));
    console.log(`  - ${pkg}: workspace=${localVer}, root=${rootVer}`);
  }
}

if (failures > 0) {
  console.log(`\nFAILED: ${failures} critical package(s) not hoisted.`);
  console.log(`
How to fix:
  1. Check the version mismatch above (workspace vs root).
  2. Add the desired version as a dependency in the root package.json
     to force npm to hoist that version. For example:
       npm install <package>@<desired-version> -w .
  3. Run: rm -rf node_modules */node_modules devapps/*/node_modules
  4. Run: npm install
  5. Run: node scripts/check-hoisting.mjs --verbose
  6. Once the check passes, remove the dependency from the root package.json
     (it was only needed to force the correct hoisting resolution).
`);
  process.exit(1);
}

console.log('All critical packages are hoisted to root node_modules.');

function listPackages(nodeModulesDir) {
  const packages = [];
  for (const entry of readdirSync(nodeModulesDir)) {
    if (entry === '.package-lock.json' || entry === '.cache') {
      continue;
    }
    if (entry.startsWith('@')) {
      const scopeDir = join(nodeModulesDir, entry);
      for (const scoped of readdirSync(scopeDir)) {
        if (existsSync(join(scopeDir, scoped, 'package.json'))) {
          packages.push(`${entry}/${scoped}`);
        }
      }
    } else if (existsSync(join(nodeModulesDir, entry, 'package.json'))) {
      packages.push(entry);
    }
  }
  return packages;
}

function readVersion(pkgDir) {
  try {
    return JSON.parse(readFileSync(join(pkgDir, 'package.json'), 'utf-8')).version ?? 'unknown';
  } catch {
    return 'unknown';
  }
}
