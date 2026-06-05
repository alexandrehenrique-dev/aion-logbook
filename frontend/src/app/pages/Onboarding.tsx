import { Book, Briefcase, Heart, Lightbulb, PenLine, TrendingUp } from 'lucide-react';
import { motion } from 'motion/react';
import { useState } from 'react';
import { useNavigate } from 'react-router';
import { Button } from '../components/Button';
import { onboardingService } from '../../services/onboardingService';
import { isMockMode } from '../../config/env';

const SUGGESTED_DIRECTIONS = [
  { id: 'studies', label: 'Estudos', name: 'Estudos', description: 'Crescimento intelectual e aprendizado contínuo', color: '#6366f1', icon: 'book', component: Book },
  { id: 'career', label: 'Carreira', name: 'Carreira', description: 'Desenvolvimento profissional e projetos', color: '#0ea5e9', icon: 'briefcase', component: Briefcase },
  { id: 'writing', label: 'Escrita', name: 'Escrita', description: 'Registro, criação e elaboração de ideias', color: '#f59e0b', icon: 'pen-line', component: PenLine },
  { id: 'projects', label: 'Projetos', name: 'Projetos', description: 'Criação e construção de ideias', color: '#8b5cf6', icon: 'lightbulb', component: Lightbulb },
  { id: 'health', label: 'Saúde', name: 'Saúde', description: 'Corpo, mente e equilíbrio pessoal', color: '#22c55e', icon: 'heart', component: Heart },
  { id: 'philosophy', label: 'Filosofia', name: 'Filosofia', description: 'Reflexão, sentido e visão de mundo', color: '#ec4899', icon: 'trending-up', component: TrendingUp },
];

