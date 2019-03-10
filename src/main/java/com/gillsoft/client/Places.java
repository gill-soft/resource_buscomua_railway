package com.gillsoft.client;

import java.io.Serializable;

public class Places implements Serializable {

	private static final long serialVersionUID = 2127858188804952846L;

	private PlacesPart top;
	private PlacesPart lower;

	public PlacesPart getTop() {
		return top;
	}

	public void setTop(PlacesPart top) {
		this.top = top;
	}

	public PlacesPart getLower() {
		return lower;
	}

	public void setLower(PlacesPart lower) {
		this.lower = lower;
	}

}
