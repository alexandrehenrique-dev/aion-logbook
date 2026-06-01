import { http } from '../lib/http/httpClient';
import type { LogEntry } from '../types';

export type CreateLogEntryRequest = Omit<LogEntry, 'id' | 'userId' | 'createdAt' | 'updatedAt'>;
export type UpdateLogEntryRequest = Partial<CreateLogEntryRequest>;

export const logbookService = {
  list: () => http.get<LogEntry[]>('/logs'),
  getById: (id: string) => http.get<LogEntry>(`/logs/${id}`),
  create: (body: CreateLogEntryRequest) => http.post<LogEntry>('/logs', body),
  update: (id: string, body: UpdateLogEntryRequest) => http.put<LogEntry>(`/logs/${id}`, body),
  delete: (id: string) => http.delete<void>(`/logs/${id}`),
};
