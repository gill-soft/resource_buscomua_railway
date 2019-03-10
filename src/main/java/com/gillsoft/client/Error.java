package com.gillsoft.client;

import java.io.Serializable;

public class Error implements Serializable {

	private static final long serialVersionUID = 4932107849543245664L;

	private String id;
	private String data;
	private String message;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
