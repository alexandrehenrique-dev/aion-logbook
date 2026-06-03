import { http } from '../lib/http/httpClient';

export type BugReportSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export type CreateBugReportRequest = {
  title: string;
  description: string;
  severity: BugReportSeverity;
  context?: string;
  url?: string;
  timestamp?: string;
};

export type BugReportResponse = {
  id: string;
  success: boolean;
};

export const bugReportService = {
  create: (payload: CreateBugReportRequest) =>
    http.post<BugReportResponse>('/bug-reports', payload),
};
