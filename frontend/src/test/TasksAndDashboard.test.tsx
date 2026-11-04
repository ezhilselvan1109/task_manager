import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { MetricsCards } from '../components/features/dashboard/MetricsCards';
import { TaskCard } from '../components/features/tasks/TaskCard';
import { TaskModal } from '../components/features/tasks/TaskModal';
import { DeleteConfirmModal } from '../components/features/tasks/DeleteConfirmModal';
import { Task, TaskMetrics } from '../types/task';

const mockMetrics: TaskMetrics = {
  totalTasks: 25,
  todoCount: 10,
  inProgressCount: 5,
  completedCount: 8,
  cancelledCount: 2,
  highPriorityCount: 7,
  overdueCount: 3,
};

const mockTask: Task = {
  id: 'task-101',
  userId: 'user-1',
  title: 'Implement OAuth Security',
  description: 'Add JWT authorization filters',
  status: 'TODO',
  priority: 'HIGH',
  dueDate: '2026-11-10T18:00:00Z',
  isDeleted: false,
  createdAt: '2026-11-04T10:00:00Z',
  updatedAt: '2026-11-04T10:00:00Z',
};

describe('Dashboard and Task Features', () => {
  describe('MetricsCards', () => {
    it('should render all KPI metric cards with correct values', () => {
      render(<MetricsCards metrics={mockMetrics} isLoading={false} />);

      expect(screen.getByText('Total Tasks')).toBeInTheDocument();
      expect(screen.getByText('25')).toBeInTheDocument();
      expect(screen.getByText('To Do')).toBeInTheDocument();
      expect(screen.getByText('10')).toBeInTheDocument();
      expect(screen.getByText('In Progress')).toBeInTheDocument();
      expect(screen.getByText('5')).toBeInTheDocument();
      expect(screen.getByText('Completed')).toBeInTheDocument();
      expect(screen.getByText('8')).toBeInTheDocument();
      expect(screen.getByText('Overdue')).toBeInTheDocument();
      expect(screen.getByText('3')).toBeInTheDocument();
    });
  });

  describe('TaskCard', () => {
    it('should render task title, description, priority badge, and trigger actions', () => {
      const handleStatusChange = vi.fn();
      const handleEdit = vi.fn();
      const handleDelete = vi.fn();

      render(
        <TaskCard
          task={mockTask}
          onStatusChange={handleStatusChange}
          onEdit={handleEdit}
          onDelete={handleDelete}
        />
      );

      expect(screen.getByText('Implement OAuth Security')).toBeInTheDocument();
      expect(screen.getByText('Add JWT authorization filters')).toBeInTheDocument();
      expect(screen.getByText('HIGH')).toBeInTheDocument();

      // Click "Start" button (from TODO to IN_PROGRESS)
      const startButton = screen.getByRole('button', { name: /start working/i });
      fireEvent.click(startButton);
      expect(handleStatusChange).toHaveBeenCalledWith('task-101', 'IN_PROGRESS');

      // Click Edit
      const editButton = screen.getByRole('button', { name: /edit task/i });
      fireEvent.click(editButton);
      expect(handleEdit).toHaveBeenCalledWith(mockTask);

      // Click Delete
      const deleteButton = screen.getByRole('button', { name: /delete task/i });
      fireEvent.click(deleteButton);
      expect(handleDelete).toHaveBeenCalledWith('task-101');
    });
  });

  describe('TaskModal', () => {
    it('should render create task modal and submit valid data', () => {
      const handleSubmit = vi.fn();
      const handleClose = vi.fn();

      render(
        <TaskModal
          isOpen={true}
          onClose={handleClose}
          onSubmit={handleSubmit}
          initialData={null}
        />
      );

      expect(screen.getByRole('heading', { name: 'Create Task' })).toBeInTheDocument();

      const titleInput = screen.getByLabelText(/title/i);
      fireEvent.change(titleInput, { target: { value: 'New Test Task' } });

      const submitButton = screen.getByRole('button', { name: /create task/i });
      fireEvent.click(submitButton);

      expect(handleSubmit).toHaveBeenCalledWith(
        expect.objectContaining({
          title: 'New Test Task',
        })
      );
    });

    it('should show validation error when title is empty on submit', () => {
      const handleSubmit = vi.fn();
      render(
        <TaskModal
          isOpen={true}
          onClose={vi.fn()}
          onSubmit={handleSubmit}
          initialData={null}
        />
      );

      const submitButton = screen.getByRole('button', { name: /create task/i });
      fireEvent.click(submitButton);

      expect(screen.getByText(/title is required/i)).toBeInTheDocument();
      expect(handleSubmit).not.toHaveBeenCalled();
    });
  });

  describe('DeleteConfirmModal', () => {
    it('should render delete confirmation dialog and execute onConfirm', () => {
      const handleConfirm = vi.fn();
      const handleClose = vi.fn();

      render(
        <DeleteConfirmModal
          isOpen={true}
          onClose={handleClose}
          onConfirm={handleConfirm}
          taskTitle="Implement OAuth Security"
        />
      );

      expect(screen.getByRole('heading', { name: /delete task/i })).toBeInTheDocument();
      expect(screen.getByText(/implement oauth security/i)).toBeInTheDocument();

      const confirmButton = screen.getByRole('button', { name: /confirm delete/i });
      fireEvent.click(confirmButton);

      expect(handleConfirm).toHaveBeenCalledTimes(1);
    });
  });
});
