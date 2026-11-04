import { apiClient } from './apiClient';
import {
  ChangeTaskStatusRequest,
  CreateTaskRequest,
  Task,
  TaskFilterParams,
  TaskMetrics,
  TaskPageResponse,
  TaskStatus,
  UpdateTaskRequest,
} from '../types/task';

export const taskApi = {
  getTasks: async (params?: TaskFilterParams): Promise<TaskPageResponse> => {
    const queryParams: Record<string, string | number> = {};

    if (params) {
      if (params.query) queryParams.query = params.query;
      if (params.status && params.status.length > 0) queryParams.status = params.status.join(',');
      if (params.priority && params.priority.length > 0) queryParams.priority = params.priority.join(',');
      if (params.dueFrom) queryParams.dueFrom = params.dueFrom;
      if (params.dueTo) queryParams.dueTo = params.dueTo;
      if (params.page !== undefined) queryParams.page = params.page;
      if (params.size !== undefined) queryParams.size = params.size;
      if (params.sort) queryParams.sort = params.sort;
    }

    const response = await apiClient.get<TaskPageResponse>('/api/v1/tasks', { params: queryParams });
    return response.data;
  },

  getTask: async (id: string): Promise<Task> => {
    const response = await apiClient.get<Task>(`/api/v1/tasks/${id}`);
    return response.data;
  },

  createTask: async (data: CreateTaskRequest): Promise<Task> => {
    const response = await apiClient.post<Task>('/api/v1/tasks', data);
    return response.data;
  },

  updateTask: async (id: string, data: UpdateTaskRequest): Promise<Task> => {
    const response = await apiClient.put<Task>(`/api/v1/tasks/${id}`, data);
    return response.data;
  },

  changeStatus: async (id: string, status: TaskStatus): Promise<Task> => {
    const payload: ChangeTaskStatusRequest = { status };
    const response = await apiClient.patch<Task>(`/api/v1/tasks/${id}/status`, payload);
    return response.data;
  },

  completeTask: async (id: string): Promise<Task> => {
    const response = await apiClient.patch<Task>(`/api/v1/tasks/${id}/complete`);
    return response.data;
  },

  deleteTask: async (id: string): Promise<void> => {
    await apiClient.delete(`/api/v1/tasks/${id}`);
  },

  getTaskMetrics: async (): Promise<TaskMetrics> => {
    const response = await apiClient.get<TaskMetrics>('/api/v1/tasks/metrics/summary');
    return response.data;
  },
};

