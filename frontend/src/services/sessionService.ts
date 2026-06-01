import { http } from '../lib/http/httpClient';
import type { SessionLog } from '../types';

export type CreateSessionRequest = Omit<SessionLog, 'id' | 'userId' | 'createdAt'>;

export const sessionService = {
  list: () => http.get<SessionLog[]>('/sessions'),
  getById: (id: string) => http.get<SessionLog>(`/sessions/${id}`),
  create: (body: CreateSessionRequest) => http.post<SessionLog>('/sessions', body),
};
