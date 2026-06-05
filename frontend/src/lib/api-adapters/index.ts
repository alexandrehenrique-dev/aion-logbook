/**
 * Adapters isolam os DTOs reais do backend da UI.
 * A UI nunca conhece os campos do backend diretamente.
 * Toda transformação de contrato fica concentrada aqui.
 */

import type {
  UserProfile,
  AnalyticsOverview,
  AnalyticsStatusDistribution,
  Plan,
  PageResponse,
} from '../../types';
import type { UserSettings } from '../../services/settingsService';

// ─── Spring Page ──────────────────────────────────────────────────────────────

/**
 * Formato exato do Spring Data Page<T>.
 * Não usar diretamente na UI — passar por unwrapSpringPage primeiro.
 */
export type SpringPage<T> = {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first?: boolean;
  last?: boolean;
};

/**
 * Converte Page<T> do Spring para o PageResponse<T> do frontend.
 * Aplicar em todos os endpoints que retornam listas paginadas.
 */
export function unwrapSpringPage<T>(page: SpringPage<T>): PageResponse<T> {
  return {
    data: page.content,
    page: page.number,
    pageSize: page.size,
    total: page.totalElements,
    totalPages: page.totalPages,
  };
}

// ─── Me / Perfil ──────────────────────────────────────────────────────────────

/**
 * DTO real retornado pelo backend em GET /api/v1/me.
 * Campos: id, keycloakSubject, email, username, fullName, onboardingCompleted, createdAt, updatedAt.
 */
export type MeResponse = {
  id: string;
  keycloakSubject: string;
  email: string;
  username: string;
  fullName: string;
  onboardingCompleted: boolean;
  createdAt: string;
  updatedAt: string;
};

/**
 * Mapeia MeResponse do backend para UserProfile da UI.
 * fullName → name (campo esperado pela UI).
 */
export function adaptMe(me: MeResponse): UserProfile {
  return {
    id: me.id,
    name: me.fullName ?? me.username ?? '',
    email: me.email,
    onboardingCompleted: me.onboardingCompleted,
  };
}

// ─── Settings ─────────────────────────────────────────────────────────────────

/**
 * DTO real retornado pelo backend em GET /api/v1/settings.
 * Inclui onboardingCompleted; não inclui language (não suportado pelo backend).
 */
export type SettingsResponse = {
  timezone?: string;
  theme?: string;
  defaultPlanDuration?: number;
  notificationsEnabled?: boolean;
  notificationLeadMinutes?: number;
  onboardingCompleted?: boolean;
};

/**
 * Mapeia SettingsResponse do backend para UserSettings da UI.
 * language é omitido pois o backend não persiste esse campo.
 */
export function adaptSettings(settings: SettingsResponse): UserSettings {
  return {
    timezone: settings.timezone,
    theme: settings.theme as UserSettings['theme'],
    defaultPlanDuration: settings.defaultPlanDuration,
    notificationsEnabled: settings.notificationsEnabled,
    notificationLeadMinutes: settings.notificationLeadMinutes,
  };
}

// ─── Analytics ────────────────────────────────────────────────────────────────

/**
 * DTO real retornado pelo backend em GET /api/v1/analytics/overview.
 */
export type AnalyticsOverviewResponse = {
  totalPlans: number;
  completedPlans: number;
  partialPlans: number;
  missedPlans: number;
  ignoredPlans: number;
  canceledPlans: number;
  activeDirections: number;
  executedMinutes: number;
  weeklyTimeMinutes: number;
  completionRate: number;
};

/**
 * Mapeia AnalyticsOverviewResponse do backend para AnalyticsOverview da UI.
 * executedMinutes → totalTimeMinutes
 * totalPlans → plansCreated
 */
export function adaptAnalyticsOverview(response: AnalyticsOverviewResponse): AnalyticsOverview {
  return {
    totalTimeMinutes: response.executedMinutes,
    completionRate: response.completionRate,
    plansCreated: response.totalPlans,
    plansCompleted: response.completedPlans,
    activeDirections: response.activeDirections,
    weeklyTimeMinutes: response.weeklyTimeMinutes,
  };
}

/**
 * DTO real retornado pelo backend em GET /api/v1/analytics/status-distribution.
 * Campo count no backend se chama total.
 */
export type StatusDistributionResponse = {
  status: string;
  total: number;
  percentage?: number;
};

/**
 * Mapeia StatusDistributionResponse do backend para AnalyticsStatusDistribution da UI.
 * total → count
 */
export function adaptStatusDistribution(
  response: StatusDistributionResponse
): AnalyticsStatusDistribution {
  return {
    status: response.status as AnalyticsStatusDistribution['status'],
    count: response.total,
    percentage: response.percentage,
  };
}

// ─── Plan ─────────────────────────────────────────────────────────────────────

/**
 * Adapta PlanResponse do backend (notificationEnabled) para o campo
 * que a UI usa internamente. O backend é fonte de verdade; a UI se adapta.
 * notificationEnabled (backend) → notify (campo legado do tipo Plan da UI)
 */
export function adaptPlan(raw: Record<string, unknown>): Plan {
  return {
    ...(raw as Plan),
    notify: raw['notificationEnabled'] as boolean | undefined,
  };
}

/**
 * Prepara payload de postpone para o backend.
 * O backend espera plannedStartAt; a UI pode enviar newPlannedStartAt (legado).
 */
export function adaptPostponeRequest(payload: {
  newPlannedStartAt?: string;
  plannedStartAt?: string;
  plannedDate?: string;
  reason?: string;
  estimatedMinutes?: number;
}): { plannedDate?: string; plannedStartAt: string; estimatedMinutes?: number; reason?: string } {
  return {
    plannedDate: payload.plannedDate,
    plannedStartAt: payload.plannedStartAt ?? payload.newPlannedStartAt ?? '',
    estimatedMinutes: payload.estimatedMinutes,
    reason: payload.reason,
  };
}

/**
 * Prepara payload de transição (start/complete/partial) para o backend.
 * O backend usa description; a UI pode usar note ou notes (legado).
 */
export function adaptTransitionRequest(payload: {
  note?: string;
  notes?: string;
  description?: string;
  actualMinutes?: number;
  reason?: string;
}): { description?: string; actualMinutes?: number; reason?: string } {
  return {
    description: payload.description ?? payload.notes ?? payload.note,
    actualMinutes: payload.actualMinutes,
    reason: payload.reason,
  };
}
