import {
  ArrowUp,
  Book,
  Briefcase,
  Heart,
  Lightbulb,
  PenLine,
  TrendingUp,
} from "lucide-react";
import { motion } from "motion/react";
import { Card } from "../components/Card";

const directions = [
  {
    id: "studies",
    name: "Estudos",
    description: "Crescimento intelectual e aprendizado contínuo",
    icon: Book,
    color: "from-primary to-primary/70",
    stats: {
      totalTime: "24h 30min",
      plansCompleted: 15,
      plansTotal: 20,
      lastActivity: "Hoje às 09:00",
      trend: "up",
    },
  },
  {
    id: "career",
    name: "Carreira",
    description: "Desenvolvimento profissional e projetos",
    icon: Briefcase,
    color: "from-secondary to-secondary/70",
    stats: {
      totalTime: "32h 15min",
      plansCompleted: 22,
      plansTotal: 25,
      lastActivity: "Hoje às 11:00",
      trend: "up",
    },
  },
  {
    id: "writing",
    name: "Escrita",
    description: "Expressão criativa e narrativa",
    icon: PenLine,
    color: "from-accent to-accent/70",
    stats: {
      totalTime: "18h 45min",
      plansCompleted: 12,
      plansTotal: 18,
      lastActivity: "Ontem às 14:00",
      trend: "stable",
    },
  },
  {
    id: "health",
    name: "Saúde",
    description: "Bem-estar físico e mental",
    icon: Heart,
    color: "from-destructive/70 to-destructive/50",
    stats: {
      totalTime: "12h 20min",
      plansCompleted: 8,
      plansTotal: 15,
      lastActivity: "Hoje às 07:00 (perdido)",
      trend: "down",
    },
  },
  {
    id: "philosophy",
    name: "Filosofia",
    description: "Reflexão e compreensão existencial",
    icon: TrendingUp,
    color: "from-primary/70 to-accent/70",
    stats: {
      totalTime: "16h 10min",
      plansCompleted: 10,
      plansTotal: 12,
      lastActivity: "Hoje às 06:30",
      trend: "up",
    },
  },
  {
    id: "projects",
    name: "Projetos",
    description: "Criação e construção de ideias",
    icon: Lightbulb,
    color: "from-secondary/70 to-primary/70",
    stats: {
      totalTime: "28h 50min",
      plansCompleted: 18,
      plansTotal: 22,
      lastActivity: "Ontem às 15:00",
      trend: "up",
    },
  },
];

export function Directions() {
  return (
    <div className="min-h-screen bg-background py-8 px-6">
      <div className="max-w-6xl mx-auto">
        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="mb-12"
        >
          <h1 className="text-3xl font-medium text-foreground mb-2">Direções</h1>
          <p className="text-lg text-muted-foreground">
            Os eixos da sua vida, caminhos e áreas existenciais
          </p>
        </motion.div>

        {/* Overview Stats */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
          className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-12"
        >
          <Card className="bg-gradient-to-br from-primary/5 to-transparent border-primary/20">
            <div className="text-center">
              <p className="text-sm text-muted-foreground mb-2">Total de Direções</p>
              <p className="text-4xl font-medium text-foreground">{directions.length}</p>
            </div>
          </Card>

          <Card className="bg-gradient-to-br from-accent/5 to-transparent border-accent/20">
            <div className="text-center">
              <p className="text-sm text-muted-foreground mb-2">Energia Investida</p>
              <p className="text-4xl font-medium text-foreground">132h</p>
            </div>
          </Card>

          <Card className="bg-gradient-to-br from-secondary/5 to-transparent border-secondary/20">
            <div className="text-center">
              <p className="text-sm text-muted-foreground mb-2">Taxa de Conclusão</p>
              <p className="text-4xl font-medium text-foreground">76%</p>
            </div>
          </Card>
        </motion.div>

        {/* Directions Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {directions.map((direction, index) => {
            const Icon = direction.icon;
            const completionRate = Math.round(
              (direction.stats.plansCompleted / direction.stats.plansTotal) * 100
            );

            return (
              <motion.div
                key={direction.id}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.2 + index * 0.1 }}
              >
                <Card hover className="cursor-pointer h-full">
                  <div className="flex flex-col h-full">
                    {/* Header */}
                    <div className="flex items-start gap-4 mb-4">
                      <div
                        className={`p-3 rounded-xl bg-gradient-to-br ${direction.color} shrink-0`}
                      >
                        <Icon className="w-6 h-6 text-white" strokeWidth={1.5} />
                      </div>

                      <div className="flex-1">
                        <h3 className="text-xl font-medium text-foreground mb-1">
                          {direction.name}
                        </h3>
                        <p className="text-sm text-muted-foreground">
                          {direction.description}
                        </p>
                      </div>

                      {direction.stats.trend === "up" && (
                        <div className="p-1.5 rounded-lg bg-accent/10 shrink-0">
                          <ArrowUp className="w-4 h-4 text-accent" />
                        </div>
                      )}
                    </div>

                    {/* Progress Bar */}
                    <div className="mb-4">
                      <div className="flex justify-between items-center mb-2">
                        <span className="text-xs text-muted-foreground">Progresso</span>
                        <span className="text-xs font-medium text-foreground">
                          {completionRate}%
                        </span>
                      </div>
                      <div className="h-2 bg-muted rounded-full overflow-hidden">
                        <motion.div
                          initial={{ width: 0 }}
                          animate={{ width: `${completionRate}%` }}
                          transition={{ duration: 0.8, delay: 0.3 + index * 0.1 }}
                          className={`h-full bg-gradient-to-r ${direction.color}`}
                        />
                      </div>
                    </div>

                    {/* Stats Grid */}
                    <div className="grid grid-cols-2 gap-4 mb-4">
                      <div>
                        <p className="text-xs text-muted-foreground mb-1">Tempo investido</p>
                        <p className="text-sm font-medium text-foreground">
                          {direction.stats.totalTime}
                        </p>
                      </div>
                      <div>
                        <p className="text-xs text-muted-foreground mb-1">Planos</p>
                        <p className="text-sm font-medium text-foreground">
                          {direction.stats.plansCompleted} / {direction.stats.plansTotal}
                        </p>
                      </div>
                    </div>

                    {/* Last Activity */}
                    <div className="mt-auto pt-4 border-t border-border">
                      <p className="text-xs text-muted-foreground">
                        Última atividade: {direction.stats.lastActivity}
                      </p>
                    </div>
                  </div>
                </Card>
              </motion.div>
            );
          })}
        </div>

        {/* Inspirational message */}
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 1 }}
          className="mt-12 text-center"
        >
          <p className="text-sm text-muted-foreground italic">
            "Cada direção é uma parte viva da sua jornada. Continue cultivando."
          </p>
        </motion.div>
      </div>
    </div>
  );
}
