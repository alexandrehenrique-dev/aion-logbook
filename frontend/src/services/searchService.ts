import { planService } from './planService';
import { directionService } from './directionService';
import { logbookService } from './logbookService';

export type SearchResultType = 'plan' | 'direction' | 'log';

export type SearchResult = {
  id: string;
  type: SearchResultType;
  title: string;
  subtitle?: string;
  path: string;
};

function normalize(s: string) {
  return s.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, '');
}

function matches(text: string, query: string) {
  return normalize(text).includes(normalize(query));
}

export const searchService = {
  async search(query: string): Promise<SearchResult[]> {
    if (!query.trim()) return [];

    const [plans, directions, logs] = await Promise.allSettled([
      planService.list({ size: 200 }),
      directionService.list(),
      logbookService.list({ size: 200 }),
    ]);

    const results: SearchResult[] = [];

    if (plans.status === 'fulfilled') {
      for (const p of plans.value.data) {
        if (matches(p.title, query) || (p.description && matches(p.description, query))) {
          results.push({ id: p.id, type: 'plan', title: p.title, subtitle: p.status, path: `/plans/${p.id}` });
        }
      }
    }

    if (directions.status === 'fulfilled') {
      for (const d of directions.value) {
        if (matches(d.name, query) || (d.description && matches(d.description, query))) {
          results.push({ id: d.id, type: 'direction', title: d.name, subtitle: d.description, path: `/directions/${d.id}` });
        }
      }
    }

    if (logs.status === 'fulfilled') {
      for (const l of logs.value.data) {
        if (matches(l.title, query) || (l.content && matches(l.content, query))) {
          results.push({ id: l.id, type: 'log', title: l.title, subtitle: l.type, path: `/logbook` });
        }
      }
    }

    return results.slice(0, 12);
  },
};
