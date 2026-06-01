import { Bell, Check, Heart, Loader2, TrendingUp } from "lucide-react";
import { useState } from "react";
import { Button } from "../components/Button";
import { Card } from "../components/Card";
import { Input } from "../components/Input";
import { ThemeToggle } from "../components/ThemeToggle";

/**
 * Esta página demonstra todos os componentes base do Aion Logbook
 * Útil para desenvolvimento e teste de componentes
 */
export function ComponentShowcase() {
  const [inputValue, setInputValue] = useState("");

  return (
    <div className="min-h-screen bg-background py-12 px-6">
      <div className="max-w-6xl mx-auto space-y-12">
        {/* Header */}
        <div className="text-center mb-16">
          <h1 className="text-4xl font-medium text-foreground mb-4">
            Showcase de Componentes
          </h1>
          <p className="text-lg text-muted-foreground">
            Design System Eirene — Aion Logbook
          </p>
          <div className="mt-6">
            <ThemeToggle />
          </div>
        </div>

        {/* Colors */}
        <section>
          <h2 className="text-2xl font-medium text-foreground mb-6">Paleta de Cores</h2>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="space-y-2">
              <div className="h-20 rounded-lg bg-primary" />
              <p className="text-sm text-foreground">Primary</p>
            </div>
            <div className="space-y-2">
              <div className="h-20 rounded-lg bg-secondary" />
              <p className="text-sm text-foreground">Secondary</p>
            </div>
            <div className="space-y-2">
              <div className="h-20 rounded-lg bg-accent" />
              <p className="text-sm text-foreground">Accent</p>
            </div>
            <div className="space-y-2">
              <div className="h-20 rounded-lg bg-muted" />
              <p className="text-sm text-foreground">Muted</p>
            </div>
          </div>
        </section>

        {/* Typography */}
        <section>
          <h2 className="text-2xl font-medium text-foreground mb-6">Tipografia</h2>
          <div className="space-y-4">
            <h1 className="text-foreground">Heading 1 — Design System</h1>
            <h2 className="text-foreground">Heading 2 — Componentes Base</h2>
            <h3 className="text-foreground">Heading 3 — Seções</h3>
            <h4 className="text-foreground">Heading 4 — Subsecções</h4>
            <p className="text-foreground">
              Parágrafo regular — Lorem ipsum dolor sit amet, consectetur adipiscing
              elit.
            </p>
            <p className="text-muted-foreground">
              Texto secundário — Informações complementares e menos importantes.
            </p>
            <p className="text-sm text-muted-foreground">
              Texto pequeno — Notas de rodapé e detalhes menores.
            </p>
          </div>
        </section>

        {/* Buttons */}
        <section>
          <h2 className="text-2xl font-medium text-foreground mb-6">Botões</h2>
          <div className="space-y-6">
            {/* Variants */}
            <div>
              <h3 className="text-lg font-medium text-foreground mb-4">Variantes</h3>
              <div className="flex flex-wrap gap-4">
                <Button variant="primary">Primary Button</Button>
                <Button variant="secondary">Secondary Button</Button>
                <Button variant="ghost">Ghost Button</Button>
                <Button variant="primary" disabled>
                  Disabled
                </Button>
              </div>
            </div>

            {/* Sizes */}
            <div>
              <h3 className="text-lg font-medium text-foreground mb-4">Tamanhos</h3>
              <div className="flex flex-wrap items-center gap-4">
                <Button size="sm">Small</Button>
                <Button size="md">Medium</Button>
                <Button size="lg">Large</Button>
              </div>
            </div>

            {/* With Icons */}
            <div>
              <h3 className="text-lg font-medium text-foreground mb-4">Com Ícones</h3>
              <div className="flex flex-wrap gap-4">
                <Button className="flex items-center gap-2">
                  <Heart className="w-4 h-4" />
                  Com Ícone
                </Button>
                <Button variant="secondary" className="flex items-center gap-2">
                  <Check className="w-4 h-4" />
                  Concluído
                </Button>
                <Button variant="ghost" className="flex items-center gap-2">
                  <Loader2 className="w-4 h-4 animate-spin" />
                  Carregando
                </Button>
              </div>
            </div>
          </div>
        </section>

        {/* Inputs */}
        <section>
          <h2 className="text-2xl font-medium text-foreground mb-6">Inputs</h2>
          <div className="space-y-6 max-w-md">
            <Input
              label="Email"
              type="email"
              placeholder="seu@email.com"
              value={inputValue}
              onChange={(e) => setInputValue(e.target.value)}
            />
            <Input
              label="Senha"
              type="password"
              placeholder="••••••••"
            />
            <Input
              label="Desabilitado"
              placeholder="Input desabilitado"
              disabled
            />
            <Input
              placeholder="Sem label"
            />
          </div>
        </section>

        {/* Cards */}
        <section>
          <h2 className="text-2xl font-medium text-foreground mb-6">Cards</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <Card>
              <h3 className="text-lg font-medium text-foreground mb-2">Card Básico</h3>
              <p className="text-sm text-muted-foreground">
                Este é um card básico sem hover effect.
              </p>
            </Card>

            <Card hover>
              <h3 className="text-lg font-medium text-foreground mb-2">
                Card com Hover
              </h3>
              <p className="text-sm text-muted-foreground">
                Este card tem efeito hover elegante.
              </p>
            </Card>

            <Card className="bg-gradient-to-br from-primary/5 to-transparent border-primary/20">
              <div className="flex items-start gap-4">
                <div className="p-2 rounded-lg bg-primary/10">
                  <TrendingUp className="w-5 h-5 text-primary" />
                </div>
                <div>
                  <h3 className="text-lg font-medium text-foreground mb-1">
                    Card com Gradiente
                  </h3>
                  <p className="text-sm text-muted-foreground">
                    Ideal para destacar informações importantes.
                  </p>
                </div>
              </div>
            </Card>

            <Card className="relative overflow-hidden">
              <div className="absolute top-2 right-2">
                <span className="px-2 py-1 text-xs rounded-full bg-accent/20 text-accent">
                  Novo
                </span>
              </div>
              <h3 className="text-lg font-medium text-foreground mb-2">
                Card com Badge
              </h3>
              <p className="text-sm text-muted-foreground">
                Card com elementos extras como badges.
              </p>
            </Card>
          </div>
        </section>

        {/* Status Badges */}
        <section>
          <h2 className="text-2xl font-medium text-foreground mb-6">
            Status Badges
          </h2>
          <div className="flex flex-wrap gap-3">
            <span className="px-3 py-1.5 rounded-full text-sm font-medium bg-accent/20 text-accent">
              Concluído
            </span>
            <span className="px-3 py-1.5 rounded-full text-sm font-medium bg-primary/20 text-primary">
              Em andamento
            </span>
            <span className="px-3 py-1.5 rounded-full text-sm font-medium bg-secondary/20 text-secondary">
              Chegou a hora
            </span>
            <span className="px-3 py-1.5 rounded-full text-sm font-medium bg-destructive/20 text-destructive">
              Ficou para trás
            </span>
            <span className="px-3 py-1.5 rounded-full text-sm font-medium bg-muted text-muted-foreground">
              Ignorado
            </span>
          </div>
        </section>

        {/* Shadows */}
        <section>
          <h2 className="text-2xl font-medium text-foreground mb-6">Elevações</h2>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
            <div
              className="p-6 rounded-lg bg-card"
              style={{ boxShadow: "var(--shadow-sm)" }}
            >
              <p className="text-sm text-center text-foreground">Shadow SM</p>
            </div>
            <div
              className="p-6 rounded-lg bg-card"
              style={{ boxShadow: "var(--shadow-md)" }}
            >
              <p className="text-sm text-center text-foreground">Shadow MD</p>
            </div>
            <div
              className="p-6 rounded-lg bg-card"
              style={{ boxShadow: "var(--shadow-lg)" }}
            >
              <p className="text-sm text-center text-foreground">Shadow LG</p>
            </div>
            <div
              className="p-6 rounded-lg bg-card"
              style={{ boxShadow: "var(--shadow-xl)" }}
            >
              <p className="text-sm text-center text-foreground">Shadow XL</p>
            </div>
          </div>
        </section>

        {/* Icons */}
        <section>
          <h2 className="text-2xl font-medium text-foreground mb-6">Ícones</h2>
          <div className="flex flex-wrap gap-6">
            <div className="flex flex-col items-center gap-2">
              <div className="p-3 rounded-lg bg-primary/10">
                <Heart className="w-6 h-6 text-primary" />
              </div>
              <span className="text-xs text-muted-foreground">Heart</span>
            </div>
            <div className="flex flex-col items-center gap-2">
              <div className="p-3 rounded-lg bg-accent/10">
                <Check className="w-6 h-6 text-accent" />
              </div>
              <span className="text-xs text-muted-foreground">Check</span>
            </div>
            <div className="flex flex-col items-center gap-2">
              <div className="p-3 rounded-lg bg-secondary/10">
                <Bell className="w-6 h-6 text-secondary" />
              </div>
              <span className="text-xs text-muted-foreground">Bell</span>
            </div>
            <div className="flex flex-col items-center gap-2">
              <div className="p-3 rounded-lg bg-muted">
                <TrendingUp className="w-6 h-6 text-muted-foreground" />
              </div>
              <span className="text-xs text-muted-foreground">TrendingUp</span>
            </div>
          </div>
        </section>

        {/* Footer */}
        <div className="text-center py-12 border-t border-border">
          <p className="text-sm text-muted-foreground italic">
            "Design System Eirene — Elegância Silenciosa e Contemplativa"
          </p>
        </div>
      </div>
    </div>
  );
}
