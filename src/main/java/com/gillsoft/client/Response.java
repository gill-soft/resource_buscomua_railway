package com.gillsoft.client;

import java.io.Serializable;

public class Response implements Serializable {

	private static final long serialVersionUID = -667452233707752303L;
	
	private String id;
	
	private Error error;
	
	private Result result;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Error getError() {
		return error;
	}

	public void setError(Error error) {
		this.error = error;
	}

	public Result getResult() {
		return result;
	}

	public void setResult(Result result) {
		this.result = result;
	}

}
