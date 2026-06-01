import { http } from '../lib/http/httpClient';
import type { DashboardToday, AnalyticsOverview } from '../types';

export const dashboardService = {
  getToday: () => http.get<DashboardToday>('/dashboard/today'),
  getSummary: () => http.get<AnalyticsOverview>('/dashboard/summary'),
};
