import { http, HttpResponse } from 'msw';
import type {
  AuthUser,
  Direction,
  Plan,
  PlanEvent,
  SessionLog,
  LogEntry,
  DashboardToday,
  AnalyticsOverview,
} from '../types';
import type { AppNotification } from '../services/notificationService';
import type { UserSettings } from '../services/settingsService';

// ─── Stateful in-memory store ─────────────────────────────────────────────────

const MOCK_USER: AuthUser = {
  id: 'user-1',
  name: 'Viajante',
  email: 'viajante@aion.app',
};

let directions: Direction[] = [
  {
    id: 'dir-estudos',
    userId: 'user-1',
    name: 'Estudos',
    description: 'Crescimento intelectual e aprendizado contínuo',
    color: '#6366f1',
    icon: 'book',
    status: 'ACTIVE',
    identityPhrase: 'Aprender é a forma mais nobre de existir.',
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
    identityPhrase: 'Construo com intenção, não por obrigação.',
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
    identityPhrase: 'Escrever é pensar com a mão.',
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
    identityPhrase: 'Cuido do instrumento que me permite tudo.',
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
    identityPhrase: 'Questionar é viver mais plenamente.',
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
  },
];

const today = new Date().toISOString().split('T')[0];

let plans: Plan[] = [
  {
    id: 'plan-1',
    userId: 'user-1',
    directionId: 'dir-estudos',
    title: 'Estudar React avançado',
    description: 'Aprofundar conhecimentos em patterns avançados e performance.',
    priority: 'HIGH',
    status: 'IN_PROGRESS',
    plannedDate: today,
    plannedStartAt: `${today}T09:00:00Z`,
    estimatedMinutes: 120,
    startedAt: `${today}T09:05:00Z`,
    tags: ['react', 'frontend'],
    createdAt: '2026-05-30T10:00:00Z',
    updatedAt: `${today}T09:05:00Z`,
  },
  {
    id: 'plan-2',
    userId: 'user-1',
    directionId: 'dir-escrita',
    title: 'Escrever capítulo 3',
    description: 'Avançar no arco do personagem principal.',
    priority: 'MEDIUM',
    status: 'DUE',
    plannedDate: today,
    plannedStartAt: `${today}T14:00:00Z`,
    estimatedMinutes: 90,
    tags: ['escrita', 'criação'],
    createdAt: '2026-05-30T10:00:00Z',
    updatedAt: '2026-05-30T10:00:00Z',
  },
  {
    id: 'plan-3',
    userId: 'user-1',
    directionId: 'dir-carreira',
    title: 'Revisar projeto cliente',
    description: 'Revisar entregáveis do sprint atual.',
    priority: 'HIGH',
    status: 'DUE',
    plannedDate: today,
    plannedStartAt: `${today}T11:00:00Z`,
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
    plannedDate: today,
    plannedStartAt: `${today}T07:00:00Z`,
    estimatedMinutes: 20,
    createdAt: '2026-05-30T10:00:00Z',
    updatedAt: `${today}T08:00:00Z`,
  },
  {
    id: 'plan-5',
    userId: 'user-1',
    directionId: 'dir-filosofia',
    title: 'Leitura filosófica',
    priority: 'LOW',
    status: 'COMPLETED',
    plannedDate: today,
    plannedStartAt: `${today}T06:30:00Z`,
    estimatedMinutes: 45,
    startedAt: `${today}T06:32:00Z`,
    finishedAt: `${today}T07:18:00Z`,
    actualMinutes: 46,
    createdAt: '2026-05-30T10:00:00Z',
    updatedAt: `${today}T07:18:00Z`,
  },
];

const TERMINAL_STATUSES = new Set(['COMPLETED', 'PARTIAL', 'CANCELED', 'IGNORED', 'MISSED', 'POSTPONED']);

