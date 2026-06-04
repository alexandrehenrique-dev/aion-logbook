import { Clock, Plus, X } from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { useEffect, useState } from 'react';
import { Button } from '../components/Button';
import { Card } from '../components/Card';
import { EmptyState } from '../components/EmptyState';
import { sessionService } from '../../services/sessionService';
import { directionService } from '../../services/directionService';
import type { SessionLog, Direction } from '../../types';
import type { CreateSessionRequest } from '../../services/sessionService';

type NewSessionForm = {
  directionId: string;
  startedAt: string;
  finishedAt: string;
  durationMinutes: number;
  result: string;
  notes: string;
};

const EMPTY_FORM: NewSessionForm = {
  directionId: '',
  startedAt: '',
  finishedAt: '',
  durationMinutes: 60,
  result: '',
  notes: '',
};

function formatMinutes(minutes: number) {
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  return h > 0 ? `${h}h ${m > 0 ? `${m}min` : ''}`.trim() : `${m}min`;
}

export function Sessions() {
  const [sessions, setSessions] = useState<SessionLog[]>([]);
  const [directions, setDirections] = useState<Direction[]>([]);
  const [loading, setLoading] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [form, setForm] = useState<NewSessionForm>(EMPTY_FORM);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    Promise.all([sessionService.list(), directionService.list()])
      .then(([s, d]) => { setSessions(s.data); setDirections(d); })
      .finally(() => setLoading(false));
  }, []);

  const dirMap = Object.fromEntries(directions.map((d) => [d.id, d]));

  const d = new Date();
  const today = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  const todaySessions = sessions.filter((s) => s.startedAt.startsWith(today));
  const todayMinutes = todaySessions.reduce((acc, s) => acc + s.durationMinutes, 0);
  const totalMinutes = sessions.reduce((acc, s) => acc + s.durationMinutes, 0);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      const payload: CreateSessionRequest = {
        directionId: form.directionId || undefined,
        startedAt: form.startedAt ? new Date(form.startedAt).toISOString() : new Date().toISOString(),
        finishedAt: form.finishedAt ? new Date(form.finishedAt).toISOString() : undefined,
        durationMinutes: form.durationMinutes,
        result: form.result || undefined,
        notes: form.notes || undefined,
      };
      const created = await sessionService.create(payload);
      setSessions((prev) => [created, ...prev]);
      setShowCreateModal(false);
      setForm(EMPTY_FORM);
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-background py-8 px-6">
        <div className="max-w-5xl mx-auto space-y-4">
          {[1, 2, 3].map((i) => <div key={i} className="h-32 bg-muted rounded-xl animate-pulse" />)}
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background py-8 px-6">
      <div className="max-w-5xl mx-auto">
        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} className="mb-8">
          <div className="flex items-center justify-between mb-2">
            <div>
              <h1 className="text-3xl font-medium text-foreground mb-2 flex items-center gap-3">
                <Clock className="w-8 h-8" />
                Sessões
              </h1>
              <p className="text-lg text-muted-foreground">Registro de tempo investido em suas direções</p>
            </div>
            <Button size="lg" className="flex items-center gap-2" onClick={() => setShowCreateModal(true)}>
              <Plus className="w-5 h-5" />
              Nova sessão
            </Button>
          </div>
        </motion.div>

        {/* Stats */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
          className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-12"
        >
          <Card className="text-center">
            <p className="text-sm text-muted-foreground mb-1">Hoje</p>
            <p className="text-3xl font-medium text-foreground">{todayMinutes > 0 ? formatMinutes(todayMinutes) : '—'}</p>
            <p className="text-xs text-muted-foreground mt-1">{todaySessions.length} sessões</p>
          </Card>
          <Card className="text-center">
            <p className="text-sm text-muted-foreground mb-1">Total investido</p>
            <p className="text-3xl font-medium text-foreground">{totalMinutes > 0 ? formatMinutes(totalMinutes) : '—'}</p>
          </Card>
          <Card className="text-center">
            <p className="text-sm text-muted-foreground mb-1">Total de sessões</p>
            <p className="text-3xl font-medium text-foreground">{sessions.length}</p>
          </Card>
        </motion.div>

        {/* Sessions list */}
        <div>
          <h2 className="text-xl font-medium text-foreground mb-6">Sessões Recentes</h2>
          {sessions.length === 0 ? (
            <EmptyState
              icon={Clock}
              title="Nenhuma sessão registrada"
              description="Quando você registrar uma sessão de trabalho, ela aparecerá aqui."
              action={{ label: 'Registrar sessão', onClick: () => setShowCreateModal(true) }}
            />
          ) : (
            <div className="space-y-4">
              {sessions.map((session, index) => {
                const dir = session.directionId ? dirMap[session.directionId] : null;
                return (
                  <motion.div
                    key={session.id}
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: 0.1 + index * 0.05 }}
                  >
                    <Card hover>
                      <div className="flex items-start justify-between gap-6">
                        <div className="flex-1">
                          <div className="flex items-center gap-3 mb-2 flex-wrap">
                            {dir && (
                              <span
                                className="text-xs px-2.5 py-1 rounded-full"
                                style={{ background: `${dir.color ?? '#6366f1'}20`, color: dir.color ?? '#6366f1' }}
                              >
                                {dir.name}
                              </span>
                            )}
                            <span className="text-xs text-muted-foreground">
                              {new Date(session.startedAt).toLocaleDateString('pt-BR', { day: 'numeric', month: 'short', year: 'numeric' })}
                            </span>
                          </div>
                          {session.result && (
                            <h3 className="text-base font-medium text-foreground mb-1">{session.result}</h3>
                          )}
                          {session.notes && (
                            <p className="text-sm text-muted-foreground mb-2 line-clamp-2">{session.notes}</p>
                          )}
                          <div className="flex items-center gap-4 text-sm text-muted-foreground">
                            <span className="flex items-center gap-1">
                              <Clock className="w-3 h-3" />
                              {new Date(session.startedAt).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })}
                              {session.finishedAt && ` - ${new Date(session.finishedAt).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })}`}
                            </span>
                          </div>
                        </div>
                        <div className="text-right shrink-0">
                          <p className="text-2xl font-medium text-foreground">{formatMinutes(session.durationMinutes)}</p>
                        </div>
                      </div>
                    </Card>
                  </motion.div>
                );
              })}
            </div>
          )}
        </div>

        <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ delay: 0.8 }} className="mt-12 text-center">
          <p className="text-sm text-muted-foreground italic">
            "Cada sessão é um passo na jornada. O tempo investido é prova de direção."
          </p>
        </motion.div>
      </div>

      {/* Create Session Modal */}
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
              className="bg-card border border-border rounded-2xl shadow-2xl w-full max-w-md overflow-hidden"
            >
              <div className="px-6 py-4 border-b border-border flex items-center justify-between">
                <h2 className="text-base font-semibold text-foreground">Registrar Sessão</h2>
                <button onClick={() => setShowCreateModal(false)} className="p-1.5 rounded-lg hover:bg-muted">
                  <X className="w-4 h-4 text-muted-foreground" />
                </button>
              </div>
              <form onSubmit={handleCreate} className="px-6 py-5 space-y-4">
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
                    <label className="block text-sm font-medium text-foreground mb-1.5">Início</label>
                    <input
                      type="datetime-local"
                      value={form.startedAt}
                      onChange={(e) => setForm((p) => ({ ...p, startedAt: e.target.value }))}
                      className="w-full px-3 py-2 rounded-lg bg-muted/50 border border-border text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-foreground mb-1.5">Fim</label>
                    <input
                      type="datetime-local"
                      value={form.finishedAt}
                      onChange={(e) => setForm((p) => ({ ...p, finishedAt: e.target.value }))}
                      className="w-full px-3 py-2 rounded-lg bg-muted/50 border border-border text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                    />
                  </div>
                </div>
                <div>
                  <label className="block text-sm font-medium text-foreground mb-1.5">Duração (min) *</label>
                  <input
                    type="number"
                    value={form.durationMinutes}
                    onChange={(e) => setForm((p) => ({ ...p, durationMinutes: Number(e.target.value) }))}
                    min={1}
                    required
                    className="w-full px-3 py-2 rounded-lg bg-muted/50 border border-border text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-foreground mb-1.5">Resultado</label>
                  <input
                    type="text"
                    value={form.result}
                    onChange={(e) => setForm((p) => ({ ...p, result: e.target.value }))}
                    placeholder="O que foi alcançado?"
                    className="w-full px-3 py-2 rounded-lg bg-muted/50 border border-border text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-foreground mb-1.5">Observações</label>
                  <textarea
                    value={form.notes}
                    onChange={(e) => setForm((p) => ({ ...p, notes: e.target.value }))}
                    rows={3}
                    className="w-full px-3 py-2 rounded-lg bg-muted/50 border border-border text-sm focus:outline-none focus:ring-2 focus:ring-ring resize-none"
                  />
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
                    disabled={saving}
                    className="flex-1 py-2.5 rounded-lg bg-primary text-primary-foreground text-sm font-medium hover:bg-primary/90 transition-colors disabled:opacity-50"
                  >
                    {saving ? 'Salvando...' : 'Registrar'}
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
