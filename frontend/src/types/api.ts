export interface FieldErrorDetail {
  field: string;
  rejectedValue?: string;
  message: string;
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  errorCode: string;
  message: string;
  path: string;
  correlationId: string;
  fieldErrors?: FieldErrorDetail[];
}

