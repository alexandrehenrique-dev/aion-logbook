import { http } from '../lib/http/httpClient';
import type { Plan, PlanEvent, PageResponse } from '../types';
import {
  unwrapSpringPage,
  adaptPlan,
  adaptPostponeRequest,
  adaptTransitionRequest,
  type SpringPage,
} from '../lib/api-adapters';

export type CreatePlanRequest = {
  directionId?: string;
  title: string;
  description?: string;
  type?: string;
  priority: string;
  plannedDate?: string;
  plannedStartAt?: string;
  plannedEndAt?: string;
  estimatedMinutes?: number;
  notificationEnabled?: boolean;
  notificationDateTime?: string;
  tags?: string[];
};

export type UpdatePlanRequest = Partial<CreatePlanRequest>;

export type PostponePlanRequest = {
  plannedStartAt: string;
  estimatedMinutes?: number;
  reason?: string;
};

export type CompletePlanRequest = {
  description?: string;
  actualMinutes?: number;
};

export type PartialPlanRequest = {
  reason?: string;
  actualMinutes?: number;
  description?: string;
};

export type PlanListParams = {
  plannedDate?: string;
  status?: string;
  directionId?: string;
  page?: number;
  size?: number;
  sort?: string;
};

export const planService = {
  list: async (params?: PlanListParams): Promise<PageResponse<Plan>> => {
    const qs = params
      ? '?' +
        new URLSearchParams(
          Object.fromEntries(
            Object.entries(params)
              .filter(([, v]) => v !== undefined)
              .map(([k, v]) => [k, String(v)])
          )
        ).toString()
      : '';
    const raw = await http.get<SpringPage<Record<string, unknown>>>(`/plans${qs}`);
    return unwrapSpringPage({
      ...raw,
      content: raw.content.map(adaptPlan),
    });
  },

  getById: (id: string) => http.get<Plan>(`/plans/${id}`).then(adaptPlan),

  create: (body: CreatePlanRequest) =>
    http.post<Record<string, unknown>>('/plans', body).then(adaptPlan),

  update: (id: string, body: UpdatePlanRequest) =>
    http.put<Record<string, unknown>>(`/plans/${id}`, body).then(adaptPlan),

  // DELETE /plans/{id} não existe no backend.
  // Para cancelar um plano, usar planService.cancel(id, { reason }).

  start: (id: string, payload?: { description?: string; note?: string }) =>
    http
      .post<Record<string, unknown>>(`/plans/${id}/start`, adaptTransitionRequest(payload ?? {}))
      .then(adaptPlan),

  complete: (id: string, payload?: CompletePlanRequest) =>
    http
      .post<Record<string, unknown>>(`/plans/${id}/complete`, adaptTransitionRequest(payload ?? {}))
      .then(adaptPlan),

  partial: (id: string, payload?: PartialPlanRequest) =>
    http
      .post<Record<string, unknown>>(`/plans/${id}/partial`, adaptTransitionRequest(payload ?? {}))
      .then(adaptPlan),

  postpone: (id: string, payload: PostponePlanRequest) =>
    http
      .post<Record<string, unknown>>(`/plans/${id}/postpone`, adaptPostponeRequest(payload))
      .then(adaptPlan),

  ignore: (id: string, payload?: { reason?: string }) =>
    http.post<Record<string, unknown>>(`/plans/${id}/ignore`, payload).then(adaptPlan),

  cancel: (id: string, payload?: { reason?: string }) =>
    http.post<Record<string, unknown>>(`/plans/${id}/cancel`, payload).then(adaptPlan),

  modify: (id: string, body: UpdatePlanRequest) =>
    http.post<Record<string, unknown>>(`/plans/${id}/modify`, body).then(adaptPlan),

  getEvents: (id: string) => http.get<PlanEvent[]>(`/plans/${id}/events`),
};
