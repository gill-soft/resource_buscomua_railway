package com.gillsoft.client;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Ticket implements Serializable {

	private static final long serialVersionUID = 6488489518145641249L;

	private String ordernumber;
	
	@JsonProperty("barcode_image")
	private String barcodeImage;
	
	@JsonProperty("qr_image")
	private String qrImage;
	
	private OrderDocument document;

	public String getOrdernumber() {
		return ordernumber;
	}

	public void setOrdernumber(String ordernumber) {
		this.ordernumber = ordernumber;
	}

	public String getBarcodeImage() {
		return barcodeImage;
	}

	public void setBarcodeImage(String barcodeImage) {
		this.barcodeImage = barcodeImage;
	}

	public String getQrImage() {
		return qrImage;
	}

	public void setQrImage(String qrImage) {
		this.qrImage = qrImage;
	}

	public OrderDocument getDocument() {
		return document;
	}

	public void setDocument(OrderDocument document) {
		this.document = document;
	}
	
}
