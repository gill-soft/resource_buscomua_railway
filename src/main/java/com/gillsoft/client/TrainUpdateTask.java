package com.gillsoft.client;

import java.util.Date;

import com.gillsoft.cache.AbstractUpdateTask;
import com.gillsoft.concurrent.SerializablePoolType;
import com.gillsoft.model.ResponseError;
import com.gillsoft.util.ContextProvider;

public class TrainUpdateTask extends AbstractUpdateTask {

	private static final long serialVersionUID = 8020863932495639088L;
	
	private static final String POOL_NAME = "RAILWAY_TRAIN_POOL";
	private static final int POOL_SIZE = 200;
	private static final SerializablePoolType poolType = new SerializablePoolType(POOL_SIZE, POOL_NAME);
	
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
		client.addRequestTask(() -> {
			try {
				Train train = client.getTrain(from, to, date, trainNumber);
				writeObject(client.getCache(), RestClient.getTrainCacheKey(from, to, date, trainNumber), train,
						getTimeToLive(train), TrainsUpdateTask.getHalfPartOfDepartureTime(date), false, false, poolType);
			} catch (ResponseError e) {
				// ошибку тоже кладем в кэш
				writeObject(client.getCache(), RestClient.getTrainCacheKey(from, to, date, trainNumber), e,
						Config.getCacheErrorTimeToLive(), 0, false, true, poolType);
			}
		});
	}
	
	// время жизни до момента отправления
	private long getTimeToLive(Train train) {
		if (Config.getCacheTripTimeToLive() != 0) {
			return Config.getCacheTripTimeToLive();
		}
		if (date.getTime() < System.currentTimeMillis()) {
			return Config.getCacheErrorTimeToLive();
		}
		return date.getTime() - System.currentTimeMillis();
	}

}
