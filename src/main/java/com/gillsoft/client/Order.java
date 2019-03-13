package com.gillsoft.client;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Order implements Serializable {

	private static final long serialVersionUID = 8380359745530139389L;

	private String ordernumber;
	private String uio;
	private boolean electronic;

	@JsonProperty("barcode_image")
	private String barcodeImage;

	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private Date paydate;

	private List<Ticket> ticket;
	private List<Ticket> documents;

	public String getOrdernumber() {
		return ordernumber;
	}

	public void setOrdernumber(String ordernumber) {
		this.ordernumber = ordernumber;
	}

	public String getUio() {
		return uio;
	}

	public void setUio(String uio) {
		this.uio = uio;
	}

	public boolean isElectronic() {
		return electronic;
	}

	public void setElectronic(boolean electronic) {
		this.electronic = electronic;
	}

	public String getBarcodeImage() {
		return barcodeImage;
	}

	public void setBarcodeImage(String barcodeImage) {
		this.barcodeImage = barcodeImage;
	}

	public Date getPaydate() {
		return paydate;
	}

	public void setPaydate(Date paydate) {
		this.paydate = paydate;
	}

	public List<Ticket> getTicket() {
		return ticket;
	}

	public void setTicket(List<Ticket> ticket) {
		this.ticket = ticket;
	}

	public List<Ticket> getDocuments() {
		return documents;
	}

	public void setDocuments(List<Ticket> documents) {
		this.documents = documents;
	}

}
