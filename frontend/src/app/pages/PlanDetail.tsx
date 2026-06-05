import {
  ArrowLeft,
  Calendar,
  CheckCircle2,
  Clock,
  Edit2,
  Flag,
  Loader2,
  SkipForward,
  Sparkles,
  Timer,
  XCircle,
} from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router';
import { Button } from '../components/Button';
import { Card } from '../components/Card';
import { planService } from '../../services/planService';
import { directionService } from '../../services/directionService';
import type { Plan, PlanEvent, Direction } from '../../types';
import { PLAN_STATUS_LABEL, PRIORITY_LABEL } from '../../types';
import { toast } from '../../utils/toast';

type ActionModal =
  | 'complete'
  | 'partial'
  | 'postpone'
  | 'ignore'
  | 'cancel'
  | 'start'
  | 'modify'
  | null;

const TERMINAL = new Set(['COMPLETED', 'PARTIAL', 'CANCELED', 'IGNORED', 'MISSED']);

export function PlanDetail() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const [plan, setPlan] = useState<Plan | null>(null);
  const [events, setEvents] = useState<PlanEvent[]>([]);
  const [directions, setDirections] = useState<Direction[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeModal, setActiveModal] = useState<ActionModal>(null);
  const [submitting, setSubmitting] = useState(false);

  // modal form state — action modals
  const [notes, setNotes] = useState('');
  const [actualMinutes, setActualMinutes] = useState('');
  const [reason, setReason] = useState('');
  const [newDate, setNewDate] = useState('');
  const [newTime, setNewTime] = useState('');

  // modal form state — modify modal
  const [modTitle, setModTitle] = useState('');
  const [modDescription, setModDescription] = useState('');
  const [modPriority, setModPriority] = useState('');
  const [modDate, setModDate] = useState('');
  const [modTime, setModTime] = useState('');
  const [modMinutes, setModMinutes] = useState('');
  const [modReason, setModReason] = useState('');

  const reload = async () => {
    if (!id) return;
    try {
      const [p, evs] = await Promise.all([planService.getById(id), planService.getEvents(id)]);
      setPlan(p);
      setEvents(evs);
    } catch {
      setError('Não foi possível carregar o plano.');
    }
  };

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    Promise.all([planService.getById(id), planService.getEvents(id), directionService.list()])
      .then(([p, evs, dirs]) => { setPlan(p); setEvents(evs); setDirections(dirs); })
      .catch(() => setError('Não foi possível carregar o plano.'))
      .finally(() => setLoading(false));
  }, [id]);

  const dirMap = Object.fromEntries(directions.map((d) => [d.id, d]));
  const dir = plan?.directionId ? dirMap[plan.directionId] : null;

  const closeModal = () => {
    setActiveModal(null);
    setNotes('');
    setActualMinutes('');
    setReason('');
    setNewDate('');
    setNewTime('');
    setModTitle('');
    setModDescription('');
    setModPriority('');
    setModDate('');
    setModTime('');
    setModMinutes('');
    setModReason('');
  };

  const openModify = () => {
    if (!plan) return;
    setModTitle(plan.title);
    setModDescription(plan.description ?? '');
    setModPriority(plan.priority);
    setModDate(plan.plannedDate ?? '');
    setModTime(plan.plannedStartAt ? new Date(plan.plannedStartAt).toTimeString().slice(0, 5) : '');
    setModMinutes(plan.estimatedMinutes ? String(plan.estimatedMinutes) : '');
    setModReason('');
    setActiveModal('modify');
  };

  const handleModify = async () => {
    if (!id || !plan) return;
    setSubmitting(true);
    try {
      await planService.modify(id, {
        title: modTitle || plan.title,
        description: modDescription || undefined,
        priority: (modPriority || plan.priority) as Plan['priority'],
        plannedDate: modDate || undefined,
        plannedStartAt: modDate && modTime ? `${modDate}T${modTime}:00Z` : undefined,
        estimatedMinutes: modMinutes ? Number(modMinutes) : undefined,
      });
      closeModal();
      await reload();
      toast.success('Plano atualizado com sucesso.');
    } catch (e: unknown) {
      const msg = (e as { body?: { error?: string } })?.body?.error ?? 'Não foi possível atualizar o plano.';
      toast.error('Algo não saiu como esperado.', msg);
    } finally {
      setSubmitting(false);
    }
  };

  const handleAction = async () => {
    if (!id || !plan) return;
    setSubmitting(true);
    try {
      switch (activeModal) {
        case 'start':
          await planService.start(id);
          break;
        case 'complete':
          await planService.complete(id, {
            description: notes || undefined,
            actualMinutes: actualMinutes ? Number(actualMinutes) : undefined,
          });
          break;
        case 'partial':
          await planService.partial(id, {
            reason: reason || undefined,
            actualMinutes: actualMinutes ? Number(actualMinutes) : undefined,
            description: notes || undefined,
          });
          break;
        case 'postpone':
          await planService.postpone(id, {
            plannedStartAt: `${newDate}T${newTime || '09:00'}:00Z`,
            reason: reason || undefined,
          });
          break;
        case 'ignore':
          await planService.ignore(id, { reason: reason || undefined });
          break;
        case 'cancel':
          await planService.cancel(id, { reason: reason || undefined });
          break;
      }
      closeModal();
      await reload();
      toast.success('Ação registrada com sucesso');
    } catch (e: unknown) {
      const msg = (e as { body?: { error?: string } })?.body?.error ?? 'Não foi possível executar a ação.';
      toast.error('Algo não saiu como esperado.', msg);
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center">
        <Loader2 className="w-8 h-8 animate-spin text-primary" />
      </div>
    );
  }

  if (error || !plan) {
    return (
      <div className="min-h-screen bg-background py-8 px-6">
        <div className="max-w-4xl mx-auto">
          <p className="text-muted-foreground">{error ?? 'Plano não encontrado.'}</p>
          <button onClick={() => navigate('/plans')} className="mt-4 text-primary hover:underline text-sm">
            Voltar para planos
          </button>
        </div>
      </div>
    );
  }

  const isTerminal = TERMINAL.has(plan.status);

  const canStart = ['PENDING', 'SCHEDULED', 'DUE'].includes(plan.status);
  const canComplete = ['IN_PROGRESS', 'DUE', 'PENDING'].includes(plan.status);
  const canPartial = ['IN_PROGRESS', 'DUE', 'PENDING', 'MISSED'].includes(plan.status);
  const canPostpone = !isTerminal;
  const canIgnore = ['SCHEDULED', 'PENDING', 'DUE', 'MISSED', 'IN_PROGRESS'].includes(plan.status);
  const canCancel = !isTerminal;

  return (
    <div className="min-h-screen bg-background py-8 px-6">
      <div className="max-w-4xl mx-auto">
        <motion.button
          initial={{ opacity: 0, x: -20 }}
          animate={{ opacity: 1, x: 0 }}
          onClick={() => navigate('/plans')}
          className="flex items-center gap-2 text-muted-foreground hover:text-foreground transition-colors mb-8"
        >
          <ArrowLeft className="w-4 h-4" />
          Voltar para planos
        </motion.button>

        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} className="mb-8">
          <div className="flex items-start justify-between mb-6">
            <div className="flex-1">
              <div className="flex items-center gap-3 mb-3 flex-wrap">
                <span className="px-3 py-1 rounded-full text-xs font-medium bg-primary/20 text-primary">
                  {PLAN_STATUS_LABEL[plan.status]}
                </span>
                <span className="px-3 py-1 rounded-full text-xs font-medium bg-destructive/10 text-destructive flex items-center gap-1">
                  <Flag className="w-3 h-3" />
                  {PRIORITY_LABEL[plan.priority]}
                </span>
                {dir && (
                  <span className="px-3 py-1 rounded-full text-xs font-medium bg-muted" style={{ borderLeft: `3px solid ${dir.color ?? '#888'}` }}>
                    {dir.name}
                  </span>
                )}
              </div>
              <h1 className="text-3xl font-medium text-foreground mb-2">{plan.title}</h1>
              <div className="flex items-center gap-4 text-sm text-muted-foreground flex-wrap">
                {plan.plannedDate && (
                  <>
                    <span className="flex items-center gap-1">
                      <Calendar className="w-4 h-4" />
                      {new Date(plan.plannedDate + 'T12:00:00').toLocaleDateString('pt-BR', { day: 'numeric', month: 'long' })}
                    </span>
                    <span>•</span>
                  </>
                )}
                {plan.plannedStartAt && (
                  <>
                    <span className="flex items-center gap-1">
                      <Clock className="w-4 h-4" />
                      {new Date(plan.plannedStartAt).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })}
                    </span>
                    <span>•</span>
                  </>
                )}
                {plan.estimatedMinutes && (
                  <span className="flex items-center gap-1">
                    <Timer className="w-4 h-4" />
                    {plan.estimatedMinutes >= 60 ? `${Math.floor(plan.estimatedMinutes / 60)}h` : ''}{plan.estimatedMinutes % 60 > 0 ? `${plan.estimatedMinutes % 60}min` : ''}
                  </span>
                )}
              </div>
            </div>
            {!isTerminal && (
              <Button className="flex items-center gap-2" size="sm" onClick={openModify}>
                <Edit2 className="w-4 h-4" />
                Modificar
              </Button>
            )}
          </div>
        </motion.div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          <div className="lg:col-span-2 space-y-8">
            {plan.description && (
              <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.1 }}>
                <Card>
                  <div className="flex items-start gap-3 mb-4">
                    <div className="p-2 rounded-lg bg-secondary/10">
                      <Sparkles className="w-5 h-5 text-secondary" />
                    </div>
                    <div>
                      <h3 className="text-lg font-medium text-foreground mb-1">Intenção Original</h3>
                      <p className="text-sm text-muted-foreground">Por que este plano existe</p>
                    </div>
                  </div>
                  <p className="text-foreground leading-relaxed">{plan.description}</p>
                </Card>
              </motion.div>
            )}

            <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.2 }}>
              <Card>
                <h3 className="text-lg font-medium text-foreground mb-4">Execução</h3>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <p className="text-sm text-muted-foreground mb-1">Início planejado</p>
                    <p className="text-foreground">
                      {plan.plannedStartAt
                        ? new Date(plan.plannedStartAt).toLocaleString('pt-BR', { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' })
                        : '—'}
                    </p>
                  </div>
                  <div>
                    <p className="text-sm text-muted-foreground mb-1">Duração estimada</p>
                    <p className="text-foreground">
                      {plan.estimatedMinutes ? `${plan.estimatedMinutes}min` : '—'}
                    </p>
                  </div>
                  <div>
                    <p className="text-sm text-muted-foreground mb-1">Início real</p>
                    <p className="text-foreground">
                      {plan.startedAt
                        ? new Date(plan.startedAt).toLocaleString('pt-BR', { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' })
                        : '—'}
                    </p>
                  </div>
                  <div>
                    <p className="text-sm text-muted-foreground mb-1">Tempo real</p>
                    <p className="text-foreground">{plan.actualMinutes ? `${plan.actualMinutes}min` : '—'}</p>
                  </div>
                </div>
                {plan.reason && (
                  <div className="pt-4 border-t border-border mt-4">
                    <p className="text-sm text-muted-foreground mb-1">Motivo</p>
                    <p className="text-foreground text-sm italic">{plan.reason}</p>
                  </div>
                )}
              </Card>
            </motion.div>

            {events.length > 0 && (
              <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.3 }}>
                <h3 className="text-lg font-medium text-foreground mb-4">Histórico</h3>
                <div className="space-y-3">
                  {events.map((ev) => (
                    <Card key={ev.id} className="py-3">
                      <div className="flex items-start justify-between">
                        <div>
                          <p className="text-sm font-medium text-foreground">{ev.description ?? ev.eventType}</p>
                          {ev.fromStatus && ev.toStatus && (
                            <p className="text-xs text-muted-foreground mt-0.5">
                              {PLAN_STATUS_LABEL[ev.fromStatus]} → {PLAN_STATUS_LABEL[ev.toStatus]}
                            </p>
                          )}
                        </div>
                        <p className="text-xs text-muted-foreground shrink-0 ml-4">
                          {new Date(ev.createdAt).toLocaleString('pt-BR', { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' })}
                        </p>
                      </div>
                    </Card>
                  ))}
                </div>
              </motion.div>
            )}
          </div>

          <motion.div initial={{ opacity: 0, x: 20 }} animate={{ opacity: 1, x: 0 }} transition={{ delay: 0.2 }} className="space-y-6">
            <Card>
              <h4 className="text-sm font-medium text-foreground mb-4">Ações</h4>
              <div className="space-y-2">
                {canStart && (
                  <button
                    onClick={() => setActiveModal('start')}
                    className="w-full text-left px-3 py-2.5 rounded-lg hover:bg-primary/10 transition-colors text-sm text-primary font-medium flex items-center gap-2"
                  >
                    <Clock className="w-4 h-4" /> Iniciar agora
                  </button>
                )}
                {canComplete && (
                  <button
                    onClick={() => setActiveModal('complete')}
                    className="w-full text-left px-3 py-2.5 rounded-lg hover:bg-emerald-500/10 transition-colors text-sm text-emerald-600 dark:text-emerald-400 font-medium flex items-center gap-2"
                  >
                    <CheckCircle2 className="w-4 h-4" /> Marcar como concluído
                  </button>
                )}
                {canPartial && (
                  <button
                    onClick={() => setActiveModal('partial')}
                    className="w-full text-left px-3 py-2.5 rounded-lg hover:bg-muted transition-colors text-sm text-foreground flex items-center gap-2"
                  >
                    <Timer className="w-4 h-4" /> Feito parcialmente
                  </button>
                )}
                {canPostpone && (
                  <button
                    onClick={() => setActiveModal('postpone')}
                    className="w-full text-left px-3 py-2.5 rounded-lg hover:bg-muted transition-colors text-sm text-foreground flex items-center gap-2"
                  >
                    <Calendar className="w-4 h-4" /> Adiar para outra data
                  </button>
                )}
                {canIgnore && (
                  <button
                    onClick={() => setActiveModal('ignore')}
                    className="w-full text-left px-3 py-2.5 rounded-lg hover:bg-muted transition-colors text-sm text-muted-foreground flex items-center gap-2"
                  >
                    <SkipForward className="w-4 h-4" /> Ignorar por escolha
                  </button>
                )}
                {canCancel && (
                  <button
                    onClick={() => setActiveModal('cancel')}
                    className="w-full text-left px-3 py-2.5 rounded-lg hover:bg-destructive/10 transition-colors text-sm text-destructive/70 flex items-center gap-2"
                  >
                    <XCircle className="w-4 h-4" /> Cancelar plano
                  </button>
                )}
                {isTerminal && (
                  <p className="text-xs text-muted-foreground text-center py-2 italic">Plano encerrado</p>
                )}
              </div>
            </Card>

            {plan.tags && plan.tags.length > 0 && (
              <Card>
                <h4 className="text-sm font-medium text-foreground mb-3">Tags</h4>
                <div className="flex flex-wrap gap-2">
                  {plan.tags.map((tag) => (
                    <span key={tag} className="text-xs px-2.5 py-1 rounded-full bg-muted text-muted-foreground">{tag}</span>
                  ))}
                </div>
              </Card>
            )}

            <div className="bg-accent/5 border border-accent/20 rounded-lg p-4">
              <p className="text-sm text-muted-foreground italic">
                "Adaptar não é desistir. Modificar um plano é inteligência, não fracasso."
              </p>
            </div>
          </motion.div>
        </div>
      </div>

      {/* Modify Modal */}
      <AnimatePresence>
        {activeModal === 'modify' && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4"
            onClick={(e) => { if (e.target === e.currentTarget) closeModal(); }}
          >
            <motion.div
              initial={{ opacity: 0, scale: 0.95, y: 20 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.95, y: 20 }}
              className="bg-card border border-border rounded-2xl shadow-2xl w-full max-w-lg overflow-hidden"
            >
              <div className="px-6 py-4 border-b border-border">
                <h2 className="text-base font-semibold text-foreground">Modificar plano</h2>
                <p className="text-xs text-muted-foreground mt-0.5">Adaptar não é desistir.</p>
              </div>
              <div className="px-6 py-5 space-y-4 max-h-[70vh] overflow-y-auto">
                <div>
                  <label className="block text-sm font-medium text-foreground mb-1.5">Título *</label>
                  <input
                    type="text"
                    value={modTitle}
                    onChange={(e) => setModTitle(e.target.value)}
                    className="w-full px-3 py-2 rounded-lg bg-muted/50 border border-border text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-foreground mb-1.5">Descrição</label>
                  <textarea
                    value={modDescription}
                    onChange={(e) => setModDescription(e.target.value)}
                    rows={3}
                    className="w-full px-3 py-2 rounded-lg bg-muted/50 border border-border text-sm focus:outline-none focus:ring-2 focus:ring-ring resize-none"
                  />
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-foreground mb-1.5">Prioridade</label>
                    <select
                      value={modPriority}
                      onChange={(e) => setModPriority(e.target.value)}
                      className="w-full px-3 py-2 rounded-lg bg-muted/50 border border-border text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                    >
                      <option value="LOW">Baixa</option>
                      <option value="MEDIUM">Média</option>
                      <option value="HIGH">Alta</option>
                      <option value="CRITICAL">Crítica</option>
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-foreground mb-1.5">Duração estimada (min)</label>
                    <input
                      type="number"
                      value={modMinutes}
                      onChange={(e) => setModMinutes(e.target.value)}
                      className="w-full px-3 py-2 rounded-lg bg-muted/50 border border-border text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                    />
                  </div>
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-foreground mb-1.5">Data planejada</label>
                    <input
                      type="date"
                      value={modDate}
                      onChange={(e) => setModDate(e.target.value)}
                      className="w-full px-3 py-2 rounded-lg bg-muted/50 border border-border text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-foreground mb-1.5">Horário</label>
                    <input
                      type="time"
                      value={modTime}
                      onChange={(e) => setModTime(e.target.value)}
                      className="w-full px-3 py-2 rounded-lg bg-muted/50 border border-border text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                    />
                  </div>
                </div>
                <div>
                  <label className="block text-sm font-medium text-foreground mb-1.5">Motivo da modificação</label>
                  <textarea
                    value={modReason}
                    onChange={(e) => setModReason(e.target.value)}
                    rows={2}
                    placeholder="Por que este plano está sendo modificado?"
                    className="w-full px-3 py-2 rounded-lg bg-muted/50 border border-border text-sm focus:outline-none focus:ring-2 focus:ring-ring resize-none"
                  />
                </div>
                <div className="flex gap-3 pt-2">
                  <button
                    onClick={closeModal}
                    className="flex-1 py-2.5 rounded-lg border border-border text-sm text-muted-foreground hover:bg-muted transition-colors"
                  >
                    Cancelar
                  </button>
                  <button
                    onClick={handleModify}
                    disabled={submitting || !modTitle.trim()}
                    className="flex-1 py-2.5 rounded-lg bg-primary text-primary-foreground text-sm font-medium hover:bg-primary/90 transition-colors disabled:opacity-50"
                  >
                    {submitting ? 'Salvando...' : 'Salvar modificações'}
                  </button>
                </div>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Action Modals */}
      <AnimatePresence>
        {activeModal && activeModal !== 'modify' && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4"
            onClick={(e) => { if (e.target === e.currentTarget) closeModal(); }}
          >
            <motion.div
              initial={{ opacity: 0, scale: 0.95, y: 20 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.95, y: 20 }}
              className="bg-card border border-border rounded-2xl shadow-2xl w-full max-w-md overflow-hidden"
            >
              <div className="px-6 py-4 border-b border-border">
                <h2 className="text-base font-semibold text-foreground">
                  {activeModal === 'start' && 'Iniciar plano'}
                  {activeModal === 'complete' && 'Marcar como concluído'}
                  {activeModal === 'partial' && 'Feito parcialmente'}
                  {activeModal === 'postpone' && 'Adiar plano'}
                  {activeModal === 'ignore' && 'Ignorar por escolha'}
                  {activeModal === 'cancel' && 'Cancelar plano'}
                </h2>
              </div>
              <div className="px-6 py-5 space-y-4">
                {activeModal === 'start' && (
                  <p className="text-sm text-muted-foreground">O plano será marcado como Em andamento agora.</p>
                )}
                {(activeModal === 'complete' || activeModal === 'partial') && (
                  <>
                    <div>
                      <label className="block text-sm font-medium text-foreground mb-1.5">Tempo investido (min)</label>
                      <input
                        type="number"
                        value={actualMinutes}
                        onChange={(e) => setActualMinutes(e.target.value)}
                        placeholder={`${plan.estimatedMinutes ?? 60}`}
                        className="w-full px-3 py-2 rounded-lg bg-muted/50 border border-border text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-foreground mb-1.5">
                        {activeModal === 'partial' ? 'Motivo / o que foi feito' : 'Observações'}
                      </label>
                      <textarea
                        value={activeModal === 'partial' ? reason : notes}
                        onChange={(e) => activeModal === 'partial' ? setReason(e.target.value) : setNotes(e.target.value)}
                        rows={3}
                        className="w-full px-3 py-2 rounded-lg bg-muted/50 border border-border text-sm focus:outline-none focus:ring-2 focus:ring-ring resize-none"
                      />
                    </div>
                  </>
                )}
                {activeModal === 'postpone' && (
                  <>
                    <div>
                      <label className="block text-sm font-medium text-foreground mb-1.5">Nova data *</label>
                      <input
                        type="date"
                        value={newDate}
                        onChange={(e) => setNewDate(e.target.value)}
                        min={new Date().toISOString().split('T')[0]}
                        className="w-full px-3 py-2 rounded-lg bg-muted/50 border border-border text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                        required
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-foreground mb-1.5">Novo horário</label>
                      <input
                        type="time"
                        value={newTime}
                        onChange={(e) => setNewTime(e.target.value)}
                        className="w-full px-3 py-2 rounded-lg bg-muted/50 border border-border text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-foreground mb-1.5">Motivo</label>
                      <input
                        type="text"
                        value={reason}
                        onChange={(e) => setReason(e.target.value)}
                        className="w-full px-3 py-2 rounded-lg bg-muted/50 border border-border text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                      />
                    </div>
                  </>
                )}
                {(activeModal === 'ignore' || activeModal === 'cancel') && (
                  <div>
                    <label className="block text-sm font-medium text-foreground mb-1.5">Motivo (opcional)</label>
                    <textarea
                      value={reason}
                      onChange={(e) => setReason(e.target.value)}
                      rows={3}
                      className="w-full px-3 py-2 rounded-lg bg-muted/50 border border-border text-sm focus:outline-none focus:ring-2 focus:ring-ring resize-none"
                    />
                  </div>
                )}
                <div className="flex gap-3 pt-2">
                  <button
                    onClick={closeModal}
                    className="flex-1 py-2.5 rounded-lg border border-border text-sm text-muted-foreground hover:bg-muted transition-colors"
                  >
                    Cancelar
                  </button>
                  <button
                    onClick={handleAction}
                    disabled={submitting || (activeModal === 'postpone' && !newDate)}
                    className="flex-1 py-2.5 rounded-lg bg-primary text-primary-foreground text-sm font-medium hover:bg-primary/90 transition-colors disabled:opacity-50"
                  >
                    {submitting ? 'Salvando...' : 'Confirmar'}
                  </button>
                </div>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
