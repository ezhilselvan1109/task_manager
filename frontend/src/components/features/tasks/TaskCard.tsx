import React from 'react';
import {
  Calendar,
  AlertTriangle,
  Play,
  Check,
  X,
  RotateCcw,
  Pencil,
  Trash2,
} from 'lucide-react';
import { Task, TaskStatus } from '../../../types/task';
import { Badge } from '../../common/Badge';

export interface TaskCardProps {
  task: Task;
  onStatusChange: (id: string, newStatus: TaskStatus) => void;
  onEdit: (task: Task) => void;
  onDelete: (id: string) => void;
}

export const TaskCard: React.FC<TaskCardProps> = ({
  task,
  onStatusChange,
  onEdit,
  onDelete,
}) => {
  const isOverdue =
    task.dueDate &&
    new Date(task.dueDate) < new Date() &&
    task.status !== 'COMPLETED' &&
    task.status !== 'CANCELLED';

  const formatDueDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString([], {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className="bg-white rounded-xl border border-slate-200/90 p-4 shadow-sm hover:shadow-md transition-all flex flex-col justify-between gap-3 group">
      <div>
        {/* Header Badges & Actions */}
        <div className="flex items-center justify-between gap-2 mb-2">
          <div className="flex items-center gap-1.5 flex-wrap">
            <Badge variant="priority" value={task.priority} />
            <Badge variant="status" value={task.status} />
          </div>

          <div className="flex items-center gap-1 opacity-80 group-hover:opacity-100 transition-opacity">
            <button
              type="button"
              onClick={() => onEdit(task)}
              aria-label="Edit task"
              title="Edit task"
              className="p-1 text-slate-400 hover:text-sky-600 rounded hover:bg-sky-50 transition-colors"
            >
              <Pencil className="w-3.5 h-3.5" />
            </button>
            <button
              type="button"
              onClick={() => onDelete(task.id)}
              aria-label="Delete task"
              title="Delete task"
              className="p-1 text-slate-400 hover:text-rose-600 rounded hover:bg-rose-50 transition-colors"
            >
              <Trash2 className="w-3.5 h-3.5" />
            </button>
          </div>
        </div>

        {/* Title & Description */}
        <h4 className="text-sm font-semibold text-slate-900 leading-snug mb-1 group-hover:text-sky-600 transition-colors">
          {task.title}
        </h4>
        {task.description && (
          <p className="text-xs text-slate-500 line-clamp-2 leading-relaxed mb-3">
            {task.description}
          </p>
        )}

        {/* Due Date Indicator */}
        {task.dueDate && (
          <div
            className={`flex items-center gap-1.5 text-xs font-medium ${
              isOverdue ? 'text-rose-600' : 'text-slate-500'
            }`}
          >
            {isOverdue ? (
              <AlertTriangle className="w-3.5 h-3.5 shrink-0 animate-bounce" />
            ) : (
              <Calendar className="w-3.5 h-3.5 shrink-0" />
            )}
            <span>{formatDueDate(task.dueDate)}</span>
            {isOverdue && <span className="text-[10px] font-bold uppercase tracking-wider">(Overdue)</span>}
          </div>
        )}
      </div>

      {/* State Machine Transition Actions */}
      <div className="pt-3 border-t border-slate-100 flex items-center justify-end gap-1.5">
        {task.status === 'TODO' && (
          <>
            <button
              type="button"
              onClick={() => onStatusChange(task.id, 'IN_PROGRESS')}
              aria-label="Start working"
              className="flex items-center gap-1 px-2 py-1 text-xs font-medium text-amber-700 bg-amber-50 hover:bg-amber-100 rounded-md transition-colors"
            >
              <Play className="w-3 h-3 fill-current" />
              Start
            </button>
            <button
              type="button"
              onClick={() => onStatusChange(task.id, 'COMPLETED')}
              aria-label="Complete task"
              className="flex items-center gap-1 px-2 py-1 text-xs font-medium text-emerald-700 bg-emerald-50 hover:bg-emerald-100 rounded-md transition-colors"
            >
              <Check className="w-3 h-3" />
              Complete
            </button>
            <button
              type="button"
              onClick={() => onStatusChange(task.id, 'CANCELLED')}
              aria-label="Cancel task"
              className="flex items-center gap-1 px-2 py-1 text-xs font-medium text-slate-600 bg-slate-50 hover:bg-slate-100 rounded-md transition-colors"
            >
              <X className="w-3 h-3" />
              Cancel
            </button>
          </>
        )}

        {task.status === 'IN_PROGRESS' && (
          <>
            <button
              type="button"
              onClick={() => onStatusChange(task.id, 'COMPLETED')}
              aria-label="Complete task"
              className="flex items-center gap-1 px-2 py-1 text-xs font-medium text-emerald-700 bg-emerald-50 hover:bg-emerald-100 rounded-md transition-colors"
            >
              <Check className="w-3 h-3" />
              Complete
            </button>
            <button
              type="button"
              onClick={() => onStatusChange(task.id, 'CANCELLED')}
              aria-label="Cancel task"
              className="flex items-center gap-1 px-2 py-1 text-xs font-medium text-slate-600 bg-slate-50 hover:bg-slate-100 rounded-md transition-colors"
            >
              <X className="w-3 h-3" />
              Cancel
            </button>
          </>
        )}

        {task.status === 'COMPLETED' && (
          <button
            type="button"
            onClick={() => onStatusChange(task.id, 'IN_PROGRESS')}
            aria-label="Reopen task"
            className="flex items-center gap-1 px-2 py-1 text-xs font-medium text-sky-700 bg-sky-50 hover:bg-sky-100 rounded-md transition-colors"
          >
            <RotateCcw className="w-3 h-3" />
            Reopen
          </button>
        )}

        {task.status === 'CANCELLED' && (
          <button
            type="button"
            onClick={() => onStatusChange(task.id, 'TODO')}
            aria-label="Reopen task"
            className="flex items-center gap-1 px-2 py-1 text-xs font-medium text-sky-700 bg-sky-50 hover:bg-sky-100 rounded-md transition-colors"
          >
            <RotateCcw className="w-3 h-3" />
            Reactivate
          </button>
        )}
      </div>
    </div>
  );
};

