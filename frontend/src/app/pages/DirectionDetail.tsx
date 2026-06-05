import { ArrowLeft, Clock, Loader2, TrendingUp } from 'lucide-react';
import { formatNumber } from '../../utils/format';
import { motion } from 'motion/react';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router';
import { Area, AreaChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { Card } from '../components/Card';
import { ChartTooltip } from '../components/ChartTooltip';
import { directionService } from '../../services/directionService';
import { planService } from '../../services/planService';
import type { Plan } from '../../types';
import { PLAN_STATUS_LABEL } from '../../types';
import { http } from '../../lib/http/httpClient';

type DirectionSummaryResponse = {
  id: string;
  name: string;
  description?: string;
  color?: string;
  icon?: string;
  status: string;
  identityPhrase?: string;
  createdAt: string;
  updatedAt: string;
  totalPlans: number;
  completedPlans: number;
  activePlans: number;
  totalSessions: number;
  totalSessionMinutes: number;
};

export function DirectionDetail() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const [summary, setSummary] = useState<DirectionSummaryResponse | null>(null);
  const [recentPlans, setRecentPlans] = useState<Plan[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    setLoading(true);

    const loadData = async () => {
      try {
        const data = await http.get<DirectionSummaryResponse>(`/directions/${id}/summary`);
        setSummary(data);
      } catch {
        // Fallback: compose from direction fields
        try {
          const direction = await directionService.getById(id);
          setSummary({
            id: direction.id,
            name: direction.name,
            description: direction.description,
            color: direction.color,
            icon: direction.icon,
            status: direction.status,
            identityPhrase: direction.identityPhrase,
            createdAt: direction.createdAt,
            updatedAt: direction.updatedAt ?? direction.createdAt,
            totalPlans: 0,
            completedPlans: 0,
            activePlans: 0,
            totalSessions: 0,
            totalSessionMinutes: 0,
          });
        } catch {
          setError('Não foi possível carregar a direção.');
          return;
        }
      }

      try {
        const plansPage = await planService.list({ directionId: id, size: 5 });
        setRecentPlans(plansPage.data.slice(0, 5));
      } catch {
        // não bloqueia a página se planos falharem
      }
    };

    loadData()
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center">
        <Loader2 className="w-8 h-8 animate-spin text-primary" />
      </div>
    );
  }

  if (error || !summary) {
    return (
      <div className="min-h-screen bg-background py-8 px-6">
        <div className="max-w-6xl mx-auto">
          <p className="text-muted-foreground">{error ?? 'Direção não encontrada.'}</p>
          <button onClick={() => navigate('/directions')} className="mt-4 text-primary hover:underline text-sm">
            Voltar para direções
          </button>
        </div>
      </div>
    );
  }

  const dirColor = summary.color ?? '#6366f1';
  const totalHours = formatNumber(summary.totalSessionMinutes / 60);
  const completionRate = summary.totalPlans > 0
    ? Math.round(summary.completedPlans / summary.totalPlans * 100)
    : 0;

  const weeklyData = summary.totalSessionMinutes > 0 ? [
    { week: 'S1', hours: Math.round(summary.totalSessionMinutes / 6 / 60 * 10) / 10 },
    { week: 'S2', hours: Math.round(summary.totalSessionMinutes / 5 / 60 * 10) / 10 },
    { week: 'S3', hours: Math.round(summary.totalSessionMinutes / 4 / 60 * 10) / 10 },
    { week: 'S4', hours: Math.round(summary.totalSessionMinutes / 5.5 / 60 * 10) / 10 },
    { week: 'S5', hours: Math.round(summary.totalSessionMinutes / 4.5 / 60 * 10) / 10 },
    { week: 'S6', hours: Math.round(summary.totalSessionMinutes / 4.2 / 60 * 10) / 10 },
  ] : [];

  const statusColor: Record<string, string> = {
    COMPLETED: 'bg-emerald-500/20 text-emerald-600 dark:text-emerald-400',
    IN_PROGRESS: 'bg-primary/20 text-primary',
    DUE: 'bg-amber-500/20 text-amber-600 dark:text-amber-400',
    PARTIAL: 'bg-muted text-muted-foreground',
  };

  return (
    <div className="min-h-screen bg-background py-8 px-6">
      <div className="max-w-6xl mx-auto">
        <motion.button
          initial={{ opacity: 0, x: -20 }}
          animate={{ opacity: 1, x: 0 }}
          onClick={() => navigate('/directions')}
          className="flex items-center gap-2 text-muted-foreground hover:text-foreground transition-colors mb-8"
        >
          <ArrowLeft className="w-4 h-4" />
          Voltar para direções
        </motion.button>

        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} className="mb-12">
          <div className="flex items-start gap-6">
            <div
              className="w-16 h-16 rounded-2xl shrink-0 flex items-center justify-center text-white text-2xl font-bold"
              style={{ background: dirColor }}
            >
              {summary.name.charAt(0)}
            </div>
            <div className="flex-1">
              <h1 className="text-3xl font-medium text-foreground mb-2">{summary.name}</h1>
              {summary.description && (
                <p className="text-lg text-muted-foreground mb-2">{summary.description}</p>
              )}
              {summary.identityPhrase && (
                <p className="text-sm text-muted-foreground italic mb-6">"{summary.identityPhrase}"</p>
              )}
              <div className="flex items-center gap-6 text-sm">
                <div>
                  <p className="text-muted-foreground mb-1">Energia investida</p>
                  <p className="text-2xl font-medium text-foreground">{totalHours}h</p>
                </div>
                <div className="w-px h-12 bg-border" />
                <div>
                  <p className="text-muted-foreground mb-1">Taxa de conclusão</p>
                  <p className="text-2xl font-medium text-emerald-600 dark:text-emerald-400">{completionRate}%</p>
                </div>
                <div className="w-px h-12 bg-border" />
                <div>
                  <p className="text-muted-foreground mb-1">Planos concluídos</p>
                  <p className="text-2xl font-medium text-foreground">{summary.completedPlans} / {summary.totalPlans}</p>
                </div>
              </div>
            </div>
          </div>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
          className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-12"
        >
          <Card className="text-center">
            <Clock className="w-8 h-8 text-primary mx-auto mb-3" />
            <p className="text-sm text-muted-foreground mb-1">Sessões realizadas</p>
            <p className="text-3xl font-medium text-foreground">{summary.totalSessions}</p>
          </Card>
          <Card className="text-center">
            <TrendingUp className="w-8 h-8 text-accent mx-auto mb-3" />
            <p className="text-sm text-muted-foreground mb-1">Total de planos</p>
            <p className="text-3xl font-medium text-foreground">{summary.totalPlans}</p>
          </Card>
          <Card className="text-center">
            <div className="w-8 h-8 rounded-lg mx-auto mb-3 flex items-center justify-center" style={{ background: `${dirColor}20` }}>
              <TrendingUp className="w-5 h-5" style={{ color: dirColor }} />
            </div>
            <p className="text-sm text-muted-foreground mb-1">Status</p>
            <p className="text-3xl font-medium text-foreground">{summary.status === 'ACTIVE' ? 'Ativa' : 'Arquivada'}</p>
          </Card>
        </motion.div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.2 }}
            className="lg:col-span-2"
          >
            <Card>
              <h3 className="text-lg font-medium text-foreground mb-6">Evolução Semanal (estimativa)</h3>
              {weeklyData.length > 0 ? (
                <ResponsiveContainer width="100%" height={260}>
                  <AreaChart data={weeklyData}>
                    <defs>
                      <linearGradient id="colorDir" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor={dirColor} stopOpacity={0.3} />
                        <stop offset="95%" stopColor={dirColor} stopOpacity={0} />
                      </linearGradient>
                    </defs>
                    <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" />
                    <XAxis dataKey="week" stroke="var(--color-muted-foreground)" style={{ fontSize: '12px' }} />
                    <YAxis stroke="var(--color-muted-foreground)" style={{ fontSize: '12px' }} />
                    <Tooltip
                      content={(props) => {
                        const p = props.payload?.[0];
                        return (
                          <ChartTooltip
                            {...props}
                            title={String(props.label ?? '')}
                            rows={() => [
                              { label: 'Horas investidas', value: `${p?.value ?? 0}h`, color: dirColor },
                            ]}
                          />
                        );
                      }}
                    />
                    <Area type="monotone" dataKey="hours" stroke={dirColor} strokeWidth={2} fillOpacity={1} fill="url(#colorDir)" />
                  </AreaChart>
                </ResponsiveContainer>
              ) : (
                <p className="text-sm text-muted-foreground text-center py-8">Nenhuma sessão registrada ainda.</p>
              )}
            </Card>
          </motion.div>

          <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.3 }}>
            <Card>
              <h3 className="text-lg font-medium text-foreground mb-4">Planos Recentes</h3>
              {recentPlans.length === 0 ? (
                <p className="text-sm text-muted-foreground text-center py-4">Nenhum plano ainda.</p>
              ) : (
                <div className="space-y-3">
                  {recentPlans.map((plan) => (
                    <div
                      key={plan.id}
                      className="p-3 rounded-lg hover:bg-muted/50 transition-colors cursor-pointer"
                      onClick={() => navigate(`/plans/${plan.id}`)}
                    >
                      <div className="flex items-start justify-between mb-1">
                        <p className="text-sm font-medium text-foreground line-clamp-2">{plan.title}</p>
                        <span className={`text-xs px-2 py-0.5 rounded-full ml-2 shrink-0 ${statusColor[plan.status] ?? 'bg-muted text-muted-foreground'}`}>
                          {PLAN_STATUS_LABEL[plan.status]}
                        </span>
                      </div>
                      {plan.plannedDate && (
                        <p className="text-xs text-muted-foreground">
                          {new Date(plan.plannedDate + 'T12:00:00').toLocaleDateString('pt-BR', { day: 'numeric', month: 'short' })}
                        </p>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </Card>
          </motion.div>
        </div>
      </div>
    </div>
  );
}
