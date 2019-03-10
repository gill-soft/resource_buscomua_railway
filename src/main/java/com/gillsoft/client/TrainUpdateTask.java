package com.gillsoft.client;

import java.util.Date;

import com.gillsoft.cache.AbstractUpdateTask;
import com.gillsoft.model.ResponseError;
import com.gillsoft.util.ContextProvider;

public class TrainUpdateTask extends AbstractUpdateTask {

	private static final long serialVersionUID = -3353809778569437497L;
	
	private String from;
	private String to;
	private Date date;
	private String trainNumber;
	
	public TrainUpdateTask(String from, String to, Date date, String trainNumber) {
		this.from = from;
		this.to = to;
		this.date = date;
		this.trainNumber = trainNumber;
	}

	@Override
	public void run() {
		RestClient client = ContextProvider.getBean(RestClient.class);
		try {
			Train train = client.getTrain(from, to, date, trainNumber);
			writeObject(client.getCache(), RestClient.getTrainCacheKey(from, to, date, trainNumber), train,
					getTimeToLive(train), Config.getCacheTripUpdateDelay());
		} catch (ResponseError e) {
			// ошибку тоже кладем в кэш
			writeObject(client.getCache(), RestClient.getTrainCacheKey(from, to, date, trainNumber), e,
					Config.getCacheErrorTimeToLive(), Config.getCacheErrorUpdateDelay());
		}
	}
	
	// время жизни до момента отправления
	private long getTimeToLive(Train train) {
		if (Config.getCacheTripTimeToLive() != 0) {
			return Config.getCacheTripTimeToLive();
		}
		return date.getTime() - System.currentTimeMillis();
	}

}
