package com.gillsoft.client;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class FiscalInfo implements Serializable {

	private static final long serialVersionUID = 1814574244561532497L;

	private String rro;
	private String server;
	private String id;
	private String tin;

	public String getRro() {
		return rro;
	}

	public void setRro(String rro) {
		this.rro = rro;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTin() {
		return tin;
	}

	public void setTin(String tin) {
		this.tin = tin;
	}

}
