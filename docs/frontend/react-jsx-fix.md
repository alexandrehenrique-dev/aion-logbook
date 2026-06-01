# Correção JSX e tipos React

## Causa raiz

O frontend estava configurado como um projeto Vite + React moderno, mas não tinha `tsconfig` nem configuração de ESLint versionados. Com isso, o TypeScript do editor não recebia uma configuração explícita de JSX e alguns arquivos usavam tipos via namespace global, como `React.ReactNode`, `React.FormEvent`, `React.ComponentProps` e `React.CSSProperties`, sem importar `React` no módulo.

Em arquivos ES module, esse padrão faz o TypeScript apontar o erro `TS2686`: `'React' refers to a UMD global, but the current file is a module`.

## Arquivos alterados

- `frontend/package.json`
- `frontend/package-lock.json`
- `frontend/tsconfig.json`
- `frontend/tsconfig.app.json`
- `frontend/tsconfig.node.json`
- `frontend/eslint.config.js`
- `frontend/vite.config.ts`
- `frontend/src/components/feedback/EmptyState.tsx`
- `frontend/src/features/bug-report/BugReportModal.tsx`
- `frontend/src/app/components/Button.tsx`
- `frontend/src/app/components/ui/aspect-ratio.tsx`
- `frontend/src/app/components/ui/collapsible.tsx`
- `frontend/src/app/components/ui/skeleton.tsx`
- `frontend/src/app/components/ui/sonner.tsx`
- `frontend/src/app/pages/Dashboard.tsx`
- `frontend/src/app/pages/PlanDetail.tsx`
- `frontend/src/app/pages/Plans.tsx`
- `frontend/src/app/pages/Sessions.tsx`

## Solução aplicada

- Adicionado `tsconfig.app.json` com `jsx: "react-jsx"` e `moduleResolution: "bundler"`, alinhando o projeto ao JSX Transform moderno do React 17+.
- Adicionado `tsconfig.node.json` para tipar `vite.config.ts`.
- Adicionado `eslint.config.js` no formato flat config moderno.
- Adicionados scripts `lint` e `build` com typecheck (`tsc -b && vite build`).
- Alinhadas dependências de runtime e tipos: `react`, `react-dom`, `@types/react`, `@types/react-dom`, `typescript`, `eslint` e `typescript-eslint`.
- Substituídos usos de tipos via namespace global `React.*` por imports de tipo explícitos quando o arquivo não importava `React`.
- Mantidos os componentes que usam `import * as React from "react"` porque eles dependem do namespace em runtime e tipos.
- Ajustado `vite.config.ts` para usar `import.meta.url` em vez de `__dirname`, compatível com ESM.

## Validações executadas

- `npm run lint`: passou.
- `npm run build`: passou.

O build emitiu apenas o aviso padrão do Vite sobre chunks maiores que 500 kB, sem falha de compilação.
