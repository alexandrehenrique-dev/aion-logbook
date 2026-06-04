import {
  BarChart3,
  Bell,
  BookMarked,
  BookOpen,
  Bug,
  Calendar as CalendarIcon,
  CheckCircle2,
  Clock,
  Compass,
  LayoutDashboard,
  LogOut,
  Menu,
  Search,
  Settings as SettingsIcon,
  User,
  X,
} from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { useCallback, useEffect, useRef, useState, type ReactNode } from 'react';
import { NavLink, useNavigate } from 'react-router';
import { useAuth } from '../../features/auth/AuthContext';
import { ThemeToggle } from '../components/ThemeToggle';
import { BugReportModal } from '../../features/bug-report/BugReportModal';
import { notificationService } from '../../services/notificationService';
import type { AppNotification } from '../../services/notificationService';
import { searchService } from '../../services/searchService';
import type { SearchResult } from '../../services/searchService';

const navItems = [
  { path: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { path: '/plans', label: 'Planos', icon: BookOpen },
  { path: '/directions', label: 'Direções', icon: Compass },
  { path: '/logbook', label: 'Logbook', icon: BookMarked },
  { path: '/analytics', label: 'Observatório', icon: BarChart3 },
  { path: '/sessions', label: 'Sessões', icon: Clock },
  { path: '/calendar', label: 'Calendário', icon: CalendarIcon },
];

function NotificationPanel({ onClose }: { onClose: () => void }) {
  const [notifs, setNotifs] = useState<AppNotification[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    notificationService.list()
      .then(setNotifs)
      .finally(() => setLoading(false));
  }, []);

  const handleMarkAllRead = async () => {
    await notificationService.markAllRead();
    setNotifs((prev) => prev.map((n) => ({ ...n, unread: false })));
  };

  const handleMarkRead = async (id: string) => {
    await notificationService.markRead(id);
    setNotifs((prev) => prev.map((n) => n.id === id ? { ...n, unread: false } : n));
  };

  const unread = notifs.filter((n) => n.unread).length;

  return (
    <motion.div
      initial={{ opacity: 0, y: -8, scale: 0.97 }}
      animate={{ opacity: 1, y: 0, scale: 1 }}
      exit={{ opacity: 0, y: -8, scale: 0.97 }}
      transition={{ duration: 0.15 }}
      className="absolute right-0 top-full mt-2 w-80 bg-card border border-border rounded-xl shadow-xl z-50 overflow-hidden"
    >
      <div className="px-4 py-3 border-b border-border flex items-center justify-between">
        <h3 className="text-sm font-medium text-foreground">Notificações</h3>
        <div className="flex items-center gap-2">
          {unread > 0 && (
            <button onClick={handleMarkAllRead} className="text-xs text-primary hover:underline">
              Marcar todas
            </button>
          )}
          <button onClick={onClose} className="p-1 rounded hover:bg-muted transition-colors">
            <X className="w-3 h-3 text-muted-foreground" />
          </button>
        </div>
      </div>
      <div className="max-h-72 overflow-y-auto">
        {loading ? (
          <p className="text-sm text-muted-foreground text-center py-8">Carregando...</p>
        ) : notifs.length === 0 ? (
          <p className="text-sm text-muted-foreground text-center py-8">Nenhuma notificação</p>
        ) : (
          notifs.map((n) => (
            <div
              key={n.id}
              onClick={() => n.unread && handleMarkRead(n.id)}
              className={`px-4 py-3 border-b border-border last:border-0 flex gap-3 cursor-pointer hover:bg-muted/30 transition-colors ${n.unread ? 'bg-primary/5' : ''}`}
            >
              {n.type === 'due' && <Clock className="w-4 h-4 text-amber-500 mt-0.5 shrink-0" />}
              {n.type === 'in_progress' && <Clock className="w-4 h-4 text-primary mt-0.5 shrink-0 animate-pulse" />}
              {n.type === 'completed' && <CheckCircle2 className="w-4 h-4 text-emerald-500 mt-0.5 shrink-0" />}
              {n.type === 'missed' && <Clock className="w-4 h-4 text-destructive/70 mt-0.5 shrink-0" />}
              {n.type === 'info' && <Bell className="w-4 h-4 text-muted-foreground mt-0.5 shrink-0" />}
              <div className="min-w-0">
                <p className="text-xs font-medium text-foreground truncate">{n.title}</p>
                {n.description && <p className="text-xs text-muted-foreground truncate">{n.description}</p>}
              </div>
              {n.unread && <span className="ml-auto w-2 h-2 rounded-full bg-primary mt-1 shrink-0" />}
            </div>
          ))
        )}
      </div>
    </motion.div>
  );
}

