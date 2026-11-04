import React, { useState, useEffect, useCallback } from 'react';
import { ChevronLeft, ChevronRight, AlertCircle, RefreshCw } from 'lucide-react';
import { Task, TaskPriority, TaskStatus, CreateTaskRequest, UpdateTaskRequest } from '../types/task';
import { taskApi } from '../api/taskApi';
import { TaskFilterBar } from '../components/features/tasks/TaskFilterBar';
import { TaskBoard } from '../components/features/tasks/TaskBoard';
import { TaskModal } from '../components/features/tasks/TaskModal';
import { DeleteConfirmModal } from '../components/features/tasks/DeleteConfirmModal';
import { Button } from '../components/common/Button';

export const TasksPage: React.FC = () => {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Filters & Pagination
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedStatus, setSelectedStatus] = useState<TaskStatus | 'ALL'>('ALL');
  const [selectedPriority, setSelectedPriority] = useState<TaskPriority | 'ALL'>('ALL');
  const [selectedSort, setSelectedSort] = useState('createdAt,desc');
  const [page, setPage] = useState(0);
  const [pageSize] = useState(20);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  // Modals state
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [editingTask, setEditingTask] = useState<Task | null>(null);
  const [deletingTask, setDeletingTask] = useState<{ id: string; title: string } | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const fetchTasks = useCallback(async () => {
    setIsLoading(true);
    setError(null);

    try {
      const response = await taskApi.getTasks({
        query: searchQuery.trim() || undefined,
        status: selectedStatus !== 'ALL' ? [selectedStatus] : undefined,
        priority: selectedPriority !== 'ALL' ? [selectedPriority] : undefined,
        sort: selectedSort,
        page,
        size: pageSize,
      });

      setTasks(response.content || []);
      setTotalPages(response.totalPages || 1);
      setTotalElements(response.totalElements || 0);
    } catch (err: unknown) {
      const errorObj = err as { response?: { data?: { detail?: string } }; message?: string };
      setError(errorObj.response?.data?.detail || errorObj.message || 'Failed to load tasks');
    } finally {
      setIsLoading(false);
    }
  }, [searchQuery, selectedStatus, selectedPriority, selectedSort, page, pageSize]);

  useEffect(() => {
    fetchTasks();
  }, [fetchTasks]);

  // Handle Search Input with reset to page 0
  const handleSearchChange = (query: string) => {
    setSearchQuery(query);
    setPage(0);
  };

  const handleStatusFilterChange = (status: TaskStatus | 'ALL') => {
    setSelectedStatus(status);
    setPage(0);
  };

  const handlePriorityFilterChange = (priority: TaskPriority | 'ALL') => {
    setSelectedPriority(priority);
    setPage(0);
  };

  const handleSortChange = (sort: string) => {
    setSelectedSort(sort);
    setPage(0);
  };

  // Status transitions
  const handleStatusChange = async (taskId: string, newStatus: TaskStatus) => {
    try {
      if (newStatus === 'COMPLETED') {
        await taskApi.completeTask(taskId);
      } else {
        await taskApi.changeStatus(taskId, newStatus);
      }
      // Re-fetch tasks after status transition to ensure consistent state
      fetchTasks();
    } catch (err: unknown) {
      const errorObj = err as { response?: { status?: number; data?: { detail?: string } }; message?: string };
      if (errorObj.response?.status === 404) {
        setError('Task not found or access denied (anti-enumeration).');
      } else {
        setError(errorObj.response?.data?.detail || errorObj.message || 'Failed to update task status');
      }
    }
  };

  // Create Task
  const handleCreateSubmit = async (data: CreateTaskRequest) => {
    setIsSubmitting(true);
    try {
      await taskApi.createTask(data);
      setIsCreateModalOpen(false);
      fetchTasks();
    } catch (err: unknown) {
      const errorObj = err as { response?: { data?: { detail?: string } }; message?: string };
      alert(errorObj.response?.data?.detail || errorObj.message || 'Failed to create task');
    } finally {
      setIsSubmitting(false);
    }
  };

  // Edit Task
  const handleEditSubmit = async (data: UpdateTaskRequest) => {
    if (!editingTask) return;
    setIsSubmitting(true);
    try {
      await taskApi.updateTask(editingTask.id, data);
      setEditingTask(null);
      fetchTasks();
    } catch (err: unknown) {
      const errorObj = err as { response?: { status?: number; data?: { detail?: string } }; message?: string };
      if (errorObj.response?.status === 404) {
        alert('Task not found or access denied.');
      } else {
        alert(errorObj.response?.data?.detail || errorObj.message || 'Failed to update task');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  // Delete Task
  const handleDeleteConfirm = async () => {
    if (!deletingTask) return;
    setIsSubmitting(true);
    try {
      await taskApi.deleteTask(deletingTask.id);
      setDeletingTask(null);
      fetchTasks();
    } catch (err: unknown) {
      const errorObj = err as { response?: { status?: number; data?: { detail?: string } }; message?: string };
      if (errorObj.response?.status === 404) {
        alert('Task not found or already deleted.');
      } else {
        alert(errorObj.response?.data?.detail || errorObj.message || 'Failed to delete task');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-2 border-b border-slate-200/80">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Task Workspace</h1>
          <p className="text-sm text-slate-500 mt-0.5">
            Organize, prioritize, and track your tasks across lifecycle stages. Total: {totalElements}
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={fetchTasks}
            isLoading={isLoading}
            leftIcon={<RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin' : ''}`} />}
            aria-label="Refresh tasks"
          >
            Refresh
          </Button>
        </div>
      </div>

      {error && (
        <div
          role="alert"
          className="flex items-center gap-2.5 p-4 bg-rose-50 border border-rose-200 rounded-xl text-rose-800 text-sm"
        >
          <AlertCircle className="w-5 h-5 flex-shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Filter and Search Bar */}
      <TaskFilterBar
        searchQuery={searchQuery}
        onSearchChange={handleSearchChange}
        selectedStatus={selectedStatus}
        onStatusChange={handleStatusFilterChange}
        selectedPriority={selectedPriority}
        onPriorityChange={handlePriorityFilterChange}
        selectedSort={selectedSort}
        onSortChange={handleSortChange}
        onOpenCreateModal={() => setIsCreateModalOpen(true)}
      />

      {/* Task Board */}
      <TaskBoard
        tasks={tasks}
        isLoading={isLoading}
        onStatusChange={handleStatusChange}
        onEdit={(task) => setEditingTask(task)}
        onDelete={(taskId) => {
          const target = tasks.find((t) => t.id === taskId);
          setDeletingTask({ id: taskId, title: target?.title || 'Selected Task' });
        }}
      />

      {/* Pagination Footer */}
      {totalPages > 1 && (
        <div className="flex items-center justify-between pt-4 border-t border-slate-200 text-sm text-slate-600">
          <div>
            Showing Page <span className="font-semibold text-slate-900">{page + 1}</span> of{' '}
            <span className="font-semibold text-slate-900">{totalPages}</span> ({totalElements} total tasks)
          </div>
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              disabled={page === 0 || isLoading}
              onClick={() => setPage((p) => Math.max(0, p - 1))}
              leftIcon={<ChevronLeft className="w-4 h-4" />}
            >
              Previous
            </Button>
            <Button
              variant="outline"
              size="sm"
              disabled={page >= totalPages - 1 || isLoading}
              onClick={() => setPage((p) => p + 1)}
              rightIcon={<ChevronRight className="w-4 h-4" />}
            >
              Next
            </Button>
          </div>
        </div>
      )}

      {/* Create Task Modal */}
      <TaskModal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        onSubmit={handleCreateSubmit}
        isLoading={isSubmitting}
      />

      {/* Edit Task Modal */}
      {editingTask && (
        <TaskModal
          isOpen={true}
          onClose={() => setEditingTask(null)}
          onSubmit={handleEditSubmit}
          initialData={editingTask}
          isLoading={isSubmitting}
        />
      )}

      {/* Delete Confirmation Modal */}
      {deletingTask && (
        <DeleteConfirmModal
          isOpen={true}
          onClose={() => setDeletingTask(null)}
          onConfirm={handleDeleteConfirm}
          taskTitle={deletingTask.title}
          isLoading={isSubmitting}
        />
      )}
    </div>
  );
};

