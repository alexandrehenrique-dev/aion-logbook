import { http } from '../lib/http/httpClient';
import type { LogEntry, PageResponse } from '../types';
import { unwrapSpringPage, type SpringPage } from '../lib/api-adapters';

export type CreateLogEntryRequest = {
  directionId?: string;
  planId?: string;
  title: string;
  content: string;
  type: LogEntry['type'];
  tags?: string[];
};

export type UpdateLogEntryRequest = Partial<CreateLogEntryRequest>;

export type LogListParams = {
  directionId?: string;
  planId?: string;
  page?: number;
  size?: number;
  sort?: string;
};

export const logbookService = {
  list: async (params?: LogListParams): Promise<PageResponse<LogEntry>> => {
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
    const raw = await http.get<SpringPage<LogEntry>>(`/logs${qs}`);
    return unwrapSpringPage(raw);
  },

  getById: (id: string) => http.get<LogEntry>(`/logs/${id}`),

  create: (body: CreateLogEntryRequest) => http.post<LogEntry>('/logs', body),

  update: (id: string, body: UpdateLogEntryRequest) => http.put<LogEntry>(`/logs/${id}`, body),

  delete: (id: string) => http.delete<void>(`/logs/${id}`),
};
