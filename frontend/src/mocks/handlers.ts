import { http, HttpResponse } from 'msw';
import type { AuthUser, Direction, Plan, SessionLog, LogEntry, DashboardToday, AnalyticsOverview } from '../types';

const MOCK_USER: AuthUser = {
  id: 'user-1',
  name: 'Viajante',
  email: 'viajante@aion.app',
};

const MOCK_DIRECTIONS: Direction[] = [
  {
    id: 'dir-estudos',
    userId: 'user-1',
    name: 'Estudos',
    description: 'Crescimento intelectual e aprendizado contínuo',
    color: '#6366f1',
    icon: 'book',
    status: 'ACTIVE',
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
  },
  {
    id: 'dir-carreira',
    userId: 'user-1',
    name: 'Carreira',
    description: 'Desenvolvimento profissional e projetos',
    color: '#0ea5e9',
    icon: 'briefcase',
    status: 'ACTIVE',
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
  },
  {
    id: 'dir-escrita',
    userId: 'user-1',
    name: 'Escrita',
    description: 'Registro, criação e elaboração de ideias',
    color: '#f59e0b',
    icon: 'pen-line',
    status: 'ACTIVE',
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
  },
  {
    id: 'dir-saude',
    userId: 'user-1',
    name: 'Saúde',
    description: 'Corpo, mente e equilíbrio pessoal',
    color: '#22c55e',
    icon: 'heart',
    status: 'ACTIVE',
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
  },
  {
    id: 'dir-filosofia',
    userId: 'user-1',
    name: 'Filosofia',
    description: 'Reflexão, sentido e visão de mundo',
    color: '#8b5cf6',
    icon: 'lightbulb',
    status: 'ACTIVE',
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
  },
];

const MOCK_PLANS: Plan[] = [
  {
    id: 'plan-1',
    userId: 'user-1',
    directionId: 'dir-estudos',
    title: 'Estudar React avançado',
    priority: 'HIGH',
    status: 'IN_PROGRESS',
    plannedDate: '2026-06-01',
    plannedStartAt: '2026-06-01T09:00:00Z',
    estimatedMinutes: 120,
    startedAt: '2026-06-01T09:05:00Z',
    createdAt: '2026-05-30T10:00:00Z',
    updatedAt: '2026-06-01T09:05:00Z',
  },
  {
    id: 'plan-2',
    userId: 'user-1',
    directionId: 'dir-escrita',
    title: 'Escrever capítulo 3',
    priority: 'MEDIUM',
    status: 'DUE',
    plannedDate: '2026-06-01',
    plannedStartAt: '2026-06-01T14:00:00Z',
    estimatedMinutes: 90,
    createdAt: '2026-05-30T10:00:00Z',
    updatedAt: '2026-05-30T10:00:00Z',
  },
  {
    id: 'plan-3',
    userId: 'user-1',
    directionId: 'dir-carreira',
    title: 'Revisar projeto cliente',
    priority: 'HIGH',
    status: 'DUE',
    plannedDate: '2026-06-01',
    plannedStartAt: '2026-06-01T11:00:00Z',
    estimatedMinutes: 60,
    createdAt: '2026-05-30T10:00:00Z',
    updatedAt: '2026-05-30T10:00:00Z',
  },
  {
    id: 'plan-4',
    userId: 'user-1',
    directionId: 'dir-saude',
    title: 'Meditação matinal',
    priority: 'MEDIUM',
    status: 'MISSED',
    plannedDate: '2026-06-01',
    plannedStartAt: '2026-06-01T07:00:00Z',
    estimatedMinutes: 20,
    createdAt: '2026-05-30T10:00:00Z',
    updatedAt: '2026-06-01T08:00:00Z',
  },
  {
    id: 'plan-5',
    userId: 'user-1',
    directionId: 'dir-filosofia',
    title: 'Leitura filosófica',
    priority: 'LOW',
    status: 'COMPLETED',
    plannedDate: '2026-06-01',
    plannedStartAt: '2026-06-01T06:30:00Z',
    estimatedMinutes: 45,
    startedAt: '2026-06-01T06:32:00Z',
    finishedAt: '2026-06-01T07:18:00Z',
    actualMinutes: 46,
    createdAt: '2026-05-30T10:00:00Z',
    updatedAt: '2026-06-01T07:18:00Z',
  },
];

