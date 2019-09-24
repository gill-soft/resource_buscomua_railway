package com.gillsoft.client;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Cost implements Serializable {

	private static final long serialVersionUID = -2426696912462019572L;

	private BigDecimal cost;
	private BigDecimal vat;
	private BigDecimal fee;
	private BigDecimal insurance;

	@JsonProperty("reserved_seat")
	private BigDecimal reservedSeat;

	private BigDecimal carrier;

	@JsonProperty("fee_vat")
	private BigDecimal feeVat;

	@JsonProperty("commission_vat")
	private BigDecimal commissionVat;

	@JsonProperty("carrier_vat")
	private BigDecimal carrierVat;

	private BigDecimal ticket;
	private BigDecimal commission;
	
	@JsonProperty("com_service")
	private BigDecimal comService;
	
	private BigDecimal service;
	
	@JsonProperty("add_ticket")
	private BigDecimal addTicket;
	
	@JsonProperty("add_reserved_seat")
	private BigDecimal addReservedSeat;

	public BigDecimal getCost() {
		return cost;
	}

	public void setCost(BigDecimal cost) {
		this.cost = cost;
	}

	public BigDecimal getVat() {
		return vat;
	}

	public void setVat(BigDecimal vat) {
		this.vat = vat;
	}

	public BigDecimal getFee() {
		return fee;
	}

	public void setFee(BigDecimal fee) {
		this.fee = fee;
	}

	public BigDecimal getInsurance() {
		return insurance;
	}

	public void setInsurance(BigDecimal insurance) {
		this.insurance = insurance;
	}

	public BigDecimal getReservedSeat() {
		return reservedSeat;
	}

	public void setReservedSeat(BigDecimal reservedSeat) {
		this.reservedSeat = reservedSeat;
	}

	public BigDecimal getCarrier() {
		return carrier;
	}

	public void setCarrier(BigDecimal carrier) {
		this.carrier = carrier;
	}

	public BigDecimal getFeeVat() {
		return feeVat;
	}

	public void setFeeVat(BigDecimal feeVat) {
		this.feeVat = feeVat;
	}

	public BigDecimal getCommissionVat() {
		return commissionVat;
	}

	public void setCommissionVat(BigDecimal commissionVat) {
		this.commissionVat = commissionVat;
	}

	public BigDecimal getCarrierVat() {
		return carrierVat;
	}

	public void setCarrierVat(BigDecimal carrierVat) {
		this.carrierVat = carrierVat;
	}

	public BigDecimal getTicket() {
		return ticket;
	}

	public void setTicket(BigDecimal ticket) {
		this.ticket = ticket;
	}

	public BigDecimal getCommission() {
		return commission;
	}

	public void setCommission(BigDecimal commission) {
		this.commission = commission;
	}

	public BigDecimal getComService() {
		return comService;
	}

	public void setComService(BigDecimal comService) {
		this.comService = comService;
	}

	public BigDecimal getService() {
		return service;
	}

	public void setService(BigDecimal service) {
		this.service = service;
	}

	public BigDecimal getAddTicket() {
		return addTicket;
	}

	public void setAddTicket(BigDecimal addTicket) {
		this.addTicket = addTicket;
	}

	public BigDecimal getAddReservedSeat() {
		return addReservedSeat;
	}

	public void setAddReservedSeat(BigDecimal addReservedSeat) {
		this.addReservedSeat = addReservedSeat;
	}

}