const planEvents: PlanEvent[] = [
  {
    id: 'event-1',
    userId: 'user-1',
    planId: 'plan-1',
    eventType: 'CREATED',
    toStatus: 'PENDING',
    description: 'Plano criado',
    createdAt: '2026-05-30T10:00:00Z',
  },
  {
    id: 'event-2',
    userId: 'user-1',
    planId: 'plan-1',
    eventType: 'STARTED',
    fromStatus: 'PENDING',
    toStatus: 'IN_PROGRESS',
    description: 'Plano iniciado',
    createdAt: `${today}T09:05:00Z`,
  },
  {
    id: 'event-3',
    userId: 'user-1',
    planId: 'plan-5',
    eventType: 'COMPLETED',
    fromStatus: 'IN_PROGRESS',
    toStatus: 'COMPLETED',
    description: 'Plano concluído',
    createdAt: `${today}T07:18:00Z`,
  },
];

let sessions: SessionLog[] = [
  {
    id: 'session-1',
    userId: 'user-1',
    planId: 'plan-5',
    directionId: 'dir-filosofia',
    startedAt: `${today}T06:32:00Z`,
    finishedAt: `${today}T07:18:00Z`,
    durationMinutes: 46,
    result: 'Leitura concluída com boa absorção.',
    notes: 'Sêneca sobre o tempo. Reflexões sobre a leveza.',
    createdAt: `${today}T07:18:00Z`,
  },
];

let logs: LogEntry[] = [
  {
    id: 'log-1',
    userId: 'user-1',
    directionId: 'dir-filosofia',
    title: 'Reflexão sobre presença',
    content: 'A leitura de hoje trouxe uma perspectiva nova sobre o tempo. Sêneca nos lembra que o tempo é o único bem realmente nosso.',
    type: 'REFLECTION',
    tags: ['filosofia', 'presença', 'tempo'],
    createdAt: `${today}T07:20:00Z`,
    updatedAt: `${today}T07:20:00Z`,
  },
];

let bugReports: Array<{ id: string; title: string; severity: string; createdAt: string }> = [];

const ONBOARDING_STORAGE_KEY = 'aion:mock:onboarding-completed';

function getOnboardingCompleted(): boolean {
  return sessionStorage.getItem(ONBOARDING_STORAGE_KEY) === 'true';
}

function setOnboardingCompleted(value: boolean): void {
  sessionStorage.setItem(ONBOARDING_STORAGE_KEY, String(value));
}

let userSettings: UserSettings = {
  timezone: 'America/Sao_Paulo',
  theme: 'system',
  defaultPlanDuration: 60,
  notificationsEnabled: true,
  notificationLeadMinutes: 15,
  language: 'pt-BR',
};

let notifications: AppNotification[] = [
  {
    id: 'notif-1',
    title: 'Chegou a hora',
    description: 'Revisar projeto cliente — 11:00',
    type: 'due',
    unread: true,
    createdAt: new Date().toISOString(),
  },
  {
    id: 'notif-2',
    title: 'Em andamento',
    description: 'Estudar React avançado — 09:00',
    type: 'in_progress',
    unread: true,
    createdAt: new Date().toISOString(),
  },
  {
    id: 'notif-3',
    title: 'Concluído',
    description: 'Leitura filosófica',
    type: 'completed',
    unread: false,
    createdAt: new Date().toISOString(),
  },
];

// ─── Helper ───────────────────────────────────────────────────────────────────

const delay = (ms: number) => new Promise((r) => setTimeout(r, ms));

function addEvent(planId: string, eventType: string, from: Plan['status'] | undefined, to: Plan['status'], description: string) {
  planEvents.push({
    id: `event-${Date.now()}-${Math.random().toString(36).slice(2)}`,
    userId: 'user-1',
    planId,
    eventType,
    fromStatus: from,
    toStatus: to,
    description,
    createdAt: new Date().toISOString(),
  });
}

