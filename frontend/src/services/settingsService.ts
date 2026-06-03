import { http } from '../lib/http/httpClient';

export type UserSettings = {
  timezone?: string;
  theme?: 'system' | 'light' | 'dark';
  defaultPlanDuration?: number;
  notificationsEnabled?: boolean;
  notificationLeadMinutes?: number;
  language?: string;
};

export const settingsService = {
  get: () => http.get<UserSettings>('/settings'),
  update: (payload: UserSettings) => http.put<UserSettings>('/settings', payload),
};