const MOCK_DASHBOARD_TODAY: DashboardToday = {
  date: '2026-06-01',
  greeting: 'Bom dia',
  plansInProgress: MOCK_PLANS.filter((p) => p.status === 'IN_PROGRESS'),
  plansDue: MOCK_PLANS.filter((p) => p.status === 'DUE'),
  plansMissed: MOCK_PLANS.filter((p) => p.status === 'MISSED'),
  plansCompleted: MOCK_PLANS.filter((p) => p.status === 'COMPLETED'),
  totalEnergyMinutes: 255,
  completionRate: 20,
};

const MOCK_ANALYTICS: AnalyticsOverview = {
  totalTimeMinutes: 7920,
  completionRate: 76,
  plansCreated: 120,
  plansCompleted: 85,
  activeDirections: 5,
  weeklyTimeMinutes: 1440,
};

const MOCK_SESSIONS: SessionLog[] = [
  {
    id: 'session-1',
    userId: 'user-1',
    planId: 'plan-5',
    directionId: 'dir-filosofia',
    startedAt: '2026-06-01T06:32:00Z',
    finishedAt: '2026-06-01T07:18:00Z',
    durationMinutes: 46,
    result: 'Leitura concluída',
    createdAt: '2026-06-01T07:18:00Z',
  },
];

const MOCK_LOG_ENTRIES: LogEntry[] = [
  {
    id: 'log-1',
    userId: 'user-1',
    directionId: 'dir-filosofia',
    title: 'Reflexão sobre presença',
    content: 'A leitura de hoje trouxe uma perspectiva nova sobre o tempo.',
    type: 'REFLECTION',
    tags: ['filosofia', 'presença'],
    createdAt: '2026-06-01T07:20:00Z',
    updatedAt: '2026-06-01T07:20:00Z',
  },
];

const delay = (ms: number) => new Promise((r) => setTimeout(r, ms));

