import { AlertCircle } from 'lucide-react';

type Props = {
  message?: string;
  onRetry?: () => void;
};

export function ErrorState({ message = 'Algo deu errado.', onRetry }: Props) {
  return (
    <div className="flex flex-col items-center justify-center py-16 px-4 text-center">
      <div className="w-14 h-14 rounded-2xl bg-destructive/10 flex items-center justify-center mb-5">
        <AlertCircle className="w-7 h-7 text-destructive" strokeWidth={1.5} />
      </div>
      <h3 className="text-base font-medium text-foreground mb-2">Ops, houve um erro</h3>
      <p className="text-sm text-muted-foreground max-w-sm mb-6">{message}</p>
      {onRetry && (
        <button
          onClick={onRetry}
          className="px-4 py-2 rounded-lg bg-primary text-primary-foreground text-sm font-medium hover:bg-primary/90 transition-colors"
        >
          Tentar novamente
        </button>
      )}
    </div>
  );
}
