import { http } from '../lib/http/httpClient';
import { isMockMode } from '../config/env';
import type { UserProfile } from '../types';
import { adaptMe, type MeResponse } from '../lib/api-adapters';

export const userService = {
  getProfile: async (): Promise<UserProfile> => {
    if (isMockMode) {
      return http.get<UserProfile>('/me');
    }
    const raw = await http.get<MeResponse>('/me');
    return adaptMe(raw);
  },
};
