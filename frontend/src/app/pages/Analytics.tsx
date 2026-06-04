import { BarChart3, Calendar, Clock, TrendingUp } from 'lucide-react';
import { motion } from 'motion/react';
import { useEffect, useState } from 'react';
import {
  Bar, BarChart, CartesianGrid, Cell, Legend,
  Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis,
} from 'recharts';
import { Card } from '../components/Card';
import { ChartTooltip } from '../components/ChartTooltip';
import { analyticsService } from '../../services/analyticsService';
import type { AnalyticsOverview, AnalyticsByDay, AnalyticsStatusDistribution, AnalyticsTimeByDirection } from '../../types';
import type { AnalyticsPlannedVsExecuted } from '../../services/analyticsService';
import { PLAN_STATUS_LABEL } from '../../types';

type Filter = '7d' | '30d' | 'month' | 'prev-month';

const FILTER_LABELS: Record<Filter, string> = {
  '7d': 'Últimos 7 dias',
  '30d': 'Últimos 30 dias',
  month: 'Este mês',
  'prev-month': 'Mês anterior',
};

function formatMinutes(minutes: number) {
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  return h > 0 ? `${h}h${m > 0 ? ` ${m}min` : ''}` : `${m}min`;
}

function getDateRange(filter: Filter): { dateFrom: string; dateTo: string } {
  const now = new Date();
  const pad = (n: number) => String(n).padStart(2, '0');
  const fmt = (d: Date) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;

  if (filter === '7d') {
    const from = new Date(now);
    from.setDate(from.getDate() - 6);
    return { dateFrom: fmt(from), dateTo: fmt(now) };
  }
  if (filter === '30d') {
    const from = new Date(now);
    from.setDate(from.getDate() - 29);
    return { dateFrom: fmt(from), dateTo: fmt(now) };
  }
  if (filter === 'month') {
    const from = new Date(now.getFullYear(), now.getMonth(), 1);
    const to = new Date(now.getFullYear(), now.getMonth() + 1, 0);
    return { dateFrom: fmt(from), dateTo: fmt(to) };
  }
  // prev-month
  const from = new Date(now.getFullYear(), now.getMonth() - 1, 1);
  const to = new Date(now.getFullYear(), now.getMonth(), 0);
  return { dateFrom: fmt(from), dateTo: fmt(to) };
}