export const handlers = [
  // Auth
  http.post('/api/auth/login', async () => {
    await delay(600);
    return HttpResponse.json({ user: MOCK_USER, token: 'mock-jwt-token' });
  }),
  http.post('/api/auth/logout', async () => {
    await delay(200);
    return HttpResponse.json({ success: true });
  }),
  http.get('/api/user/me', () => HttpResponse.json(MOCK_USER)),

  // Dashboard
  http.get('/api/dashboard/today', async () => {
    await delay(300);
    return HttpResponse.json(MOCK_DASHBOARD_TODAY);
  }),
  http.get('/api/dashboard/summary', async () => {
    await delay(300);
    return HttpResponse.json(MOCK_ANALYTICS);
  }),

  // Directions
  http.get('/api/directions', async () => {
    await delay(200);
    return HttpResponse.json(MOCK_DIRECTIONS);
  }),
  http.get('/api/directions/:id', ({ params }) => {
    const d = MOCK_DIRECTIONS.find((x) => x.id === params.id);
    return d ? HttpResponse.json(d) : new HttpResponse(null, { status: 404 });
  }),
  http.post('/api/directions', async ({ request }) => {
    const body = (await request.json()) as Partial<Direction>;
    const newDir: Direction = {
      id: `dir-${Date.now()}`,
      userId: 'user-1',
      status: 'ACTIVE',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      name: body.name ?? '',
      ...body,
    };
    return HttpResponse.json(newDir, { status: 201 });
  }),
  http.put('/api/directions/:id', async ({ params, request }) => {
    const body = await request.json();
    const d = MOCK_DIRECTIONS.find((x) => x.id === params.id);
    if (!d) return new HttpResponse(null, { status: 404 });
    return HttpResponse.json({ ...d, ...(body as object), updatedAt: new Date().toISOString() });
  }),
  http.delete('/api/directions/:id', ({ params }) => {
    const d = MOCK_DIRECTIONS.find((x) => x.id === params.id);
    return d ? new HttpResponse(null, { status: 204 }) : new HttpResponse(null, { status: 404 });
  }),

  // Plans
  http.get('/api/plans', async () => {
    await delay(300);
    return HttpResponse.json(MOCK_PLANS);
  }),
  http.get('/api/plans/:id', ({ params }) => {
    const p = MOCK_PLANS.find((x) => x.id === params.id);
    return p ? HttpResponse.json(p) : new HttpResponse(null, { status: 404 });
  }),
  http.post('/api/plans', async ({ request }) => {
    const body = (await request.json()) as Partial<Plan>;
    const newPlan: Plan = {
      id: `plan-${Date.now()}`,
      userId: 'user-1',
      priority: 'MEDIUM',
      status: 'PENDING',
      title: body.title ?? '',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      ...body,
    };
    return HttpResponse.json(newPlan, { status: 201 });
  }),
  http.put('/api/plans/:id', async ({ params, request }) => {
    const body = await request.json();
    const p = MOCK_PLANS.find((x) => x.id === params.id);
    if (!p) return new HttpResponse(null, { status: 404 });
    return HttpResponse.json({ ...p, ...(body as object), updatedAt: new Date().toISOString() });
  }),
  http.delete('/api/plans/:id', ({ params }) => {
    const p = MOCK_PLANS.find((x) => x.id === params.id);
    return p ? new HttpResponse(null, { status: 204 }) : new HttpResponse(null, { status: 404 });
  }),
  // Plan state transitions
  http.post('/api/plans/:id/start', ({ params }) => {
    const p = MOCK_PLANS.find((x) => x.id === params.id);
    if (!p) return new HttpResponse(null, { status: 404 });
    return HttpResponse.json({ ...p, status: 'IN_PROGRESS', startedAt: new Date().toISOString() });
  }),
  http.post('/api/plans/:id/complete', ({ params }) => {
    const p = MOCK_PLANS.find((x) => x.id === params.id);
    if (!p) return new HttpResponse(null, { status: 404 });
    return HttpResponse.json({ ...p, status: 'COMPLETED', finishedAt: new Date().toISOString() });
  }),
  http.post('/api/plans/:id/partial', ({ params }) => {
    const p = MOCK_PLANS.find((x) => x.id === params.id);
    if (!p) return new HttpResponse(null, { status: 404 });
    return HttpResponse.json({ ...p, status: 'PARTIAL' });
  }),
  http.post('/api/plans/:id/postpone', ({ params }) => {
    const p = MOCK_PLANS.find((x) => x.id === params.id);
    if (!p) return new HttpResponse(null, { status: 404 });
    return HttpResponse.json({ ...p, status: 'POSTPONED' });
  }),
  http.post('/api/plans/:id/ignore', ({ params }) => {
    const p = MOCK_PLANS.find((x) => x.id === params.id);
    if (!p) return new HttpResponse(null, { status: 404 });
    return HttpResponse.json({ ...p, status: 'IGNORED' });
  }),
  http.post('/api/plans/:id/cancel', ({ params }) => {
    const p = MOCK_PLANS.find((x) => x.id === params.id);
    if (!p) return new HttpResponse(null, { status: 404 });
    return HttpResponse.json({ ...p, status: 'CANCELED' });
  }),
  http.post('/api/plans/:id/modify', async ({ params, request }) => {
    const body = await request.json();
    const p = MOCK_PLANS.find((x) => x.id === params.id);
    if (!p) return new HttpResponse(null, { status: 404 });
    return HttpResponse.json({ ...p, ...(body as object), updatedAt: new Date().toISOString() });
  }),
  http.get('/api/plans/:id/events', () => HttpResponse.json([])),

  // Sessions
  http.get('/api/sessions', async () => {
    await delay(200);
    return HttpResponse.json(MOCK_SESSIONS);
  }),
  http.get('/api/sessions/:id', ({ params }) => {
    const s = MOCK_SESSIONS.find((x) => x.id === params.id);
    return s ? HttpResponse.json(s) : new HttpResponse(null, { status: 404 });
  }),
  http.post('/api/sessions', async ({ request }) => {
    const body = (await request.json()) as Partial<SessionLog>;
    const newSession: SessionLog = {
      id: `session-${Date.now()}`,
      userId: 'user-1',
      startedAt: new Date().toISOString(),
      durationMinutes: 0,
      createdAt: new Date().toISOString(),
      ...body,
    };
    return HttpResponse.json(newSession, { status: 201 });
  }),

  // Logbook
  http.get('/api/logs', async () => {
    await delay(200);
    return HttpResponse.json(MOCK_LOG_ENTRIES);
  }),
  http.get('/api/logs/:id', ({ params }) => {
    const l = MOCK_LOG_ENTRIES.find((x) => x.id === params.id);
    return l ? HttpResponse.json(l) : new HttpResponse(null, { status: 404 });
  }),
  http.post('/api/logs', async ({ request }) => {
    const body = (await request.json()) as Partial<LogEntry>;
    const newEntry: LogEntry = {
      id: `log-${Date.now()}`,
      userId: 'user-1',
      title: body.title ?? '',
      content: body.content ?? '',
      type: body.type ?? 'REFLECTION',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      ...body,
    };
    return HttpResponse.json(newEntry, { status: 201 });
  }),
  http.put('/api/logs/:id', async ({ params, request }) => {
    const body = await request.json();
    const l = MOCK_LOG_ENTRIES.find((x) => x.id === params.id);
    if (!l) return new HttpResponse(null, { status: 404 });
    return HttpResponse.json({ ...l, ...(body as object), updatedAt: new Date().toISOString() });
  }),
  http.delete('/api/logs/:id', ({ params }) => {
    const l = MOCK_LOG_ENTRIES.find((x) => x.id === params.id);
    return l ? new HttpResponse(null, { status: 204 }) : new HttpResponse(null, { status: 404 });
  }),

  // Analytics
  http.get('/api/analytics/overview', async () => {
    await delay(300);
    return HttpResponse.json(MOCK_ANALYTICS);
  }),
  http.get('/api/analytics/plans-by-day', () =>
    HttpResponse.json([
      { day: 'Seg', planned: 4, executed: 3 },
      { day: 'Ter', planned: 5, executed: 5 },
      { day: 'Qua', planned: 3, executed: 2 },
      { day: 'Qui', planned: 6, executed: 5 },
      { day: 'Sex', planned: 4, executed: 4 },
      { day: 'Sáb', planned: 2, executed: 2 },
      { day: 'Dom', planned: 1, executed: 1 },
    ])
  ),
  http.get('/api/analytics/status-distribution', () =>
    HttpResponse.json([
      { status: 'COMPLETED', count: 85 },
      { status: 'MISSED', count: 15 },
      { status: 'PARTIAL', count: 10 },
      { status: 'IGNORED', count: 5 },
      { status: 'CANCELED', count: 5 },
    ])
  ),
  http.get('/api/analytics/time-by-direction', () =>
    HttpResponse.json([
      { directionId: 'dir-estudos', directionName: 'Estudos', color: '#6366f1', totalMinutes: 1470 },
      { directionId: 'dir-carreira', directionName: 'Carreira', color: '#0ea5e9', totalMinutes: 1935 },
      { directionId: 'dir-escrita', directionName: 'Escrita', color: '#f59e0b', totalMinutes: 900 },
      { directionId: 'dir-saude', directionName: 'Saúde', color: '#22c55e', totalMinutes: 720 },
      { directionId: 'dir-filosofia', directionName: 'Filosofia', color: '#8b5cf6', totalMinutes: 480 },
    ])
  ),
  http.get('/api/analytics/planned-vs-executed', () =>
    HttpResponse.json([
      { day: 'Seg', plannedMinutes: 480, executedMinutes: 390 },
      { day: 'Ter', plannedMinutes: 420, executedMinutes: 432 },
      { day: 'Qua', plannedMinutes: 360, executedMinutes: 348 },
      { day: 'Qui', plannedMinutes: 510, executedMinutes: 420 },
      { day: 'Sex', plannedMinutes: 420, executedMinutes: 360 },
      { day: 'Sáb', plannedMinutes: 240, executedMinutes: 270 },
      { day: 'Dom', plannedMinutes: 180, executedMinutes: 192 },
    ])
  ),

  // Bug report mock
  http.post('/api/bug-report', async ({ request }) => {
    await delay(500);
    const body = await request.json();
    console.log('[Mock] Bug report received:', body);
    // TODO: integrar report de bugs com webhook do Telegram no backend.
    return HttpResponse.json({ success: true, id: `bug-${Date.now()}` });
  }),
];
