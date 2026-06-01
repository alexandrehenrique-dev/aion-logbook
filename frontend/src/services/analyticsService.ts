import { http } from '../lib/http/httpClient';
import type {
  AnalyticsOverview,
  AnalyticsByDay,
  AnalyticsStatusDistribution,
  AnalyticsTimeByDirection,
} from '../types';

export type AnalyticsPlannedVsExecuted = {
  day: string;
  plannedMinutes: number;
  executedMinutes: number;
};

export const analyticsService = {
  getOverview: () => http.get<AnalyticsOverview>('/analytics/overview'),
  getPlansByDay: () => http.get<AnalyticsByDay[]>('/analytics/plans-by-day'),
  getStatusDistribution: () => http.get<AnalyticsStatusDistribution[]>('/analytics/status-distribution'),
  getTimeByDirection: () => http.get<AnalyticsTimeByDirection[]>('/analytics/time-by-direction'),
  getPlannedVsExecuted: () => http.get<AnalyticsPlannedVsExecuted[]>('/analytics/planned-vs-executed'),
};
