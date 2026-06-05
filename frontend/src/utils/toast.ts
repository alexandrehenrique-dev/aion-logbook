import { toast as sonnerToast } from 'sonner';
import { browserNotificationService } from '../services/browserNotificationService';

export const toast = {
  success(title: string, description?: string) {
    sonnerToast.success(title, { description });
  },
  error(title: string, description?: string) {
    sonnerToast.error(title, { description });
  },
  info(title: string, description?: string) {
    sonnerToast(title, { description });
  },

  notify(title: string, body?: string, onClick?: () => void) {
    sonnerToast(title, { description: body });
    browserNotificationService.notify(title, { body, onClick, alwaysFire: true });
  },
};
