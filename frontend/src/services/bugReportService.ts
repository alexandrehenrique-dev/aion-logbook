import { http } from '../lib/http/httpClient';

export type BugReportSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export type CreateBugReportRequest = {
  title: string;
  description: string;
  severity: BugReportSeverity;
  page?: string;
  metadata?: Record<string, unknown>;
};

export type BugReportResponse = {
  id: string;
  status: string;
  telegramSent: boolean;
  message: string;
};

export const bugReportService = {
  create: (payload: CreateBugReportRequest) =>
    http.post<BugReportResponse>('/bug-reports', payload),
};
