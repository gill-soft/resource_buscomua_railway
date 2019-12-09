package com.gillsoft.client;

import java.util.Date;
import java.util.List;

import com.gillsoft.cache.AbstractUpdateTask;
import com.gillsoft.concurrent.SerializablePoolType;
import com.gillsoft.model.ResponseError;
import com.gillsoft.util.ContextProvider;

public class RouteUpdateTask extends AbstractUpdateTask {

	private static final long serialVersionUID = 5964528266364148528L;
	private static final String POOL_NAME = "RAILWAY_ROUTE_POOL";
	private static final int POOL_SIZE = 20;
	private static final SerializablePoolType poolType = new SerializablePoolType(POOL_SIZE, POOL_NAME);
	
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
		try {
			List<Country> route = client.getRoute(from, to, date, trainNumber);
			writeObject(client.getCache(), RestClient.getRouteCacheKey(date, trainNumber), route,
					getTimeToLive(), 0, false, true, poolType);
		} catch (ResponseError e) {
			// ошибку тоже кладем в кэш и не обновляем
			writeObject(client.getCache(), RestClient.getRouteCacheKey(date, trainNumber), e,
					getTimeToLive(), 0, false, true, poolType);
		}
	}
	
	// время жизни до момента отправления
	private long getTimeToLive() {
		if (Config.getCacheTripTimeToLive() != 0) {
			return Config.getCacheTripTimeToLive();
		}
		return date.getTime() - System.currentTimeMillis();
	}

}
