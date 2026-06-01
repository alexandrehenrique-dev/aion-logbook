import { Clock, Pause } from "lucide-react";
import { motion } from "motion/react";
import { useState } from "react";
import { Button } from "../components/Button";
import { Card } from "../components/Card";
import { EmptyState } from "../components/EmptyState";

const mockSessions = [
  {
    id: "1",
    directionId: "studies",
    directionName: "Estudos",
    planTitle: "Estudar React avançado",
    startTime: "2026-05-27T09:05:00Z",
    endTime: "2026-05-27T10:30:00Z",
    duration: 85,
    notes: "Comecei pelos fundamentos de RSC. Muito conteúdo novo para absorver.",
    mood: "good",
  },
  {
    id: "2",
    directionId: "writing",
    directionName: "Escrita",
    planTitle: "Escrever capítulo 3",
    startTime: "2026-05-26T14:00:00Z",
    endTime: "2026-05-26T15:45:00Z",
    duration: 105,
    notes: "Fluxo muito bom. Consegui avançar bastante no arco do personagem.",
    mood: "great",
  },
  {
    id: "3",
    directionId: "philosophy",
    directionName: "Filosofia",
    planTitle: "Leitura filosófica",
    startTime: "2026-05-27T06:30:00Z",
    endTime: "2026-05-27T07:15:00Z",
    duration: 45,
    notes: "Leitura calma de Sêneca. Reflexões sobre tempo e direção.",
    mood: "good",
  },
];

export function Sessions() {
  const [activeSession] = useState<string | null>(null);

  const moodEmoji = {
    great: "😊",
    good: "🙂",
    neutral: "😐",
    tired: "😔",
    difficult: "😓",
  };

  return (
    <div className="min-h-screen bg-background py-8 px-6">
      <div className="max-w-5xl mx-auto">
        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="mb-12"
        >
          <h1 className="text-3xl font-medium text-foreground mb-2 flex items-center gap-3">
            <Clock className="w-8 h-8" />
            Sessões
          </h1>
          <p className="text-lg text-muted-foreground">
            Registro de tempo investido em suas direções
          </p>
        </motion.div>

        {/* Active session card */}
        {activeSession && (
          <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            className="mb-8"
          >
            <Card className="border-2 border-primary/30 bg-gradient-to-br from-primary/5 to-transparent">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-4">
                  <div className="w-12 h-12 rounded-full bg-primary/20 flex items-center justify-center">
                    <motion.div
                      animate={{ scale: [1, 1.2, 1] }}
                      transition={{ duration: 2, repeat: Infinity }}
                    >
                      <div className="w-3 h-3 rounded-full bg-primary" />
                    </motion.div>
                  </div>
                  <div>
                    <p className="text-sm text-muted-foreground">Sessão em andamento</p>
                    <p className="text-lg font-medium text-foreground">Estudar React avançado</p>
                    <p className="text-sm text-muted-foreground">Estudos • 1h 25min</p>
                  </div>
                </div>
                <Button variant="secondary" className="flex items-center gap-2">
                  <Pause className="w-4 h-4" />
                  Pausar
                </Button>
              </div>
            </Card>
          </motion.div>
        )}

        {/* Stats */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
          className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-12"
        >
          <Card className="text-center">
            <p className="text-sm text-muted-foreground mb-1">Hoje</p>
            <p className="text-3xl font-medium text-foreground">4h 15min</p>
          </Card>
          <Card className="text-center">
            <p className="text-sm text-muted-foreground mb-1">Esta semana</p>
            <p className="text-3xl font-medium text-foreground">18h 30min</p>
          </Card>
          <Card className="text-center">
            <p className="text-sm text-muted-foreground mb-1">Total de sessões</p>
            <p className="text-3xl font-medium text-foreground">142</p>
          </Card>
        </motion.div>

        {/* Sessions list */}
        <div>
          <h2 className="text-xl font-medium text-foreground mb-6">Sessões Recentes</h2>

          {mockSessions.length === 0 ? (
            <EmptyState
              icon={Clock}
              title="Nenhuma sessão registrada"
              description="Quando você iniciar uma sessão de trabalho, ela aparecerá aqui."
              action={{
                label: "Iniciar sessão",
                onClick: () => {},
              }}
            />
          ) : (
            <div className="space-y-4">
              {mockSessions.map((session, index) => (
                <motion.div
                  key={session.id}
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: 0.2 + index * 0.1 }}
                >
                  <Card hover className="cursor-pointer">
                    <div className="flex items-start justify-between gap-6">
                      <div className="flex-1">
                        <div className="flex items-center gap-3 mb-2">
                          <span className="text-xs px-2 py-1 rounded-full bg-primary/10 text-primary">
                            {session.directionName}
                          </span>
                          <span className="text-xs text-muted-foreground">
                            {new Date(session.startTime).toLocaleDateString("pt-BR", {
                              day: "numeric",
                              month: "short",
                            })}
                          </span>
                        </div>
                        <h3 className="text-lg font-medium text-foreground mb-2">
                          {session.planTitle}
                        </h3>
                        <p className="text-sm text-muted-foreground mb-3">{session.notes}</p>
                        <div className="flex items-center gap-4 text-sm text-muted-foreground">
                          <span className="flex items-center gap-1">
                            <Clock className="w-3 h-3" />
                            {new Date(session.startTime).toLocaleTimeString("pt-BR", {
                              hour: "2-digit",
                              minute: "2-digit",
                            })}{" "}
                            -{" "}
                            {new Date(session.endTime).toLocaleTimeString("pt-BR", {
                              hour: "2-digit",
                              minute: "2-digit",
                            })}
                          </span>
                          {session.mood && (
                            <>
                              <span>•</span>
                              <span>{moodEmoji[session.mood as keyof typeof moodEmoji]}</span>
                            </>
                          )}
                        </div>
                      </div>

                      <div className="text-right">
                        <p className="text-2xl font-medium text-foreground">
                          {session.duration}min
                        </p>
                      </div>
                    </div>
                  </Card>
                </motion.div>
              ))}
            </div>
          )}
        </div>

        {/* Inspirational message */}
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.8 }}
          className="mt-12 text-center"
        >
          <p className="text-sm text-muted-foreground italic">
            "Cada sessão é um passo na jornada. O tempo investido é prova de direção."
          </p>
        </motion.div>
      </div>
    </div>
  );
}
