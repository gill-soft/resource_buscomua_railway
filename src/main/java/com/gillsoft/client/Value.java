package com.gillsoft.client;

import java.io.Serializable;
import java.math.BigDecimal;

public class Value implements Serializable {

	private static final long serialVersionUID = -693669181403116894L;
	
	private String value;
	private String code;
	private BigDecimal cost;
	
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

	public BigDecimal getCost() {
		return cost;
	}

	public void setCost(BigDecimal cost) {
		this.cost = cost;
	}

}
