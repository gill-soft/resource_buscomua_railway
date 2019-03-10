package com.gillsoft.client;

import java.util.List;

public class Country extends Locality {

	private static final long serialVersionUID = -8774356783564292199L;

	private List<Locality> stations;
	private List<Locality> railways;

	public List<Locality> getStations() {
		return stations;
	}

	public void setStations(List<Locality> stations) {
		this.stations = stations;
	}

	public List<Locality> getRailways() {
		return railways;
	}

	public void setRailways(List<Locality> railways) {
		this.railways = railways;
	}

}
