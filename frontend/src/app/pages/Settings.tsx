import { Bell, Globe, Palette, Settings as SettingsIcon, User } from "lucide-react";
import { motion } from "motion/react";
import { useState } from "react";
import { Card } from "../components/Card";
import { Input } from "../components/Input";
import { ThemeToggle } from "../components/ThemeToggle";

export function Settings() {
  const [name, setName] = useState("Viajante");
  const [email, setEmail] = useState("usuario@example.com");

  return (
    <div className="min-h-screen bg-background py-8 px-6">
      <div className="max-w-4xl mx-auto">
        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="mb-12"
        >
          <h1 className="text-3xl font-medium text-foreground mb-2 flex items-center gap-3">
            <SettingsIcon className="w-8 h-8" />
            Configurações
          </h1>
          <p className="text-lg text-muted-foreground">
            Personalize sua experiência no Aion Logbook
          </p>
        </motion.div>

        <div className="space-y-8">
          {/* Profile */}
          <motion.section
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.1 }}
          >
            <Card>
              <div className="flex items-center gap-3 mb-6">
                <div className="p-2 rounded-lg bg-primary/10">
                  <User className="w-5 h-5 text-primary" />
                </div>
                <div>
                  <h2 className="text-lg font-medium text-foreground">Perfil</h2>
                  <p className="text-sm text-muted-foreground">
                    Informações básicas da sua conta
                  </p>
                </div>
              </div>

              <div className="space-y-6 max-w-md">
                <Input
                  label="Nome"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="Seu nome"
                />
                <Input
                  label="Email"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="seu@email.com"
                />

                <div className="pt-4">
                  <button className="text-sm text-primary hover:underline">
                    Alterar senha
                  </button>
                </div>
              </div>
            </Card>
          </motion.section>

          {/* Appearance */}
          <motion.section
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.2 }}
          >
            <Card>
              <div className="flex items-center gap-3 mb-6">
                <div className="p-2 rounded-lg bg-secondary/10">
                  <Palette className="w-5 h-5 text-secondary" />
                </div>
                <div>
                  <h2 className="text-lg font-medium text-foreground">Aparência</h2>
                  <p className="text-sm text-muted-foreground">
                    Personalize o visual da interface
                  </p>
                </div>
              </div>

              <div className="space-y-6">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm font-medium text-foreground mb-1">Tema</p>
                    <p className="text-sm text-muted-foreground">
                      Alternar entre modo claro e escuro
                    </p>
                  </div>
                  <ThemeToggle />
                </div>

                <div className="pt-4 border-t border-border">
                  <p className="text-xs text-muted-foreground">
                    O tema dark mode representa um observatório noturno — contemplativo e
                    profundo.
                  </p>
                </div>
              </div>
            </Card>
          </motion.section>

          {/* Notifications */}
          <motion.section
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.3 }}
          >
            <Card>
              <div className="flex items-center gap-3 mb-6">
                <div className="p-2 rounded-lg bg-accent/10">
                  <Bell className="w-5 h-5 text-accent" />
                </div>
                <div>
                  <h2 className="text-lg font-medium text-foreground">Notificações</h2>
                  <p className="text-sm text-muted-foreground">
                    Gerencie como você quer ser lembrado
                  </p>
                </div>
              </div>

              <div className="space-y-4">
                <div className="flex items-center justify-between py-3">
                  <div>
                    <p className="text-sm font-medium text-foreground">Lembrete de planos</p>
                    <p className="text-sm text-muted-foreground">
                      Avisar 10 minutos antes de cada plano
                    </p>
                  </div>
                  <label className="relative inline-block w-12 h-6">
                    <input type="checkbox" className="sr-only peer" defaultChecked />
                    <div className="w-full h-full bg-muted rounded-full peer-checked:bg-primary transition-colors cursor-pointer" />
                    <div className="absolute left-1 top-1 w-4 h-4 bg-white rounded-full transition-transform peer-checked:translate-x-6" />
                  </label>
                </div>

                <div className="flex items-center justify-between py-3 border-t border-border">
                  <div>
                    <p className="text-sm font-medium text-foreground">Reflexões diárias</p>
                    <p className="text-sm text-muted-foreground">
                      Convite gentil para registrar o dia
                    </p>
                  </div>
                  <label className="relative inline-block w-12 h-6">
                    <input type="checkbox" className="sr-only peer" defaultChecked />
                    <div className="w-full h-full bg-muted rounded-full peer-checked:bg-primary transition-colors cursor-pointer" />
                    <div className="absolute left-1 top-1 w-4 h-4 bg-white rounded-full transition-transform peer-checked:translate-x-6" />
                  </label>
                </div>

                <div className="flex items-center justify-between py-3 border-t border-border">
                  <div>
                    <p className="text-sm font-medium text-foreground">
                      Relatório semanal
                    </p>
                    <p className="text-sm text-muted-foreground">
                      Resumo contemplativo da semana
                    </p>
                  </div>
                  <label className="relative inline-block w-12 h-6">
                    <input type="checkbox" className="sr-only peer" />
                    <div className="w-full h-full bg-muted rounded-full peer-checked:bg-primary transition-colors cursor-pointer" />
                    <div className="absolute left-1 top-1 w-4 h-4 bg-white rounded-full transition-transform peer-checked:translate-x-6" />
                  </label>
                </div>

                <div className="pt-4 border-t border-border">
                  <p className="text-xs text-muted-foreground">
                    Lembretes são gentis e nunca agressivos. Você controla sua jornada.
                  </p>
                </div>
              </div>
            </Card>
          </motion.section>

          {/* Language & Region */}
          <motion.section
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.4 }}
          >
            <Card>
              <div className="flex items-center gap-3 mb-6">
                <div className="p-2 rounded-lg bg-muted">
                  <Globe className="w-5 h-5 text-muted-foreground" />
                </div>
                <div>
                  <h2 className="text-lg font-medium text-foreground">Idioma e Região</h2>
                  <p className="text-sm text-muted-foreground">
                    Formato de data, hora e idioma
                  </p>
                </div>
              </div>

              <div className="space-y-4 max-w-md">
                <div>
                  <label className="block text-sm mb-2 text-foreground/80">Idioma</label>
                  <select className="w-full px-4 py-2.5 rounded-lg bg-input-background border border-input text-foreground focus:outline-none focus:ring-2 focus:ring-ring">
                    <option>Português (Brasil)</option>
                    <option>English</option>
                    <option>Español</option>
                  </select>
                </div>

                <div>
                  <label className="block text-sm mb-2 text-foreground/80">Fuso horário</label>
                  <select className="w-full px-4 py-2.5 rounded-lg bg-input-background border border-input text-foreground focus:outline-none focus:ring-2 focus:ring-ring">
                    <option>America/Sao_Paulo (GMT-3)</option>
                    <option>America/New_York (GMT-5)</option>
                    <option>Europe/London (GMT+0)</option>
                  </select>
                </div>
              </div>
            </Card>
          </motion.section>

          {/* About */}
          <motion.section
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.5 }}
          >
            <Card className="bg-gradient-to-br from-muted/20 to-transparent">
              <h2 className="text-lg font-medium text-foreground mb-4">Sobre o Aion Logbook</h2>
              <p className="text-sm text-muted-foreground mb-4 leading-relaxed">
                Aion Logbook não é uma agenda comum. É um mapa para não se perder de si mesmo.
                Um sistema pessoal de direção, memória e travessia.
              </p>
              <div className="flex items-center gap-4 text-sm text-muted-foreground">
                <span>Versão 1.0.0</span>
                <span>•</span>
                <button className="text-primary hover:underline">
                  Termos de uso
                </button>
                <span>•</span>
                <button className="text-primary hover:underline">
                  Privacidade
                </button>
              </div>
            </Card>
          </motion.section>

          {/* Footer message */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.6 }}
            className="text-center py-8"
          >
            <p className="text-sm text-muted-foreground italic">
              "Planejar é lembrar da direção. Registrar é provar que a jornada aconteceu."
            </p>
          </motion.div>
        </div>
      </div>
    </div>
  );
}
