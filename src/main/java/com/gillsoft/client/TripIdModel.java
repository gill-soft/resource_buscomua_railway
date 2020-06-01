package com.gillsoft.client;

import java.util.Date;

import com.gillsoft.model.AbstractJsonModel;

public class TripIdModel extends AbstractJsonModel {

	private static final long serialVersionUID = -130344874781711022L;

	private String car;
	private String line;
	private String clas;
	private String type;
	private String train;
	private String from;
	private String to;
	private Date date;

	public TripIdModel() {

	}

	public TripIdModel(String car, String line, String clas, String type, String train, String from, String to, Date date) {
		this.car = car;
		this.line = line;
		this.clas = clas;
		this.type = type;
		this.train = train;
		this.from = from;
		this.to = to;
		this.date = date;
	}

	public String getCar() {
		return car;
	}

	public void setCar(String car) {
		this.car = car;
	}

	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
	}

	public String getClas() {
		return clas;
	}

	public void setClas(String clas) {
		this.clas = clas;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTrain() {
		return train;
	}

	public void setTrain(String train) {
		this.train = train;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public TripIdModel create(String json) {
		return (TripIdModel) super.create(json);
	}

}