function ProfileMenu({ onClose, onBugReport }: { onClose: () => void; onBugReport: () => void }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <motion.div
      initial={{ opacity: 0, y: -8, scale: 0.97 }}
      animate={{ opacity: 1, y: 0, scale: 1 }}
      exit={{ opacity: 0, y: -8, scale: 0.97 }}
      transition={{ duration: 0.15 }}
      className="absolute right-0 top-full mt-2 w-56 bg-card border border-border rounded-xl shadow-xl z-50 overflow-hidden"
    >
      <div className="px-4 py-3 border-b border-border">
        <p className="text-sm font-medium text-foreground">{user?.name ?? 'Viajante'}</p>
        <p className="text-xs text-muted-foreground">{user?.email ?? ''}</p>
      </div>
      <div className="py-1">
        <button
          onClick={() => { onBugReport(); onClose(); }}
          className="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-muted-foreground hover:bg-muted hover:text-foreground transition-colors"
        >
          <Bug className="w-4 h-4" />
          Reportar bug
        </button>
        <button
          onClick={handleLogout}
          className="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-destructive hover:bg-destructive/10 transition-colors"
        >
          <LogOut className="w-4 h-4" />
          Sair
        </button>
      </div>
    </motion.div>
  );
}

const TYPE_LABEL: Record<string, string> = {
  plan: 'Plano',
  direction: 'Direção',
  log: 'Logbook',
};

