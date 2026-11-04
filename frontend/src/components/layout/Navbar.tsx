import React from 'react';
import { NavLink } from 'react-router-dom';
import { Bell, CheckSquare, LogOut, LayoutDashboard, ListTodo } from 'lucide-react';
import { User } from '../../types/auth';

export interface NavbarProps {
  user: User | null;
  unreadCount: number;
  onToggleNotificationDrawer: () => void;
  onLogout: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({
  user,
  unreadCount,
  onToggleNotificationDrawer,
  onLogout,
}) => {
  return (
    <header className="sticky top-0 z-40 bg-white border-b border-slate-200 shadow-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        {/* Brand Logo & Title */}
        <div className="flex items-center gap-8">
          <NavLink to="/dashboard" className="flex items-center gap-2.5 text-slate-900 group">
            <div className="w-9 h-9 rounded-lg bg-sky-600 flex items-center justify-center text-white shadow-sm group-hover:bg-sky-700 transition-colors">
              <CheckSquare className="w-5 h-5" />
            </div>
            <span className="font-bold text-lg tracking-tight">Task Manager</span>
          </NavLink>

          {/* Navigation Links */}
          <nav className="hidden sm:flex items-center gap-1">
            <NavLink
              to="/dashboard"
              className={({ isActive }) =>
                `flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                  isActive
                    ? 'bg-sky-50 text-sky-700'
                    : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
                }`
              }
            >
              <LayoutDashboard className="w-4 h-4" />
              Dashboard
            </NavLink>
            <NavLink
              to="/tasks"
              className={({ isActive }) =>
                `flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                  isActive
                    ? 'bg-sky-50 text-sky-700'
                    : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
                }`
              }
            >
              <ListTodo className="w-4 h-4" />
              Tasks
            </NavLink>
          </nav>
        </div>

        {/* Right Actions: Notifications & User Profile */}
        <div className="flex items-center gap-3">
          {/* Notification Bell */}
          <button
            type="button"
            onClick={onToggleNotificationDrawer}
            aria-label="Open notifications"
            className="relative p-2 text-slate-500 hover:text-slate-700 hover:bg-slate-100 rounded-lg transition-colors focus:outline-none focus:ring-2 focus:ring-sky-500"
          >
            <Bell className="w-5 h-5" />
            {unreadCount > 0 && (
              <span className="absolute top-1.5 right-1.5 min-w-[18px] h-[18px] px-1 bg-rose-600 text-white text-[10px] font-bold rounded-full flex items-center justify-center border-2 border-white animate-in zoom-in-50">
                {unreadCount > 99 ? '99+' : unreadCount}
              </span>
            )}
          </button>

          {/* User Profile & Logout */}
          <div className="flex items-center gap-3 pl-3 border-l border-slate-200">
            <div className="hidden md:flex flex-col text-right">
              <span className="text-sm font-semibold text-slate-800 leading-tight">
                {user?.fullName || 'User'}
              </span>
              <span className="text-xs text-slate-400">{user?.email}</span>
            </div>

            <button
              type="button"
              onClick={onLogout}
              aria-label="Log out"
              title="Log out"
              className="p-2 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-colors focus:outline-none focus:ring-2 focus:ring-rose-500"
            >
              <LogOut className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>
    </header>
  );
};