function buildDashboardToday(): DashboardToday {
  const todayStr = new Date().toISOString().split('T')[0];
  const todayPlans = plans.filter((p) => p.plannedDate === todayStr || p.plannedStartAt?.startsWith(todayStr));
  const hour = new Date().getHours();
  const greeting = hour < 12 ? 'Bom dia' : hour < 18 ? 'Boa tarde' : 'Boa noite';
  const completed = todayPlans.filter((p) => p.status === 'COMPLETED' || p.status === 'PARTIAL');
  const scheduled = todayPlans.filter((p) => p.status === 'SCHEDULED');
  const totalPlans = todayPlans.length;
  const completionRate = totalPlans > 0 ? Math.round((completed.length / totalPlans) * 100) : 0;
  const totalEnergy = sessions
    .filter((s) => s.startedAt.startsWith(todayStr))
    .reduce((acc, s) => acc + s.durationMinutes, 0);

  return {
    date: todayStr,
    greeting,
    plansInProgress: todayPlans.filter((p) => p.status === 'IN_PROGRESS'),
    plansDue: todayPlans.filter((p) => p.status === 'DUE'),
    plansMissed: todayPlans.filter((p) => p.status === 'MISSED'),
    plansCompleted: completed,
    plansPending: todayPlans.filter((p) => p.status === 'PENDING'),
    plansScheduled: scheduled,
    activeDirections: directions.filter((d) => d.status === 'ACTIVE').length,
    lastSessions: sessions.slice(-3),
    lastLogEntries: logs.slice(-3),
    totalEnergyMinutes: totalEnergy,
    completionRate,
  };
}

const MOCK_ANALYTICS: AnalyticsOverview = {
  totalTimeMinutes: 7920,
  completionRate: 76,
  plansCreated: 120,
  plansCompleted: 85,
  activeDirections: 5,
  weeklyTimeMinutes: 1440,
};

// ─── Handlers ─────────────────────────────────────────────────────────────────

