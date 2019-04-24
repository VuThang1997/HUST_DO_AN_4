package edu.hust.model;

public class ReportError {

	private int errorCode;
	
	private int description;

	public ReportError() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ReportError(int errorCode, int description) {
		super();
		this.errorCode = errorCode;
		this.description = description;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public int getDescription() {
		return description;
	}

	public void setDescription(int description) {
		this.description = description;
	}
	
	
}
