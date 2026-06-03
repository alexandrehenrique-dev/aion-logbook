import { ArrowUp, Compass, Plus, X } from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';
import { Button } from '../components/Button';
import { Card } from '../components/Card';
import { directionService } from '../../services/directionService';
import type { Direction } from '../../types';
import type { CreateDirectionRequest } from '../../services/directionService';

const DIRECTION_COLORS = [
  '#6366f1', '#0ea5e9', '#f59e0b', '#22c55e', '#8b5cf6',
  '#ec4899', '#f97316', '#14b8a6', '#ef4444', '#a855f7',
];

type NewDirForm = {
  name: string;
  description: string;
  color: string;
  icon: string;
  identityPhrase: string;
};

const EMPTY_FORM: NewDirForm = {
  name: '',
  description: '',
  color: DIRECTION_COLORS[0],
  icon: '',
  identityPhrase: '',
};

export function Directions() {
  const navigate = useNavigate();
  const [directions, setDirections] = useState<Direction[]>([]);
  const [loading, setLoading] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [form, setForm] = useState<NewDirForm>(EMPTY_FORM);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    directionService.list()
      .then(setDirections)
      .finally(() => setLoading(false));
  }, []);

  const activeDirections = directions.filter((d) => d.status === 'ACTIVE');

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.name.trim()) return;
    setSaving(true);
    try {
      const payload: CreateDirectionRequest = {
        name: form.name,
        description: form.description || undefined,
        color: form.color,
        icon: form.icon || undefined,
        identityPhrase: form.identityPhrase || undefined,
        status: 'ACTIVE',
      };
      const created = await directionService.create(payload);
      setDirections((prev) => [...prev, created]);
      setShowCreateModal(false);
      setForm(EMPTY_FORM);
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-background py-8 px-6">
        <div className="max-w-6xl mx-auto grid grid-cols-1 md:grid-cols-2 gap-6">
          {[1, 2, 3, 4].map((i) => (
            <div key={i} className="h-48 bg-muted rounded-xl animate-pulse" />
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background py-8 px-6">
      <div className="max-w-6xl mx-auto">
        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} className="mb-12">
          <div className="flex items-start justify-between mb-2">
            <div>
              <h1 className="text-3xl font-medium text-foreground flex items-center gap-3 mb-2">
                <Compass className="w-8 h-8" />
                Direções
              </h1>
              <p className="text-lg text-muted-foreground">
                Os eixos da sua vida, caminhos e áreas existenciais
              </p>
            </div>
            <Button size="lg" className="flex items-center gap-2" onClick={() => setShowCreateModal(true)}>
              <Plus className="w-5 h-5" />
              Nova Direção
            </Button>
          </div>
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
              <p className="text-sm text-muted-foreground mb-2">Direções Ativas</p>
              <p className="text-4xl font-medium text-foreground">{activeDirections.length}</p>
            </div>
          </Card>
          <Card className="bg-gradient-to-br from-accent/5 to-transparent border-accent/20">
            <div className="text-center">
              <p className="text-sm text-muted-foreground mb-2">Total criadas</p>
              <p className="text-4xl font-medium text-foreground">{directions.length}</p>
            </div>
          </Card>
          <Card className="bg-gradient-to-br from-secondary/5 to-transparent border-secondary/20">
            <div className="text-center">
              <p className="text-sm text-muted-foreground mb-2">Arquivadas</p>
              <p className="text-4xl font-medium text-foreground">{directions.filter((d) => d.status === 'ARCHIVED').length}</p>
            </div>
          </Card>
        </motion.div>

        {activeDirections.length === 0 ? (
          <Card>
            <div className="text-center py-12">
              <p className="text-muted-foreground mb-2">Nenhuma direção ainda</p>
              <p className="text-sm text-muted-foreground/70 italic mb-4">
                Toda jornada começa com um primeiro eixo.
              </p>
              <Button onClick={() => setShowCreateModal(true)}>Criar primeira direção</Button>
            </div>
          </Card>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {activeDirections.map((direction, index) => (
              <motion.div
                key={direction.id}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.2 + index * 0.08 }}
                onClick={() => navigate(`/directions/${direction.id}`)}
              >
                <Card hover className="cursor-pointer h-full">
                  <div className="flex flex-col h-full">
                    <div className="flex items-start gap-4 mb-4">
                      <div
                        className="w-12 h-12 rounded-xl shrink-0 flex items-center justify-center text-white text-xl font-bold"
                        style={{ background: direction.color ?? '#6366f1' }}
                      >
                        {direction.name.charAt(0)}
                      </div>
                      <div className="flex-1">
                        <h3 className="text-xl font-medium text-foreground mb-1">{direction.name}</h3>
                        {direction.description && (
                          <p className="text-sm text-muted-foreground">{direction.description}</p>
                        )}
                      </div>
                      <ArrowUp className="w-4 h-4 text-muted-foreground shrink-0 mt-1" />
                    </div>

                    {direction.identityPhrase && (
                      <p className="text-sm text-muted-foreground italic mb-4 pl-16">
                        "{direction.identityPhrase}"
                      </p>
                    )}

                    <div className="mt-auto pt-4 border-t border-border">
                      <div className="flex items-center gap-2">
                        <div className="w-2 h-2 rounded-full bg-emerald-500" />
                        <p className="text-xs text-muted-foreground">Ativa desde {new Date(direction.createdAt).toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' })}</p>
                      </div>
                    </div>
                  </div>
                </Card>
              </motion.div>
            ))}
          </div>
        )}

        <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ delay: 1 }} className="mt-12 text-center">
          <p className="text-sm text-muted-foreground italic">
            "Cada direção é uma parte viva da sua jornada. Continue cultivando."
          </p>
        </motion.div>
      </div>

      {/* Create Direction Modal */}
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
                <h2 className="text-base font-semibold text-foreground">Nova Direção</h2>
                <button onClick={() => setShowCreateModal(false)} className="p-1.5 rounded-lg hover:bg-muted">
                  <X className="w-4 h-4 text-muted-foreground" />
                </button>
              </div>
              <form onSubmit={handleCreate} className="px-6 py-5 space-y-4">
                <div>
                  <label className="block text-sm font-medium text-foreground mb-1.5">Nome *</label>
                  <input
                    type="text"
                    value={form.name}
                    onChange={(e) => setForm((p) => ({ ...p, name: e.target.value }))}
                    placeholder="Ex: Estudos, Saúde, Carreira..."
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
                    placeholder="O que esta direção representa para você?"
                    rows={2}
                    className="w-full px-3 py-2 rounded-lg bg-muted/50 border border-border text-sm focus:outline-none focus:ring-2 focus:ring-ring resize-none"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-foreground mb-1.5">Frase de identidade</label>
                  <input
                    type="text"
                    value={form.identityPhrase}
                    onChange={(e) => setForm((p) => ({ ...p, identityPhrase: e.target.value }))}
                    placeholder="Uma frase que captura sua intenção..."
                    className="w-full px-3 py-2 rounded-lg bg-muted/50 border border-border text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-foreground mb-2">Cor</label>
                  <div className="flex flex-wrap gap-2">
                    {DIRECTION_COLORS.map((c) => (
                      <button
                        key={c}
                        type="button"
                        onClick={() => setForm((p) => ({ ...p, color: c }))}
                        className={`w-7 h-7 rounded-full transition-transform ${form.color === c ? 'scale-125 ring-2 ring-offset-2 ring-foreground' : 'hover:scale-110'}`}
                        style={{ background: c }}
                      />
                    ))}
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
                    disabled={saving || !form.name.trim()}
                    className="flex-1 py-2.5 rounded-lg bg-primary text-primary-foreground text-sm font-medium hover:bg-primary/90 transition-colors disabled:opacity-50"
                  >
                    {saving ? 'Criando...' : 'Criar direção'}
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
