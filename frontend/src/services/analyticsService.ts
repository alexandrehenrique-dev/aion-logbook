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

type DateRange = { dateFrom?: string; dateTo?: string };

function toQuery(params: DateRange) {
  const q = new URLSearchParams();
  if (params.dateFrom) q.set('dateFrom', params.dateFrom);
  if (params.dateTo) q.set('dateTo', params.dateTo);
  const s = q.toString();
  return s ? `?${s}` : '';
}

export const analyticsService = {
  getOverview: (p: DateRange = {}) => http.get<AnalyticsOverview>(`/analytics/overview${toQuery(p)}`),
  getPlansByDay: (p: DateRange = {}) => http.get<AnalyticsByDay[]>(`/analytics/plans-by-day${toQuery(p)}`),
  getStatusDistribution: (p: DateRange = {}) => http.get<AnalyticsStatusDistribution[]>(`/analytics/status-distribution${toQuery(p)}`),
  getTimeByDirection: (p: DateRange = {}) => http.get<AnalyticsTimeByDirection[]>(`/analytics/time-by-direction${toQuery(p)}`),
  getPlannedVsExecuted: (p: DateRange = {}) => http.get<AnalyticsPlannedVsExecuted[]>(`/analytics/planned-vs-executed${toQuery(p)}`),
};
