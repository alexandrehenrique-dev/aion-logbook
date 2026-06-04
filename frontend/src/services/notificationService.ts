import { http } from '../lib/http/httpClient';
import { isNotificationsEnabled } from '../config/env';

export type AppNotification = {
  id: string;
  title: string;
  description?: string;
  type: 'due' | 'in_progress' | 'completed' | 'missed' | 'info';
  unread: boolean;
  createdAt: string;
};

// Retorna lista vazia quando o módulo de notificações não está habilitado,
// evitando chamadas para /api/v1/notifications que ainda não existe no backend.
const notAvailable = (): Promise<never> =>
  Promise.reject(new Error('Módulo de notificações não disponível.'));

export const notificationService = {
  list: (): Promise<AppNotification[]> => {
    if (!isNotificationsEnabled) return Promise.resolve([]);
    return http.get<AppNotification[]>('/notifications');
  },

  markRead: (id: string): Promise<void> => {
    if (!isNotificationsEnabled) return notAvailable();
    return http.post<void>(`/notifications/${id}/read`);
  },

  markAllRead: (): Promise<void> => {
    if (!isNotificationsEnabled) return notAvailable();
    return http.post<void>('/notifications/read-all');
  },
};
