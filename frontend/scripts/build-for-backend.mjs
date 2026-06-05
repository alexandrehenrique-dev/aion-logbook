import { execSync } from 'child_process';
import { existsSync, mkdirSync, readdirSync, rmSync } from 'fs';
import { resolve, dirname } from 'path';
import { fileURLToPath } from 'url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const frontendRoot = resolve(__dirname, '..');
const staticDir = resolve(frontendRoot, '../backend/src/main/resources/static');

function hasCommand(command) {
  try {
    execSync(`${command} --version`, { stdio: 'ignore' });
    return true;
  } catch {
    return false;
  }
}

const packageManager = hasCommand('pnpm') ? 'pnpm' : 'npm';
const viteCommand = packageManager === 'pnpm' ? 'pnpm vite' : 'npx vite';

// Aceita --mode <nome> para trocar o arquivo .env carregado pelo Vite.
// Exemplo: node build-for-backend.mjs --mode backend-local
const args = process.argv.slice(2);
const modeIndex = args.indexOf('--mode');
const mode = modeIndex !== -1 && args[modeIndex + 1] ? args[modeIndex + 1] : 'backend';

console.log('Building frontend for Spring Boot backend...');
console.log(`Target: ${staticDir}`);
console.log(`Package manager: ${packageManager}`);
console.log(`Mode: ${mode} (lê .env.${mode})`);

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
  execSync(
    `${packageManager} run typecheck && ${viteCommand} build --mode ${mode} --outDir "${staticDir}" --emptyOutDir`,
    {
      cwd: frontendRoot,
      stdio: 'inherit',
    }
  );
  console.log(`\nBuild complete. Files written to: ${staticDir}`);
} catch (err) {
  console.error('Build failed:', err.message);
  process.exit(1);
}