export function Analytics() {
  const [filter, setFilter] = useState<Filter>('7d');
  const [overview, setOverview] = useState<AnalyticsOverview | null>(null);
  const [plansByDay, setPlansByDay] = useState<AnalyticsByDay[]>([]);
  const [statusDist, setStatusDist] = useState<AnalyticsStatusDistribution[]>([]);
  const [timeByDir, setTimeByDir] = useState<AnalyticsTimeByDirection[]>([]);
  const [plannedVsExec, setPlannedVsExec] = useState<AnalyticsPlannedVsExecuted[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    const range = getDateRange(filter);
    Promise.all([
      analyticsService.getOverview(range),
      analyticsService.getPlansByDay(range),
      analyticsService.getStatusDistribution(range),
      analyticsService.getTimeByDirection(range),
      analyticsService.getPlannedVsExecuted(range),
    ])
      .then(([ov, pbd, sd, tbd, pve]) => {
        setOverview(ov);
        setPlansByDay(pbd);
        setStatusDist(sd);
        setTimeByDir(tbd);
        setPlannedVsExec(pve);
      })
      .finally(() => setLoading(false));
  }, [filter]);

  if (loading || !overview) {
    return (
      <div className="min-h-screen bg-background py-8 px-6">
        <div className="max-w-7xl mx-auto space-y-6">
          <div className="h-10 bg-muted rounded w-48 animate-pulse" />
          <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
            {[1, 2, 3, 4].map((i) => <div key={i} className="h-32 bg-muted rounded-xl animate-pulse" />)}
          </div>
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            {[1, 2].map((i) => <div key={i} className="h-72 bg-muted rounded-xl animate-pulse" />)}
          </div>
        </div>
      </div>
    );
  }

  const dirChartData = timeByDir
    .filter((d) => d.totalMinutes > 0)
    .map((d) => ({ name: d.directionName, value: Math.round(d.totalMinutes / 60 * 10) / 10, color: d.color ?? '#6366f1' }));

  return (
    <div className="min-h-screen bg-background py-8 px-6">
      <div className="max-w-7xl mx-auto">
        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} className="mb-8">
          <div className="flex items-center justify-between flex-wrap gap-4">
            <div>
              <h1 className="text-3xl font-medium text-foreground mb-2 flex items-center gap-3">
                <BarChart3 className="w-8 h-8" />
                Observatório
              </h1>
              <p className="text-lg text-muted-foreground">Leitura de padrões e consciência de energia</p>
            </div>
            <div className="flex gap-2">
              {(Object.keys(FILTER_LABELS) as Filter[]).map((f) => (
                <button
                  key={f}
                  onClick={() => setFilter(f)}
                  className={`text-xs px-3 py-1.5 rounded-full border transition-colors ${filter === f ? 'bg-primary text-primary-foreground border-primary' : 'border-border text-muted-foreground hover:border-primary/50'}`}
                >
                  {FILTER_LABELS[f]}
                </button>
              ))}
            </div>
          </div>
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
            <p className="text-3xl font-medium text-foreground">{formatMinutes(overview.totalTimeMinutes)}</p>
            {overview.weeklyTimeMinutes && (
              <p className="text-xs text-muted-foreground mt-2">Esta semana: {formatMinutes(overview.weeklyTimeMinutes)}</p>
            )}
          </Card>
          <Card className="text-center">
            <TrendingUp className="w-8 h-8 text-accent mx-auto mb-3" />
            <p className="text-sm text-muted-foreground mb-1">Taxa de Conclusão</p>
            <p className="text-3xl font-medium text-foreground">{overview.completionRate}%</p>
          </Card>
          <Card className="text-center">
            <Calendar className="w-8 h-8 text-secondary mx-auto mb-3" />
            <p className="text-sm text-muted-foreground mb-1">Planos Criados</p>
            <p className="text-3xl font-medium text-foreground">{overview.plansCreated}</p>
            <p className="text-xs text-muted-foreground mt-2">{overview.plansCompleted} concluídos</p>
          </Card>
          <Card className="text-center">
            <BarChart3 className="w-8 h-8 text-primary mx-auto mb-3" />
            <p className="text-sm text-muted-foreground mb-1">Direções Ativas</p>
            <p className="text-3xl font-medium text-foreground">{overview.activeDirections}</p>
          </Card>
        </motion.div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
          {/* Plans by day */}
          <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.2 }}>
            <Card>
              <h3 className="text-lg font-medium text-foreground mb-6">Planos por Dia</h3>
              {plansByDay.length === 0 ? (
                <p className="text-sm text-muted-foreground text-center py-8">Sem dados disponíveis</p>
              ) : (
                <ResponsiveContainer width="100%" height={280}>
                  <BarChart data={plansByDay}>
                    <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" />
                    <XAxis dataKey="day" stroke="var(--color-muted-foreground)" style={{ fontSize: '12px' }} />
                    <YAxis stroke="var(--color-muted-foreground)" style={{ fontSize: '12px' }} allowDecimals={false} />
                    <Tooltip
                      content={(props) => (
                        <ChartTooltip
                          {...props}
                          title={(props.payload?.[0]?.payload as AnalyticsByDay)?.date
                            ? new Date(((props.payload?.[0]?.payload as AnalyticsByDay).date ?? '') + 'T12:00:00').toLocaleDateString('pt-BR', { weekday: 'short', day: 'numeric', month: 'short' })
                            : String(props.label ?? '')}
                          rows={(payload) => [
                            { label: 'Planejados', value: String(payload[0]?.planned ?? 0), color: 'var(--color-muted-foreground)' },
                            { label: 'Executados', value: String(payload[0]?.executed ?? 0), color: 'var(--color-primary)' },
                          ]}
                        />
                      )}
                    />
                    <Legend wrapperStyle={{ fontSize: '12px' }} iconType="circle" />
                    <Bar dataKey="planned" name="Planejados" fill="var(--color-muted)" radius={[4, 4, 0, 0]} />
                    <Bar dataKey="executed" name="Executados" fill="var(--color-primary)" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              )}
            </Card>
          </motion.div>

          {/* Planned vs executed minutes */}
          <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.3 }}>
            <Card>
              <h3 className="text-lg font-medium text-foreground mb-6">Planejado vs Executado (min)</h3>
              {plannedVsExec.length === 0 ? (
                <p className="text-sm text-muted-foreground text-center py-8">Sem dados disponíveis</p>
              ) : (
                <ResponsiveContainer width="100%" height={280}>
                  <BarChart data={plannedVsExec}>
                    <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" />
                    <XAxis dataKey="day" stroke="var(--color-muted-foreground)" style={{ fontSize: '12px' }} />
                    <YAxis stroke="var(--color-muted-foreground)" style={{ fontSize: '12px' }} />
                    <Tooltip
                      content={(props) => {
                        const p = props.payload?.[0]?.payload as AnalyticsPlannedVsExecuted | undefined;
                        const planned = p?.plannedMinutes ?? 0;
                        const executed = p?.executedMinutes ?? 0;
                        const diff = executed - planned;
                        return (
                          <ChartTooltip
                            {...props}
                            title={String(props.label ?? '')}
                            rows={() => [
                              { label: 'Planejado', value: formatMinutes(planned), color: 'var(--color-muted-foreground)' },
                              { label: 'Executado', value: formatMinutes(executed), color: 'var(--color-primary)' },
                              { label: 'Diferença', value: `${diff >= 0 ? '+' : ''}${formatMinutes(Math.abs(diff))}` },
                            ]}
                          />
                        );
                      }}
                    />
                    <Legend wrapperStyle={{ fontSize: '12px' }} iconType="circle" />
                    <Bar dataKey="plannedMinutes" name="Planejado" fill="var(--color-muted)" radius={[4, 4, 0, 0]} />
                    <Bar dataKey="executedMinutes" name="Executado" fill="var(--color-primary)" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              )}
            </Card>
          </motion.div>

          {/* Direction distribution */}
          <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.4 }}>
            <Card>
              <h3 className="text-lg font-medium text-foreground mb-6">Energia por Direção</h3>
              {dirChartData.length === 0 ? (
                <p className="text-sm text-muted-foreground text-center py-8">Nenhuma sessão registrada ainda</p>
              ) : (
                <ResponsiveContainer width="100%" height={280}>
                  <PieChart>
                    <Pie data={dirChartData} cx="50%" cy="50%" labelLine={false} label={(e) => e.name} outerRadius={90} dataKey="value">
                      {dirChartData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={entry.color} />
                      ))}
                    </Pie>
                    <Tooltip
                      content={(props) => {
                        const entry = props.payload?.[0];
                        if (!entry) return null;
                        const total = dirChartData.reduce((s, d) => s + d.value, 0);
                        const value = Number(entry.value ?? 0);
                        const pct = total > 0 ? ((value / total) * 100).toFixed(1) : '0.0';
                        return (
                          <ChartTooltip
                            {...props}
                            title={String(entry.name ?? '')}
                            rows={() => [
                              { label: 'Horas investidas', value: `${value}h`, color: entry.payload?.color as string },
                              { label: 'Percentual', value: `${pct}%` },
                            ]}
                          />
                        );
                      }}
                    />
                  </PieChart>
                </ResponsiveContainer>
              )}
            </Card>
          </motion.div>

          {/* Status distribution */}
          <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.5 }}>
            <Card>
              <h3 className="text-lg font-medium text-foreground mb-6">Distribuição por Status</h3>
              {statusDist.length === 0 ? (
                <p className="text-sm text-muted-foreground text-center py-8">Sem dados disponíveis</p>
              ) : (
                <div className="space-y-4">
                  {statusDist.map((item, index) => {
                    const total = statusDist.reduce((sum, s) => sum + s.count, 0);
                    const pct = item.percentage ?? Math.round((item.count / total) * 100);
                    return (
                      <motion.div key={item.status} initial={{ opacity: 0, x: -20 }} animate={{ opacity: 1, x: 0 }} transition={{ delay: 0.55 + index * 0.05 }}>
                        <div className="flex items-center justify-between mb-1.5">
                          <span className="text-sm text-foreground">{PLAN_STATUS_LABEL[item.status]}</span>
                          <span className="text-sm font-medium text-muted-foreground">{item.count} ({pct}%)</span>
                        </div>
                        <div className="h-2 bg-muted rounded-full overflow-hidden">
                          <motion.div initial={{ width: 0 }} animate={{ width: `${pct}%` }} transition={{ duration: 0.8, delay: 0.55 + index * 0.05 }} className="h-full bg-primary" />
                        </div>
                      </motion.div>
                    );
                  })}
                </div>
              )}
            </Card>
          </motion.div>
        </div>

        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.7 }}>
          <Card className="bg-gradient-to-br from-primary/5 to-transparent border-primary/20">
            <h3 className="text-lg font-medium text-foreground mb-4">Reflexões</h3>
            <p className="text-sm text-muted-foreground italic">
              "Estes números não são cobranças. São apenas observações da sua jornada."
            </p>
          </Card>
        </motion.div>
      </div>
    </div>
  );
}
