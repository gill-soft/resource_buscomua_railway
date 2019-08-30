package com.gillsoft.client;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class Params implements Serializable {
	
	private static final long serialVersionUID = -6463298395302250347L;
	
	private String name;
	private String train;
	
	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd")
	private Date date;
	
	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd")
	private Date date1;
	
	@JsonProperty("code_station_from")
	private String codeStationFrom;
	
	@JsonProperty("code_station_to")
	private String codeStationTo;
	
	@JsonProperty("wagon_type")
	private String wagonType;
	
	@JsonProperty("wagon_class")
	private String wagonClass;
	
	@JsonProperty("wagon_number")
	private String wagonNumber;
	
	@JsonProperty("no_bedding")
	private String noBedding = "true";
	
	@JsonProperty("no_electronic")
	private String noElectronic = "false";
	
	private String places;
	
	@JsonProperty("reserve_id")
	private String reserveId;
	
	@JsonProperty("transaction_type")
	private String transactionType;
	
	@JsonProperty("src_id")
	private String srcId;
	
	private String uid;
	private Document passport;
	private Map<String, List<Document>> documents;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTrain() {
		return train;
	}

	public void setTrain(String train) {
		this.train = train;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Date getDate1() {
		return date1;
	}

	public void setDate1(Date date1) {
		this.date1 = date1;
	}

	public String getCodeStationFrom() {
		return codeStationFrom;
	}

	public void setCodeStationFrom(String codeStationFrom) {
		this.codeStationFrom = codeStationFrom;
	}

	public String getCodeStationTo() {
		return codeStationTo;
	}

	public void setCodeStationTo(String codeStationTo) {
		this.codeStationTo = codeStationTo;
	}

	public String getWagonType() {
		return wagonType;
	}

	public void setWagonType(String wagonType) {
		this.wagonType = wagonType;
	}

	public String getWagonClass() {
		return wagonClass;
	}

	public void setWagonClass(String wagonClass) {
		this.wagonClass = wagonClass;
	}

	public String getWagonNumber() {
		return wagonNumber;
	}

	public void setWagonNumber(String wagonNumber) {
		this.wagonNumber = wagonNumber;
	}

	public String getNoBedding() {
		return noBedding;
	}

	public void setNoBedding(String noBedding) {
		this.noBedding = noBedding;
	}

	public String getNoElectronic() {
		return noElectronic;
	}

	public void setNoElectronic(String noElectronic) {
		this.noElectronic = noElectronic;
	}

	public String getPlaces() {
		return places;
	}

	public void setPlaces(String places) {
		this.places = places;
	}

	public String getReserveId() {
		return reserveId;
	}

	public void setReserveId(String reserveId) {
		this.reserveId = reserveId;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public String getSrcId() {
		return srcId;
	}

	public void setSrcId(String srcId) {
		this.srcId = srcId;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public Document getPassport() {
		return passport;
	}

	public void setPassport(Document passport) {
		this.passport = passport;
	}

	public Map<String, List<Document>> getDocuments() {
		return documents;
	}

	public void setDocuments(Map<String, List<Document>> documents) {
		this.documents = documents;
	}

}
