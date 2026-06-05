import { Bell, CheckCircle2, Globe, Palette, Settings as SettingsIcon, Smartphone, User } from 'lucide-react';
import { motion } from 'motion/react';
import { useEffect, useState } from 'react';
import { Card } from '../components/Card';
import { Input } from '../components/Input';
import { ThemeToggle } from '../components/ThemeToggle';
import { settingsService } from '../../services/settingsService';
import { useAuth } from '../../features/auth/AuthContext';
import type { UserSettings } from '../../services/settingsService';
import { browserNotificationService } from '../../services/browserNotificationService';
import { toast } from '../../utils/toast';

export function Settings() {
  const { user } = useAuth();

  const [settings, setSettings] = useState<UserSettings>({
    timezone: 'America/Sao_Paulo',
    theme: 'system',
    defaultPlanDuration: 60,
    notificationsEnabled: true,
    notificationLeadMinutes: 15,
    language: 'pt-BR',
  });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [saved, setSaved] = useState(false);
  const [browserPermission, setBrowserPermission] = useState<NotificationPermission | 'unsupported'>('default');

  useEffect(() => {
    settingsService.get()
      .then(setSettings)
      .finally(() => setLoading(false));
    setBrowserPermission(browserNotificationService.getPermission());
  }, []);

  const handleSave = async () => {
    setSaving(true);
    try {
      const updated = await settingsService.update(settings);
      setSettings(updated);
      setSaved(true);
      toast.success('Configurações salvas com sucesso.');
      setTimeout(() => setSaved(false), 2500);
    } catch {
      toast.error('Não conseguimos salvar as configurações agora.');
    } finally {
      setSaving(false);
    }
  };

  const handleRequestBrowserPermission = async () => {
    const perm = await browserNotificationService.requestPermission();
    setBrowserPermission(perm);
    if (perm === 'granted') toast.success('Notificações ativadas', 'Você será avisado mesmo fora da aba.');
    else if (perm === 'denied') toast.error('Permissão negada', 'Ative nas configurações do navegador para receber avisos.');
  };

  const updateSetting = <K extends keyof UserSettings>(key: K, value: UserSettings[K]) => {
    setSettings((prev) => ({ ...prev, [key]: value }));
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-background py-8 px-6">
        <div className="max-w-4xl mx-auto space-y-6">
          {[1, 2, 3].map((i) => <div key={i} className="h-48 bg-muted rounded-xl animate-pulse" />)}
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background py-8 px-6">
      <div className="max-w-4xl mx-auto">
        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} className="mb-12">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-medium text-foreground mb-2 flex items-center gap-3">
                <SettingsIcon className="w-8 h-8" />
                Configurações
              </h1>
              <p className="text-lg text-muted-foreground">Personalize sua experiência no Aion Logbook</p>
            </div>
            <button
              onClick={handleSave}
              disabled={saving}
              className="flex items-center gap-2 px-4 py-2 rounded-lg bg-primary text-primary-foreground text-sm font-medium hover:bg-primary/90 transition-colors disabled:opacity-50"
            >
              {saved ? <><CheckCircle2 className="w-4 h-4" /> Salvo!</> : saving ? 'Salvando...' : 'Salvar alterações'}
            </button>
          </div>
        </motion.div>

        <div className="space-y-8">
          {/* Profile */}
          <motion.section initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.1 }}>
            <Card>
              <div className="flex items-center gap-3 mb-6">
                <div className="p-2 rounded-lg bg-primary/10">
                  <User className="w-5 h-5 text-primary" />
                </div>
                <div>
                  <h2 className="text-lg font-medium text-foreground">Perfil</h2>
                  <p className="text-sm text-muted-foreground">Informações básicas da sua conta</p>
                </div>
              </div>
              <div className="space-y-6 max-w-md">
                <Input label="Nome" value={user?.name ?? ''} disabled placeholder="Seu nome" />
                <Input label="Email" type="email" value={user?.email ?? ''} disabled placeholder="seu@email.com" />
                <div className="pt-4">
                  <button
                    type="button"
                    onClick={() => alert('Alteração de senha gerenciada pelo Keycloak em modo de produção.')}
                    className="text-sm text-primary hover:underline"
                  >
                    Alterar senha
                  </button>
                  <p className="text-xs text-muted-foreground mt-1">Gerenciado pelo Keycloak em produção</p>
                </div>
              </div>
            </Card>
          </motion.section>

          {/* Appearance */}
          <motion.section initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.2 }}>
            <Card>
              <div className="flex items-center gap-3 mb-6">
                <div className="p-2 rounded-lg bg-secondary/10">
                  <Palette className="w-5 h-5 text-secondary" />
                </div>
                <div>
                  <h2 className="text-lg font-medium text-foreground">Aparência</h2>
                  <p className="text-sm text-muted-foreground">Personalize o visual da interface</p>
                </div>
              </div>
              <div className="space-y-6">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm font-medium text-foreground mb-1">Tema</p>
                    <p className="text-sm text-muted-foreground">Alternar entre modo claro e escuro</p>
                  </div>
                  <ThemeToggle />
                </div>
                <div className="pt-4 border-t border-border">
                  <div>
                    <label className="block text-sm font-medium text-foreground mb-2">Duração padrão de planos (min)</label>
                    <input
                      type="number"
                      value={settings.defaultPlanDuration ?? 60}
                      onChange={(e) => updateSetting('defaultPlanDuration', Number(e.target.value))}
                      min={5}
                      max={480}
                      className="w-32 px-3 py-2 rounded-lg bg-muted/50 border border-border text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                    />
                  </div>
                </div>
              </div>
            </Card>
          </motion.section>

          {/* Notifications */}
          <motion.section initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.3 }}>
            <Card>
              <div className="flex items-center gap-3 mb-6">
                <div className="p-2 rounded-lg bg-accent/10">
                  <Bell className="w-5 h-5 text-accent" />
                </div>
                <div>
                  <h2 className="text-lg font-medium text-foreground">Notificações</h2>
                  <p className="text-sm text-muted-foreground">Gerencie como você quer ser lembrado</p>
                </div>
              </div>
              <div className="space-y-4">
                <div className="flex items-center justify-between py-3">
                  <div>
                    <p className="text-sm font-medium text-foreground">Ativar notificações</p>
                    <p className="text-sm text-muted-foreground">Avisar antes de cada plano</p>
                  </div>
                  <label className="relative inline-block w-12 h-6 cursor-pointer">
                    <input
                      type="checkbox"
                      className="sr-only peer"
                      checked={settings.notificationsEnabled ?? true}
                      onChange={(e) => updateSetting('notificationsEnabled', e.target.checked)}
                    />
                    <div className="w-full h-full bg-muted rounded-full peer-checked:bg-primary transition-colors" />
                    <div className="absolute left-1 top-1 w-4 h-4 bg-white rounded-full transition-transform peer-checked:translate-x-6" />
                  </label>
                </div>
                {settings.notificationsEnabled && (
                  <div className="py-3 border-t border-border">
                    <label className="block text-sm font-medium text-foreground mb-2">
                      Antecedência (minutos)
                    </label>
                    <input
                      type="number"
                      value={settings.notificationLeadMinutes ?? 15}
                      onChange={(e) => updateSetting('notificationLeadMinutes', Number(e.target.value))}
                      min={1}
                      max={120}
                      className="w-24 px-3 py-2 rounded-lg bg-muted/50 border border-border text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                    />
                  </div>
                )}
                {browserPermission !== 'unsupported' && (
                  <div className="pt-3 border-t border-border">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <Smartphone className="w-4 h-4 text-muted-foreground" />
                        <div>
                          <p className="text-sm font-medium text-foreground">Notificações do navegador</p>
                          <p className="text-xs text-muted-foreground">
                            {browserPermission === 'granted' ? 'Ativadas' : browserPermission === 'denied' ? 'Bloqueadas pelo navegador' : 'Não solicitadas'}
                          </p>
                        </div>
                      </div>
                      {browserPermission !== 'granted' && browserPermission !== 'denied' && (
                        <button
                          onClick={handleRequestBrowserPermission}
                          className="text-xs px-3 py-1.5 rounded-lg bg-primary/10 text-primary hover:bg-primary/20 transition-colors"
                        >
                          Ativar
                        </button>
                      )}
                      {browserPermission === 'granted' && (
                        <span className="text-xs text-emerald-600 dark:text-emerald-400 font-medium">✓ Ativas</span>
                      )}
                    </div>
                  </div>
                )}
                <div className="pt-2 border-t border-border">
                  <p className="text-xs text-muted-foreground">Lembretes são gentis e nunca agressivos.</p>
                </div>
              </div>
            </Card>
          </motion.section>

          {/* Language & Region */}
          <motion.section initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.4 }}>
            <Card>
              <div className="flex items-center gap-3 mb-6">
                <div className="p-2 rounded-lg bg-muted">
                  <Globe className="w-5 h-5 text-muted-foreground" />
                </div>
                <div>
                  <h2 className="text-lg font-medium text-foreground">Idioma e Região</h2>
                  <p className="text-sm text-muted-foreground">Formato de data, hora e idioma</p>
                </div>
              </div>
              <div className="space-y-4 max-w-md">
                <div>
                  <label className="block text-sm mb-2 text-foreground/80">Fuso horário</label>
                  <select
                    value={settings.timezone ?? 'America/Sao_Paulo'}
                    onChange={(e) => updateSetting('timezone', e.target.value)}
                    className="w-full px-4 py-2.5 rounded-lg bg-muted/50 border border-border text-foreground text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                  >
                    <option value="America/Sao_Paulo">America/Sao_Paulo (GMT-3)</option>
                    <option value="America/New_York">America/New_York (GMT-5)</option>
                    <option value="Europe/London">Europe/London (GMT+0)</option>
                    <option value="Europe/Lisbon">Europe/Lisbon (GMT+0/+1)</option>
                  </select>
                </div>
              </div>
            </Card>
          </motion.section>

          {/* About */}
          <motion.section initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.5 }}>
            <Card className="bg-gradient-to-br from-muted/20 to-transparent">
              <h2 className="text-lg font-medium text-foreground mb-4">Sobre o Aion Logbook</h2>
              <p className="text-sm text-muted-foreground mb-4 leading-relaxed">
                Aion Logbook não é uma agenda comum. É um mapa para não se perder de si mesmo.
              </p>
              <div className="flex items-center gap-4 text-sm text-muted-foreground">
                <span>Versão 1.0.0</span>
              </div>
            </Card>
          </motion.section>

          <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ delay: 0.6 }} className="text-center py-8">
            <p className="text-sm text-muted-foreground italic">
              "Planejar é lembrar da direção. Registrar é provar que a jornada aconteceu."
            </p>
          </motion.div>
        </div>
      </div>
    </div>
  );
}
