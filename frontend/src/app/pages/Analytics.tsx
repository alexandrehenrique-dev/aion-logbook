import { BarChart3, Calendar, Clock, TrendingUp } from "lucide-react";
import { motion } from "motion/react";
import {
  Area,
  AreaChart,
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { Card } from "../components/Card";

const weeklyData = [
  { day: "Seg", planned: 8, executed: 6.5 },
  { day: "Ter", planned: 7, executed: 7.2 },
  { day: "Qua", planned: 6, executed: 5.8 },
  { day: "Qui", planned: 8.5, executed: 7 },
  { day: "Sex", planned: 7, executed: 6 },
  { day: "Sáb", planned: 4, executed: 4.5 },
  { day: "Dom", planned: 3, executed: 3.2 },
];

const directionData = [
  { name: "Estudos", value: 24.5, color: "#2c5f6f" },
  { name: "Carreira", value: 32.25, color: "#c9a86a" },
  { name: "Escrita", value: 18.75, color: "#6b8e7f" },
  { name: "Saúde", value: 12.3, color: "#c85a54" },
  { name: "Filosofia", value: 16.17, color: "#4a8a9e" },
  { name: "Projetos", value: 28.83, color: "#9d7f4a" },
];

const statusData = [
  { name: "Concluídos", value: 85 },
  { name: "Ficou para trás", value: 12 },
  { name: "Parcialmente", value: 18 },
  { name: "Ignorados", value: 5 },
];

const trendData = [
  { week: "S1", hours: 28 },
  { week: "S2", hours: 32 },
  { week: "S3", hours: 35 },
  { week: "S4", hours: 33 },
  { week: "S5", hours: 38 },
  { week: "S6", hours: 42 },
];

export function Analytics() {
  return (
    <div className="min-h-screen bg-background py-8 px-6">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="mb-12"
        >
          <h1 className="text-3xl font-medium text-foreground mb-2 flex items-center gap-3">
            <BarChart3 className="w-8 h-8" />
            Observatório
          </h1>
          <p className="text-lg text-muted-foreground">
            Leitura de padrões e consciência de energia
          </p>
        </motion.div>

        {/* Key Metrics */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
          className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-12"
        >
          <Card className="text-center">
            <Clock className="w-8 h-8 text-primary mx-auto mb-3" />
            <p className="text-sm text-muted-foreground mb-1">Energia Investida</p>
            <p className="text-3xl font-medium text-foreground">132h</p>
            <p className="text-xs text-muted-foreground mt-2">Esta semana: 43h</p>
          </Card>

          <Card className="text-center">
            <TrendingUp className="w-8 h-8 text-accent mx-auto mb-3" />
            <p className="text-sm text-muted-foreground mb-1">Taxa de Conclusão</p>
            <p className="text-3xl font-medium text-foreground">76%</p>
            <p className="text-xs text-accent mt-2">↑ 8% vs. semana passada</p>
          </Card>

          <Card className="text-center">
            <Calendar className="w-8 h-8 text-secondary mx-auto mb-3" />
            <p className="text-sm text-muted-foreground mb-1">Planos Criados</p>
            <p className="text-3xl font-medium text-foreground">120</p>
            <p className="text-xs text-muted-foreground mt-2">85 concluídos</p>
          </Card>

          <Card className="text-center">
            <BarChart3 className="w-8 h-8 text-primary mx-auto mb-3" />
            <p className="text-sm text-muted-foreground mb-1">Direções Ativas</p>
            <p className="text-3xl font-medium text-foreground">6</p>
            <p className="text-xs text-muted-foreground mt-2">Todas em movimento</p>
          </Card>
        </motion.div>

        {/* Charts Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
          {/* Weekly Comparison */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.2 }}
          >
            <Card>
              <h3 className="text-lg font-medium text-foreground mb-6">
                Planejado vs Executado
              </h3>
              <ResponsiveContainer width="100%" height={300}>
                <BarChart data={weeklyData}>
                  <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" />
                  <XAxis
                    dataKey="day"
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
                  />
                  <Legend
                    wrapperStyle={{ fontSize: "12px" }}
                    iconType="circle"
                  />
                  <Bar
                    dataKey="planned"
                    name="Planejado (h)"
                    fill="var(--color-muted)"
                    radius={[4, 4, 0, 0]}
                  />
                  <Bar
                    dataKey="executed"
                    name="Executado (h)"
                    fill="var(--color-primary)"
                    radius={[4, 4, 0, 0]}
                  />
                </BarChart>
              </ResponsiveContainer>
            </Card>
          </motion.div>

          {/* Direction Distribution */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.3 }}
          >
            <Card>
              <h3 className="text-lg font-medium text-foreground mb-6">
                Energia por Direção
              </h3>
              <ResponsiveContainer width="100%" height={300}>
                <PieChart>
                  <Pie
                    data={directionData}
                    cx="50%"
                    cy="50%"
                    labelLine={false}
                    label={(entry) => entry.name}
                    outerRadius={100}
                    dataKey="value"
                  >
                    {directionData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip
                    contentStyle={{
                      backgroundColor: "var(--color-card)",
                      border: "1px solid var(--color-border)",
                      borderRadius: "8px",
                      fontSize: "12px",
                    }}
                    formatter={(value: number) => `${value.toFixed(1)}h`}
                  />
                </PieChart>
              </ResponsiveContainer>
            </Card>
          </motion.div>

          {/* Trend Over Time */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.4 }}
          >
            <Card>
              <h3 className="text-lg font-medium text-foreground mb-6">
                Tendência de Energia
              </h3>
              <ResponsiveContainer width="100%" height={300}>
                <AreaChart data={trendData}>
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

          {/* Status Distribution */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.5 }}
          >
            <Card>
              <h3 className="text-lg font-medium text-foreground mb-6">
                Distribuição por Status
              </h3>
              <div className="space-y-4">
                {statusData.map((item, index) => {
                  const total = statusData.reduce((sum, s) => sum + s.value, 0);
                  const percentage = Math.round((item.value / total) * 100);

                  return (
                    <motion.div
                      key={item.name}
                      initial={{ opacity: 0, x: -20 }}
                      animate={{ opacity: 1, x: 0 }}
                      transition={{ delay: 0.6 + index * 0.1 }}
                    >
                      <div className="flex items-center justify-between mb-2">
                        <span className="text-sm text-foreground">{item.name}</span>
                        <span className="text-sm font-medium text-muted-foreground">
                          {item.value} ({percentage}%)
                        </span>
                      </div>
                      <div className="h-2 bg-muted rounded-full overflow-hidden">
                        <motion.div
                          initial={{ width: 0 }}
                          animate={{ width: `${percentage}%` }}
                          transition={{ duration: 0.8, delay: 0.6 + index * 0.1 }}
                          className="h-full bg-primary"
                        />
                      </div>
                    </motion.div>
                  );
                })}
              </div>
            </Card>
          </motion.div>
        </div>

        {/* Insights */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.7 }}
        >
          <Card className="bg-gradient-to-br from-primary/5 to-transparent border-primary/20">
            <h3 className="text-lg font-medium text-foreground mb-4">Reflexões</h3>
            <div className="space-y-3 text-sm">
              <p className="text-muted-foreground">
                • Sua energia está crescendo: +15% nas últimas 6 semanas
              </p>
              <p className="text-muted-foreground">
                • Carreira e Projetos recebem mais atenção (61h combinadas)
              </p>
              <p className="text-muted-foreground">
                • Considere resgatar planos de Saúde que ficaram para trás
              </p>
              <p className="text-muted-foreground italic mt-4">
                "Estes números não são cobranças. São apenas observações da sua jornada."
              </p>
            </div>
          </Card>
        </motion.div>
      </div>
    </div>
  );
}
