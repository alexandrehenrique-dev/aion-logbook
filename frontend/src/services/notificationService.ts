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
  list: () => http.get<AppNotification[]>('/notifications'),
  markRead: (id: string) => http.post<void>(`/notifications/${id}/read`),
  markAllRead: () => http.post<void>('/notifications/read-all'),
};
