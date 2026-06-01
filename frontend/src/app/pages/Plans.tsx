import {
  CheckCircle2,
  Clock,
  Filter,
  LayoutList,
  Pause,
  Plus,
  Search,
  XCircle,
} from "lucide-react";
import { motion } from "motion/react";
import { useState } from "react";
import { Button } from "../components/Button";
import { Card } from "../components/Card";

const statusConfig = {
  completed: { label: "Concluído", color: "text-accent", bg: "bg-accent/10", icon: CheckCircle2 },
  "in-progress": {
    label: "Em andamento",
    color: "text-primary",
    bg: "bg-primary/10",
    icon: Clock,
  },
  missed: {
    label: "Ficou para trás",
    color: "text-destructive/70",
    bg: "bg-destructive/10",
    icon: Pause,
  },
  due: { label: "Chegou a hora", color: "text-secondary", bg: "bg-secondary/10", icon: Clock },
  partial: {
    label: "Feito parcialmente",
    color: "text-muted-foreground",
    bg: "bg-muted",
    icon: CheckCircle2,
  },
  ignored: {
    label: "Ignorado por escolha",
    color: "text-muted-foreground",
    bg: "bg-muted",
    icon: XCircle,
  },
};

const mockPlans = [
  {
    id: 1,
    title: "Estudar React avançado",
    direction: "Estudos",
    date: "2026-05-27",
    time: "09:00",
    duration: "2h",
    priority: "high",
    status: "in-progress" as const,
  },
  {
    id: 2,
    title: "Escrever capítulo 3",
    direction: "Escrita",
    date: "2026-05-27",
    time: "14:00",
    duration: "1h30",
    priority: "medium",
    status: "due" as const,
  },
  {
    id: 3,
    title: "Revisar projeto cliente",
    direction: "Carreira",
    date: "2026-05-27",
    time: "11:00",
    duration: "1h",
    priority: "high",
    status: "due" as const,
  },
  {
    id: 4,
    title: "Meditação matinal",
    direction: "Saúde",
    date: "2026-05-27",
    time: "07:00",
    duration: "20min",
    priority: "low",
    status: "missed" as const,
  },
  {
    id: 5,
    title: "Leitura filosófica",
    direction: "Filosofia",
    date: "2026-05-27",
    time: "06:30",
    duration: "45min",
    priority: "medium",
    status: "completed" as const,
  },
  {
    id: 6,
    title: "Desenvolver nova feature",
    direction: "Projetos",
    date: "2026-05-26",
    time: "15:00",
    duration: "3h",
    priority: "high",
    status: "partial" as const,
  },
];

export function Plans() {
  const [searchQuery, setSearchQuery] = useState("");
  const [filterStatus, setFilterStatus] = useState<string>("all");

  const filteredPlans = mockPlans.filter((plan) => {
    const matchesSearch = plan.title.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesStatus = filterStatus === "all" || plan.status === filterStatus;
    return matchesSearch && matchesStatus;
  });

  return (
    <div className="min-h-screen bg-background py-8 px-6">
      <div className="max-w-6xl mx-auto">
        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="mb-8"
        >
          <div className="flex items-center justify-between mb-6">
            <div>
              <h1 className="text-3xl font-medium text-foreground flex items-center gap-3 mb-2">
                <LayoutList className="w-8 h-8" />
                Planos
              </h1>
              <p className="text-muted-foreground">
                Suas intenções de direção e travessia
              </p>
            </div>
            <Button size="lg" className="flex items-center gap-2">
              <Plus className="w-5 h-5" />
              Novo plano
            </Button>
          </div>

          {/* Search and Filters */}
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
                  <option value="completed">Concluídos</option>
                  <option value="in-progress">Em andamento</option>
                  <option value="due">Chegou a hora</option>
                  <option value="missed">Ficou para trás</option>
                  <option value="partial">Parcialmente</option>
                  <option value="ignored">Ignorados</option>
                </select>
              </div>
            </div>
          </Card>
        </motion.div>

        {/* Plans List */}
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
              const StatusIcon = statusConfig[plan.status].icon;

              return (
                <motion.div
                  key={plan.id}
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: index * 0.05 }}
                >
                  <Card hover className="cursor-pointer">
                    <div className="flex items-start justify-between gap-6">
                      <div className="flex-1">
                        <div className="flex items-start gap-4 mb-3">
                          <div
                            className={`p-2 rounded-lg ${statusConfig[plan.status].bg} shrink-0`}
                          >
                            <StatusIcon
                              className={`w-5 h-5 ${statusConfig[plan.status].color}`}
                            />
                          </div>

                          <div className="flex-1">
                            <h3 className="text-lg font-medium text-foreground mb-1">
                              {plan.title}
                            </h3>
                            <div className="flex flex-wrap items-center gap-3 text-sm text-muted-foreground">
                              <span className="flex items-center gap-1">
                                <span className="w-2 h-2 rounded-full bg-primary" />
                                {plan.direction}
                              </span>
                              <span>•</span>
                              <span>{plan.time}</span>
                              <span>•</span>
                              <span>{plan.duration}</span>
                              <span>•</span>
                              <span>
                                {new Date(plan.date).toLocaleDateString("pt-BR", {
                                  day: "numeric",
                                  month: "short",
                                })}
                              </span>
                            </div>
                          </div>
                        </div>
                      </div>

                      <div className="shrink-0">
                        <span
                          className={`text-xs px-3 py-1.5 rounded-full ${statusConfig[plan.status].bg} ${statusConfig[plan.status].color} font-medium`}
                        >
                          {statusConfig[plan.status].label}
                        </span>
                      </div>
                    </div>
                  </Card>
                </motion.div>
              );
            })
          )}
        </div>

        {/* Summary */}
        {filteredPlans.length > 0 && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.5 }}
            className="mt-8 text-center"
          >
            <p className="text-sm text-muted-foreground">
              {filteredPlans.length} {filteredPlans.length === 1 ? "plano" : "planos"}{" "}
              {filterStatus !== "all" && "nesta visualização"}
            </p>
          </motion.div>
        )}
      </div>
    </div>
  );
}
