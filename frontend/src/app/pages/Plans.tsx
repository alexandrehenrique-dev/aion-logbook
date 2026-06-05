import {
  CheckCircle2,
  Clock,
  Filter,
  LayoutList,
  Pause,
  Plus,
  Search,
  X,
  XCircle,
} from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';
import { Button } from '../components/Button';
import { Card } from '../components/Card';
import { planService } from '../../services/planService';
import { directionService } from '../../services/directionService';
import { toast } from '../../utils/toast';
import type { Plan, Direction, Priority } from '../../types';
import { PLAN_STATUS_LABEL, PRIORITY_LABEL } from '../../types';
import type { PlanStatus } from '../../types';

const STATUS_STYLE: Record<string, { color: string; bg: string; icon: typeof CheckCircle2 }> = {
  COMPLETED: { color: 'text-emerald-600 dark:text-emerald-400', bg: 'bg-emerald-500/10', icon: CheckCircle2 },
  IN_PROGRESS: { color: 'text-primary', bg: 'bg-primary/10', icon: Clock },
  DUE: { color: 'text-amber-600 dark:text-amber-400', bg: 'bg-amber-500/10', icon: Clock },
  MISSED: { color: 'text-destructive/70', bg: 'bg-destructive/10', icon: Pause },
  PARTIAL: { color: 'text-muted-foreground', bg: 'bg-muted', icon: CheckCircle2 },
  IGNORED: { color: 'text-muted-foreground', bg: 'bg-muted', icon: XCircle },
  PENDING: { color: 'text-secondary', bg: 'bg-secondary/10', icon: Clock },
  SCHEDULED: { color: 'text-muted-foreground', bg: 'bg-muted', icon: Clock },
  POSTPONED: { color: 'text-muted-foreground', bg: 'bg-muted', icon: Clock },
  CANCELED: { color: 'text-muted-foreground', bg: 'bg-muted', icon: XCircle },
  DRAFT: { color: 'text-muted-foreground', bg: 'bg-muted', icon: Clock },
};

const PRIORITY_COLOR: Record<Priority, string> = {
  LOW: 'bg-muted text-muted-foreground',
  MEDIUM: 'bg-secondary/10 text-secondary',
  HIGH: 'bg-amber-500/10 text-amber-600 dark:text-amber-400',
  CRITICAL: 'bg-destructive/10 text-destructive',
};

type NewPlanForm = {
  title: string;
  description: string;
  directionId: string;
  priority: Priority;
  plannedDate: string;
  plannedStartAt: string;
  estimatedMinutes: number;
};

