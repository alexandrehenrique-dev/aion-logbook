import { AlertCircle, CheckCircle2, Info, XCircle } from "lucide-react";
import { motion } from "motion/react";
import { ReactNode } from "react";

interface StateMessageProps {
  variant: "success" | "error" | "warning" | "info";
  title?: string;
  children: ReactNode;
  onDismiss?: () => void;
}

export function StateMessage({ variant, title, children, onDismiss }: StateMessageProps) {
  const variants = {
    success: {
      bg: "bg-accent/10",
      border: "border-accent/30",
      icon: CheckCircle2,
      iconColor: "text-accent",
    },
    error: {
      bg: "bg-destructive/10",
      border: "border-destructive/30",
      icon: AlertCircle,
      iconColor: "text-destructive",
    },
    warning: {
      bg: "bg-secondary/10",
      border: "border-secondary/30",
      icon: AlertCircle,
      iconColor: "text-secondary",
    },
    info: {
      bg: "bg-primary/10",
      border: "border-primary/30",
      icon: Info,
      iconColor: "text-primary",
    },
  };

  const config = variants[variant];
  const Icon = config.icon;

  return (
    <motion.div
      initial={{ opacity: 0, y: -10 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: -10 }}
      transition={{ duration: 0.3 }}
      className={`rounded-lg border p-4 ${config.bg} ${config.border}`}
    >
      <div className="flex items-start gap-3">
        <Icon className={`w-5 h-5 ${config.iconColor} shrink-0 mt-0.5`} />
        <div className="flex-1">
          {title && <p className="font-medium text-foreground mb-1">{title}</p>}
          <div className="text-sm text-muted-foreground">{children}</div>
        </div>
        {onDismiss && (
          <button
            onClick={onDismiss}
            className="text-muted-foreground hover:text-foreground transition-colors"
            aria-label="Fechar"
          >
            <XCircle className="w-4 h-4" />
          </button>
        )}
      </div>
    </motion.div>
  );
}

export function SuccessMessage({ title, children }: { title?: string; children: ReactNode }) {
  return (
    <StateMessage variant="success" title={title}>
      {children}
    </StateMessage>
  );
}

export function ErrorMessage({ title, children }: { title?: string; children: ReactNode }) {
  return (
    <StateMessage variant="error" title={title}>
      {children}
    </StateMessage>
  );
}
