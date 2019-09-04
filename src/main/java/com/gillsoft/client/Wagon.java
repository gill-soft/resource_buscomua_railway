package com.gillsoft.client;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Wagon implements Serializable {

	private static final long serialVersionUID = -6798844754212291120L;

	private String seats;
	private Places places;
	private Money cost;
	private Money costreserve;
	private Value country;
	private String number;
	private Value type;
	private Value railway;
	private String charline;
	private String subtype;
	
	@JsonProperty("class")
	private Value clas;

	public String getSeats() {
		return seats;
	}

	public void setSeats(String seats) {
		this.seats = seats;
	}

	public Places getPlaces() {
		return places;
	}

	public void setPlaces(Places places) {
		this.places = places;
	}

	public Money getCost() {
		return cost;
	}

	public void setCost(Money cost) {
		this.cost = cost;
	}

	public Money getCostreserve() {
		return costreserve;
	}

	public void setCostreserve(Money costreserve) {
		this.costreserve = costreserve;
	}

	public Value getCountry() {
		return country;
	}

	public void setCountry(Value country) {
		this.country = country;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public Value getType() {
		return type;
	}

	public void setType(Value type) {
		this.type = type;
	}

	public Value getRailway() {
		return railway;
	}

	public void setRailway(Value railway) {
		this.railway = railway;
	}

	public String getCharline() {
		return charline;
	}

	public void setCharline(String charline) {
		this.charline = charline;
	}

	public Value getClas() {
		return clas;
	}

	public void setClas(Value clas) {
		this.clas = clas;
	}

	public String getSubtype() {
		return subtype;
	}

	public void setSubtype(String subtype) {
		this.subtype = subtype;
	}
	
}
