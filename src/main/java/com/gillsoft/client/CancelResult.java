package com.gillsoft.client;

import java.io.Serializable;
import java.util.List;

public class CancelResult implements Serializable {

	private static final long serialVersionUID = 9047745559263430861L;

	private String id;
	private List<String> uio;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<String> getUio() {
		return uio;
	}

	public void setUio(List<String> uio) {
		this.uio = uio;
	}

}
