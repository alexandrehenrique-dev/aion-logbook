import { http } from '../lib/http/httpClient';
import type { Plan, PlanEvent } from '../types';

export type CreatePlanRequest = Omit<Plan, 'id' | 'userId' | 'createdAt' | 'updatedAt'>;
export type UpdatePlanRequest = Partial<CreatePlanRequest>;

export type PostponePlanRequest = {
  newPlannedStartAt: string;
  reason?: string;
};

export type CompletePlanRequest = {
  notes?: string;
  actualMinutes?: number;
};

export type PartialPlanRequest = {
  reason?: string;
  actualMinutes?: number;
  notes?: string;
};

export type PlanListParams = {
  dateFrom?: string;
  dateTo?: string;
  status?: string;
  directionId?: string;
  page?: number;
  size?: number;
};

export const planService = {
  list: (params?: PlanListParams) => {
    const qs = params ? '?' + new URLSearchParams(
      Object.fromEntries(Object.entries(params).filter(([, v]) => v !== undefined).map(([k, v]) => [k, String(v)]))
    ).toString() : '';
    return http.get<Plan[]>(`/plans${qs}`);
  },
  getById: (id: string) => http.get<Plan>(`/plans/${id}`),
  create: (body: CreatePlanRequest) => http.post<Plan>('/plans', body),
  update: (id: string, body: UpdatePlanRequest) => http.put<Plan>(`/plans/${id}`, body),
  delete: (id: string) => http.delete<void>(`/plans/${id}`),

  start: (id: string, payload?: { note?: string }) => http.post<Plan>(`/plans/${id}/start`, payload),
  complete: (id: string, payload?: CompletePlanRequest) => http.post<Plan>(`/plans/${id}/complete`, payload),
  partial: (id: string, payload?: PartialPlanRequest) => http.post<Plan>(`/plans/${id}/partial`, payload),
  postpone: (id: string, payload: PostponePlanRequest) => http.post<Plan>(`/plans/${id}/postpone`, payload),
  ignore: (id: string, payload?: { reason?: string }) => http.post<Plan>(`/plans/${id}/ignore`, payload),
  cancel: (id: string, payload?: { reason?: string }) => http.post<Plan>(`/plans/${id}/cancel`, payload),
  modify: (id: string, body: UpdatePlanRequest) => http.post<Plan>(`/plans/${id}/modify`, body),

  getEvents: (id: string) => http.get<PlanEvent[]>(`/plans/${id}/events`),
};
