package com.sankalp.userservice.dto;

public class ApiResult<T> {
	private boolean success;
	private String message;
	private T data;
	private String errorCode;

	public ApiResult() {
	}

	public ApiResult(boolean success, String message, T data, String errorCode) {
		this.success = success;
		this.message = message;
		this.data = data;
		this.errorCode = errorCode;
	}

	public static <T> ApiResult<T> success(String message, T data) {
		return new ApiResult<>(true, message, data, null);
	}

	public static <T> ApiResult<T> error(String message) {
		return new ApiResult<>(false, message, null, null);
	}

	public static <T> ApiResult<T> error(String message, String errorCode) {
		return new ApiResult<>(false, message, null, errorCode);
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
}
