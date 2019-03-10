package com.gillsoft.client;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Result implements Serializable {
	
	private static final long serialVersionUID = 4487255889338078761L;
	
	private List<Locality> station;
	private List<Country> country;
	private List<Country> countries;
	private List<Train> trains;
	private Train train;
	
	@JsonProperty("station_from")
	private Value stationFrom;
	
	@JsonProperty("station_to")
	private Value stationTo;

	public List<Locality> getStation() {
		return station;
	}

	public void setStation(List<Locality> station) {
		this.station = station;
	}

	public List<Country> getCountry() {
		return country;
	}

	public void setCountry(List<Country> country) {
		this.country = country;
	}

	public List<Country> getCountries() {
		return countries;
	}

	public void setCountries(List<Country> countries) {
		this.countries = countries;
	}

	public List<Train> getTrains() {
		return trains;
	}

	public void setTrains(List<Train> trains) {
		this.trains = trains;
	}

	public Train getTrain() {
		return train;
	}

	public void setTrain(Train train) {
		this.train = train;
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
