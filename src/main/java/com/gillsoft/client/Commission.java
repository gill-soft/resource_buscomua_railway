package com.gillsoft.client;

import java.io.Serializable;
import java.math.BigDecimal;

public class Commission implements Serializable {

	private static final long serialVersionUID = 5494787319804771128L;

	private BigDecimal provider;
	private BigDecimal agent;
	private BigDecimal service;
	private BigDecimal total;

	public BigDecimal getProvider() {
		return provider;
	}

	public void setProvider(BigDecimal provider) {
		this.provider = provider;
	}

	public BigDecimal getAgent() {
		return agent;
	}

	public void setAgent(BigDecimal agent) {
		this.agent = agent;
	}

	public BigDecimal getService() {
		return service;
	}

	public void setService(BigDecimal service) {
		this.service = service;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

}
