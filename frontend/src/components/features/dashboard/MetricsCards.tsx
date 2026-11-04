import React from 'react';
import {
  ListTodo,
  Clock,
  PlayCircle,
  CheckCircle2,
  XCircle,
  Flame,
  AlertTriangle,
} from 'lucide-react';
import { TaskMetrics } from '../../../types/task';

export interface MetricsCardsProps {
  metrics: TaskMetrics | null;
  isLoading: boolean;
}

export const MetricsCards: React.FC<MetricsCardsProps> = ({ metrics, isLoading }) => {
  if (isLoading) {
    return (
      <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-7 gap-4">
        {Array.from({ length: 7 }).map((_, i) => (
          <div key={i} className="h-24 bg-white rounded-xl border border-slate-200 animate-pulse" />
        ))}
      </div>
    );
  }

  const cards = [
    {
      title: 'Total Tasks',
      count: metrics?.totalTasks ?? 0,
      icon: ListTodo,
      color: 'text-slate-700 bg-slate-100',
    },
    {
      title: 'To Do',
      count: metrics?.todoCount ?? 0,
      icon: Clock,
      color: 'text-sky-700 bg-sky-50 border-sky-100',
    },
    {
      title: 'In Progress',
      count: metrics?.inProgressCount ?? 0,
      icon: PlayCircle,
      color: 'text-amber-700 bg-amber-50 border-amber-100',
    },
    {
      title: 'Completed',
      count: metrics?.completedCount ?? 0,
      icon: CheckCircle2,
      color: 'text-emerald-700 bg-emerald-50 border-emerald-100',
    },
    {
      title: 'Cancelled',
      count: metrics?.cancelledCount ?? 0,
      icon: XCircle,
      color: 'text-slate-600 bg-slate-50 border-slate-100',
    },
    {
      title: 'High Priority',
      count: metrics?.highPriorityCount ?? 0,
      icon: Flame,
      color: 'text-orange-700 bg-orange-50 border-orange-100',
    },
    {
      title: 'Overdue',
      count: metrics?.overdueCount ?? 0,
      icon: AlertTriangle,
      color: 'text-rose-700 bg-rose-50 border-rose-100 font-bold',
      pulse: (metrics?.overdueCount ?? 0) > 0,
    },
  ];

  return (
    <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-7 gap-3.5">
      {cards.map((card) => {
        const Icon = card.icon;
        return (
          <div
            key={card.title}
            className={`p-4 rounded-xl bg-white border border-slate-200/80 shadow-sm flex flex-col justify-between transition-all hover:shadow-md ${
              card.pulse ? 'ring-2 ring-rose-400' : ''
            }`}
          >
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs font-medium text-slate-500 tracking-tight">{card.title}</span>
              <div className={`p-1.5 rounded-lg ${card.color}`}>
                <Icon className="w-3.5 h-3.5" />
              </div>
            </div>
            <div className="text-2xl font-bold tracking-tight text-slate-900">{card.count}</div>
          </div>
        );
      })}
    </div>
  );
};

