const ptBR = new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 1 });

export function formatNumber(value: number): string {
  return ptBR.format(value);
}

export function formatPercentage(value: number): string {
  return ptBR.format(value) + '%';
}

export function formatDuration(minutes: number): string {
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  if (h > 0 && m > 0) return `${h}h ${m}min`;
  if (h > 0) return `${h}h`;
  return `${m}min`;
}
