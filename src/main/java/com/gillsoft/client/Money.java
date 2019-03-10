package com.gillsoft.client;

import java.io.Serializable;
import java.math.BigDecimal;

import com.gillsoft.model.Currency;

public class Money implements Serializable {

	private static final long serialVersionUID = -2426696912462019572L;

	private Currency currency;
	private BigDecimal value;

	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

}
