export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';

export interface Task {
  id: string;
  userId: string;
  title: string;
  description?: string;
  status: TaskStatus;
  priority: TaskPriority;
  dueDate?: string;
  isDeleted: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateTaskRequest {
  title: string;
  description?: string;
  priority?: TaskPriority;
  dueDate?: string;
}

export interface UpdateTaskRequest {
  title: string;
  description?: string;
  priority?: TaskPriority;
  dueDate?: string;
}

export interface ChangeTaskStatusRequest {
  status: TaskStatus;
}

export interface TaskPageResponse {
  content: Task[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface TaskMetrics {
  totalTasks: number;
  todoCount: number;
  inProgressCount: number;
  completedCount: number;
  cancelledCount: number;
  highPriorityCount: number;
  overdueCount: number;
}

export interface TaskFilterParams {
  query?: string;
  status?: TaskStatus[];
  priority?: TaskPriority[];
  dueFrom?: string;
  dueTo?: string;
  page?: number;
  size?: number;
  sort?: string;
}

