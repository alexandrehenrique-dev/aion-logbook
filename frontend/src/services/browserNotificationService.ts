export const browserNotificationService = {
  isSupported(): boolean {
    return 'Notification' in window;
  },

  getPermission(): NotificationPermission | 'unsupported' {
    if (!this.isSupported()) return 'unsupported';
    return Notification.permission;
  },

  async requestPermission(): Promise<NotificationPermission | 'unsupported'> {
    if (!this.isSupported()) return 'unsupported';
    if (Notification.permission === 'granted') return 'granted';
    return Notification.requestPermission();
  },

  notify(title: string, options?: NotificationOptions & { onClick?: () => void }): void {
    if (!this.isSupported() || Notification.permission !== 'granted') return;
    const { onClick, ...notifOptions } = options ?? {};
    const notif = new Notification(title, {
      icon: '/favicon.svg',
      badge: '/favicon.svg',
      ...notifOptions,
    });
    if (onClick) notif.onclick = onClick;
  },
};
