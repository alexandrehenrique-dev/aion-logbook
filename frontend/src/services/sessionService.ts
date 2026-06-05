import { http } from '../lib/http/httpClient';
import type { SessionLog, PageResponse } from '../types';
import { unwrapSpringPage, type SpringPage } from '../lib/api-adapters';

export type CreateSessionRequest = {
  planId?: string;
  directionId?: string;
  startedAt: string;
  finishedAt?: string;
  actualMinutes: number;
  result?: string;
  notes?: string;
};

export type SessionListParams = {
  directionId?: string;
  planId?: string;
  dateFrom?: string;
  dateTo?: string;
  page?: number;
  size?: number;
  sort?: string;
};

export const sessionService = {
  list: async (params?: SessionListParams): Promise<PageResponse<SessionLog>> => {
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
    const raw = await http.get<SpringPage<SessionLog>>(`/sessions${qs}`);
    return unwrapSpringPage(raw);
  },

  getById: (id: string) => http.get<SessionLog>(`/sessions/${id}`),

  create: (body: CreateSessionRequest) => http.post<SessionLog>('/sessions', body),
};
