import { http } from '../lib/http/httpClient';
import type { UserProfile } from '../types';

export const userService = {
  getProfile: () => http.get<UserProfile>('/me'),
};
