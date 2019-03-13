package com.gillsoft.client;

import java.io.Serializable;

public class Rsb implements Serializable {

	private static final long serialVersionUID = -1071873984209862538L;

	private Commission commission;

	public Commission getCommission() {
		return commission;
	}

	public void setCommission(Commission commission) {
		this.commission = commission;
	}
	
}
