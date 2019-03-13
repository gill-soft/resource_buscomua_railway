package com.gillsoft.client;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderTrain implements Serializable {

	private static final long serialVersionUID = -7818616729799928347L;

	private String fasted;
	private String value;
	private String model;
	private String departure;
	private String category;
	
	@JsonProperty("class")
	private String clas;

	public String getFasted() {
		return fasted;
	}

	public void setFasted(String fasted) {
		this.fasted = fasted;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getDeparture() {
		return departure;
	}

	public void setDeparture(String departure) {
		this.departure = departure;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getClas() {
		return clas;
	}

	public void setClas(String clas) {
		this.clas = clas;
	}
}
