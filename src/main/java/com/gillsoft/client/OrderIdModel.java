package com.gillsoft.client;

import java.util.List;
import java.util.Map;

import com.gillsoft.model.AbstractJsonModel;

public class OrderIdModel extends AbstractJsonModel {

	private static final long serialVersionUID = 4318484251645220464L;
	
	private Map<String, List<String>> orders;

	public OrderIdModel() {

	}

	public Map<String, List<String>> getOrders() {
		return orders;
	}

	public void setOrders(Map<String, List<String>> orders) {
		this.orders = orders;
	}

	@Override
	public OrderIdModel create(String json) {
		return (OrderIdModel) super.create(json);
	}
	
}
