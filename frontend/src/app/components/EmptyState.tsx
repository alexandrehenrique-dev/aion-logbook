import { LucideIcon } from "lucide-react";
import { motion } from "motion/react";
import { ReactNode } from "react";
import { Button } from "./Button";

interface EmptyStateProps {
  icon?: LucideIcon;
  title: string;
  description: string;
  action?: {
    label: string;
    onClick: () => void;
  };
  children?: ReactNode;
}

export function EmptyState({ icon: Icon, title, description, action, children }: EmptyStateProps) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.6 }}
      className="flex flex-col items-center justify-center text-center py-16 px-4"
    >
      {Icon && (
        <div className="mb-6 p-4 rounded-2xl bg-muted/30">
          <Icon className="w-12 h-12 text-muted-foreground" strokeWidth={1.5} />
        </div>
      )}

      <h3 className="text-xl font-medium text-foreground mb-3">{title}</h3>

      <p className="text-muted-foreground max-w-md mb-8 leading-relaxed">{description}</p>

      {action && (
        <Button onClick={action.onClick} size="lg">
          {action.label}
        </Button>
      )}

      {children && <div className="mt-8">{children}</div>}
    </motion.div>
  );
}
