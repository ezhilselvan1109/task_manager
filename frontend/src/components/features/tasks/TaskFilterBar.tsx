import React from 'react';
import { Search, Plus, Filter, ArrowUpDown } from 'lucide-react';
import { TaskPriority, TaskStatus } from '../../../types/task';
import { Button } from '../../common/Button';

export interface TaskFilterBarProps {
  searchQuery: string;
  onSearchChange: (query: string) => void;
  selectedStatus: TaskStatus | 'ALL';
  onStatusChange: (status: TaskStatus | 'ALL') => void;
  selectedPriority: TaskPriority | 'ALL';
  onPriorityChange: (priority: TaskPriority | 'ALL') => void;
  selectedSort: string;
  onSortChange: (sort: string) => void;
  onOpenCreateModal: () => void;
}

export const TaskFilterBar: React.FC<TaskFilterBarProps> = ({
  searchQuery,
  onSearchChange,
  selectedStatus,
  onStatusChange,
  selectedPriority,
  onPriorityChange,
  selectedSort,
  onSortChange,
  onOpenCreateModal,
}) => {
  return (
    <div className="bg-white rounded-xl border border-slate-200/80 p-4 shadow-sm flex flex-col md:flex-row items-stretch md:items-center justify-between gap-3.5">
      {/* Search Input */}
      <div className="relative flex-1">
        <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
        <input
          type="text"
          value={searchQuery}
          onChange={(e) => onSearchChange(e.target.value)}
          placeholder="Search tasks by title or description..."
          className="w-full pl-9 pr-4 py-2 bg-slate-50 border border-slate-200 rounded-lg text-sm text-slate-900 placeholder:text-slate-400 focus:bg-white focus:outline-none focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500 transition-colors"
        />
      </div>

      {/* Filter and Sort Dropdowns */}
      <div className="flex flex-wrap items-center gap-2">
        {/* Status Filter */}
        <div className="flex items-center gap-1.5 bg-slate-50 border border-slate-200 rounded-lg px-2.5 py-1.5 text-xs text-slate-600">
          <Filter className="w-3.5 h-3.5 text-slate-400" />
          <select
            value={selectedStatus}
            onChange={(e) => onStatusChange(e.target.value as TaskStatus | 'ALL')}
            className="bg-transparent text-slate-700 font-medium focus:outline-none cursor-pointer"
            aria-label="Filter by status"
          >
            <option value="ALL">All Statuses</option>
            <option value="TODO">To Do</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="COMPLETED">Completed</option>
            <option value="CANCELLED">Cancelled</option>
          </select>
        </div>

        {/* Priority Filter */}
        <div className="flex items-center gap-1.5 bg-slate-50 border border-slate-200 rounded-lg px-2.5 py-1.5 text-xs text-slate-600">
          <select
            value={selectedPriority}
            onChange={(e) => onPriorityChange(e.target.value as TaskPriority | 'ALL')}
            className="bg-transparent text-slate-700 font-medium focus:outline-none cursor-pointer"
            aria-label="Filter by priority"
          >
            <option value="ALL">All Priorities</option>
            <option value="LOW">Low</option>
            <option value="MEDIUM">Medium</option>
            <option value="HIGH">High</option>
            <option value="URGENT">Urgent</option>
          </select>
        </div>

        {/* Sort Order */}
        <div className="flex items-center gap-1.5 bg-slate-50 border border-slate-200 rounded-lg px-2.5 py-1.5 text-xs text-slate-600">
          <ArrowUpDown className="w-3.5 h-3.5 text-slate-400" />
          <select
            value={selectedSort}
            onChange={(e) => onSortChange(e.target.value)}
            className="bg-transparent text-slate-700 font-medium focus:outline-none cursor-pointer"
            aria-label="Sort tasks"
          >
            <option value="createdAt,desc">Newest First</option>
            <option value="createdAt,asc">Oldest First</option>
            <option value="dueDate,asc">Due Date (Earliest)</option>
            <option value="dueDate,desc">Due Date (Latest)</option>
            <option value="priority,desc">Priority (High to Low)</option>
            <option value="title,asc">Title (A - Z)</option>
          </select>
        </div>

        {/* New Task Button */}
        <Button
          variant="primary"
          size="sm"
          onClick={onOpenCreateModal}
          className="whitespace-nowrap"
        >
          <Plus className="w-4 h-4" />
          New Task
        </Button>
      </div>
    </div>
  );
};

