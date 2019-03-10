package com.gillsoft.client;

import java.io.Serializable;

public class Value implements Serializable {

	private static final long serialVersionUID = -3490164903042513237L;
	
	private String value;
	private String code;
	
	public Value() {
		
	}

	public Value(String value, String code) {
		this.value = value;
		this.code = code;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}