export function Onboarding() {
  const navigate = useNavigate();
  const [step, setStep] = useState(1);
  const [selectedIds, setSelectedIds] = useState<string[]>([]);
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  const toggleDirection = (id: string) => {
    setSelectedIds((prev) =>
      prev.includes(id) ? prev.filter((d) => d !== id) : [...prev, id]
    );
  };

  const handleNext = async () => {
    if (step < 4) {
      setStep((prev) => prev + 1);
      return;
    }

    // Final step: call HTTP onboarding service
    setSubmitting(true);
    setSubmitError(null);
    try {
      const selectedDirections = SUGGESTED_DIRECTIONS
        .filter((d) => selectedIds.includes(d.id))
        .map((d) => ({ name: d.name, description: d.description, color: d.color, icon: d.icon }));

      if (selectedDirections.length > 0) {
        await onboardingService.createDirections(selectedDirections);
      }
      await onboardingService.complete();
      navigate('/dashboard');
    } catch {
      if (isMockMode) {
        navigate('/dashboard');
      } else {
        setSubmitError('Erro ao salvar. Verifique sua conexão e tente novamente.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  const renderStep = () => {
    switch (step) {
      case 1:
        return (
          <motion.div key="step-1" initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -20 }} className="text-center max-w-2xl mx-auto">
            <h2 className="text-3xl md:text-4xl font-medium mb-6 text-foreground">
              Boas-vindas ao Aion Logbook
            </h2>
            <p className="text-base md:text-lg text-muted-foreground leading-relaxed">
              Este é o início da sua jornada de direção pessoal.
              <br />
              Vamos configurar juntos o seu espaço de travessia.
            </p>
          </motion.div>
        );

      case 2:
        return (
          <motion.div key="step-2" initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -20 }} className="max-w-3xl mx-auto w-full">
            <h2 className="text-2xl md:text-3xl font-medium mb-3 text-center text-foreground">
              Quais direções da sua vida você deseja acompanhar?
            </h2>
            <p className="text-center text-muted-foreground mb-8 md:mb-12 text-sm md:text-base">
              Selecione as áreas que deseja observar e cultivar
            </p>
            <div className="grid grid-cols-2 md:grid-cols-3 gap-3 md:gap-4">
              {SUGGESTED_DIRECTIONS.map((direction) => {
                const Icon = direction.component;
                const isSelected = selectedIds.includes(direction.id);
                return (
                  <motion.button
                    key={direction.id}
                    onClick={() => toggleDirection(direction.id)}
                    whileHover={{ scale: 1.02 }}
                    whileTap={{ scale: 0.98 }}
                    className={`p-4 md:p-6 rounded-xl border-2 transition-all duration-300 text-left ${isSelected ? 'border-primary bg-primary/5' : 'border-border bg-card hover:border-primary/30'}`}
                  >
                    <Icon className={`w-7 h-7 md:w-8 md:h-8 mb-2 md:mb-3 ${isSelected ? 'text-primary' : 'text-muted-foreground'}`} />
                    <p className={`text-sm md:text-base font-medium ${isSelected ? 'text-primary' : 'text-foreground'}`}>
                      {direction.label}
                    </p>
                  </motion.button>
                );
              })}
            </div>
          </motion.div>
        );

      case 3:
        return (
          <motion.div key="step-3" initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -20 }} className="text-center max-w-2xl mx-auto">
            <h2 className="text-2xl md:text-3xl font-medium mb-6 text-foreground">
              {selectedIds.length > 0
                ? `Você selecionou ${selectedIds.length} ${selectedIds.length === 1 ? 'direção' : 'direções'}`
                : 'Você pode começar sem direções'}
            </h2>
            <p className="text-base md:text-lg text-muted-foreground leading-relaxed">
              Você não precisa organizar tudo hoje.
              <br />
              Apenas recuperar direção.
            </p>
          </motion.div>
        );

      case 4:
        return (
          <motion.div key="step-4" initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -20 }} className="text-center max-w-2xl mx-auto">
            <h2 className="text-2xl md:text-3xl font-medium mb-6 text-foreground">
              Tudo pronto para começar
            </h2>
            <p className="text-base md:text-lg text-muted-foreground leading-relaxed mb-8">
              A travessia ainda não começou, mas você já deu o primeiro passo.
              <br />
              Lembre-se: você pode começar pequeno.
            </p>
            <div className="bg-muted/30 rounded-xl p-6 border border-border">
              <p className="text-sm text-muted-foreground italic">
                "Planejar é lembrar da direção. Registrar é provar que a jornada aconteceu."
              </p>
            </div>
          </motion.div>
        );

      default:
        return null;
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-background via-background to-muted/10 px-4 py-10 md:py-12">
      <div className="max-w-5xl mx-auto">
        <motion.div initial={{ opacity: 0, y: -20 }} animate={{ opacity: 1, y: 0 }} className="mb-12 md:mb-16">
          <div className="flex items-center justify-center gap-2">
            {[1, 2, 3, 4].map((i) => (
              <div
                key={i}
                className={`h-1.5 rounded-full transition-all duration-500 ${i <= step ? 'w-12 bg-primary' : 'w-8 bg-border'}`}
              />
            ))}
          </div>
          <p className="text-center text-sm text-muted-foreground mt-4">Etapa {step} de 4</p>
        </motion.div>

        <div className="mb-12 md:mb-16 min-h-[360px] md:min-h-[400px] flex items-center justify-center">
          {renderStep()}
        </div>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.3 }}
          className="flex justify-center gap-4"
        >
          {step > 1 && (
            <Button variant="ghost" onClick={() => setStep((prev) => prev - 1)} disabled={submitting}>
              Voltar
            </Button>
          )}
          <div className="flex flex-col items-center gap-2">
            {submitError && (
              <p className="text-sm text-destructive">{submitError}</p>
            )}
            <Button onClick={handleNext} disabled={submitting}>
              {submitting ? 'Salvando...' : step === 4 ? 'Começar jornada' : 'Continuar'}
            </Button>
          </div>
        </motion.div>
      </div>
    </div>
  );
}
