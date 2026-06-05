import { http } from '../lib/http/httpClient';

export type AppNotification = {
  id: string;
  title: string;
  description?: string;
  type: 'due' | 'in_progress' | 'completed' | 'missed' | 'info';
  unread: boolean;
  createdAt: string;
};

export const notificationService = {
  list: (): Promise<AppNotification[]> =>
    http.get<AppNotification[]>('/notifications'),

  markRead: (id: string): Promise<void> =>
    http.post<void>(`/notifications/${id}/read`),

  markAllRead: (): Promise<void> =>
    http.post<void>('/notifications/read-all'),
};
