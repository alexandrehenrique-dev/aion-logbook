import { env } from '../../config/env';

class ApiError extends Error {
  constructor(
    public readonly status: number,
    message: string,
    public readonly body?: unknown
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

let _tokenGetter: (() => Promise<string | undefined> | string | undefined) | null = null;

export function registerTokenGetter(fn: () => Promise<string | undefined> | string | undefined): void {
  _tokenGetter = fn;
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const url = `${env.apiBaseUrl}${path}`;
  const token = _tokenGetter ? await Promise.resolve(_tokenGetter()) : undefined;

  const response = await fetch(url, {
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...init?.headers,
    },
    ...init,
  });

  if (!response.ok) {
    let body: unknown;
    try {
      body = await response.json();
    } catch {
      // ignore parse errors
    }
    throw new ApiError(response.status, `HTTP ${response.status}: ${response.statusText}`, body);
  }

  if (response.status === 204) return undefined as T;

  return response.json() as Promise<T>;
}

export const http = {
  get: <T>(path: string) => request<T>(path, { method: 'GET' }),
  post: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: 'POST', body: JSON.stringify(body) }),
  put: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: 'PUT', body: JSON.stringify(body) }),
  patch: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: 'PATCH', body: JSON.stringify(body) }),
  delete: <T>(path: string) => request<T>(path, { method: 'DELETE' }),
};

export { ApiError };
