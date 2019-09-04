package com.gillsoft;

import java.util.List;
import java.util.Map;

import com.gillsoft.model.SeatType;

public class CarriageScheme {

	private Map<Integer, List<List<String>>> scheme;
	private Map<String, SeatType> seats;

	public Map<Integer, List<List<String>>> getScheme() {
		return scheme;
	}

	public void setScheme(Map<Integer, List<List<String>>> scheme) {
		this.scheme = scheme;
	}

	public Map<String, SeatType> getSeats() {
		return seats;
	}

	public void setSeats(Map<String, SeatType> seats) {
		this.seats = seats;
	}

}