function GlobalSearch() {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<SearchResult[]>([]);
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const searchRef = useRef<HTMLDivElement>(null);
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const runSearch = useCallback((q: string) => {
    if (!q.trim()) { setResults([]); setOpen(false); return; }
    setLoading(true);
    searchService.search(q)
      .then((r) => { setResults(r); setOpen(true); })
      .finally(() => setLoading(false));
  }, []);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const q = e.target.value;
    setQuery(q);
    if (debounceRef.current) clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => runSearch(q), 300);
  };

  const handleSelect = (result: SearchResult) => {
    navigate(result.path);
    setQuery('');
    setResults([]);
    setOpen(false);
  };

  useEffect(() => {
    function handleClick(e: MouseEvent) {
      if (searchRef.current && !searchRef.current.contains(e.target as Node)) setOpen(false);
    }
    document.addEventListener('mousedown', handleClick);
    return () => document.removeEventListener('mousedown', handleClick);
  }, []);

  const grouped = results.reduce<Record<string, SearchResult[]>>((acc, r) => {
    (acc[r.type] ??= []).push(r);
    return acc;
  }, {});

  return (
    <div className="relative flex-1 max-w-md" ref={searchRef}>
      <div className="relative">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
        <input
          type="text"
          value={query}
          onChange={handleChange}
          onFocus={() => query.trim() && setOpen(true)}
          placeholder="Buscar planos, direções, logs..."
          className="w-full pl-9 pr-4 py-2 rounded-lg bg-muted/50 border border-border text-sm focus:outline-none focus:ring-2 focus:ring-ring"
        />
        {loading && (
          <span className="absolute right-3 top-1/2 -translate-y-1/2 w-3 h-3 border-2 border-primary border-t-transparent rounded-full animate-spin" />
        )}
      </div>
      <AnimatePresence>
        {open && (
          <motion.div
            initial={{ opacity: 0, y: -6 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -6 }}
            transition={{ duration: 0.15 }}
            className="absolute top-full mt-2 left-0 right-0 bg-card border border-border rounded-xl shadow-xl z-50 overflow-hidden max-h-80 overflow-y-auto"
          >
            {results.length === 0 ? (
              <p className="text-sm text-muted-foreground text-center py-6">Nenhum resultado para "{query}"</p>
            ) : (
              Object.entries(grouped).map(([type, items]) => (
                <div key={type}>
                  <p className="text-xs font-medium text-muted-foreground px-4 py-2 bg-muted/30">{TYPE_LABEL[type] ?? type}</p>
                  {items.map((r) => (
                    <button
                      key={r.id}
                      onClick={() => handleSelect(r)}
                      className="w-full text-left px-4 py-2.5 hover:bg-muted/50 transition-colors flex flex-col gap-0.5"
                    >
                      <span className="text-sm text-foreground truncate">{r.title}</span>
                      {r.subtitle && <span className="text-xs text-muted-foreground">{r.subtitle}</span>}
                    </button>
                  ))}
                </div>
              ))
            )}
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}

export function MainLayout({ children }: { children: ReactNode }) {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [notifOpen, setNotifOpen] = useState(false);
  const [profileOpen, setProfileOpen] = useState(false);
  const [bugReportOpen, setBugReportOpen] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);

  const notifRef = useRef<HTMLDivElement>(null);
  const profileRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    notificationService.list()
      .then((notifs) => setUnreadCount(notifs.filter((n) => n.unread).length))
      .catch(() => {});
  }, []);

  // Close dropdowns on outside click
  useEffect(() => {
    function handleClick(e: MouseEvent) {
      if (notifRef.current && !notifRef.current.contains(e.target as Node)) setNotifOpen(false);
      if (profileRef.current && !profileRef.current.contains(e.target as Node)) setProfileOpen(false);
    }
    document.addEventListener('mousedown', handleClick);
    return () => document.removeEventListener('mousedown', handleClick);
  }, []);

  const SidebarContent = () => (
    <>
      <div className="p-6 border-b border-border">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-primary to-accent flex items-center justify-center shrink-0">
            <Compass className="w-5 h-5 text-white" strokeWidth={2} />
          </div>
          <div className="min-w-0">
            <h2 className="text-lg font-medium text-foreground truncate">Aion Logbook</h2>
            <p className="text-xs text-muted-foreground">Sua jornada</p>
          </div>
        </div>
      </div>

      <nav className="flex-1 p-4 overflow-y-auto">
        <div className="space-y-1">
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.path}
                to={item.path}
                onClick={() => setSidebarOpen(false)}
                className={({ isActive }) =>
                  `flex items-center gap-3 px-4 py-3 rounded-lg transition-all duration-200 ${
                    isActive
                      ? 'bg-primary text-primary-foreground shadow-sm'
                      : 'text-muted-foreground hover:bg-muted hover:text-foreground'
                  }`
                }
              >
                {({ isActive }) => (
                  <>
                    <Icon className="w-5 h-5 shrink-0" strokeWidth={isActive ? 2 : 1.5} />
                    <span className={`text-sm ${isActive ? 'font-medium' : ''}`}>{item.label}</span>
                  </>
                )}
              </NavLink>
            );
          })}
        </div>
      </nav>

      <div className="p-4 border-t border-border space-y-1">
        <NavLink
          to="/settings"
          onClick={() => setSidebarOpen(false)}
          className={({ isActive }) =>
            `flex items-center gap-3 px-4 py-3 rounded-lg transition-colors ${
              isActive
                ? 'bg-primary text-primary-foreground'
                : 'text-muted-foreground hover:bg-muted hover:text-foreground'
            }`
          }
        >
          <SettingsIcon className="w-5 h-5 shrink-0" strokeWidth={1.5} />
          <span className="text-sm">Configurações</span>
        </NavLink>
      </div>
    </>
  );

  return (
    <div className="flex h-screen bg-background overflow-hidden">
      {/* Desktop sidebar */}
      <aside className="hidden md:flex w-64 border-r border-border bg-card/30 backdrop-blur-sm flex-col shrink-0 h-screen sticky top-0 overflow-y-auto">
        <SidebarContent />
      </aside>

      {/* Mobile sidebar overlay */}
      <AnimatePresence>
        {sidebarOpen && (
          <>
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="fixed inset-0 bg-black/50 z-40 md:hidden"
              onClick={() => setSidebarOpen(false)}
            />
            <motion.aside
              initial={{ x: -280 }}
              animate={{ x: 0 }}
              exit={{ x: -280 }}
              transition={{ type: 'spring', damping: 30, stiffness: 300 }}
              className="fixed left-0 top-0 bottom-0 w-72 bg-card border-r border-border flex flex-col z-50 md:hidden"
            >
              <button
                onClick={() => setSidebarOpen(false)}
                className="absolute top-4 right-4 p-2 rounded-lg hover:bg-muted transition-colors"
                aria-label="Fechar menu"
              >
                <X className="w-5 h-5 text-muted-foreground" />
              </button>
              <SidebarContent />
            </motion.aside>
          </>
        )}
      </AnimatePresence>

      {/* Main content */}
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        {/* Top header */}
        <header className="border-b border-border bg-card/30 backdrop-blur-sm shrink-0 z-30">
          <div className="px-4 md:px-6 py-3 flex items-center gap-3">
            {/* Mobile hamburger */}
            <button
              onClick={() => setSidebarOpen(true)}
              className="md:hidden p-2 rounded-lg hover:bg-muted/50 transition-colors"
              aria-label="Abrir menu"
            >
              <Menu className="w-5 h-5 text-muted-foreground" />
            </button>

            {/* Search */}
            <GlobalSearch />

            {/* Right actions */}
            <div className="flex items-center gap-1 ml-auto">
              {/* Notifications */}
              <div className="relative" ref={notifRef}>
                <button
                  onClick={() => { setNotifOpen((p) => !p); setProfileOpen(false); }}
                  className="p-2 rounded-lg hover:bg-muted/50 transition-colors relative"
                  aria-label="Notificações"
                >
                  <Bell className="w-5 h-5 text-muted-foreground" />
                  {unreadCount > 0 && (
                    <span className="absolute top-1.5 right-1.5 w-2 h-2 bg-primary rounded-full" />
                  )}
                </button>
                <AnimatePresence>
                  {notifOpen && <NotificationPanel onClose={() => setNotifOpen(false)} />}
                </AnimatePresence>
              </div>

              <ThemeToggle />

              {/* Profile */}
              <div className="relative" ref={profileRef}>
                <button
                  onClick={() => { setProfileOpen((p) => !p); setNotifOpen(false); }}
                  className="p-2 rounded-lg hover:bg-muted/50 transition-colors"
                  aria-label="Perfil"
                >
                  <User className="w-5 h-5 text-muted-foreground" />
                </button>
                <AnimatePresence>
                  {profileOpen && (
                    <ProfileMenu
                      onClose={() => setProfileOpen(false)}
                      onBugReport={() => setBugReportOpen(true)}
                    />
                  )}
                </AnimatePresence>
              </div>
            </div>
          </div>
        </header>

        {/* Page content */}
        <main className="flex-1 overflow-y-auto">
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 0.3 }}
          >
            {children}
          </motion.div>
        </main>
      </div>

      {bugReportOpen && <BugReportModal onClose={() => setBugReportOpen(false)} />}
    </div>
  );
}
