import { useState, type FormEvent } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { Bug, X } from 'lucide-react';
import { bugReportService } from '../../services/bugReportService';
import type { BugReportSeverity } from '../../services/bugReportService';
import { toast } from '../../utils/toast';

const SEVERITY_LABELS: Record<BugReportSeverity, string> = {
  LOW: 'Baixa',
  MEDIUM: 'Média',
  HIGH: 'Alta',
  CRITICAL: 'Crítica',
};

type BugReportForm = {
  title: string;
  description: string;
  severity: BugReportSeverity;
  context: string;
};

type Props = { onClose: () => void };

export function BugReportModal({ onClose }: Props) {
  const [form, setForm] = useState<BugReportForm>({
    title: '',
    description: '',
    severity: 'MEDIUM',
    context: window.location.pathname,
  });
  const [status, setStatus] = useState<'idle' | 'submitting' | 'success' | 'error'>('idle');

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!form.title.trim() || !form.description.trim()) return;

    setStatus('submitting');
    try {
      await bugReportService.create({ ...form, url: window.location.href, timestamp: new Date().toISOString() });
      setStatus('success');
      toast.success('Bug report enviado', 'Obrigado pelo feedback!');
      setTimeout(onClose, 1500);
    } catch {
      setStatus('error');
      toast.error('Erro ao enviar report');
    }
  };

  return (
    <AnimatePresence>
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4"
        onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}
      >
        <motion.div
          initial={{ opacity: 0, scale: 0.95, y: 20 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          exit={{ opacity: 0, scale: 0.95, y: 20 }}
          transition={{ duration: 0.2 }}
          className="bg-card border border-border rounded-2xl shadow-2xl w-full max-w-md overflow-hidden"
        >
          <div className="px-6 py-4 border-b border-border flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Bug className="w-5 h-5 text-primary" />
              <h2 className="text-base font-semibold text-foreground">Reportar bug</h2>
            </div>
            <button onClick={onClose} className="p-1.5 rounded-lg hover:bg-muted transition-colors">
              <X className="w-4 h-4 text-muted-foreground" />
            </button>
          </div>

          {status === 'success' ? (
            <div className="px-6 py-10 text-center">
              <p className="text-lg font-medium text-foreground mb-2">Obrigado!</p>
              <p className="text-sm text-muted-foreground">Seu report foi registrado.</p>
            </div>
          ) : (
            <form onSubmit={handleSubmit} className="px-6 py-5 space-y-4">
              <div>
                <label className="block text-sm font-medium text-foreground mb-1.5">Título</label>
                <input
                  type="text"
                  value={form.title}
                  onChange={(e) => setForm((p) => ({ ...p, title: e.target.value }))}
                  placeholder="Descreva brevemente o problema"
                  className="w-full px-3 py-2 rounded-lg bg-muted/50 border border-border text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-foreground mb-1.5">Descrição</label>
                <textarea
                  value={form.description}
                  onChange={(e) => setForm((p) => ({ ...p, description: e.target.value }))}
                  placeholder="O que aconteceu? O que você esperava que acontecesse?"
                  rows={4}
                  className="w-full px-3 py-2 rounded-lg bg-muted/50 border border-border text-sm focus:outline-none focus:ring-2 focus:ring-ring resize-none"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-foreground mb-1.5">Severidade</label>
                <div className="grid grid-cols-4 gap-2">
                  {(Object.keys(SEVERITY_LABELS) as BugReportSeverity[]).map((s) => (
                    <button
                      key={s}
                      type="button"
                      onClick={() => setForm((p) => ({ ...p, severity: s }))}
                      className={`py-1.5 rounded-lg text-xs font-medium border transition-colors ${
                        form.severity === s
                          ? 'bg-primary text-primary-foreground border-primary'
                          : 'bg-muted/30 text-muted-foreground border-border hover:border-primary/50'
                      }`}
                    >
                      {SEVERITY_LABELS[s]}
                    </button>
                  ))}
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-foreground mb-1.5">Contexto / Tela</label>
                <input
                  type="text"
                  value={form.context}
                  onChange={(e) => setForm((p) => ({ ...p, context: e.target.value }))}
                  className="w-full px-3 py-2 rounded-lg bg-muted/50 border border-border text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                />
              </div>

              {status === 'error' && (
                <p className="text-sm text-destructive">Erro ao enviar. Tente novamente.</p>
              )}

              <div className="flex gap-3 pt-2">
                <button
                  type="button"
                  onClick={onClose}
                  className="flex-1 py-2.5 rounded-lg border border-border text-sm text-muted-foreground hover:bg-muted transition-colors"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  disabled={status === 'submitting'}
                  className="flex-1 py-2.5 rounded-lg bg-primary text-primary-foreground text-sm font-medium hover:bg-primary/90 transition-colors disabled:opacity-50"
                >
                  {status === 'submitting' ? 'Enviando...' : 'Enviar report'}
                </button>
              </div>
            </form>
          )}
        </motion.div>
      </motion.div>
    </AnimatePresence>
  );
}
