package com.gillsoft.client;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

public class OrderResult implements Serializable {

	private static final long serialVersionUID = -6552072426501656107L;

	private String id;
	private String uio;
	private String ordernumber;

	@JsonProperty("wait_time")
	private String waitTime;

	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private Date sysdate;

	private OrderWagon wagon;

	@JsonProperty("date_to")
	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private Date dateTo;

	@JsonProperty("date_from")
	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private Date dateFrom;

	@JsonProperty("station_from")
	private Value stationFrom;

	@JsonProperty("station_to")
	private Value stationTo;

	private boolean electronic;
	private OrderTrain train;
	private List<OrderDocument> documents;
	
	private List<Order> order;
	
	private OrderDocument document;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUio() {
		return uio;
	}

	public void setUio(String uio) {
		this.uio = uio;
	}

	public String getOrdernumber() {
		return ordernumber;
	}

	public void setOrdernumber(String ordernumber) {
		this.ordernumber = ordernumber;
	}

	public String getWaitTime() {
		return waitTime;
	}

	public void setWaitTime(String waitTime) {
		this.waitTime = waitTime;
	}

	public Date getSysdate() {
		return sysdate;
	}

	public void setSysdate(Date sysdate) {
		this.sysdate = sysdate;
	}

	public OrderWagon getWagon() {
		return wagon;
	}

	public void setWagon(OrderWagon wagon) {
		this.wagon = wagon;
	}

	public Date getDateTo() {
		return dateTo;
	}

	public void setDateTo(Date dateTo) {
		this.dateTo = dateTo;
	}

	public Date getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(Date dateFrom) {
		this.dateFrom = dateFrom;
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

	public boolean isElectronic() {
		return electronic;
	}

	public void setElectronic(boolean electronic) {
		this.electronic = electronic;
	}

	public OrderTrain getTrain() {
		return train;
	}

	public void setTrain(OrderTrain train) {
		this.train = train;
	}

	public List<OrderDocument> getDocuments() {
		return documents;
	}

	public void setDocuments(List<OrderDocument> documents) {
		this.documents = documents;
	}

	public List<Order> getOrder() {
		return order;
	}

	public void setOrder(List<Order> order) {
		this.order = order;
	}

	public OrderDocument getDocument() {
		return document;
	}

	public void setDocument(OrderDocument document) {
		this.document = document;
	}
	
	public Train createTrain() {
		Train train = new Train();
		train.setNumber(this.train.getValue());
		train.setModel(this.train.getModel());
		train.setCategory(this.train.getCategory());
		train.setFasted(new Value(null, this.train.getFasted()));
		train.setClas(new Value(null, this.train.getClas()));
		train.setDepartureDate(dateFrom);
		train.setArrivalDate(dateTo);
		train.setStationFrom(stationFrom);
		train.setStationTo(stationTo);
		return train;
	}
	
	public Wagon createWagon() {
		Wagon wagon = new Wagon();
		wagon.setNumber(this.wagon.getValue());
		wagon.setClas(new Value(null, this.wagon.getClas()));
		wagon.setType(new Value(null, this.wagon.getType()));
		return wagon;
	}

}