function localDateString(): string {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

function localTimezoneOffset(): string {
  const offset = new Date().getTimezoneOffset(); // positive = west of UTC
  const sign = offset <= 0 ? '+' : '-';
  const abs = Math.abs(offset);
  return `${sign}${String(Math.floor(abs / 60)).padStart(2, '0')}:${String(abs % 60).padStart(2, '0')}`;
}

const EMPTY_FORM: NewPlanForm = {
  title: '',
  description: '',
  directionId: '',
  priority: 'MEDIUM',
  plannedDate: localDateString(),
  plannedStartAt: '',
  estimatedMinutes: 60,
};

export function Plans() {
  const navigate = useNavigate();
  const [plans, setPlans] = useState<Plan[]>([]);
  const [directions, setDirections] = useState<Direction[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [filterStatus, setFilterStatus] = useState<string>('all');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [form, setForm] = useState<NewPlanForm>(EMPTY_FORM);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    Promise.all([planService.list(), directionService.list()])
      .then(([p, d]) => { setPlans(p.data); setDirections(d); })
      .finally(() => setLoading(false));
  }, []);

  const directionMap = Object.fromEntries(directions.map((d) => [d.id, d]));

  const filteredPlans = plans.filter((plan) => {
    const matchesSearch = plan.title.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesStatus = filterStatus === 'all' || plan.status === filterStatus;
    return matchesSearch && matchesStatus;
  });

  const isValidTime = (t: string) => /^\d{2}:\d{2}$/.test(t);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.title.trim()) return;
    const hasTime = form.plannedStartAt && isValidTime(form.plannedStartAt);
    setSaving(true);
    try {
      const created = await planService.create({
        title: form.title,
        description: form.description || undefined,
        directionId: form.directionId || undefined,
        priority: form.priority,
        plannedDate: form.plannedDate || undefined,
        plannedStartAt: hasTime ? `${form.plannedDate}T${form.plannedStartAt}:00${localTimezoneOffset()}` : undefined,
        estimatedMinutes: form.estimatedMinutes,
        notificationEnabled: hasTime ? true : undefined,
      });
      setPlans((prev) => [created, ...prev]);
      setShowCreateModal(false);
      setForm(EMPTY_FORM);
      toast.notify('Plano criado', 'Seu plano foi salvo com sucesso.');
    } catch {
      toast.error('Não conseguimos criar o plano agora.');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-background py-8 px-6">
        <div className="max-w-6xl mx-auto space-y-4">
          {[1, 2, 3].map((i) => (
            <div key={i} className="h-24 bg-muted rounded-xl animate-pulse" />
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background py-8 px-6">
      <div className="max-w-6xl mx-auto">
        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} className="mb-8">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h1 className="text-3xl font-medium text-foreground flex items-center gap-3 mb-2">
                <LayoutList className="w-8 h-8" />
                Planos
              </h1>
              <p className="text-muted-foreground">Suas intenções de direção e travessia</p>
            </div>
            <Button size="lg" className="flex items-center gap-2" onClick={() => setShowCreateModal(true)}>
              <Plus className="w-5 h-5" />
              Novo plano
            </Button>
          </div>

          <Card>
            <div className="flex flex-col md:flex-row gap-4">
              <div className="flex-1">
                <div className="relative">
                  <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
                  <input
                    type="text"
                    placeholder="Buscar planos..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="w-full pl-10 pr-4 py-2.5 rounded-lg bg-muted/50 border border-border text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                  />
                </div>
              </div>
              <div className="flex items-center gap-2">
                <Filter className="w-4 h-4 text-muted-foreground" />
                <select
                  value={filterStatus}
                  onChange={(e) => setFilterStatus(e.target.value)}
                  className="px-4 py-2.5 rounded-lg bg-muted/50 border border-border text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                >
                  <option value="all">Todos os status</option>
                  {(Object.keys(PLAN_STATUS_LABEL) as PlanStatus[]).map((s) => (
                    <option key={s} value={s}>{PLAN_STATUS_LABEL[s]}</option>
                  ))}
                </select>
              </div>
            </div>
          </Card>
        </motion.div>

        <div className="space-y-4">
          {filteredPlans.length === 0 ? (
            <Card>
              <div className="text-center py-12">
                <p className="text-muted-foreground mb-2">Nenhum plano encontrado</p>
                <p className="text-sm text-muted-foreground/70 italic">
                  A travessia ainda não começou. Você pode começar pequeno.
                </p>
              </div>
            </Card>
          ) : (
            filteredPlans.map((plan, index) => {
              const style = STATUS_STYLE[plan.status] ?? STATUS_STYLE['PENDING'];
              const StatusIcon = style.icon;
              const dir = plan.directionId ? directionMap[plan.directionId] : null;

              return (
                <motion.div
                  key={plan.id}
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: index * 0.04 }}
                  onClick={() => navigate(`/plans/${plan.id}`)}
                >
                  <Card hover className="cursor-pointer">
                    <div className="flex items-start justify-between gap-6">
                      <div className="flex-1">
                        <div className="flex items-start gap-4 mb-3">
                          <div className={`p-2 rounded-lg ${style.bg} shrink-0`}>
                            <StatusIcon className={`w-5 h-5 ${style.color}`} />
                          </div>
                          <div className="flex-1">
                            <h3 className="text-lg font-medium text-foreground mb-1">{plan.title}</h3>
                            <div className="flex flex-wrap items-center gap-3 text-sm text-muted-foreground">
                              {dir && (
                                <>
                                  <span className="flex items-center gap-1">
                                    <span className="w-2 h-2 rounded-full" style={{ background: dir.color ?? '#888' }} />
                                    {dir.name}
                                  </span>
                                  <span>•</span>
                                </>
                              )}
                              {plan.plannedStartAt && (
                                <>
                                  <span>
                                    {new Date(plan.plannedStartAt).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })}
                                  </span>
                                  <span>•</span>
                                </>
                              )}
                              {plan.estimatedMinutes && (
                                <span>{Math.floor(plan.estimatedMinutes / 60) > 0 ? `${Math.floor(plan.estimatedMinutes / 60)}h` : ''}{plan.estimatedMinutes % 60 > 0 ? `${plan.estimatedMinutes % 60}min` : ''}</span>
                              )}
                              {plan.plannedDate && (
                                <>
                                  <span>•</span>
                                  <span>
                                    {new Date(plan.plannedDate + 'T12:00:00').toLocaleDateString('pt-BR', { day: 'numeric', month: 'short' })}
                                  </span>
                                </>
                              )}
                            </div>
                          </div>
                        </div>
                      </div>
                      <div className="flex flex-col items-end gap-2 shrink-0">
                        <span className={`text-xs px-3 py-1.5 rounded-full ${style.bg} ${style.color} font-medium`}>
                          {PLAN_STATUS_LABEL[plan.status]}
                        </span>
                        <span className={`text-xs px-2 py-0.5 rounded-full ${PRIORITY_COLOR[plan.priority]}`}>
                          {PRIORITY_LABEL[plan.priority]}
                        </span>
                      </div>
                    </div>
                  </Card>
                </motion.div>
              );
            })
          )}
        </div>

        {filteredPlans.length > 0 && (
          <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ delay: 0.5 }} className="mt-8 text-center">
            <p className="text-sm text-muted-foreground">
              {filteredPlans.length} {filteredPlans.length === 1 ? 'plano' : 'planos'}
              {filterStatus !== 'all' && ' nesta visualização'}
            </p>
          </motion.div>
        )}
      </div>

      {/* Create Plan Modal */}
      <AnimatePresence>
        {showCreateModal && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4"
            onClick={(e) => { if (e.target === e.currentTarget) setShowCreateModal(false); }}
          >
            <motion.div
              initial={{ opacity: 0, scale: 0.95, y: 20 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.95, y: 20 }}
              className="bg-card border border-border rounded-2xl shadow-2xl w-full max-w-lg overflow-hidden max-h-[90vh] overflow-y-auto"
            >
              <div className="px-6 py-4 border-b border-border flex items-center justify-between sticky top-0 bg-card z-10">
                <h2 className="text-base font-semibold text-foreground">Novo Plano</h2>
                <button onClick={() => setShowCreateModal(false)} className="p-1.5 rounded-lg hover:bg-muted">
                  <X className="w-4 h-4 text-muted-foreground" />
                </button>
              </div>
              <form onSubmit={handleCreate} className="px-6 py-5 space-y-4">
                <div>
                  <label className="block text-sm font-medium text-foreground mb-1.5">Título *</label>
                  <input
                    type="text"
                    value={form.title}
                    onChange={(e) => setForm((p) => ({ ...p, title: e.target.value }))}
                    placeholder="O que você quer fazer?"
                    className="w-full px-3 py-2 rounded-lg bg-muted/50 border border-border text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                    required
                    autoFocus
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-foreground mb-1.5">Descrição</label>
                  <textarea
                    value={form.description}
                    onChange={(e) => setForm((p) => ({ ...p, description: e.target.value }))}
                    placeholder="Por que este plano existe? Qual a intenção?"
                    rows={3}
                    className="w-full px-3 py-2 rounded-lg bg-muted/50 border border-border text-sm focus:outline-none focus:ring-2 focus:ring-ring resize-none"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-foreground mb-1.5">Direção</label>
                  <select
                    value={form.directionId}
                    onChange={(e) => setForm((p) => ({ ...p, directionId: e.target.value }))}
                    className="w-full px-3 py-2 rounded-lg bg-muted/50 border border-border text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                  >
                    <option value="">Sem direção</option>
                    {directions.filter((d) => d.status === 'ACTIVE').map((d) => (
                      <option key={d.id} value={d.id}>{d.name}</option>
                    ))}
                  </select>
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-foreground mb-1.5">Prioridade</label>
                    <select
                      value={form.priority}
                      onChange={(e) => setForm((p) => ({ ...p, priority: e.target.value as Priority }))}
                      className="w-full px-3 py-2 rounded-lg bg-muted/50 border border-border text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                    >
                      {(Object.keys(PRIORITY_LABEL) as Priority[]).map((p) => (
                        <option key={p} value={p}>{PRIORITY_LABEL[p]}</option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-foreground mb-1.5">Duração (min)</label>
                    <input
                      type="number"
                      value={form.estimatedMinutes}
                      onChange={(e) => setForm((p) => ({ ...p, estimatedMinutes: Number(e.target.value) }))}
                      min={1}
                      className="w-full px-3 py-2 rounded-lg bg-muted/50 border border-border text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                    />
                  </div>
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-foreground mb-1.5">Data</label>
                    <input
                      type="date"
                      value={form.plannedDate}
                      onChange={(e) => setForm((p) => ({ ...p, plannedDate: e.target.value }))}
                      className="w-full px-3 py-2 rounded-lg bg-muted/50 border border-border text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-foreground mb-1.5">Horário</label>
                    <input
                      type="time"
                      value={form.plannedStartAt}
                      onChange={(e) => setForm((p) => ({ ...p, plannedStartAt: e.target.value }))}
                      className="w-full px-3 py-2 rounded-lg bg-muted/50 border border-border text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                    />
                  </div>
                </div>
                <div className="flex gap-3 pt-2">
                  <button
                    type="button"
                    onClick={() => setShowCreateModal(false)}
                    className="flex-1 py-2.5 rounded-lg border border-border text-sm text-muted-foreground hover:bg-muted transition-colors"
                  >
                    Cancelar
                  </button>
                  <button
                    type="submit"
                    disabled={saving || !form.title.trim()}
                    className="flex-1 py-2.5 rounded-lg bg-primary text-primary-foreground text-sm font-medium hover:bg-primary/90 transition-colors disabled:opacity-50"
                  >
                    {saving ? 'Criando...' : 'Criar plano'}
                  </button>
                </div>
              </form>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
