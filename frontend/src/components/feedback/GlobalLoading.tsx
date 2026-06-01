import { Compass } from 'lucide-react';
import { motion } from 'motion/react';

export function GlobalLoading() {
  return (
    <div className="fixed inset-0 bg-background/80 backdrop-blur-sm z-50 flex items-center justify-center">
      <motion.div
        animate={{ rotate: 360 }}
        transition={{ duration: 2, repeat: Infinity, ease: 'linear' }}
      >
        <Compass className="w-10 h-10 text-primary" strokeWidth={1.5} />
      </motion.div>
    </div>
  );
}
