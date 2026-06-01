import { Monitor, Moon, Sun } from 'lucide-react';
import { motion } from 'motion/react';
import { useTheme } from 'next-themes';
import { useEffect, useState } from 'react';
import { cn } from '../../utils/cn';

type ThemeOption = 'system' | 'light' | 'dark';

const CYCLE: ThemeOption[] = ['system', 'light', 'dark'];

const ICONS: Record<ThemeOption, typeof Sun> = {
  system: Monitor,
  light: Sun,
  dark: Moon,
};

const LABELS: Record<ThemeOption, string> = {
  system: 'Sistema',
  light: 'Claro',
  dark: 'Escuro',
};

export function ThemeToggle({ className }: { className?: string }) {
  const { theme, setTheme } = useTheme();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  if (!mounted) return null;

  const current = (theme as ThemeOption) ?? 'system';
  const Icon = ICONS[current] ?? Monitor;

  const handleCycle = () => {
    const idx = CYCLE.indexOf(current);
    const next = CYCLE[(idx + 1) % CYCLE.length];
    setTheme(next);
  };

  return (
    <motion.button
      onClick={handleCycle}
      className={cn(
        'relative p-2 rounded-lg',
        'bg-muted/50 hover:bg-muted',
        'transition-colors duration-200',
        'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring',
        className
      )}
      whileHover={{ scale: 1.05 }}
      whileTap={{ scale: 0.95 }}
      aria-label={`Tema atual: ${LABELS[current]}. Clique para alternar`}
      title={LABELS[current]}
    >
      <Icon className="w-5 h-5 text-foreground" />
    </motion.button>
  );
}
