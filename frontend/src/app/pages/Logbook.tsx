import { BookMarked, Filter, Plus, Trash2, X } from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { useEffect, useState } from 'react';
import { Button } from '../components/Button';
import { Card } from '../components/Card';
import { logbookService } from '../../services/logbookService';
import { directionService } from '../../services/directionService';
import type { LogEntry, Direction } from '../../types';
import type { CreateLogEntryRequest } from '../../services/logbookService';

const LOG_TYPE_LABEL: Record<LogEntry['type'], string> = {
  REFLECTION: 'Reflexão',
  SYNTHESIS: 'Síntese',
  LEARNING: 'Aprendizado',
  BLOCKER: 'Bloqueio',
  DECISION: 'Decisão',
  IDEA: 'Ideia',
  FEEDBACK: 'Feedback',
};

const LOG_TYPE_COLOR: Record<LogEntry['type'], string> = {
  REFLECTION: 'bg-primary/10 text-primary',
  SYNTHESIS: 'bg-secondary/10 text-secondary',
  LEARNING: 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400',
  BLOCKER: 'bg-destructive/10 text-destructive',
  DECISION: 'bg-amber-500/10 text-amber-600 dark:text-amber-400',
  IDEA: 'bg-purple-500/10 text-purple-600 dark:text-purple-400',
  FEEDBACK: 'bg-muted text-muted-foreground',
};

type NewLogForm = {
  title: string;
  content: string;
  type: LogEntry['type'];
  directionId: string;
  tags: string;
};

const EMPTY_FORM: NewLogForm = {
  title: '',
  content: '',
  type: 'REFLECTION',
  directionId: '',
  tags: '',
};

