import React from 'react';
import { TaskPriority, TaskStatus } from '../../types/task';

export interface BadgeProps {
  variant?: 'status' | 'priority' | 'default';
  value: TaskStatus | TaskPriority | string;
  className?: string;
}

export const Badge: React.FC<BadgeProps> = ({ variant = 'default', value, className = '' }) => {
  let styleClasses = 'bg-slate-100 text-slate-700 border-slate-200';

  if (variant === 'status') {
    switch (value) {
      case 'TODO':
        styleClasses = 'bg-sky-50 text-sky-700 border-sky-200';
        break;
      case 'IN_PROGRESS':
        styleClasses = 'bg-amber-50 text-amber-700 border-amber-200';
        break;
      case 'COMPLETED':
        styleClasses = 'bg-emerald-50 text-emerald-700 border-emerald-200';
        break;
      case 'CANCELLED':
        styleClasses = 'bg-slate-100 text-slate-600 border-slate-200';
        break;
    }
  } else if (variant === 'priority') {
    switch (value) {
      case 'LOW':
        styleClasses = 'bg-slate-50 text-slate-600 border-slate-200';
        break;
      case 'MEDIUM':
        styleClasses = 'bg-blue-50 text-blue-700 border-blue-200';
        break;
      case 'HIGH':
        styleClasses = 'bg-orange-50 text-orange-700 border-orange-200';
        break;
      case 'URGENT':
        styleClasses = 'bg-rose-50 text-rose-700 border-rose-200 animate-pulse';
        break;
    }
  }

  return (
    <span
      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold border ${styleClasses} ${className}`}
    >
      {value}
    </span>
  );
};

