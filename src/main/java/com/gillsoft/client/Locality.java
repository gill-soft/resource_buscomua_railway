package com.gillsoft.client;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Locality implements Serializable {

	private static final long serialVersionUID = -1491137613862997634L;

	@JsonProperty("name_lt")
	private String nameLt;

	@JsonProperty("name_uk")
	private String nameUk;

	@JsonProperty("name_ru")
	private String nameRu;

	private String railway;
	private String code;
	
	@JsonProperty("departure_time")
	private String departureTime;
	
	@JsonProperty("arrival_time")
	private String arrivalTime;
	
	private String name;
	private int distance;
	private int days;
	
	public String getNameLt() {
		return nameLt;
	}

	public void setNameLt(String nameLt) {
		this.nameLt = nameLt;
	}

	public String getNameUk() {
		return nameUk;
	}

	public void setNameUk(String nameUk) {
		this.nameUk = nameUk;
	}

	public String getNameRu() {
		return nameRu;
	}

	public void setNameRu(String nameRu) {
		this.nameRu = nameRu;
	}

	public String getRailway() {
		return railway;
	}

	public void setRailway(String railway) {
		this.railway = railway;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDepartureTime() {
		return departureTime;
	}

	public void setDepartureTime(String departureTime) {
		this.departureTime = departureTime;
	}

	public String getArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(String arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public int getDays() {
		return days;
	}

	public void setDays(int days) {
		this.days = days;
	}

}
