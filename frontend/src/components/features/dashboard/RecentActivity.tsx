import React from 'react';
import { NavLink } from 'react-router-dom';
import { ArrowRight, Bell, CheckCircle2, PlusCircle, RefreshCw, Trash2 } from 'lucide-react';
import { NotificationItem } from '../../../types/notification';

export interface RecentActivityProps {
  notifications: NotificationItem[];
  isLoading: boolean;
}

export const RecentActivity: React.FC<RecentActivityProps> = ({ notifications, isLoading }) => {
  const renderIcon = (eventType: string) => {
    switch (eventType) {
      case 'TASK_COMPLETED':
        return <CheckCircle2 className="w-4 h-4 text-emerald-500" />;
      case 'TASK_CREATED':
        return <PlusCircle className="w-4 h-4 text-sky-500" />;
      case 'TASK_DELETED':
        return <Trash2 className="w-4 h-4 text-rose-500" />;
      default:
        return <RefreshCw className="w-4 h-4 text-amber-500" />;
    }
  };

  return (
    <div className="bg-white rounded-xl border border-slate-200/80 shadow-sm p-6">
      <div className="flex items-center justify-between pb-4 border-b border-slate-100 mb-4">
        <div className="flex items-center gap-2">
          <Bell className="w-4 h-4 text-slate-500" />
          <h3 className="text-base font-bold text-slate-800">Recent Activity</h3>
        </div>
        <NavLink
          to="/tasks"
          className="text-xs font-semibold text-sky-600 hover:text-sky-700 flex items-center gap-1 transition-colors"
        >
          View all tasks
          <ArrowRight className="w-3.5 h-3.5" />
        </NavLink>
      </div>

      {isLoading ? (
        <div className="space-y-3">
          {Array.from({ length: 4 }).map((_, i) => (
            <div key={i} className="h-12 bg-slate-50 rounded-lg animate-pulse" />
          ))}
        </div>
      ) : notifications.length === 0 ? (
        <div className="py-8 text-center text-slate-400 text-sm">
          No recent activity to display.
        </div>
      ) : (
        <div className="divide-y divide-slate-50">
          {notifications.slice(0, 5).map((item) => (
            <div key={item.id} className="py-3 flex items-start gap-3">
              <div className="p-1.5 rounded-full bg-slate-50 shrink-0 mt-0.5">
                {renderIcon(item.eventType)}
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-xs font-semibold text-slate-800 truncate mb-0.5">{item.title}</p>
                <p className="text-xs text-slate-500 truncate">{item.message}</p>
              </div>
              <span className="text-[11px] text-slate-400 shrink-0">
                {new Date(item.createdAt).toLocaleDateString([], {
                  month: 'short',
                  day: 'numeric',
                })}
              </span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

