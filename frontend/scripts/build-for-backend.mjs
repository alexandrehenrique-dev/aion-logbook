import { execSync } from 'child_process';
import { existsSync, mkdirSync, readdirSync, rmSync } from 'fs';
import { resolve, dirname } from 'path';
import { fileURLToPath } from 'url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const frontendRoot = resolve(__dirname, '..');
const staticDir = resolve(frontendRoot, '../backend/src/main/resources/static');

console.log('Building frontend for Spring Boot backend...');
console.log(`Target: ${staticDir}`);

if (!existsSync(staticDir)) {
  console.log('Creating static directory...');
  mkdirSync(staticDir, { recursive: true });
} else {
  console.log('Cleaning existing static files...');
  const entries = readdirSync(staticDir);
  for (const entry of entries) {
    rmSync(resolve(staticDir, entry), { recursive: true, force: true });
  }
}

try {
  execSync(`pnpm tsc -b && pnpm vite build --outDir "${staticDir}" --emptyOutDir`, {
    cwd: frontendRoot,
    stdio: 'inherit',
  });
  console.log(`\nBuild complete. Files written to: ${staticDir}`);
} catch (err) {
  console.error('Build failed:', err.message);
  process.exit(1);
}
