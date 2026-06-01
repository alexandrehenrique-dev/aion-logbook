import { Calendar as CalendarIcon, ChevronLeft, ChevronRight } from "lucide-react";
import { motion } from "motion/react";
import { useState } from "react";
import { Card } from "../components/Card";

const monthData = [
  {
    date: "2026-05-01",
    plans: 2,
    completed: 2,
    intensity: "low",
  },
  {
    date: "2026-05-02",
    plans: 3,
    completed: 2,
    intensity: "medium",
  },
  {
    date: "2026-05-26",
    plans: 4,
    completed: 3,
    intensity: "high",
  },
  {
    date: "2026-05-27",
    plans: 5,
    completed: 2,
    intensity: "high",
  },
];

export function Calendar() {
  const [currentMonth, setCurrentMonth] = useState(new Date(2026, 4, 1)); // May 2026
  const [selectedDate, setSelectedDate] = useState<string | null>("2026-05-27");

  const monthName = currentMonth.toLocaleDateString("pt-BR", {
    month: "long",
    year: "numeric",
  });

  const daysInMonth = new Date(
    currentMonth.getFullYear(),
    currentMonth.getMonth() + 1,
    0
  ).getDate();

  const firstDayOfMonth = new Date(
    currentMonth.getFullYear(),
    currentMonth.getMonth(),
    1
  ).getDay();

  const days = Array.from({ length: daysInMonth }, (_, i) => i + 1);
  const emptyDays = Array.from({ length: firstDayOfMonth }, (_, i) => i);

  const getDateData = (day: number) => {
    const dateStr = `${currentMonth.getFullYear()}-${String(
      currentMonth.getMonth() + 1
    ).padStart(2, "0")}-${String(day).padStart(2, "0")}`;
    return monthData.find((d) => d.date === dateStr);
  };

  const getIntensityColor = (intensity?: string) => {
    if (!intensity) return "bg-muted/20";
    switch (intensity) {
      case "low":
        return "bg-primary/20";
      case "medium":
        return "bg-primary/40";
      case "high":
        return "bg-primary/60";
      default:
        return "bg-muted/20";
    }
  };

  return (
    <div className="min-h-screen bg-background py-8 px-6">
      <div className="max-w-6xl mx-auto">
        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="mb-12"
        >
          <h1 className="text-3xl font-medium text-foreground mb-2 flex items-center gap-3">
            <CalendarIcon className="w-8 h-8" />
            Calendário
          </h1>
          <p className="text-lg text-muted-foreground">
            Mapa temporal da sua jornada
          </p>
        </motion.div>

        <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
          {/* Calendar */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.1 }}
            className="lg:col-span-3"
          >
            <Card>
              {/* Month navigation */}
              <div className="flex items-center justify-between mb-8">
                <h2 className="text-xl font-medium text-foreground capitalize">
                  {monthName}
                </h2>
                <div className="flex items-center gap-2">
                  <button
                    onClick={() =>
                      setCurrentMonth(
                        new Date(currentMonth.getFullYear(), currentMonth.getMonth() - 1)
                      )
                    }
                    className="p-2 rounded-lg hover:bg-muted transition-colors"
                  >
                    <ChevronLeft className="w-5 h-5 text-muted-foreground" />
                  </button>
                  <button
                    onClick={() =>
                      setCurrentMonth(
                        new Date(currentMonth.getFullYear(), currentMonth.getMonth() + 1)
                      )
                    }
                    className="p-2 rounded-lg hover:bg-muted transition-colors"
                  >
                    <ChevronRight className="w-5 h-5 text-muted-foreground" />
                  </button>
                </div>
              </div>

              {/* Weekday headers */}
              <div className="grid grid-cols-7 gap-2 mb-2">
                {["Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"].map((day) => (
                  <div
                    key={day}
                    className="text-center text-sm font-medium text-muted-foreground py-2"
                  >
                    {day}
                  </div>
                ))}
              </div>

              {/* Calendar grid */}
              <div className="grid grid-cols-7 gap-2">
                {/* Empty days */}
                {emptyDays.map((i) => (
                  <div key={`empty-${i}`} className="aspect-square" />
                ))}

                {/* Days */}
                {days.map((day) => {
                  const dateData = getDateData(day);
                  const dateStr = `${currentMonth.getFullYear()}-${String(
                    currentMonth.getMonth() + 1
                  ).padStart(2, "0")}-${String(day).padStart(2, "0")}`;
                  const isToday = dateStr === "2026-05-27";
                  const isSelected = dateStr === selectedDate;

                  return (
                    <motion.button
                      key={day}
                      onClick={() => setSelectedDate(dateStr)}
                      whileHover={{ scale: 1.05 }}
                      whileTap={{ scale: 0.95 }}
                      className={`
                        aspect-square rounded-lg p-2 transition-all
                        ${isSelected ? "ring-2 ring-primary" : ""}
                        ${isToday ? "border-2 border-primary/50" : "border border-border"}
                        ${dateData ? getIntensityColor(dateData.intensity) : "bg-card"}
                        hover:border-primary/30
                      `}
                    >
                      <div className="flex flex-col items-center justify-center h-full">
                        <span
                          className={`text-sm font-medium ${
                            dateData ? "text-foreground" : "text-muted-foreground"
                          }`}
                        >
                          {day}
                        </span>
                        {dateData && (
                          <div className="flex items-center gap-0.5 mt-1">
                            {Array.from({ length: Math.min(dateData.plans, 3) }).map((_, i) => (
                              <div
                                key={i}
                                className="w-1 h-1 rounded-full bg-primary"
                              />
                            ))}
                          </div>
                        )}
                      </div>
                    </motion.button>
                  );
                })}
              </div>

              {/* Legend */}
              <div className="mt-8 pt-6 border-t border-border flex items-center justify-between">
                <div className="flex items-center gap-4 text-sm text-muted-foreground">
                  <div className="flex items-center gap-2">
                    <div className="w-4 h-4 rounded bg-primary/20" />
                    <span>Leve</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <div className="w-4 h-4 rounded bg-primary/40" />
                    <span>Moderado</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <div className="w-4 h-4 rounded bg-primary/60" />
                    <span>Intenso</span>
                  </div>
                </div>
              </div>
            </Card>
          </motion.div>

          {/* Sidebar - Selected day details */}
          <motion.div
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: 0.2 }}
            className="space-y-6"
          >
            {selectedDate && (
              <>
                <Card>
                  <h3 className="text-lg font-medium text-foreground mb-4">
                    {new Date(selectedDate).toLocaleDateString("pt-BR", {
                      day: "numeric",
                      month: "long",
                    })}
                  </h3>
                  <div className="space-y-4">
                    <div>
                      <p className="text-sm text-muted-foreground mb-1">Planos</p>
                      <p className="text-2xl font-medium text-foreground">5</p>
                    </div>
                    <div>
                      <p className="text-sm text-muted-foreground mb-1">Concluídos</p>
                      <p className="text-2xl font-medium text-accent">2</p>
                    </div>
                    <div>
                      <p className="text-sm text-muted-foreground mb-1">Energia</p>
                      <p className="text-2xl font-medium text-foreground">4h 15min</p>
                    </div>
                  </div>
                </Card>

                <Card className="bg-gradient-to-br from-primary/5 to-transparent">
                  <h4 className="text-sm font-medium text-foreground mb-3">Direções ativas</h4>
                  <div className="space-y-2">
                    <div className="flex items-center justify-between text-sm">
                      <span className="text-muted-foreground">Estudos</span>
                      <span className="font-medium text-foreground">2h</span>
                    </div>
                    <div className="flex items-center justify-between text-sm">
                      <span className="text-muted-foreground">Escrita</span>
                      <span className="font-medium text-foreground">1h 30min</span>
                    </div>
                    <div className="flex items-center justify-between text-sm">
                      <span className="text-muted-foreground">Carreira</span>
                      <span className="font-medium text-foreground">45min</span>
                    </div>
                  </div>
                </Card>
              </>
            )}

            <div className="bg-muted/20 border border-border rounded-lg p-4">
              <p className="text-sm text-muted-foreground italic">
                "O calendário não é uma cobrança. É um mapa de onde você investiu sua energia."
              </p>
            </div>
          </motion.div>
        </div>
      </div>
    </div>
  );
}
