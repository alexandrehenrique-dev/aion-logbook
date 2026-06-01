import { type HTMLAttributes, forwardRef } from "react";
import { cn } from "../../utils/cn";

interface CardProps extends HTMLAttributes<HTMLDivElement> {
  hover?: boolean;
}

export const Card = forwardRef<HTMLDivElement, CardProps>(
  ({ className, hover = false, children, ...props }, ref) => {
    return (
      <div
        ref={ref}
        className={cn(
          "rounded-xl bg-card border border-border p-6",
          "transition-all duration-300 ease-out",
          hover && "hover:shadow-lg hover:border-ring/20 hover:-translate-y-0.5",
          className
        )}
        style={{
          boxShadow: "var(--shadow-md)",
        }}
        {...props}
      >
        {children}
      </div>
    );
  }
);

Card.displayName = "Card";
