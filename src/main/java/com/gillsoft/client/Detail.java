package com.gillsoft.client;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

public class Detail implements Serializable {

	private static final long serialVersionUID = -4904295920978881114L;

	private Value places;
	private String kind;
	private OrderWagon wagon;
	private OrderTrain train;
	private Value railway;
	
	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonProperty("departure_date")
	private Date departureDate;
	
	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonProperty("arrival_date")
	private Date arrivalDate;
	
	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private Date date;
	
	@JsonProperty("station_from")
	private Value stationFrom;

	@JsonProperty("station_to")
	private Value stationTo;

	public Value getPlaces() {
		return places;
	}

	public void setPlaces(Value places) {
		this.places = places;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public OrderWagon getWagon() {
		return wagon;
	}

	public void setWagon(OrderWagon wagon) {
		this.wagon = wagon;
	}

	public OrderTrain getTrain() {
		return train;
	}

	public void setTrain(OrderTrain train) {
		this.train = train;
	}

	public Value getRailway() {
		return railway;
	}

	public void setRailway(Value railway) {
		this.railway = railway;
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

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
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

}
