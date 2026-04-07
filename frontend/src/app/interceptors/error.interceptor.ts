import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';

/**
 * Error Interceptor - Beautifies error messages to hide technical details
 * Maps HTTP status codes and error types to user-friendly messages
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // Log full error for debugging (dev tools only)
      console.error('[HTTP Error]', {
        status: error.status,
        statusText: error.statusText,
        url: error.url,
        body: error.error,
        message: error.message
      });

      // Transform error to user-friendly response
      const userFriendlyError = transformErrorForUser(error);
      
      return throwError(() => userFriendlyError);
    })
  );
};

/**
 * Transform HTTP errors into user-friendly messages without exposing:
 * - Port numbers
 * - API paths/endpoints
 * - Technical stack details
 * - Backend error traces
 */
function transformErrorForUser(error: HttpErrorResponse): any {
  let userMessage = 'Something went wrong. Please try again.';
  let errorCode = 'UNKNOWN_ERROR';

  // Extract server error message if available
  const serverMessage = error.error?.message || error.error?.error || '';

  // Map HTTP status codes to user-friendly messages
  if (error.status === 0) {
    userMessage = 'Unable to connect to the server. Please check your internet connection.';
    errorCode = 'CONNECTION_ERROR';
  } else if (error.status === 400) {
    // Bad Request - validation error
    if (serverMessage.includes('quantity') || serverMessage.toLowerCase().includes('invalid')) {
      userMessage = 'Please check your input values and try again.';
    } else if (serverMessage.includes('portfolio') || serverMessage.includes('stock')) {
      userMessage = 'Please select valid portfolio and stock information.';
    } else {
      userMessage = 'The information provided is invalid. Please review your entries.';
    }
    errorCode = 'VALIDATION_ERROR';
  } else if (error.status === 401) {
    userMessage = 'Your session has expired. Please log in again.';
    errorCode = 'UNAUTHORIZED';
  } else if (error.status === 403) {
    // Forbidden - could be multiple reasons
    if (serverMessage.toLowerCase().includes('insufficient') || serverMessage.toLowerCase().includes('balance')) {
      userMessage = 'Insufficient balance for this transaction.';
    } else if (serverMessage.toLowerCase().includes('wallet') || serverMessage.toLowerCase().includes('reserve')) {
      userMessage = 'Your wallet cannot process this request. Please contact support.';
    } else if (serverMessage.toLowerCase().includes('locked') || serverMessage.toLowerCase().includes('permission')) {
      userMessage = 'You do not have permission to perform this action.';
    } else {
      userMessage = 'This action is not allowed at this time.';
    }
    errorCode = 'ACCESS_DENIED';
  } else if (error.status === 404) {
    if (serverMessage.toLowerCase().includes('portfolio') || serverMessage.toLowerCase().includes('not found')) {
      userMessage = 'The requested item could not be found.';
    } else {
      userMessage = 'Resource not found. Please refresh and try again.';
    }
    errorCode = 'NOT_FOUND';
  } else if (error.status === 409) {
    userMessage = 'This action conflicts with an existing order. Please try again.';
    errorCode = 'CONFLICT';
  } else if (error.status === 422) {
    userMessage = 'The order data is incomplete or invalid. Please check all fields.';
    errorCode = 'UNPROCESSABLE';
  } else if (error.status === 429) {
    userMessage = 'Too many requests. Please wait a moment and try again.';
    errorCode = 'RATE_LIMITED';
  } else if (error.status === 500 || error.status === 502 || error.status === 503) {
    userMessage = 'Server is temporarily unavailable. Please try again in a moment.';
    errorCode = 'SERVER_ERROR';
  } else if (error.status === 504) {
    userMessage = 'Request timed out. Please try again.';
    errorCode = 'TIMEOUT';
  }

  // Return clean error object without exposing URL or port
  return {
    message: userMessage,
    code: errorCode,
    status: error.status,
    // Preserve some context for debugging in dev mode
    ...(error.status >= 400 && error.status < 500 && { originalMessage: serverMessage })
  };
}
