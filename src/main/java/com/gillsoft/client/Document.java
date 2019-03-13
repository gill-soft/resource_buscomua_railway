package com.gillsoft.client;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Document implements Serializable {

	private static final long serialVersionUID = 2621541100220161454L;

	private int number;
	private String kind;

	@JsonProperty("count_place")
	private int countPlace;

	private String firstname;
	private String lastname;
	
	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd")
	private Date child;

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public int getCountPlace() {
		return countPlace;
	}

	public void setCountPlace(int countPlace) {
		this.countPlace = countPlace;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public Date getChild() {
		return child;
	}

	public void setChild(Date child) {
		this.child = child;
	}

}
