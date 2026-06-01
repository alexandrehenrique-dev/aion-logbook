import { forwardRef, type InputHTMLAttributes } from "react";
import { cn } from "../../utils/cn";

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string;
}

export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ className, label, type = "text", ...props }, ref) => {
    return (
      <div className="w-full">
        {label && (
          <label className="block text-sm mb-2 text-foreground/80">
            {label}
          </label>
        )}
        <input
          type={type}
          ref={ref}
          className={cn(
            "w-full px-4 py-2.5 rounded-lg",
            "bg-input-background border border-input",
            "text-foreground placeholder:text-muted-foreground",
            "transition-all duration-200 ease-out",
            "focus:outline-none focus:ring-2 focus:ring-ring focus:border-transparent",
            "hover:border-ring/40",
            "disabled:opacity-50 disabled:pointer-events-none",
            className
          )}
          {...props}
        />
      </div>
    );
  }
);

Input.displayName = "Input";
