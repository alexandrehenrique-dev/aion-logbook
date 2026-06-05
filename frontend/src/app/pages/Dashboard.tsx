import {
  Bell,
  Calendar,
  CalendarClock,
  CheckCircle2,
  Clock,
  PauseCircle,
  TrendingUp,
  XCircle,
} from 'lucide-react';
import { formatPercentage, formatDuration } from '../../utils/format';
import { motion } from 'motion/react';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';
import { Card } from '../components/Card';
import { dashboardService } from '../../services/dashboardService';
import { directionService } from '../../services/directionService';
import type { DashboardToday, Direction } from '../../types';
import { PLAN_STATUS_LABEL } from '../../types';

function getGreeting() {
  const h = new Date().getHours();
  if (h < 12) return 'Bom dia';
  if (h < 18) return 'Boa tarde';
  return 'Boa noite';
}

function DashboardSkeleton() {
  return (
    <div className="animate-pulse space-y-6">
      <div className="h-8 bg-muted rounded w-64" />
      <div className="h-32 bg-muted rounded-xl" />
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2 h-64 bg-muted rounded-xl" />
        <div className="space-y-4">
          <div className="h-32 bg-muted rounded-xl" />
          <div className="h-32 bg-muted rounded-xl" />
        </div>
      </div>
    </div>
  );
}

