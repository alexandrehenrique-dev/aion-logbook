// Domain contracts for Aion Logbook — aligned with future Spring Boot backend

export type PlanStatus =
  | 'DRAFT'
  | 'SCHEDULED'
  | 'PENDING'
  | 'DUE'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'PARTIAL'
  | 'POSTPONED'
  | 'IGNORED'
  | 'CANCELED'
  | 'MISSED';

export const PLAN_STATUS_LABEL: Record<PlanStatus, string> = {
  DRAFT: 'Rascunho',
  SCHEDULED: 'Agendado',
  PENDING: 'Pendente',
  DUE: 'Chegou a hora',
  IN_PROGRESS: 'Em andamento',
  COMPLETED: 'Concluído',
  PARTIAL: 'Feito parcialmente',
  POSTPONED: 'Reagendado',
  IGNORED: 'Ignorado por escolha',
  CANCELED: 'Cancelado',
  MISSED: 'Ficou para trás',
};

export type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export const PRIORITY_LABEL: Record<Priority, string> = {
  LOW: 'Baixa',
  MEDIUM: 'Média',
  HIGH: 'Alta',
  CRITICAL: 'Crítica',
};

export type Direction = {
  id: string;
  userId: string;
  name: string;
  description?: string;
  color?: string;
  icon?: string;
  status: 'ACTIVE' | 'ARCHIVED';
  identityPhrase?: string;
  createdAt: string;
  updatedAt: string;
};

export type Plan = {
  id: string;
  userId: string;
  directionId?: string;
  title: string;
  description?: string;
  type?: string;
  priority: Priority;
  status: PlanStatus;
  plannedDate?: string;
  plannedStartAt?: string;
  plannedEndAt?: string;
  estimatedMinutes?: number;
  notify?: boolean;
  notificationDateTime?: string;
  startedAt?: string;
  finishedAt?: string;
  actualMinutes?: number;
  reason?: string;
  tags?: string[];
  createdAt: string;
  updatedAt: string;
  lastStatusChangedAt?: string;
};

export type SessionLog = {
  id: string;
  userId: string;
  planId?: string;
  directionId?: string;
  startedAt: string;
  finishedAt?: string;
  durationMinutes: number;
  result?: string;
  notes?: string;
  createdAt: string;
};

export type PlanEventType =
  | 'CREATED'
  | 'UPDATED'
  | 'RESCHEDULED'
  | 'STARTED'
  | 'COMPLETED'
  | 'PARTIAL_COMPLETED'
  | 'POSTPONED'
  | 'IGNORED'
  | 'CANCELED'
  | 'MISSED'
  | 'DUE'
  | 'NOTE_ADDED'
  | 'MODIFIED';

export const PLAN_EVENT_TYPE_LABEL: Record<PlanEventType, string> = {
  CREATED: 'Plano criado',
  UPDATED: 'Plano atualizado',
  RESCHEDULED: 'Plano reagendado',
  STARTED: 'Plano iniciado',
  COMPLETED: 'Plano concluído',
  PARTIAL_COMPLETED: 'Concluído parcialmente',
  POSTPONED: 'Plano reagendado',
  IGNORED: 'Pausado por escolha',
  CANCELED: 'Plano cancelado',
  MISSED: 'Plano não executado no prazo',
  DUE: 'Chegou a hora',
  NOTE_ADDED: 'Observação registrada',
  MODIFIED: 'Plano modificado',
};

export function getPlanEventLabel(eventType: string): string {
  return PLAN_EVENT_TYPE_LABEL[eventType as PlanEventType] ?? eventType;
}

export type PlanEvent = {
  id: string;
  userId: string;
  planId: string;
  eventType: PlanEventType | string;
  fromStatus?: PlanStatus;
  toStatus?: PlanStatus;
  description?: string;
  metadata?: Record<string, unknown>;
  createdAt: string;
};

export type LogEntry = {
  id: string;
  userId: string;
  directionId?: string;
  planId?: string;
  title: string;
  content: string;
  type: 'REFLECTION' | 'SYNTHESIS' | 'LEARNING' | 'BLOCKER' | 'DECISION' | 'IDEA' | 'FEEDBACK';
  tags?: string[];
  createdAt: string;
  updatedAt: string;
};

export type AuthUser = {
  id: string;
  name: string;
  email: string;
  avatar?: string;
};

// Perfil retornado pelo backend (GET /users/me) quando VITE_API_MODE=real.
// Inclui estado da aplicação além do que o token Keycloak carrega.
export type UserProfile = {
  id: string;
  name: string;
  email: string;
  onboardingCompleted: boolean;
};

export type DashboardToday = {
  date: string;
  greeting: string;
  plansInProgress: Plan[];
  plansDue: Plan[];
  plansMissed: Plan[];
  plansCompleted: Plan[];
  plansPending?: Plan[];
  plansScheduled?: Plan[];
  activeDirections?: number;
  lastSessions?: SessionLog[];
  lastLogEntries?: LogEntry[];
  totalEnergyMinutes: number;
  completionRate: number;
};

export type AnalyticsOverview = {
  totalTimeMinutes: number;
  completionRate: number;
  plansCreated: number;
  plansCompleted: number;
  activeDirections: number;
  weeklyTimeMinutes?: number;
};

export type AnalyticsByDay = {
  day: string;
  date?: string;
  planned: number;
  executed: number;
};

export type AnalyticsStatusDistribution = {
  status: PlanStatus;
  count: number;
  percentage?: number;
};

export type AnalyticsTimeByDirection = {
  directionId: string;
  directionName: string;
  color?: string;
  totalMinutes: number;
};

// Generic API response wrappers
export type ApiResponse<T> = {
  data: T;
  success: boolean;
  message?: string;
};

export type PageResponse<T> = {
  data: T[];
  page: number;
  pageSize: number;
  total: number;
  totalPages: number;
};

export type PaginatedResponse<T> = PageResponse<T>;
