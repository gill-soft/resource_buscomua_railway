package com.gillsoft.client;

import java.util.Date;
import java.util.List;

import com.gillsoft.cache.AbstractUpdateTask;
import com.gillsoft.model.ResponseError;
import com.gillsoft.util.ContextProvider;

public class TrainsUpdateTask extends AbstractUpdateTask {

	private static final long serialVersionUID = -5103341288539272796L;
	
	private String from;
	private String to;
	private Date date;
	
	public TrainsUpdateTask(String from, String to, Date date) {
		this.from = from;
		this.to = to;
		this.date = date;
	}

	@Override
	public void run() {
		RestClient client = ContextProvider.getBean(RestClient.class);
		try {
			Result result = client.getTrains(from, to, date);
			List<Train> trains = result.getTrains();
			for (Train train : trains) {
				try {
					client.getCachedTrain(from, to, train.getDepartureDate(), train.getNumber());
				} catch (Exception e) {
				}
			}
			writeObject(client.getCache(), RestClient.getTrainsCacheKey(date, from, to), result,
					getTimeToLive(trains), Config.getCacheTripUpdateDelay());
		} catch (ResponseError e) {
			// ошибку тоже кладем в кэш
			writeObject(client.getCache(), RestClient.getTrainsCacheKey(date, from, to), e,
					Config.getCacheErrorTimeToLive(), Config.getCacheErrorUpdateDelay());
		}
	}
	
	// время жизни до момента самого позднего отправления
	private long getTimeToLive(List<Train> trains) {
		if (Config.getCacheTripTimeToLive() != 0) {
			return Config.getCacheTripTimeToLive();
		}
		long max = 0;
		for (Train train : trains) {
			Date date = train.getDepartureDate();
			if (date != null && date.getTime() > max) {
				max = date.getTime();
			}
		}
		if (max == 0
				|| max < System.currentTimeMillis()) {
			return Config.getCacheErrorTimeToLive();
		}
		return max - System.currentTimeMillis();
	}

}