export function Dashboard() {
  const navigate = useNavigate();
  const [data, setData] = useState<DashboardToday | null>(null);
  const [directions, setDirections] = useState<Direction[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    Promise.all([dashboardService.getToday(), directionService.list()])
      .then(([d, dirs]) => { setData(d); setDirections(dirs); })
      .catch(() => setError('Não foi possível carregar o dashboard.'))
      .finally(() => setLoading(false));
  }, []);

  const dirMap = Object.fromEntries(directions.map((d) => [d.id, d]));

  const greeting = getGreeting();

  if (loading) {
    return (
      <div className="max-w-7xl mx-auto px-4 md:px-6 py-8">
        <DashboardSkeleton />
      </div>
    );
  }

  if (error || !data) {
    return (
      <div className="max-w-7xl mx-auto px-4 md:px-6 py-8 text-center">
        <p className="text-muted-foreground">{error ?? 'Erro inesperado.'}</p>
      </div>
    );
  }

  const currentFocus = data.plansInProgress[0] ?? data.plansDue[0] ?? (data.plansScheduled ?? [])[0] ?? null;

  return (
    <div className="min-h-screen bg-background">
      <div className="max-w-7xl mx-auto px-4 md:px-6 py-8">
        {/* Hero */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
          className="mb-10"
        >
          <div className="flex flex-wrap items-baseline gap-3 mb-2">
            <h2 className="text-2xl md:text-3xl font-medium text-foreground">{greeting}</h2>
            <span className="text-muted-foreground text-sm md:text-base">
              {new Date().toLocaleDateString('pt-BR', {
                weekday: 'long',
                day: 'numeric',
                month: 'long',
              })}
            </span>
          </div>
          <p className="text-base md:text-lg text-muted-foreground mt-3 mb-6">
            O que merece sua energia agora?
          </p>

          {currentFocus && (
            <Card
              hover
              className="border-2 border-primary/20 bg-gradient-to-br from-primary/5 to-transparent cursor-pointer"
              onClick={() => navigate(`/plans/${currentFocus.id}`)}
            >
              <div className="flex items-start justify-between gap-4">
                <div className="min-w-0">
                  <p className="text-sm text-muted-foreground mb-1">{PLAN_STATUS_LABEL[currentFocus.status]}</p>
                  <h3 className="text-lg md:text-xl font-medium text-foreground mb-1 truncate">{currentFocus.title}</h3>
                  {currentFocus.directionId && dirMap[currentFocus.directionId] && (
                    <p className="text-sm text-muted-foreground flex items-center gap-1">
                      <span className="w-2 h-2 rounded-full inline-block" style={{ background: dirMap[currentFocus.directionId].color ?? '#888' }} />
                      {dirMap[currentFocus.directionId].name}
                    </p>
                  )}
                </div>
                {currentFocus.estimatedMinutes && (
                  <div className="text-right shrink-0">
                    <p className="text-xl md:text-2xl font-medium text-primary">
                      {Math.floor(currentFocus.estimatedMinutes / 60)}h
                      {currentFocus.estimatedMinutes % 60 > 0 && `${currentFocus.estimatedMinutes % 60}min`}
                    </p>
                    <p className="text-sm text-muted-foreground">planejadas</p>
                  </div>
                )}
              </div>
            </Card>
          )}
        </motion.div>

        {/* Main grid */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 md:gap-8">
          {/* Timeline */}
          <motion.div
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.6, delay: 0.2 }}
            className="lg:col-span-2 space-y-6"
          >
            <h3 className="text-lg md:text-xl font-medium text-foreground flex items-center gap-2">
              <Calendar className="w-5 h-5" />
              Timeline do dia
            </h3>

            <Card>
              {[...data.plansInProgress, ...data.plansDue, ...(data.plansScheduled ?? []), ...(data.plansPending ?? []), ...data.plansMissed, ...data.plansCompleted].length === 0 ? (
                <p className="text-sm text-muted-foreground text-center py-8">Nenhum plano para hoje.</p>
              ) : (
                <div className="space-y-4">
                  {[...data.plansInProgress, ...data.plansDue, ...(data.plansScheduled ?? []), ...(data.plansPending ?? []), ...data.plansMissed, ...data.plansCompleted].map((plan, index) => (
                    <motion.div
                      key={plan.id}
                      initial={{ opacity: 0, x: -10 }}
                      animate={{ opacity: 1, x: 0 }}
                      transition={{ delay: index * 0.08 }}
                      className="flex items-start gap-4 pb-4 border-b border-border last:border-0 last:pb-0 hover:bg-muted/30 -mx-2 px-2 rounded-lg cursor-pointer transition-colors"
                      onClick={() => navigate(`/plans/${plan.id}`)}
                    >
                      <div className="w-14 text-xs text-muted-foreground shrink-0 pt-1">
                        {plan.plannedStartAt
                          ? new Date(plan.plannedStartAt).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
                          : '--:--'}
                      </div>
                      <div
                        className={`w-1 rounded-full shrink-0 self-stretch min-h-[20px] ${
                          plan.status === 'COMPLETED' ? 'bg-emerald-500' :
                          plan.status === 'IN_PROGRESS' ? 'bg-primary' :
                          plan.status === 'MISSED' ? 'bg-destructive/50' :
                          plan.status === 'DUE' ? 'bg-amber-500' :
                          plan.status === 'SCHEDULED' ? 'bg-violet-400' :
                          plan.status === 'POSTPONED' ? 'bg-violet-300' :
                          plan.status === 'PENDING' ? 'bg-muted-foreground/30' : 'bg-border'
                        }`}
                      />
                      <div className="flex-1 min-w-0">
                        <h4 className="font-medium text-foreground mb-0.5 text-sm md:text-base truncate">{plan.title}</h4>
                        <p className="text-xs text-muted-foreground">
                          {plan.directionId && dirMap[plan.directionId]
                            ? `${dirMap[plan.directionId].name} · `
                            : ''}
                          {PLAN_STATUS_LABEL[plan.status]}
                        </p>
                      </div>
                      {plan.status === 'COMPLETED' && <CheckCircle2 className="w-5 h-5 text-emerald-500 shrink-0 mt-0.5" />}
                      {plan.status === 'IN_PROGRESS' && <Clock className="w-5 h-5 text-primary shrink-0 animate-pulse mt-0.5" />}
                      {plan.status === 'MISSED' && <XCircle className="w-5 h-5 text-destructive/50 shrink-0 mt-0.5" />}
                      {plan.status === 'DUE' && <Bell className="w-5 h-5 text-amber-500 shrink-0 mt-0.5" />}
                      {plan.status === 'SCHEDULED' && <CalendarClock className="w-5 h-5 text-violet-400 shrink-0 mt-0.5" />}
                      {plan.status === 'POSTPONED' && <CalendarClock className="w-5 h-5 text-violet-300 shrink-0 mt-0.5" />}
                      {plan.status === 'PENDING' && <Clock className="w-5 h-5 text-muted-foreground/50 shrink-0 mt-0.5" />}
                    </motion.div>
                  ))}
                </div>
              )}
            </Card>
          </motion.div>

          {/* Side cards */}
          <motion.div
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.6, delay: 0.3 }}
            className="space-y-4 md:space-y-6"
          >
            {(data.plansScheduled ?? []).length > 0 && (
              <Card hover>
                <div className="flex items-center justify-between mb-3">
                  <h4 className="font-medium text-foreground flex items-center gap-2 text-sm">
                    <CalendarClock className="w-4 h-4 text-violet-400" />
                    Planejados para hoje
                  </h4>
                  <span className="text-xs bg-violet-400/20 text-violet-600 dark:text-violet-300 px-2 py-0.5 rounded-full">
                    {data.plansScheduled!.length}
                  </span>
                </div>
                <div className="space-y-2">
                  {data.plansScheduled!.map((p) => (
                    <div key={p.id} className="text-sm cursor-pointer hover:bg-muted/50 p-1.5 rounded-lg transition-colors" onClick={() => navigate(`/plans/${p.id}`)}>
                      <p className="text-foreground font-medium truncate">{p.title}</p>
                      {p.plannedStartAt && (
                        <p className="text-xs text-muted-foreground">
                          {new Date(p.plannedStartAt).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })}
                          {p.directionId && dirMap[p.directionId] ? ` · ${dirMap[p.directionId].name}` : ''}
                        </p>
                      )}
                    </div>
                  ))}
                </div>
              </Card>
            )}

            {data.plansDue.length > 0 && (
              <Card hover>
                <div className="flex items-center justify-between mb-3">
                  <h4 className="font-medium text-foreground flex items-center gap-2 text-sm">
                    <Clock className="w-4 h-4 text-amber-500" />
                    Chegou a hora
                  </h4>
                  <span className="text-xs bg-amber-500/20 text-amber-600 dark:text-amber-400 px-2 py-0.5 rounded-full">
                    {data.plansDue.length}
                  </span>
                </div>
                <div className="space-y-2">
                  {data.plansDue.map((p) => (
                    <div key={p.id} className="text-sm cursor-pointer hover:bg-muted/50 p-1.5 rounded-lg transition-colors" onClick={() => navigate(`/plans/${p.id}`)}>
                      <p className="text-foreground font-medium truncate">{p.title}</p>
                      {p.directionId && dirMap[p.directionId] && (
                        <p className="text-xs text-muted-foreground">{dirMap[p.directionId].name}</p>
                      )}
                    </div>
                  ))}
                </div>
              </Card>
            )}

            {data.plansMissed.length > 0 && (
              <Card hover>
                <div className="flex items-center justify-between mb-3">
                  <h4 className="font-medium text-foreground flex items-center gap-2 text-sm">
                    <PauseCircle className="w-4 h-4 text-destructive/70" />
                    Ficou para trás
                  </h4>
                  <span className="text-xs bg-destructive/10 text-destructive px-2 py-0.5 rounded-full">
                    {data.plansMissed.length}
                  </span>
                </div>
                <div className="space-y-2">
                  {data.plansMissed.map((p) => (
                    <div key={p.id} className="text-sm cursor-pointer hover:bg-muted/50 p-1.5 rounded-lg transition-colors" onClick={() => navigate(`/plans/${p.id}`)}>
                      <p className="text-foreground font-medium truncate">{p.title}</p>
                      {p.directionId && dirMap[p.directionId] && (
                        <p className="text-xs text-muted-foreground">{dirMap[p.directionId].name}</p>
                      )}
                    </div>
                  ))}
                </div>
                <p className="text-xs text-muted-foreground mt-3 italic">Mas ainda pode ser resgatado</p>
              </Card>
            )}

            {data.plansCompleted.length > 0 && (
              <Card hover>
                <div className="flex items-center justify-between mb-3">
                  <h4 className="font-medium text-foreground flex items-center gap-2 text-sm">
                    <CheckCircle2 className="w-4 h-4 text-emerald-500" />
                    Concluídos
                  </h4>
                  <span className="text-xs bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 px-2 py-0.5 rounded-full">
                    {data.plansCompleted.length}
                  </span>
                </div>
                <div className="space-y-2">
                  {data.plansCompleted.map((p) => (
                    <div key={p.id} className="text-sm cursor-pointer hover:bg-muted/50 p-1.5 rounded-lg transition-colors" onClick={() => navigate(`/plans/${p.id}`)}>
                      <p className="text-foreground font-medium truncate">{p.title}</p>
                      {p.directionId && dirMap[p.directionId] && (
                        <p className="text-xs text-muted-foreground">{dirMap[p.directionId].name}</p>
                      )}
                    </div>
                  ))}
                </div>
              </Card>
            )}

            <Card className="bg-gradient-to-br from-muted/30 to-transparent">
              <h4 className="font-medium text-foreground mb-3 flex items-center gap-2 text-sm">
                <TrendingUp className="w-4 h-4" />
                Resumo
              </h4>
              <div className="space-y-2.5">
                <div className="flex justify-between items-center">
                  <span className="text-xs text-muted-foreground">Energia investida</span>
                  <span className="text-sm font-medium text-foreground">
                    {formatDuration(data.totalEnergyMinutes)}
                  </span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-xs text-muted-foreground">Taxa de conclusão</span>
                  <span className="text-sm font-medium text-emerald-600 dark:text-emerald-400">
                    {formatPercentage(data.completionRate)}
                  </span>
                </div>
              </div>
            </Card>
          </motion.div>
        </div>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.5 }}
          className="mt-10 md:mt-12 text-center"
        >
          <p className="text-sm text-muted-foreground italic">
            "Ainda há tempo para recuperar direção. Nem tudo precisa ser feito hoje."
          </p>
        </motion.div>
      </div>
    </div>
  );
}
