package com.gillsoft.client;

public class Response<T> extends AbstractResponse {

	private static final long serialVersionUID = -667452233707752303L;
	
	private T result;

	public T getResult() {
		return result;
	}

	public void setResult(T result) {
		this.result = result;
	}

}
