import { http } from '../lib/http/httpClient';
import type { Direction } from '../types';

export type CreateDirectionRequest = Omit<Direction, 'id' | 'userId' | 'createdAt' | 'updatedAt'>;
export type UpdateDirectionRequest = Partial<CreateDirectionRequest>;

export const directionService = {
  list: () => http.get<Direction[]>('/directions'),
  getById: (id: string) => http.get<Direction>(`/directions/${id}`),
  create: (body: CreateDirectionRequest) => http.post<Direction>('/directions', body),
  update: (id: string, body: UpdateDirectionRequest) => http.put<Direction>(`/directions/${id}`, body),
  delete: (id: string) => http.delete<void>(`/directions/${id}`),
};
