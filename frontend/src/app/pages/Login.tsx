import { Compass } from 'lucide-react';
import { motion } from 'motion/react';
import { useState } from 'react';
import { useNavigate } from 'react-router';
import { Button } from '../components/Button';
import { ThemeToggle } from '../components/ThemeToggle';
import { useAuth } from '../../features/auth/AuthContext';

const ONBOARDING_KEY = 'aion:onboarding-completed';

export function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [isEntering, setIsEntering] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleEnter = async () => {
    setIsEntering(true);
    setError(null);
    try {
      await login('dev@aion.app', 'mock');
      const onboardingDone = localStorage.getItem(ONBOARDING_KEY) === 'true';
      navigate(onboardingDone ? '/dashboard' : '/onboarding');
    } catch {
      setError('Não foi possível entrar. Tente novamente.');
      setIsEntering(false);
    }
  };

  return (
    <div className="min-h-screen relative overflow-hidden bg-gradient-to-br from-background via-background to-muted/20">
      {/* Animated particles */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        {[...Array(20)].map((_, i) => (
          <motion.div
            key={i}
            className="absolute w-1 h-1 rounded-full bg-primary/10"
            initial={{
              x: Math.random() * (typeof window !== 'undefined' ? window.innerWidth : 800),
              y: Math.random() * (typeof window !== 'undefined' ? window.innerHeight : 600),
            }}
            animate={{
              y: [null, Math.random() * (typeof window !== 'undefined' ? window.innerHeight : 600)],
              opacity: [0, 0.5, 0],
            }}
            transition={{
              duration: 10 + Math.random() * 10,
              repeat: Infinity,
              ease: 'linear',
              delay: Math.random() * 5,
            }}
          />
        ))}
      </div>

      <motion.div
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6, delay: 0.2 }}
        className="absolute top-6 right-6 z-10"
      >
        <ThemeToggle />
      </motion.div>

      <div className="relative z-10 flex items-center justify-center min-h-screen px-4">
        <motion.div
          initial={{ opacity: 0, scale: 0.95 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.8, ease: 'easeOut' }}
          className="max-w-2xl w-full text-center"
        >
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.3 }}
            className="mb-12 flex flex-col items-center gap-6"
          >
            <motion.div
              animate={{ rotate: [0, 360] }}
              transition={{ duration: 40, repeat: Infinity, ease: 'linear' }}
              className="w-20 h-20 rounded-2xl bg-gradient-to-br from-primary to-accent flex items-center justify-center shadow-xl"
            >
              <Compass className="w-10 h-10 text-white" strokeWidth={1.5} />
            </motion.div>
            <h1 className="text-4xl md:text-6xl font-medium text-foreground tracking-tight">
              Aion Logbook
            </h1>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.5 }}
            className="mb-8 space-y-4"
          >
            <p className="text-xl md:text-2xl text-foreground/90 font-light leading-relaxed">
              Não é uma agenda.
              <br />
              É um mapa para não se perder de si mesmo.
            </p>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.7 }}
            className="mb-12"
          >
            <p className="text-base md:text-lg text-muted-foreground leading-relaxed max-w-xl mx-auto">
              Planejar é lembrar da direção.
              <br />
              Registrar é provar que a jornada aconteceu.
            </p>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.9 }}
          >
            {error && (
              <p className="text-sm text-destructive mb-4">{error}</p>
            )}
            <Button size="lg" onClick={handleEnter} disabled={isEntering} className="px-12 py-4 text-lg">
              {isEntering ? 'Entrando...' : 'Entrar'}
            </Button>
          </motion.div>

          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 0.6, delay: 1.2 }}
            className="mt-16"
          >
            <p className="text-sm text-muted-foreground/60">
              Um sistema pessoal de direção, memória e travessia
            </p>
          </motion.div>
        </motion.div>
      </div>

      <div className="absolute bottom-0 left-0 right-0 h-32 bg-gradient-to-t from-background/50 to-transparent pointer-events-none" />
    </div>
  );
}
