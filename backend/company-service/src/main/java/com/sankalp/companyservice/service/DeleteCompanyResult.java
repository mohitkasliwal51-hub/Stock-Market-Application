package com.sankalp.companyservice.service;

import com.sankalp.companyservice.entity.Company;

public class DeleteCompanyResult {

	public enum Status {
		DELETED,
		NOT_FOUND,
		BLOCKED_BY_DEPENDENCIES
	}

	private final Status status;
	private final Company company;
	private final String message;

	public DeleteCompanyResult(Status status, Company company, String message) {
		this.status = status;
		this.company = company;
		this.message = message;
	}

	public Status getStatus() {
		return status;
	}

	public Company getCompany() {
		return company;
	}

	public String getMessage() {
		return message;
	}
}
