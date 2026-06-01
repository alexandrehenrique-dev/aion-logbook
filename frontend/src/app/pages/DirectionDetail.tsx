import { ArrowLeft, Book, Clock, TrendingUp } from "lucide-react";
import { motion } from "motion/react";
import { useNavigate } from "react-router";
import { Area, AreaChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { Card } from "../components/Card";

const weeklyProgress = [
  { week: "S1", hours: 18 },
  { week: "S2", hours: 22 },
  { week: "S3", hours: 24 },
  { week: "S4", hours: 20 },
  { week: "S5", hours: 26 },
  { week: "S6", hours: 24.5 },
];

const recentPlans = [
  {
    id: "1",
    title: "Estudar React avançado",
    status: "in-progress",
    date: "2026-05-27",
  },
  {
    id: "2",
    title: "Revisar TypeScript generics",
    status: "completed",
    date: "2026-05-26",
  },
  {
    id: "3",
    title: "Ler documentação Next.js 15",
    status: "completed",
    date: "2026-05-25",
  },
  {
    id: "4",
    title: "Praticar algoritmos",
    status: "partial",
    date: "2026-05-24",
  },
];

export function DirectionDetail() {
  const navigate = useNavigate();

  const direction = {
    id: "studies",
    name: "Estudos",
    description: "Crescimento intelectual e aprendizado contínuo",
    color: "from-primary to-primary/70",
    totalTime: 1470, // minutes = 24.5 hours
    plansCompleted: 15,
    plansTotal: 20,
    completionRate: 75,
    averageSessionDuration: 98, // minutes
    totalSessions: 28,
  };

  return (
    <div className="min-h-screen bg-background py-8 px-6">
      <div className="max-w-6xl mx-auto">
        {/* Back button */}
        <motion.button
          initial={{ opacity: 0, x: -20 }}
          animate={{ opacity: 1, x: 0 }}
          onClick={() => navigate("/directions")}
          className="flex items-center gap-2 text-muted-foreground hover:text-foreground transition-colors mb-8"
        >
          <ArrowLeft className="w-4 h-4" />
          Voltar para direções
        </motion.button>

        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="mb-12"
        >
          <div className="flex items-start gap-6">
            <div className={`p-4 rounded-2xl bg-gradient-to-br ${direction.color}`}>
              <Book className="w-10 h-10 text-white" strokeWidth={1.5} />
            </div>
            <div className="flex-1">
              <h1 className="text-3xl font-medium text-foreground mb-2">{direction.name}</h1>
              <p className="text-lg text-muted-foreground mb-6">{direction.description}</p>

              <div className="flex items-center gap-6 text-sm">
                <div>
                  <p className="text-muted-foreground mb-1">Energia investida</p>
                  <p className="text-2xl font-medium text-foreground">24.5h</p>
                </div>
                <div className="w-px h-12 bg-border" />
                <div>
                  <p className="text-muted-foreground mb-1">Taxa de conclusão</p>
                  <p className="text-2xl font-medium text-accent">{direction.completionRate}%</p>
                </div>
                <div className="w-px h-12 bg-border" />
                <div>
                  <p className="text-muted-foreground mb-1">Planos concluídos</p>
                  <p className="text-2xl font-medium text-foreground">
                    {direction.plansCompleted} / {direction.plansTotal}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </motion.div>

        {/* Stats grid */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
          className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-12"
        >
          <Card className="text-center">
            <Clock className="w-8 h-8 text-primary mx-auto mb-3" />
            <p className="text-sm text-muted-foreground mb-1">Sessões realizadas</p>
            <p className="text-3xl font-medium text-foreground">{direction.totalSessions}</p>
          </Card>

          <Card className="text-center">
            <TrendingUp className="w-8 h-8 text-accent mx-auto mb-3" />
            <p className="text-sm text-muted-foreground mb-1">Duração média</p>
            <p className="text-3xl font-medium text-foreground">
              {direction.averageSessionDuration}min
            </p>
          </Card>

          <Card className="text-center">
            <div className="w-8 h-8 rounded-lg bg-secondary/20 mx-auto mb-3 flex items-center justify-center">
              <TrendingUp className="w-5 h-5 text-secondary" />
            </div>
            <p className="text-sm text-muted-foreground mb-1">Tendência</p>
            <p className="text-3xl font-medium text-foreground">↑ Crescendo</p>
          </Card>
        </motion.div>

        {/* Content grid */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Left: Trend chart */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.2 }}
            className="lg:col-span-2"
          >
            <Card>
              <h3 className="text-lg font-medium text-foreground mb-6">
                Evolução Semanal
              </h3>
              <ResponsiveContainer width="100%" height={300}>
                <AreaChart data={weeklyProgress}>
                  <defs>
                    <linearGradient id="colorHours" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="var(--color-primary)" stopOpacity={0.3} />
                      <stop offset="95%" stopColor="var(--color-primary)" stopOpacity={0} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" />
                  <XAxis
                    dataKey="week"
                    stroke="var(--color-muted-foreground)"
                    style={{ fontSize: "12px" }}
                  />
                  <YAxis
                    stroke="var(--color-muted-foreground)"
                    style={{ fontSize: "12px" }}
                  />
                  <Tooltip
                    contentStyle={{
                      backgroundColor: "var(--color-card)",
                      border: "1px solid var(--color-border)",
                      borderRadius: "8px",
                      fontSize: "12px",
                    }}
                    formatter={(value: number) => [`${value}h`, "Horas"]}
                  />
                  <Area
                    type="monotone"
                    dataKey="hours"
                    stroke="var(--color-primary)"
                    strokeWidth={2}
                    fillOpacity={1}
                    fill="url(#colorHours)"
                  />
                </AreaChart>
              </ResponsiveContainer>
            </Card>
          </motion.div>

          {/* Right: Recent plans */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.3 }}
          >
            <Card>
              <h3 className="text-lg font-medium text-foreground mb-6">Planos Recentes</h3>
              <div className="space-y-3">
                {recentPlans.map((plan) => (
                  <div
                    key={plan.id}
                    className="p-3 rounded-lg hover:bg-muted/50 transition-colors cursor-pointer"
                  >
                    <div className="flex items-start justify-between mb-1">
                      <p className="text-sm font-medium text-foreground">{plan.title}</p>
                      <span
                        className={`text-xs px-2 py-0.5 rounded-full ${
                          plan.status === "completed"
                            ? "bg-accent/20 text-accent"
                            : plan.status === "in-progress"
                            ? "bg-primary/20 text-primary"
                            : "bg-muted text-muted-foreground"
                        }`}
                      >
                        {plan.status === "completed"
                          ? "✓"
                          : plan.status === "in-progress"
                          ? "→"
                          : "~"}
                      </span>
                    </div>
                    <p className="text-xs text-muted-foreground">
                      {new Date(plan.date).toLocaleDateString("pt-BR", {
                        day: "numeric",
                        month: "short",
                      })}
                    </p>
                  </div>
                ))}
              </div>
            </Card>
          </motion.div>
        </div>

        {/* Reflections section */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.4 }}
          className="mt-12"
        >
          <Card className="bg-gradient-to-br from-primary/5 to-transparent border-primary/20">
            <h3 className="text-lg font-medium text-foreground mb-4">Observações</h3>
            <div className="space-y-3 text-sm">
              <p className="text-muted-foreground">
                • Sua consistência em Estudos está crescendo (+14% nas últimas semanas)
              </p>
              <p className="text-muted-foreground">
                • Sessões matinais têm 25% mais taxa de conclusão
              </p>
              <p className="text-muted-foreground">
                • Você está próximo de completar 30 sessões nesta direção
              </p>
              <p className="text-muted-foreground italic mt-4">
                "Esta direção está viva e em movimento. Continue cultivando."
              </p>
            </div>
          </Card>
        </motion.div>
      </div>
    </div>
  );
}