export function Logbook() {
  const [entries, setEntries] = useState<LogEntry[]>([]);
  const [directions, setDirections] = useState<Direction[]>([]);
  const [loading, setLoading] = useState(true);
  const [filterType, setFilterType] = useState<string>('all');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [editingEntry, setEditingEntry] = useState<LogEntry | null>(null);
  const [form, setForm] = useState<NewLogForm>(EMPTY_FORM);
  const [saving, setSaving] = useState(false);
  const [deleting, setDeleting] = useState<string | null>(null);

  useEffect(() => {
    Promise.all([logbookService.list(), directionService.list()])
      .then(([e, d]) => { setEntries(e); setDirections(d); })
      .finally(() => setLoading(false));
  }, []);

  const dirMap = Object.fromEntries(directions.map((d) => [d.id, d]));

  const filtered = entries.filter((e) => filterType === 'all' || e.type === filterType);

  const openCreate = () => {
    setEditingEntry(null);
    setForm(EMPTY_FORM);
    setShowCreateModal(true);
  };

  const openEdit = (entry: LogEntry) => {
    setEditingEntry(entry);
    setForm({
      title: entry.title,
      content: entry.content,
      type: entry.type,
      directionId: entry.directionId ?? '',
      tags: entry.tags?.join(', ') ?? '',
    });
    setShowCreateModal(true);
  };

  const closeModal = () => {
    setShowCreateModal(false);
    setEditingEntry(null);
    setForm(EMPTY_FORM);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.title.trim() || !form.content.trim()) return;
    setSaving(true);
    try {
      const payload: CreateLogEntryRequest = {
        title: form.title,
        content: form.content,
        type: form.type,
        directionId: form.directionId || undefined,
        tags: form.tags ? form.tags.split(',').map((t) => t.trim()).filter(Boolean) : undefined,
      };
      if (editingEntry) {
        const updated = await logbookService.update(editingEntry.id, payload);
        setEntries((prev) => prev.map((e) => (e.id === editingEntry.id ? updated : e)));
      } else {
        const created = await logbookService.create(payload);
        setEntries((prev) => [created, ...prev]);
      }
      closeModal();
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm('Remover este registro do logbook?')) return;
    setDeleting(id);
    try {
      await logbookService.delete(id);
      setEntries((prev) => prev.filter((e) => e.id !== id));
    } finally {
      setDeleting(null);
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
          <div className="flex items-center justify-between mb-6">
            <div>
              <h1 className="text-3xl font-medium text-foreground flex items-center gap-3 mb-2">
                <BookMarked className="w-8 h-8" />
                Logbook
              </h1>
              <p className="text-muted-foreground">Registro vivo da sua jornada</p>
            </div>
            <Button size="lg" className="flex items-center gap-2" onClick={openCreate}>
              <Plus className="w-5 h-5" />
              Novo registro
            </Button>
          </div>

          <Card>
            <div className="flex items-center gap-2 flex-wrap">
              <Filter className="w-4 h-4 text-muted-foreground shrink-0" />
              <button
                onClick={() => setFilterType('all')}
                className={`text-xs px-3 py-1.5 rounded-full transition-colors ${filterType === 'all' ? 'bg-primary text-primary-foreground' : 'bg-muted text-muted-foreground hover:bg-muted/70'}`}
              >
                Todos ({entries.length})
              </button>
              {(Object.keys(LOG_TYPE_LABEL) as LogEntry['type'][]).map((t) => {
                const count = entries.filter((e) => e.type === t).length;
                if (count === 0) return null;
                return (
                  <button
                    key={t}
                    onClick={() => setFilterType(t)}
                    className={`text-xs px-3 py-1.5 rounded-full transition-colors ${filterType === t ? 'bg-primary text-primary-foreground' : 'bg-muted text-muted-foreground hover:bg-muted/70'}`}
                  >
                    {LOG_TYPE_LABEL[t]} ({count})
                  </button>
                );
              })}
            </div>
          </Card>
        </motion.div>

        <div className="space-y-4">
          {filtered.length === 0 ? (
            <Card>
              <div className="text-center py-12">
                <p className="text-muted-foreground mb-2">Nenhum registro encontrado</p>
                <p className="text-sm text-muted-foreground/70 italic">
                  O logbook é onde a jornada ganha permanência. Comece registrando.
                </p>
              </div>
            </Card>
          ) : (
            filtered.map((entry, index) => {
              const dir = entry.directionId ? dirMap[entry.directionId] : null;
              return (
                <motion.div
                  key={entry.id}
                  initial={{ opacity: 0, y: 16 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: index * 0.04 }}
                >
                  <Card hover>
                    <div className="flex items-start justify-between gap-4">
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2 mb-2 flex-wrap">
                          <span className={`text-xs px-2.5 py-1 rounded-full font-medium ${LOG_TYPE_COLOR[entry.type]}`}>
                            {LOG_TYPE_LABEL[entry.type]}
                          </span>
                          {dir && (
                            <span className="text-xs px-2.5 py-1 rounded-full bg-muted text-muted-foreground flex items-center gap-1">
                              <span className="w-1.5 h-1.5 rounded-full" style={{ background: dir.color ?? '#888' }} />
                              {dir.name}
                            </span>
                          )}
                          <span className="text-xs text-muted-foreground ml-auto">
                            {new Date(entry.createdAt).toLocaleDateString('pt-BR', { day: 'numeric', month: 'short', year: 'numeric' })}
                          </span>
                        </div>
                        <h3
                          className="text-base font-medium text-foreground mb-2 cursor-pointer hover:text-primary transition-colors"
                          onClick={() => openEdit(entry)}
                        >
                          {entry.title}
                        </h3>
                        <p className="text-sm text-muted-foreground leading-relaxed line-clamp-3">{entry.content}</p>
                        {entry.tags && entry.tags.length > 0 && (
                          <div className="flex flex-wrap gap-1.5 mt-3">
                            {entry.tags.map((tag) => (
                              <span key={tag} className="text-xs px-2 py-0.5 rounded-full bg-muted text-muted-foreground">#{tag}</span>
                            ))}
                          </div>
                        )}
                      </div>
                      <div className="flex flex-col gap-1 shrink-0">
                        <button
                          onClick={() => openEdit(entry)}
                          className="p-1.5 rounded-lg hover:bg-muted text-muted-foreground hover:text-foreground transition-colors text-xs"
                        >
                          Editar
                        </button>
                        <button
                          onClick={() => handleDelete(entry.id)}
                          disabled={deleting === entry.id}
                          className="p-1.5 rounded-lg hover:bg-destructive/10 text-muted-foreground hover:text-destructive transition-colors"
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                        </button>
                      </div>
                    </div>
                  </Card>
                </motion.div>
              );
            })
          )}
        </div>
      </div>

      {/* Create / Edit Modal */}
      <AnimatePresence>
        {showCreateModal && (
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
              className="bg-card border border-border rounded-2xl shadow-2xl w-full max-w-lg overflow-hidden max-h-[90vh] overflow-y-auto"
            >
              <div className="px-6 py-4 border-b border-border flex items-center justify-between sticky top-0 bg-card z-10">
                <h2 className="text-base font-semibold text-foreground">
                  {editingEntry ? 'Editar registro' : 'Novo registro'}
                </h2>
                <button onClick={closeModal} className="p-1.5 rounded-lg hover:bg-muted">
                  <X className="w-4 h-4 text-muted-foreground" />
                </button>
              </div>
              <form onSubmit={handleSubmit} className="px-6 py-5 space-y-4">
                <div>
                  <label className="block text-sm font-medium text-foreground mb-1.5">Título *</label>
                  <input
                    type="text"
                    value={form.title}
                    onChange={(e) => setForm((p) => ({ ...p, title: e.target.value }))}
                    placeholder="Nomeie este momento..."
                    className="w-full px-3 py-2 rounded-lg bg-muted/50 border border-border text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                    required
                    autoFocus
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-foreground mb-1.5">Conteúdo *</label>
                  <textarea
                    value={form.content}
                    onChange={(e) => setForm((p) => ({ ...p, content: e.target.value }))}
                    placeholder="O que você quer registrar?"
                    rows={5}
                    className="w-full px-3 py-2 rounded-lg bg-muted/50 border border-border text-sm focus:outline-none focus:ring-2 focus:ring-ring resize-none"
                    required
                  />
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-foreground mb-1.5">Tipo</label>
                    <select
                      value={form.type}
                      onChange={(e) => setForm((p) => ({ ...p, type: e.target.value as LogEntry['type'] }))}
                      className="w-full px-3 py-2 rounded-lg bg-muted/50 border border-border text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                    >
                      {(Object.keys(LOG_TYPE_LABEL) as LogEntry['type'][]).map((t) => (
                        <option key={t} value={t}>{LOG_TYPE_LABEL[t]}</option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-foreground mb-1.5">Direção</label>
                    <select
                      value={form.directionId}
                      onChange={(e) => setForm((p) => ({ ...p, directionId: e.target.value }))}
                      className="w-full px-3 py-2 rounded-lg bg-muted/50 border border-border text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                    >
                      <option value="">Nenhuma</option>
                      {directions.filter((d) => d.status === 'ACTIVE').map((d) => (
                        <option key={d.id} value={d.id}>{d.name}</option>
                      ))}
                    </select>
                  </div>
                </div>
                <div>
                  <label className="block text-sm font-medium text-foreground mb-1.5">Tags (separadas por vírgula)</label>
                  <input
                    type="text"
                    value={form.tags}
                    onChange={(e) => setForm((p) => ({ ...p, tags: e.target.value }))}
                    placeholder="reflexão, tempo, presença..."
                    className="w-full px-3 py-2 rounded-lg bg-muted/50 border border-border text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                  />
                </div>
                <div className="flex gap-3 pt-2">
                  <button
                    type="button"
                    onClick={closeModal}
                    className="flex-1 py-2.5 rounded-lg border border-border text-sm text-muted-foreground hover:bg-muted transition-colors"
                  >
                    Cancelar
                  </button>
                  <button
                    type="submit"
                    disabled={saving}
                    className="flex-1 py-2.5 rounded-lg bg-primary text-primary-foreground text-sm font-medium hover:bg-primary/90 transition-colors disabled:opacity-50"
                  >
                    {saving ? 'Salvando...' : editingEntry ? 'Salvar' : 'Criar registro'}
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
