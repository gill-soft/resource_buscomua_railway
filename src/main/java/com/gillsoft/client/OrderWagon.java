package com.gillsoft.client;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderWagon implements Serializable {

	private static final long serialVersionUID = -5004517189630821217L;
	
	private String value;
	private String firm;
	private String type;
	
	@JsonProperty("class")
	private String clas;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getFirm() {
		return firm;
	}

	public void setFirm(String firm) {
		this.firm = firm;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getClas() {
		return clas;
	}

	public void setClas(String clas) {
		this.clas = clas;
	}
}
