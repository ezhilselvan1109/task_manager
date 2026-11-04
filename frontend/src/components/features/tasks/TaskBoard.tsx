import React from 'react';
import { Task, TaskStatus } from '../../../types/task';
import { TaskCard } from './TaskCard';
import { LoadingSpinner } from '../../common/LoadingSpinner';

export interface TaskBoardProps {
  tasks: Task[];
  isLoading?: boolean;
  onStatusChange: (id: string, newStatus: TaskStatus) => void;
  onEdit: (task: Task) => void;
  onDelete: (id: string) => void;
}

interface LaneConfig {
  status: TaskStatus;
  title: string;
  badgeBg: string;
  badgeText: string;
  topBorder: string;
}

const LANES: LaneConfig[] = [
  {
    status: 'TODO',
    title: 'To Do',
    badgeBg: 'bg-blue-100',
    badgeText: 'text-blue-800',
    topBorder: 'border-t-blue-500',
  },
  {
    status: 'IN_PROGRESS',
    title: 'In Progress',
    badgeBg: 'bg-amber-100',
    badgeText: 'text-amber-800',
    topBorder: 'border-t-amber-500',
  },
  {
    status: 'COMPLETED',
    title: 'Completed',
    badgeBg: 'bg-emerald-100',
    badgeText: 'text-emerald-800',
    topBorder: 'border-t-emerald-500',
  },
  {
    status: 'CANCELLED',
    title: 'Cancelled',
    badgeBg: 'bg-slate-200',
    badgeText: 'text-slate-700',
    topBorder: 'border-t-slate-400',
  },
];

export const TaskBoard: React.FC<TaskBoardProps> = ({
  tasks,
  isLoading = false,
  onStatusChange,
  onEdit,
  onDelete,
}) => {
  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-24">
        <LoadingSpinner size="lg" text="Loading tasks..." />
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-5 items-start">
      {LANES.map((lane) => {
        const laneTasks = tasks.filter((t) => t.status === lane.status);

        return (
          <div
            key={lane.status}
            data-testid={`lane-${lane.status.toLowerCase()}`}
            className={`bg-slate-50/70 rounded-xl border border-slate-200/80 border-t-4 ${lane.topBorder} p-4 flex flex-col gap-3 min-h-[400px]`}
          >
            {/* Lane Header */}
            <div className="flex items-center justify-between px-1">
              <h3 className="font-semibold text-slate-800 text-sm">{lane.title}</h3>
              <span
                className={`text-xs font-semibold px-2 py-0.5 rounded-full ${lane.badgeBg} ${lane.badgeText}`}
              >
                {laneTasks.length}
              </span>
            </div>

            {/* Lane Cards */}
            <div className="flex flex-col gap-3">
              {laneTasks.length === 0 ? (
                <div className="py-8 text-center text-xs text-slate-400 border border-dashed border-slate-200 rounded-lg">
                  No tasks in this lane
                </div>
              ) : (
                laneTasks.map((task) => (
                  <TaskCard
                    key={task.id}
                    task={task}
                    onStatusChange={onStatusChange}
                    onEdit={onEdit}
                    onDelete={onDelete}
                  />
                ))
              )}
            </div>
          </div>
        );
      })}
    </div>
  );
};

