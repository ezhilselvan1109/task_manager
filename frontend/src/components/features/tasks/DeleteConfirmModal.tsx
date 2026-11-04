import React from 'react';
import { AlertCircle } from 'lucide-react';
import { Modal } from '../../common/Modal';
import { Button } from '../../common/Button';

export interface DeleteConfirmModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => Promise<void>;
  taskTitle: string;
  isLoading?: boolean;
}

export const DeleteConfirmModal: React.FC<DeleteConfirmModalProps> = ({
  isOpen,
  onClose,
  onConfirm,
  taskTitle,
  isLoading = false,
}) => {
  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Delete Task" maxWidth="sm">
      <div className="flex flex-col items-center text-center gap-3">
        <div className="w-12 h-12 rounded-full bg-rose-50 text-rose-600 flex items-center justify-center">
          <AlertCircle className="w-6 h-6" />
        </div>
        <div>
          <p className="text-sm font-semibold text-slate-800">
            Are you sure you want to delete this task?
          </p>
          <p className="text-xs text-slate-500 mt-1 italic">
            &ldquo;{taskTitle}&rdquo;
          </p>
          <p className="text-xs text-slate-400 mt-2">
            This action will soft-delete the task and broadcast an event to your notification feed.
          </p>
        </div>
      </div>

      <div className="flex items-center justify-end gap-2 mt-6 pt-4 border-t border-slate-100">
        <Button variant="ghost" onClick={onClose} disabled={isLoading}>
          Cancel
        </Button>
        <Button
          variant="danger"
          onClick={onConfirm}
          isLoading={isLoading}
          aria-label="Confirm delete"
        >
          Delete Task
        </Button>
      </div>
    </Modal>
  );
};

