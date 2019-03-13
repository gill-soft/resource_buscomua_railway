package com.gillsoft.client;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class OrderDocument implements Serializable {

	private static final long serialVersionUID = -6132871920071899411L;
	
	private String ordernumber;
	private String uid;
	private int number;
	private String status;
	private String text;
	private String sysnumber;

	@JsonProperty("insurance_company")
	private Company insurance;
	
	private Document passport;
	private Rsb rsb;
	private Detail detail;
	
	@JsonDeserialize(using = CostMapDeserializer.class)
	private Map<String, Cost> costs;

	public String getOrdernumber() {
		return ordernumber;
	}

	public void setOrdernumber(String ordernumber) {
		this.ordernumber = ordernumber;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getSysnumber() {
		return sysnumber;
	}

	public void setSysnumber(String sysnumber) {
		this.sysnumber = sysnumber;
	}

	public Company getInsurance() {
		return insurance;
	}

	public void setInsurance(Company insurance) {
		this.insurance = insurance;
	}

	public Document getPassport() {
		return passport;
	}

	public void setPassport(Document passport) {
		this.passport = passport;
	}

	public Rsb getRsb() {
		return rsb;
	}

	public void setRsb(Rsb rsb) {
		this.rsb = rsb;
	}

	public Detail getDetail() {
		return detail;
	}

	public void setDetail(Detail detail) {
		this.detail = detail;
	}

	public Map<String, Cost> getCosts() {
		return costs;
	}

	public void setCosts(Map<String, Cost> costs) {
		this.costs = costs;
	}
	
}
