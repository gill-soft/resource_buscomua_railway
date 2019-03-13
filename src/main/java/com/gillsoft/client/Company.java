package com.gillsoft.client;

import java.io.Serializable;

public class Company implements Serializable {

	private static final long serialVersionUID = -974992428240718256L;

	private String telephone;
	private String name;
	private String address;

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

}
