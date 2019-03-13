package com.gillsoft.client;

import java.io.Serializable;

public abstract class AbstractResponse implements Serializable {

	private static final long serialVersionUID = 2221208128122632414L;

	private String id;
	
	private Error error;
	
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
	
}
