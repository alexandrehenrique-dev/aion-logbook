import { http } from '../lib/http/httpClient';
import type { Plan, PlanEvent } from '../types';

export type CreatePlanRequest = Omit<Plan, 'id' | 'userId' | 'createdAt' | 'updatedAt'>;
export type UpdatePlanRequest = Partial<CreatePlanRequest>;

export const planService = {
  list: () => http.get<Plan[]>('/plans'),
  getById: (id: string) => http.get<Plan>(`/plans/${id}`),
  create: (body: CreatePlanRequest) => http.post<Plan>('/plans', body),
  update: (id: string, body: UpdatePlanRequest) => http.put<Plan>(`/plans/${id}`, body),
  delete: (id: string) => http.delete<void>(`/plans/${id}`),

  start: (id: string) => http.post<Plan>(`/plans/${id}/start`),
  complete: (id: string) => http.post<Plan>(`/plans/${id}/complete`),
  partial: (id: string, reason?: string) => http.post<Plan>(`/plans/${id}/partial`, { reason }),
  postpone: (id: string, newDate?: string) => http.post<Plan>(`/plans/${id}/postpone`, { newDate }),
  ignore: (id: string, reason?: string) => http.post<Plan>(`/plans/${id}/ignore`, { reason }),
  cancel: (id: string, reason?: string) => http.post<Plan>(`/plans/${id}/cancel`, { reason }),
  modify: (id: string, body: UpdatePlanRequest) => http.post<Plan>(`/plans/${id}/modify`, body),

  getEvents: (id: string) => http.get<PlanEvent[]>(`/plans/${id}/events`),
};
