import {
  ArrowLeft,
  Calendar,
  Clock,
  Edit2,
  Flag,
  Sparkles,
  Timer,
} from "lucide-react";
import { motion } from "motion/react";
import { useNavigate } from "react-router";
import { Button } from "../components/Button";
import { Card } from "../components/Card";

export function PlanDetail() {
  const navigate = useNavigate();

  const plan = {
    id: "1",
    title: "Estudar React avançado",
    description:
      "Aprofundar conhecimentos em patterns avançados, performance e arquitetura de aplicações React em escala.",
    direction: "Estudos",
    date: "2026-05-27",
    time: "09:00",
    duration: 120,
    priority: "high",
    status: "in-progress",
    intention: "Evoluir minha capacidade de criar aplicações React mais robustas e escaláveis.",
    observations:
      "Focando especialmente em React Server Components e Suspense. Material base: documentação oficial e posts do Dan Abramov.",
    timeline: [
      { event: "Criado", timestamp: "2026-05-26T20:30:00Z" },
      { event: "Iniciado", timestamp: "2026-05-27T09:05:00Z" },
    ],
    sessions: [
      {
        id: "s1",
        startTime: "09:05",
        endTime: "10:30",
        duration: 85,
        notes: "Comecei pelos fundamentos de RSC. Muito conteúdo novo.",
      },
    ],
  };

  return (
    <div className="min-h-screen bg-background py-8 px-6">
      <div className="max-w-4xl mx-auto">
        {/* Back button */}
        <motion.button
          initial={{ opacity: 0, x: -20 }}
          animate={{ opacity: 1, x: 0 }}
          onClick={() => navigate("/plans")}
          className="flex items-center gap-2 text-muted-foreground hover:text-foreground transition-colors mb-8"
        >
          <ArrowLeft className="w-4 h-4" />
          Voltar para planos
        </motion.button>

        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="mb-8"
        >
          <div className="flex items-start justify-between mb-6">
            <div className="flex-1">
              <div className="flex items-center gap-3 mb-3">
                <span className="px-3 py-1 rounded-full text-xs font-medium bg-primary/20 text-primary">
                  Em andamento
                </span>
                <span className="px-3 py-1 rounded-full text-xs font-medium bg-destructive/10 text-destructive flex items-center gap-1">
                  <Flag className="w-3 h-3" />
                  Alta prioridade
                </span>
              </div>
              <h1 className="text-3xl font-medium text-foreground mb-2">{plan.title}</h1>
              <div className="flex items-center gap-4 text-sm text-muted-foreground">
                <span className="flex items-center gap-1">
                  <span className="w-2 h-2 rounded-full bg-primary" />
                  {plan.direction}
                </span>
                <span>•</span>
                <span className="flex items-center gap-1">
                  <Calendar className="w-4 h-4" />
                  27 de maio
                </span>
                <span>•</span>
                <span className="flex items-center gap-1">
                  <Clock className="w-4 h-4" />
                  {plan.time}
                </span>
                <span>•</span>
                <span className="flex items-center gap-1">
                  <Timer className="w-4 h-4" />
                  2h
                </span>
              </div>
            </div>

            <Button className="flex items-center gap-2" size="sm">
              <Edit2 className="w-4 h-4" />
              Modificar plano
            </Button>
          </div>
        </motion.div>

        {/* Main content grid */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Left column - main content */}
          <div className="lg:col-span-2 space-y-8">
            {/* Intention */}
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.1 }}
            >
              <Card>
                <div className="flex items-start gap-3 mb-4">
                  <div className="p-2 rounded-lg bg-secondary/10">
                    <Sparkles className="w-5 h-5 text-secondary" />
                  </div>
                  <div>
                    <h3 className="text-lg font-medium text-foreground mb-1">
                      Intenção Original
                    </h3>
                    <p className="text-sm text-muted-foreground">Por que este plano existe</p>
                  </div>
                </div>
                <div className="space-y-4">
                  <div>
                    <p className="text-sm text-muted-foreground mb-2">Descrição</p>
                    <p className="text-foreground leading-relaxed">{plan.description}</p>
                  </div>
                  <div>
                    <p className="text-sm text-muted-foreground mb-2">Motivação</p>
                    <p className="text-foreground leading-relaxed italic">{plan.intention}</p>
                  </div>
                </div>
              </Card>
            </motion.div>

            {/* Execution */}
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.2 }}
            >
              <Card>
                <h3 className="text-lg font-medium text-foreground mb-4">Execução</h3>
                <div className="space-y-4">
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <p className="text-sm text-muted-foreground mb-1">Início planejado</p>
                      <p className="text-foreground">27 mai, 09:00</p>
                    </div>
                    <div>
                      <p className="text-sm text-muted-foreground mb-1">Duração planejada</p>
                      <p className="text-foreground">2h</p>
                    </div>
                    <div>
                      <p className="text-sm text-muted-foreground mb-1">Início real</p>
                      <p className="text-foreground">27 mai, 09:05</p>
                    </div>
                    <div>
                      <p className="text-sm text-muted-foreground mb-1">Tempo investido</p>
                      <p className="text-foreground">1h 25min</p>
                    </div>
                  </div>

                  {plan.observations && (
                    <div className="pt-4 border-t border-border">
                      <p className="text-sm text-muted-foreground mb-2">Observações</p>
                      <p className="text-foreground leading-relaxed">{plan.observations}</p>
                    </div>
                  )}
                </div>
              </Card>
            </motion.div>

            {/* Sessions */}
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.3 }}
            >
              <h3 className="text-lg font-medium text-foreground mb-4">Sessões Vinculadas</h3>
              <div className="space-y-4">
                {plan.sessions.map((session, index) => (
                  <Card key={session.id} hover>
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <div className="flex items-center gap-3 mb-2">
                          <span className="text-sm font-medium text-foreground">
                            Sessão {index + 1}
                          </span>
                          <span className="text-xs text-muted-foreground">
                            {session.startTime} - {session.endTime}
                          </span>
                        </div>
                        <p className="text-sm text-muted-foreground mb-2">{session.notes}</p>
                      </div>
                      <div className="text-right">
                        <p className="text-lg font-medium text-foreground">{session.duration}min</p>
                      </div>
                    </div>
                  </Card>
                ))}
              </div>
            </motion.div>
          </div>

          {/* Right column - timeline */}
          <motion.div
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: 0.2 }}
            className="space-y-8"
          >
            <Card>
              <h3 className="text-lg font-medium text-foreground mb-6">Timeline</h3>
              <div className="space-y-4">
                {plan.timeline.map((event, index) => (
                  <div key={index} className="flex items-start gap-3 relative">
                    {index < plan.timeline.length - 1 && (
                      <div className="absolute left-2 top-8 bottom-0 w-px bg-border" />
                    )}
                    <div className="w-5 h-5 rounded-full bg-primary/20 flex items-center justify-center shrink-0 relative z-10">
                      <div className="w-2 h-2 rounded-full bg-primary" />
                    </div>
                    <div>
                      <p className="text-sm font-medium text-foreground">{event.event}</p>
                      <p className="text-xs text-muted-foreground">
                        {new Date(event.timestamp).toLocaleString("pt-BR")}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            </Card>

            {/* Quick actions */}
            <Card className="bg-gradient-to-br from-muted/20 to-transparent">
              <h4 className="text-sm font-medium text-foreground mb-4">Ações</h4>
              <div className="space-y-2">
                <button className="w-full text-left px-3 py-2 rounded-lg hover:bg-muted transition-colors text-sm text-foreground">
                  Marcar como concluído
                </button>
                <button className="w-full text-left px-3 py-2 rounded-lg hover:bg-muted transition-colors text-sm text-foreground">
                  Adicionar reflexão
                </button>
                <button className="w-full text-left px-3 py-2 rounded-lg hover:bg-muted transition-colors text-sm text-foreground">
                  Adiar para amanhã
                </button>
                <button className="w-full text-left px-3 py-2 rounded-lg hover:bg-muted transition-colors text-sm text-muted-foreground">
                  Ignorar por escolha
                </button>
              </div>
            </Card>

            {/* Compassionate message */}
            <div className="bg-accent/5 border border-accent/20 rounded-lg p-4">
              <p className="text-sm text-muted-foreground italic">
                "Adaptar não é desistir. Modificar um plano é inteligência, não fracasso."
              </p>
            </div>
          </motion.div>
        </div>
      </div>
    </div>
  );
}
