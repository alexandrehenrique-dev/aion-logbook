import type { ValueType, NameType } from 'recharts/types/component/DefaultTooltipContent';

export type TooltipRow = { label: string; value: string; color?: string };

type Props = {
  active?: boolean;
  payload?: { name?: NameType; value?: ValueType; color?: string; payload?: Record<string, unknown> }[];
  label?: string | number;
  title?: string;
  rows?: (payload: Record<string, unknown>[]) => TooltipRow[];
};

export function ChartTooltip({ active, payload, label, title, rows }: Props) {
  if (!active || !payload || payload.length === 0) return null;

  const items: TooltipRow[] = rows
    ? rows(payload as Record<string, unknown>[])
    : payload.map((p) => ({
        label: String(p.name ?? ''),
        value: String(p.value ?? ''),
        color: p.color as string | undefined,
      }));

  const heading = title ?? String(label ?? '');

  return (
    <div className="bg-card border border-border rounded-lg shadow-lg px-3 py-2.5 text-xs min-w-[140px]">
      {heading && <p className="font-medium text-foreground mb-2">{heading}</p>}
      <div className="space-y-1">
        {items.map((item, i) => (
          <div key={i} className="flex items-center justify-between gap-4">
            <span className="flex items-center gap-1.5 text-muted-foreground">
              {item.color && (
                <span className="w-2 h-2 rounded-full shrink-0" style={{ backgroundColor: item.color }} />
              )}
              {item.label}
            </span>
            <span className="font-medium text-foreground">{item.value}</span>
          </div>
        ))}
      </div>
    </div>
  );
}
