import { Calendar as CalendarIcon, ChevronLeft, ChevronRight, Loader2 } from 'lucide-react';
import { motion } from 'motion/react';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';
import { Card } from '../components/Card';
import { planService } from '../../services/planService';
import type { Plan } from '../../types';
import { PLAN_STATUS_LABEL } from '../../types';

function toDateStr(year: number, month: number, day: number) {
  return `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
}

function getIntensityColor(count: number) {
  if (count === 0) return 'bg-card';
  if (count <= 2) return 'bg-primary/20';
  if (count <= 4) return 'bg-primary/40';
  return 'bg-primary/60';
}

const STATUS_DOT: Record<string, string> = {
  COMPLETED: 'bg-emerald-500',
  IN_PROGRESS: 'bg-primary',
  DUE: 'bg-amber-500',
  MISSED: 'bg-destructive/60',
  PARTIAL: 'bg-secondary',
};

export function Calendar() {
  const navigate = useNavigate();
  const today = new Date();
  const todayStr = toDateStr(today.getFullYear(), today.getMonth(), today.getDate());

  const [currentMonth, setCurrentMonth] = useState(new Date(today.getFullYear(), today.getMonth(), 1));
  const [selectedDate, setSelectedDate] = useState<string>(todayStr);
  const [plans, setPlans] = useState<Plan[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const year = currentMonth.getFullYear();
    const month = currentMonth.getMonth();
    const dateFrom = toDateStr(year, month, 1);
    const lastDay = new Date(year, month + 1, 0).getDate();
    const dateTo = toDateStr(year, month, lastDay);

    setLoading(true);
    planService.list({ dateFrom, dateTo })
      .then(setPlans)
      .catch(() => setPlans([]))
      .finally(() => setLoading(false));
  }, [currentMonth]);

  const monthName = currentMonth.toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' });

  const year = currentMonth.getFullYear();
  const month = currentMonth.getMonth();
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const firstDayOfMonth = new Date(year, month, 1).getDay();

  const days = Array.from({ length: daysInMonth }, (_, i) => i + 1);
  const emptyDays = Array.from({ length: firstDayOfMonth }, (_, i) => i);

  // Group plans by date
  const plansByDate = plans.reduce<Record<string, Plan[]>>((acc, plan) => {
    const d = plan.plannedDate ?? plan.plannedStartAt?.split('T')[0];
    if (d) {
      acc[d] = acc[d] ?? [];
      acc[d].push(plan);
    }
    return acc;
  }, {});

  const selectedPlans = plansByDate[selectedDate] ?? [];
  const completedOnDay = selectedPlans.filter((p) => p.status === 'COMPLETED' || p.status === 'PARTIAL').length;

  return (
    <div className="min-h-screen bg-background py-8 px-6">
      <div className="max-w-6xl mx-auto">
        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} className="mb-12">
          <h1 className="text-3xl font-medium text-foreground mb-2 flex items-center gap-3">
            <CalendarIcon className="w-8 h-8" />
            Calendário
          </h1>
          <p className="text-lg text-muted-foreground">Mapa temporal da sua jornada</p>
        </motion.div>

        <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
          {/* Calendar */}
          <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.1 }} className="lg:col-span-3">
            <Card>
              <div className="flex items-center justify-between mb-8">
                <h2 className="text-xl font-medium text-foreground capitalize">{monthName}</h2>
                <div className="flex items-center gap-2">
                  {loading && <Loader2 className="w-4 h-4 animate-spin text-muted-foreground" />}
                  <button
                    onClick={() => setCurrentMonth(new Date(year, month - 1))}
                    className="p-2 rounded-lg hover:bg-muted transition-colors"
                  >
                    <ChevronLeft className="w-5 h-5 text-muted-foreground" />
                  </button>
                  <button
                    onClick={() => setCurrentMonth(new Date(today.getFullYear(), today.getMonth(), 1))}
                    className="px-3 py-1.5 rounded-lg hover:bg-muted transition-colors text-xs text-muted-foreground"
                  >
                    Hoje
                  </button>
                  <button
                    onClick={() => setCurrentMonth(new Date(year, month + 1))}
                    className="p-2 rounded-lg hover:bg-muted transition-colors"
                  >
                    <ChevronRight className="w-5 h-5 text-muted-foreground" />
                  </button>
                </div>
              </div>

              <div className="grid grid-cols-7 gap-2 mb-2">
                {['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'].map((d) => (
                  <div key={d} className="text-center text-sm font-medium text-muted-foreground py-2">{d}</div>
                ))}
              </div>

              <div className="grid grid-cols-7 gap-2">
                {emptyDays.map((i) => <div key={`empty-${i}`} className="aspect-square" />)}
                {days.map((day) => {
                  const dateStr = toDateStr(year, month, day);
                  const dayPlans = plansByDate[dateStr] ?? [];
                  const isToday = dateStr === todayStr;
                  const isSelected = dateStr === selectedDate;

                  return (
                    <motion.button
                      key={day}
                      onClick={() => setSelectedDate(dateStr)}
                      whileHover={{ scale: 1.05 }}
                      whileTap={{ scale: 0.95 }}
                      className={`aspect-square rounded-lg p-1 transition-all ${
                        isSelected ? 'ring-2 ring-primary' : ''
                      } ${
                        isToday ? 'border-2 border-primary/50' : 'border border-border'
                      } ${getIntensityColor(dayPlans.length)} hover:border-primary/30`}
                    >
                      <div className="flex flex-col items-center justify-center h-full gap-0.5">
                        <span className={`text-sm font-medium ${isToday ? 'text-primary' : dayPlans.length > 0 ? 'text-foreground' : 'text-muted-foreground'}`}>
                          {day}
                        </span>
                        {dayPlans.length > 0 && (
                          <div className="flex items-center gap-0.5">
                            {dayPlans.slice(0, 3).map((p, i) => (
                              <div key={i} className={`w-1.5 h-1.5 rounded-full ${STATUS_DOT[p.status] ?? 'bg-primary'}`} />
                            ))}
                          </div>
                        )}
                      </div>
                    </motion.button>
                  );
                })}
              </div>

              <div className="mt-8 pt-6 border-t border-border flex items-center gap-4 text-sm text-muted-foreground flex-wrap">
                <div className="flex items-center gap-2"><div className="w-4 h-4 rounded bg-primary/20" /><span>Leve</span></div>
                <div className="flex items-center gap-2"><div className="w-4 h-4 rounded bg-primary/40" /><span>Moderado</span></div>
                <div className="flex items-center gap-2"><div className="w-4 h-4 rounded bg-primary/60" /><span>Intenso</span></div>
              </div>
            </Card>
          </motion.div>

          {/* Sidebar */}
          <motion.div initial={{ opacity: 0, x: 20 }} animate={{ opacity: 1, x: 0 }} transition={{ delay: 0.2 }} className="space-y-6">
            <Card>
              <h3 className="text-lg font-medium text-foreground mb-4">
                {new Date(selectedDate + 'T12:00:00').toLocaleDateString('pt-BR', { day: 'numeric', month: 'long' })}
                {selectedDate === todayStr && <span className="text-xs text-primary ml-2">(hoje)</span>}
              </h3>
              <div className="space-y-3">
                <div>
                  <p className="text-sm text-muted-foreground mb-1">Planos</p>
                  <p className="text-2xl font-medium text-foreground">{selectedPlans.length}</p>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground mb-1">Concluídos</p>
                  <p className="text-2xl font-medium text-emerald-600 dark:text-emerald-400">{completedOnDay}</p>
                </div>
              </div>
            </Card>

            {selectedPlans.length > 0 && (
              <Card>
                <h4 className="text-sm font-medium text-foreground mb-3">Planos do dia</h4>
                <div className="space-y-2">
                  {selectedPlans.map((plan) => (
                    <div
                      key={plan.id}
                      className="flex items-start gap-2 p-2 rounded-lg hover:bg-muted/50 cursor-pointer transition-colors"
                      onClick={() => navigate(`/plans/${plan.id}`)}
                    >
                      <div className={`w-2 h-2 rounded-full mt-1.5 shrink-0 ${STATUS_DOT[plan.status] ?? 'bg-primary'}`} />
                      <div className="min-w-0">
                        <p className="text-xs font-medium text-foreground line-clamp-2">{plan.title}</p>
                        <p className="text-xs text-muted-foreground">{PLAN_STATUS_LABEL[plan.status]}</p>
                      </div>
                    </div>
                  ))}
                </div>
              </Card>
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
