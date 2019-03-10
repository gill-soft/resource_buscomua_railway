package com.gillsoft.client;

import java.io.Serializable;

public class PlacesPart implements Serializable {

	private static final long serialVersionUID = -5347813394072004360L;

	private int value;
	private int side;

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getSide() {
		return side;
	}

	public void setSide(int side) {
		this.side = side;
	}

}
