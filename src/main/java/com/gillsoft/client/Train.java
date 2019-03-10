package com.gillsoft.client;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

public class Train implements Serializable {

	private static final long serialVersionUID = -7639583279072494465L;
	
	private boolean added;
	private String number;
	
	@JsonProperty("departure_date")
	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date departureDate;
	
	@JsonProperty("arrival_date")
	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date arrivalDate;
	
	private String model;
	private String category;
	
	@JsonProperty("station_from")
	private Value stationFrom;
	
	@JsonProperty("station_to")
	private Value stationTo;
	
	private Value fasted;
	
	@JsonProperty("class")
	private Value clas;
	
	private List<Wagon> wagons;

	public boolean isAdded() {
		return added;
	}

	public void setAdded(boolean added) {
		this.added = added;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public Date getDepartureDate() {
		return departureDate;
	}

	public void setDepartureDate(Date departureDate) {
		this.departureDate = departureDate;
	}

	public Date getArrivalDate() {
		return arrivalDate;
	}

	public void setArrivalDate(Date arrivalDate) {
		this.arrivalDate = arrivalDate;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Value getStationFrom() {
		return stationFrom;
	}

	public void setStationFrom(Value stationFrom) {
		this.stationFrom = stationFrom;
	}

	public Value getStationTo() {
		return stationTo;
	}

	public void setStationTo(Value stationTo) {
		this.stationTo = stationTo;
	}

	public Value getFasted() {
		return fasted;
	}

	public void setFasted(Value fasted) {
		this.fasted = fasted;
	}

	public Value getClas() {
		return clas;
	}

	public void setClas(Value clas) {
		this.clas = clas;
	}

	public List<Wagon> getWagons() {
		return wagons;
	}

	public void setWagons(List<Wagon> wagons) {
		this.wagons = wagons;
	}

}
