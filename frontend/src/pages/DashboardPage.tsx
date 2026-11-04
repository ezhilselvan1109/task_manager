import React, { useEffect, useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { Plus, ArrowRight, RefreshCw, AlertCircle } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { taskApi } from '../api/taskApi';
import { notificationApi } from '../api/notificationApi';
import { TaskMetrics, CreateTaskRequest } from '../types/task';
import { NotificationItem } from '../types/notification';
import { MetricsCards } from '../components/features/dashboard/MetricsCards';
import { RecentActivity } from '../components/features/dashboard/RecentActivity';
import { Button } from '../components/common/Button';
import { TaskModal } from '../components/features/tasks/TaskModal';

export const DashboardPage: React.FC = () => {
  const { user } = useAuth();

  const [metrics, setMetrics] = useState<TaskMetrics | null>(null);
  const [recentNotifications, setRecentNotifications] = useState<NotificationItem[]>([]);
  const [isLoadingMetrics, setIsLoadingMetrics] = useState(true);
  const [isLoadingNotifications, setIsLoadingNotifications] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Quick Task Creation Modal
  const [isTaskModalOpen, setIsTaskModalOpen] = useState(false);
  const [isCreatingTask, setIsCreatingTask] = useState(false);

  const fetchDashboardData = useCallback(async () => {
    setError(null);
    try {
      const [metricsRes, notifRes] = await Promise.allSettled([
        taskApi.getTaskMetrics(),
        notificationApi.getNotifications(0, 5),
      ]);

      if (metricsRes.status === 'fulfilled') {
        setMetrics(metricsRes.value);
      } else {
        // Fallback default metrics if endpoint is empty
        setMetrics({
          totalTasks: 0,
          todoCount: 0,
          inProgressCount: 0,
          completedCount: 0,
          cancelledCount: 0,
          highPriorityCount: 0,
          overdueCount: 0,
        });
      }

      if (notifRes.status === 'fulfilled') {
        setRecentNotifications(notifRes.value.content || []);
      } else {
        setRecentNotifications([]);
      }
    } catch {
      setError('Failed to load dashboard metrics. Please try again.');
    } finally {
      setIsLoadingMetrics(false);
      setIsLoadingNotifications(false);
      setIsRefreshing(false);
    }
  }, []);

  useEffect(() => {
    fetchDashboardData();
  }, [fetchDashboardData]);

  const handleRefresh = () => {
    setIsRefreshing(true);
    fetchDashboardData();
  };

  const handleCreateTask = async (data: CreateTaskRequest) => {
    setIsCreatingTask(true);
    try {
      await taskApi.createTask(data);
      setIsTaskModalOpen(false);
      fetchDashboardData();
    } catch (err: unknown) {
      const errorObj = err as { response?: { data?: { detail?: string } }; message?: string };
      alert(errorObj.response?.data?.detail || errorObj.message || 'Failed to create task');
    } finally {
      setIsCreatingTask(false);
    }
  };

  return (
    <div className="space-y-8">
      {/* Header Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-2 border-b border-slate-200/80">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">
            Welcome back, {user?.fullName || 'User'}!
          </h1>
          <p className="text-sm text-slate-500 mt-0.5">
            Overview of your tasks, performance KPIs, and recent event activities.
          </p>
        </div>
        <div className="flex items-center gap-2.5">
          <Button
            variant="outline"
            size="sm"
            onClick={handleRefresh}
            isLoading={isRefreshing}
            leftIcon={<RefreshCw className={`w-4 h-4 ${isRefreshing ? 'animate-spin' : ''}`} />}
            aria-label="Refresh dashboard data"
          >
            Refresh
          </Button>
          <Button
            variant="primary"
            size="sm"
            onClick={() => setIsTaskModalOpen(true)}
            leftIcon={<Plus className="w-4 h-4" />}
          >
            New Task
          </Button>
        </div>
      </div>

      {error && (
        <div
          role="alert"
          className="flex items-center gap-2.5 p-4 bg-amber-50 border border-amber-200 rounded-xl text-amber-800 text-sm"
        >
          <AlertCircle className="w-5 h-5 flex-shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* KPI Summary Cards */}
      <section aria-labelledby="kpi-heading">
        <h2 id="kpi-heading" className="text-base font-semibold text-slate-800 mb-4">
          Task Performance Metrics
        </h2>
        <MetricsCards metrics={metrics} isLoading={isLoadingMetrics} />
      </section>

      {/* Grid: Quick Actions & Recent Activity */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Quick Shortcuts */}
        <div className="lg:col-span-2 space-y-4">
          <div className="bg-gradient-to-br from-sky-600 to-indigo-700 rounded-2xl p-6 text-white shadow-sm flex flex-col justify-between min-h-[220px]">
            <div>
              <span className="inline-block px-2.5 py-0.5 bg-white/20 backdrop-blur-sm rounded-full text-xs font-medium tracking-wide uppercase">
                Task Workspace
              </span>
              <h3 className="text-xl font-bold mt-2">Manage your active sprint tasks</h3>
              <p className="text-sky-100 text-sm mt-1 max-w-lg">
                Drag, drop, update statuses, prioritize deadlines, and track real-time event updates across your personal workspace.
              </p>
            </div>
            <div className="pt-4 flex items-center gap-3">
              <Link to="/tasks">
                <Button
                  variant="secondary"
                  size="md"
                  rightIcon={<ArrowRight className="w-4 h-4" />}
                  className="bg-white text-sky-700 hover:bg-sky-50 shadow-none border-none font-semibold"
                >
                  Open Task Board
                </Button>
              </Link>
            </div>
          </div>
        </div>

        {/* Recent Activity Sidebar */}
        <div className="lg:col-span-1">
          <RecentActivity
            notifications={recentNotifications}
            isLoading={isLoadingNotifications}
          />
        </div>
      </div>

      {/* Create Task Modal */}
      <TaskModal
        isOpen={isTaskModalOpen}
        onClose={() => setIsTaskModalOpen(false)}
        onSubmit={handleCreateTask}
        isLoading={isCreatingTask}
      />
    </div>
  );
};

