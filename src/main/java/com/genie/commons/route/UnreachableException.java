package com.genie.commons.route;

public class UnreachableException extends Exception {
	public UnreachableException() {
	}

	public UnreachableException(String message) {
		super(message);
	}

	public UnreachableException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnreachableException(Throwable cause) {
		super(cause);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 8205905560960252946L;

}
