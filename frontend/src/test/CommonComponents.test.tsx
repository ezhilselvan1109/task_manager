import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { Button } from '../components/common/Button';
import { Input } from '../components/common/Input';
import { Badge } from '../components/common/Badge';
import { Modal } from '../components/common/Modal';

describe('Common UI Components', () => {
  describe('Button', () => {
    it('should render button text and handle clicks', () => {
      const handleClick = vi.fn();
      render(<Button onClick={handleClick}>Click Me</Button>);

      const button = screen.getByRole('button', { name: /click me/i });
      fireEvent.click(button);

      expect(handleClick).toHaveBeenCalledTimes(1);
    });

    it('should show loading spinner and disable button when isLoading is true', () => {
      const handleClick = vi.fn();
      render(<Button isLoading onClick={handleClick}>Submit</Button>);

      const button = screen.getByRole('button', { name: /submit/i });
      expect(button).toBeDisabled();
      fireEvent.click(button);

      expect(handleClick).not.toHaveBeenCalled();
    });
  });

  describe('Input', () => {
    it('should render label and input field', () => {
      render(<Input label="Task Title" id="title" placeholder="Enter title" />);

      expect(screen.getByLabelText(/task title/i)).toBeInTheDocument();
      expect(screen.getByPlaceholderText(/enter title/i)).toBeInTheDocument();
    });

    it('should display error message when error prop is provided', () => {
      render(<Input label="Email" id="email" error="Invalid email address" />);

      expect(screen.getByText(/invalid email address/i)).toBeInTheDocument();
      const input = screen.getByLabelText(/email/i);
      expect(input).toHaveAttribute('aria-invalid', 'true');
    });
  });

  describe('Badge', () => {
    it('should render task status badge with appropriate styling', () => {
      render(<Badge variant="status" value="IN_PROGRESS" />);
      expect(screen.getByText('IN_PROGRESS')).toBeInTheDocument();
    });

    it('should render priority badge with appropriate styling', () => {
      render(<Badge variant="priority" value="URGENT" />);
      expect(screen.getByText('URGENT')).toBeInTheDocument();
    });
  });

  describe('Modal', () => {
    it('should render modal content when isOpen is true', () => {
      const handleClose = vi.fn();
      render(
        <Modal isOpen={true} onClose={handleClose} title="Create Task">
          <div>Modal Body Content</div>
        </Modal>
      );

      expect(screen.getByText('Create Task')).toBeInTheDocument();
      expect(screen.getByText('Modal Body Content')).toBeInTheDocument();
    });

    it('should trigger onClose when close button is clicked or Escape key pressed', () => {
      const handleClose = vi.fn();
      render(
        <Modal isOpen={true} onClose={handleClose} title="Edit Task">
          <div>Content</div>
        </Modal>
      );

      const closeButton = screen.getByRole('button', { name: /close modal/i });
      fireEvent.click(closeButton);
      expect(handleClose).toHaveBeenCalledTimes(1);

      fireEvent.keyDown(window, { key: 'Escape' });
      expect(handleClose).toHaveBeenCalledTimes(2);
    });
  });
});