export const handlers = [
  // Auth
  http.post('/api/v1/auth/login', async () => {
    await delay(600);
    return HttpResponse.json({ user: MOCK_USER, token: 'mock-jwt-token' });
  }),
  http.post('/api/v1/auth/logout', async () => {
    await delay(200);
    return HttpResponse.json({ success: true });
  }),

  // User profile
  http.get('/api/v1/me', () => HttpResponse.json({ ...MOCK_USER, onboardingCompleted: getOnboardingCompleted() })),

  // Onboarding
  http.get('/api/v1/onboarding/status', () => HttpResponse.json({ completed: getOnboardingCompleted() })),
  http.post('/api/v1/onboarding/directions', async ({ request }) => {
    const body = (await request.json()) as { directions: Partial<Direction>[] };
    const newDirs: Direction[] = (body.directions ?? []).map((d, i) => ({
      id: `dir-onboard-${Date.now()}-${i}`,
      userId: 'user-1',
      status: 'ACTIVE' as const,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      name: d.name ?? '',
      description: d.description,
      color: d.color,
      icon: d.icon,
      identityPhrase: d.identityPhrase,
    }));
    directions = [...directions, ...newDirs];
    return new HttpResponse(null, { status: 204 });
  }),
  http.post('/api/v1/onboarding/complete', async () => {
    setOnboardingCompleted(true);
    return new HttpResponse(null, { status: 204 });
  }),

  // Dashboard
  http.get('/api/v1/dashboard/today', async () => {
    await delay(300);
    return HttpResponse.json(buildDashboardToday());
  }),
  http.get('/api/v1/dashboard/summary', async () => {
    await delay(300);
    return HttpResponse.json(MOCK_ANALYTICS);
  }),

  // Directions
  http.get('/api/v1/directions', async () => {
    await delay(200);
    return HttpResponse.json(directions);
  }),
  http.get('/api/v1/directions/:id', ({ params }) => {
    const d = directions.find((x) => x.id === params.id);
    return d ? HttpResponse.json(d) : new HttpResponse(null, { status: 404 });
  }),
  http.get('/api/v1/directions/:id/summary', ({ params }) => {
    const d = directions.find((x) => x.id === params.id);
    if (!d) return new HttpResponse(null, { status: 404 });
    const dirPlans = plans.filter((p) => p.directionId === params.id);
    const completedPlans = dirPlans.filter((p) => p.status === 'COMPLETED' || p.status === 'PARTIAL');
    const dirSessions = sessions.filter((s) => s.directionId === params.id);
    return HttpResponse.json({
      direction: d,
      totalPlans: dirPlans.length,
      plansCompleted: completedPlans.length,
      completionRate: dirPlans.length > 0 ? Math.round((completedPlans.length / dirPlans.length) * 100) : 0,
      totalMinutes: dirSessions.reduce((acc, s) => acc + s.durationMinutes, 0),
      totalSessions: dirSessions.length,
      recentPlans: dirPlans.slice(-5),
    });
  }),
  http.post('/api/v1/directions', async ({ request }) => {
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
    directions = [...directions, newDir];
    return HttpResponse.json(newDir, { status: 201 });
  }),
  http.put('/api/v1/directions/:id', async ({ params, request }) => {
    const body = await request.json();
    const idx = directions.findIndex((x) => x.id === params.id);
    if (idx === -1) return new HttpResponse(null, { status: 404 });
    const updated = { ...directions[idx], ...(body as object), updatedAt: new Date().toISOString() };
    directions = directions.map((d, i) => (i === idx ? updated : d));
    return HttpResponse.json(updated);
  }),
  http.delete('/api/v1/directions/:id', ({ params }) => {
    const idx = directions.findIndex((x) => x.id === params.id);
    if (idx === -1) return new HttpResponse(null, { status: 404 });
    directions = directions.map((d, i) =>
      i === idx ? { ...d, status: 'ARCHIVED' as const, updatedAt: new Date().toISOString() } : d
    );
    return new HttpResponse(null, { status: 204 });
  }),

  // Plans
  http.get('/api/v1/plans', async ({ request }) => {
    await delay(300);
    const url = new URL(request.url);
    let result = [...plans];
    const dateFrom = url.searchParams.get('dateFrom');
    const dateTo = url.searchParams.get('dateTo');
    const status = url.searchParams.get('status');
    const directionId = url.searchParams.get('directionId');
    if (dateFrom) result = result.filter((p) => (p.plannedDate ?? '') >= dateFrom);
    if (dateTo) result = result.filter((p) => (p.plannedDate ?? '') <= dateTo);
    if (status) result = result.filter((p) => p.status === status);
    if (directionId) result = result.filter((p) => p.directionId === directionId);
    return HttpResponse.json({ content: result, number: 0, size: result.length, totalElements: result.length, totalPages: result.length === 0 ? 0 : 1, first: true, last: true, empty: result.length === 0 });
  }),
  http.get('/api/v1/plans/:id', ({ params }) => {
    const p = plans.find((x) => x.id === params.id);
    return p ? HttpResponse.json(p) : new HttpResponse(null, { status: 404 });
  }),
  http.post('/api/v1/plans', async ({ request }) => {
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
    plans = [...plans, newPlan];
    addEvent(newPlan.id, 'CREATED', undefined, newPlan.status, 'Plano criado');
    return HttpResponse.json(newPlan, { status: 201 });
  }),
  http.put('/api/v1/plans/:id', async ({ params, request }) => {
    const body = await request.json();
    const idx = plans.findIndex((x) => x.id === params.id);
    if (idx === -1) return new HttpResponse(null, { status: 404 });
    const updated = { ...plans[idx], ...(body as object), updatedAt: new Date().toISOString() };
    plans = plans.map((p, i) => (i === idx ? updated : p));
    return HttpResponse.json(updated);
  }),
  http.delete('/api/v1/plans/:id', ({ params }) => {
    const idx = plans.findIndex((x) => x.id === params.id);
    if (idx === -1) return new HttpResponse(null, { status: 404 });
    plans = plans.filter((_, i) => i !== idx);
    return new HttpResponse(null, { status: 204 });
  }),

  // Plan state transitions
  http.post('/api/v1/plans/:id/start', ({ params }) => {
    const idx = plans.findIndex((x) => x.id === params.id);
    if (idx === -1) return new HttpResponse(null, { status: 404 });
    const p = plans[idx];
    if (TERMINAL_STATUSES.has(p.status)) {
      return HttpResponse.json({ error: 'Plano já encerrado' }, { status: 422 });
    }
    const inProgress = plans.find((x) => x.status === 'IN_PROGRESS' && x.id !== p.id);
    if (inProgress) {
      return HttpResponse.json({ error: 'Já existe um plano em andamento' }, { status: 422 });
    }
    const updated = { ...p, status: 'IN_PROGRESS' as const, startedAt: new Date().toISOString(), updatedAt: new Date().toISOString() };
    plans = plans.map((x, i) => (i === idx ? updated : x));
    addEvent(p.id, 'STARTED', p.status, 'IN_PROGRESS', 'Plano iniciado');
    return HttpResponse.json(updated);
  }),
  http.post('/api/v1/plans/:id/complete', async ({ params, request }) => {
    const idx = plans.findIndex((x) => x.id === params.id);
    if (idx === -1) return new HttpResponse(null, { status: 404 });
    const p = plans[idx];
    if (p.status !== 'IN_PROGRESS' && p.status !== 'DUE' && p.status !== 'PENDING') {
      return HttpResponse.json({ error: 'Status inválido para completar' }, { status: 422 });
    }
    const body = (await request.json().catch(() => ({}))) as { notes?: string; actualMinutes?: number };
    const finishedAt = new Date().toISOString();
    const updated = {
      ...p,
      status: 'COMPLETED' as const,
      finishedAt,
      actualMinutes: body.actualMinutes ?? p.estimatedMinutes,
      updatedAt: finishedAt,
      lastStatusChangedAt: finishedAt,
    };
    plans = plans.map((x, i) => (i === idx ? updated : x));
    addEvent(p.id, 'COMPLETED', p.status, 'COMPLETED', body.notes ? `Concluído: ${body.notes}` : 'Plano concluído');
    if (body.actualMinutes) {
      sessions = [...sessions, {
        id: `session-${Date.now()}`,
        userId: 'user-1',
        planId: p.id,
        directionId: p.directionId,
        startedAt: p.startedAt ?? finishedAt,
        finishedAt,
        durationMinutes: body.actualMinutes,
        result: body.notes ?? 'Plano concluído',
        createdAt: finishedAt,
      }];
    }
    return HttpResponse.json(updated);
  }),
  http.post('/api/v1/plans/:id/partial', async ({ params, request }) => {
    const idx = plans.findIndex((x) => x.id === params.id);
    if (idx === -1) return new HttpResponse(null, { status: 404 });
    const p = plans[idx];
    const allowedStatuses = ['IN_PROGRESS', 'DUE', 'PENDING', 'MISSED'] as Plan['status'][];
    if (!allowedStatuses.includes(p.status)) {
      return HttpResponse.json({ error: 'Status inválido para parcial' }, { status: 422 });
    }
    const body = (await request.json().catch(() => ({}))) as { reason?: string; actualMinutes?: number; notes?: string };
    const finishedAt = new Date().toISOString();
    const updated = { ...p, status: 'PARTIAL' as const, finishedAt, actualMinutes: body.actualMinutes, reason: body.reason, updatedAt: finishedAt };
    plans = plans.map((x, i) => (i === idx ? updated : x));
    addEvent(p.id, 'PARTIAL', p.status, 'PARTIAL', body.reason ?? 'Feito parcialmente');
    return HttpResponse.json(updated);
  }),
  http.post('/api/v1/plans/:id/postpone', async ({ params, request }) => {
    const idx = plans.findIndex((x) => x.id === params.id);
    if (idx === -1) return new HttpResponse(null, { status: 404 });
    const p = plans[idx];
    if (TERMINAL_STATUSES.has(p.status)) {
      return HttpResponse.json({ error: 'Plano já encerrado' }, { status: 422 });
    }
    const body = (await request.json()) as { newPlannedStartAt: string; reason?: string };
    if (!body.newPlannedStartAt) {
      return HttpResponse.json({ error: 'newPlannedStartAt é obrigatório' }, { status: 400 });
    }
    const newDate = body.newPlannedStartAt.split('T')[0];
    const updated = {
      ...p,
      status: 'POSTPONED' as const,
      plannedStartAt: body.newPlannedStartAt,
      plannedDate: newDate,
      reason: body.reason,
      updatedAt: new Date().toISOString(),
    };
    plans = plans.map((x, i) => (i === idx ? updated : x));
    addEvent(p.id, 'POSTPONED', p.status, 'POSTPONED', body.reason ?? `Adiado para ${newDate}`);
    return HttpResponse.json(updated);
  }),
  http.post('/api/v1/plans/:id/ignore', async ({ params, request }) => {
    const idx = plans.findIndex((x) => x.id === params.id);
    if (idx === -1) return new HttpResponse(null, { status: 404 });
    const p = plans[idx];
    const ignorable = ['SCHEDULED', 'PENDING', 'DUE', 'MISSED', 'IN_PROGRESS'] as Plan['status'][];
    if (!ignorable.includes(p.status)) {
      return HttpResponse.json({ error: 'Status inválido para ignorar' }, { status: 422 });
    }
    const body = (await request.json().catch(() => ({}))) as { reason?: string };
    const updated = { ...p, status: 'IGNORED' as const, reason: body.reason, updatedAt: new Date().toISOString() };
    plans = plans.map((x, i) => (i === idx ? updated : x));
    addEvent(p.id, 'IGNORED', p.status, 'IGNORED', body.reason ?? 'Ignorado por escolha');
    return HttpResponse.json(updated);
  }),
  http.post('/api/v1/plans/:id/cancel', async ({ params, request }) => {
    const idx = plans.findIndex((x) => x.id === params.id);
    if (idx === -1) return new HttpResponse(null, { status: 404 });
    const p = plans[idx];
    if (TERMINAL_STATUSES.has(p.status)) {
      return HttpResponse.json({ error: 'Plano já encerrado' }, { status: 422 });
    }
    const body = (await request.json().catch(() => ({}))) as { reason?: string };
    const updated = { ...p, status: 'CANCELED' as const, reason: body.reason, updatedAt: new Date().toISOString() };
    plans = plans.map((x, i) => (i === idx ? updated : x));
    addEvent(p.id, 'CANCELED', p.status, 'CANCELED', body.reason ?? 'Cancelado');
    return HttpResponse.json(updated);
  }),
  http.post('/api/v1/plans/:id/modify', async ({ params, request }) => {
    const body = await request.json();
    const idx = plans.findIndex((x) => x.id === params.id);
    if (idx === -1) return new HttpResponse(null, { status: 404 });
    const updated = { ...plans[idx], ...(body as object), updatedAt: new Date().toISOString() };
    plans = plans.map((x, i) => (i === idx ? updated : x));
    addEvent(params.id as string, 'MODIFIED', plans[idx].status, plans[idx].status, 'Plano modificado');
    return HttpResponse.json(updated);
  }),
  http.get('/api/v1/plans/:id/events', ({ params }) => {
    return HttpResponse.json(planEvents.filter((e) => e.planId === params.id));
  }),

  // Sessions
  http.get('/api/v1/sessions', async () => {
    await delay(200);
    return HttpResponse.json({ content: sessions, number: 0, size: sessions.length, totalElements: sessions.length, totalPages: sessions.length === 0 ? 0 : 1, first: true, last: true, empty: sessions.length === 0 });
  }),
  http.get('/api/v1/sessions/:id', ({ params }) => {
    const s = sessions.find((x) => x.id === params.id);
    return s ? HttpResponse.json(s) : new HttpResponse(null, { status: 404 });
  }),
  http.post('/api/v1/sessions', async ({ request }) => {
    const body = (await request.json()) as Partial<SessionLog>;
    const newSession: SessionLog = {
      id: `session-${Date.now()}`,
      userId: 'user-1',
      startedAt: new Date().toISOString(),
      durationMinutes: 0,
      createdAt: new Date().toISOString(),
      ...body,
    };
    sessions = [...sessions, newSession];
    return HttpResponse.json(newSession, { status: 201 });
  }),

  // Logbook
  http.get('/api/v1/logs', async () => {
    await delay(200);
    return HttpResponse.json({ content: logs, number: 0, size: logs.length, totalElements: logs.length, totalPages: logs.length === 0 ? 0 : 1, first: true, last: true, empty: logs.length === 0 });
  }),
  http.get('/api/v1/logs/:id', ({ params }) => {
    const l = logs.find((x) => x.id === params.id);
    return l ? HttpResponse.json(l) : new HttpResponse(null, { status: 404 });
  }),
  http.post('/api/v1/logs', async ({ request }) => {
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
    logs = [...logs, newEntry];
    return HttpResponse.json(newEntry, { status: 201 });
  }),
  http.put('/api/v1/logs/:id', async ({ params, request }) => {
    const body = await request.json();
    const idx = logs.findIndex((x) => x.id === params.id);
    if (idx === -1) return new HttpResponse(null, { status: 404 });
    const updated = { ...logs[idx], ...(body as object), updatedAt: new Date().toISOString() };
    logs = logs.map((l, i) => (i === idx ? updated : l));
    return HttpResponse.json(updated);
  }),
  http.delete('/api/v1/logs/:id', ({ params }) => {
    const idx = logs.findIndex((x) => x.id === params.id);
    if (idx === -1) return new HttpResponse(null, { status: 404 });
    logs = logs.filter((_, i) => i !== idx);
    return new HttpResponse(null, { status: 204 });
  }),

  // Analytics
  http.get('/api/v1/analytics/overview', async () => {
    await delay(300);
    const completedCount = plans.filter((p) => p.status === 'COMPLETED' || p.status === 'PARTIAL').length;
    const total = sessions.reduce((acc, s) => acc + s.durationMinutes, 0);
    return HttpResponse.json({
      ...MOCK_ANALYTICS,
      plansCompleted: completedCount,
      plansCreated: plans.length,
      totalTimeMinutes: total || MOCK_ANALYTICS.totalTimeMinutes,
      activeDirections: directions.filter((d) => d.status === 'ACTIVE').length,
    });
  }),
  http.get('/api/v1/analytics/plans-by-day', () =>
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
  http.get('/api/v1/analytics/status-distribution', () => {
    const dist: Record<string, number> = {};
    plans.forEach((p) => { dist[p.status] = (dist[p.status] ?? 0) + 1; });
    const total = plans.length || 1;
    return HttpResponse.json(
      Object.entries(dist).map(([status, count]) => ({ status, count, percentage: Math.round((count / total) * 100) }))
    );
  }),
  http.get('/api/v1/analytics/time-by-direction', () => {
    const timeMap: Record<string, number> = {};
    sessions.forEach((s) => {
      if (s.directionId) timeMap[s.directionId] = (timeMap[s.directionId] ?? 0) + s.durationMinutes;
    });
    return HttpResponse.json(
      directions
        .filter((d) => d.status === 'ACTIVE')
        .map((d) => ({
          directionId: d.id,
          directionName: d.name,
          color: d.color,
          totalMinutes: timeMap[d.id] ?? 0,
        }))
    );
  }),
  http.get('/api/v1/analytics/planned-vs-executed', () =>
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

  // Bug reports
  http.post('/api/v1/bug-reports', async ({ request }) => {
    await delay(500);
    const body = (await request.json()) as Record<string, unknown>;
    const report = { id: `bug-${Date.now()}`, title: String(body.title ?? ''), severity: String(body.severity ?? 'MEDIUM'), createdAt: new Date().toISOString() };
    bugReports = [...bugReports, report];
    return HttpResponse.json({ success: true, id: report.id });
  }),

  // Settings
  http.get('/api/v1/settings', () => HttpResponse.json(userSettings)),
  http.put('/api/v1/settings', async ({ request }) => {
    const body = (await request.json()) as UserSettings;
    userSettings = { ...userSettings, ...body };
    return HttpResponse.json(userSettings);
  }),

  // Notifications
  http.get('/api/v1/notifications', () => HttpResponse.json(notifications)),
  http.post('/api/v1/notifications/:id/read', ({ params }) => {
    notifications = notifications.map((n) =>
      n.id === params.id ? { ...n, unread: false } : n
    );
    return new HttpResponse(null, { status: 204 });
  }),
  http.post('/api/v1/notifications/read-all', () => {
    notifications = notifications.map((n) => ({ ...n, unread: false }));
    return new HttpResponse(null, { status: 204 });
  }),
];
