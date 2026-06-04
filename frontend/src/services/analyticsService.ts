import { http } from '../lib/http/httpClient';
import { isMockMode } from '../config/env';
import type {
  AnalyticsOverview,
  AnalyticsByDay,
  AnalyticsStatusDistribution,
  AnalyticsTimeByDirection,
} from '../types';
import {
  adaptAnalyticsOverview,
  adaptStatusDistribution,
  type AnalyticsOverviewResponse,
  type StatusDistributionResponse,
} from '../lib/api-adapters';

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
  getOverview: async (p: DateRange = {}): Promise<AnalyticsOverview> => {
    if (isMockMode) {
      return http.get<AnalyticsOverview>(`/analytics/overview${toQuery(p)}`);
    }
    const raw = await http.get<AnalyticsOverviewResponse>(`/analytics/overview${toQuery(p)}`);
    return adaptAnalyticsOverview(raw);
  },

  getPlansByDay: (p: DateRange = {}) =>
    http.get<AnalyticsByDay[]>(`/analytics/plans-by-day${toQuery(p)}`),

  getStatusDistribution: async (p: DateRange = {}): Promise<AnalyticsStatusDistribution[]> => {
    if (isMockMode) {
      return http.get<AnalyticsStatusDistribution[]>(`/analytics/status-distribution${toQuery(p)}`);
    }
    const raw = await http.get<StatusDistributionResponse[]>(
      `/analytics/status-distribution${toQuery(p)}`
    );
    return raw.map(adaptStatusDistribution);
  },

  getTimeByDirection: (p: DateRange = {}) =>
    http.get<AnalyticsTimeByDirection[]>(`/analytics/time-by-direction${toQuery(p)}`),

  getPlannedVsExecuted: (p: DateRange = {}) =>
    http.get<AnalyticsPlannedVsExecuted[]>(`/analytics/planned-vs-executed${toQuery(p)}`),
};
