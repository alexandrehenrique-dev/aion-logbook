import { http } from '../lib/http/httpClient';
import type { AuthUser } from '../types';

export type LoginRequest = { email: string; password: string };
export type LoginResponse = { user: AuthUser; token: string };

export const authService = {
  login: (body: LoginRequest) => http.post<LoginResponse>('/auth/login', body),
  logout: () => http.post<{ success: boolean }>('/auth/logout'),
  me: () => http.get<AuthUser>('/user/me'),
};
