package com.gillsoft.client;

import java.util.Date;
import java.util.List;

import com.gillsoft.cache.AbstractUpdateTask;
import com.gillsoft.model.ResponseError;
import com.gillsoft.util.ContextProvider;

public class RouteUpdateTask extends AbstractUpdateTask {

	private String from;
	private String to;
	private Date date;
	private String trainNumber;
	
	public RouteUpdateTask(String from, String to, Date date, String trainNumber) {
		this.from = from;
		this.to = to;
		this.date = date;
		this.trainNumber = trainNumber;
	}

	@Override
	public void run() {
		RestClient client = ContextProvider.getBean(RestClient.class);
		client.addRequestTask(() -> {
			try {
				List<Country> route = client.getRoute(from, to, date, trainNumber);
				writeObject(client.getCache(), RestClient.getRouteCacheKey(date, trainNumber), route,
						getTimeToLive(), 0, false, true);
			} catch (ResponseError e) {
				// ошибку тоже кладем в кэш и не обновляем
				writeObject(client.getCache(), RestClient.getRouteCacheKey(date, trainNumber), e,
						getTimeToLive(), 0, false, true);
			}
		});
	}
	
	// время жизни до момента отправления
	private long getTimeToLive() {
		if (Config.getCacheTripTimeToLive() != 0) {
			return Config.getCacheTripTimeToLive();
		}
		if (date.getTime() < System.currentTimeMillis()) {
			return Config.getCacheErrorTimeToLive();
		}
		return date.getTime() - System.currentTimeMillis();
	}

}
