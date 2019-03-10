package com.gillsoft.client;

import java.io.IOException;
import java.net.URI;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.datetime.FastDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.gillsoft.cache.CacheHandler;
import com.gillsoft.cache.IOCacheException;
import com.gillsoft.cache.RedisMemoryCache;
import com.gillsoft.logging.RequestResponseLoggingInterceptor;
import com.gillsoft.model.ResponseError;
import com.gillsoft.util.RestTemplateUtil;
import com.gillsoft.util.StringUtil;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class RestClient {
	
	private static Logger LOGGER = LogManager.getLogger(RestClient.class);
	
	public static final String STATIONS_CACHE_KEY = "buscomua.stations";
	public static final String TRAINS_CACHE_KEY = "buscomua.trains.";
	public static final String TRAIN_CACHE_KEY = "buscomua.train.";
	public static final String ROUTE_CACHE_KEY = "buscomua.route.";
	
	public static final String DATE_FORMAT = "dd-MM-yyyy";
	public static final String DATE_TIME_FORMAT = "dd-MM-yyyy HH:mm";
	public static final FastDateFormat dateFormat = FastDateFormat.getInstance(DATE_FORMAT);
	public static final FastDateFormat dateTimeFormat = FastDateFormat.getInstance(DATE_TIME_FORMAT);
	
	public static final int RESERVETION_STATUS = 0;
	public static final int PAIED_STATUS = 1; //TODO change
	public static final int RETURNED_STATUS = 2; //TODO change
	public static final int CANCEL_STATUS = 4;
	
//	Можливі коди статусу: 0 – резерв; 
//	1 – сплачено (наприклад, після транзакції pay); 
//	2 – відхилено (наприклад, після транзакції revocation);  
//	3 – у стані купівлі; 
//	4 – у стані відхилення замовлення; 
//	5 – транзакція виконана з помилкою; 
//	6 – відмінено вручну; 
//	8 – у стані погашення; 
//	9 – погашено (наприклад, після транзакції cancel); 
//	10 – часткове погашення; 
//	11 – повернення документу; 
//	12 – в стані повернення.
	
	private static final String CONFIRM_CODE = "0";
	private static final String LANG_RU = "ru";
	private static final String SERVICE = "gd";
	
	private static final String STATIONS_LIST = "stations list";
	private static final String TRAINS = "trains";
	private static final String PRICES = "prices";
	private static final String PLACES = "places";
	private static final String ROUTE = "train_route";
	private static final String RESERVATION = "reservation.json";
	private static final String COMMIT = "commit.json";
	private static final String SHOW_BOOKING = "booking_show.json";
	private static final String CANCEL = "cancel.json";
	private static final String GET_REFUND_AMOUNT = "get_refund_amount.json";
	private static final String MAKE_REFUND = "make_refund.json";
	
	@Autowired
    @Qualifier("RedisMemoryCache")
	private CacheHandler cache;
	
	private RestTemplate template;
	
	// для запросов поиска с меньшим таймаутом
	private RestTemplate searchTemplate;
	
	public RestClient() {
		template = createNewPoolingTemplate(Config.getRequestTimeout());
		searchTemplate = createNewPoolingTemplate(Config.getSearchRequestTimeout());
	}
	
	public RestTemplate createNewPoolingTemplate(int requestTimeout) {
		RestTemplate template = new RestTemplate(new BufferingClientHttpRequestFactory(
				RestTemplateUtil.createPoolingFactory(Config.getUrl(), 300, requestTimeout, true, true)));
		template.setInterceptors(Collections.singletonList(
				new RequestResponseLoggingInterceptor() {

					@Override
					public ClientHttpResponse execute(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
							throws IOException {
						return new ClientHttpResponseWrapper(execution.execute(request, body));
					}

				}));
		for (HttpMessageConverter<?> conv : template.getMessageConverters()) {
			if (conv instanceof MappingJackson2HttpMessageConverter) {
				((MappingJackson2HttpMessageConverter) conv).setSupportedMediaTypes(
						Collections.singletonList(MediaType.valueOf("text/x-json")));
			}
		}
		return template;
	}
	
	@SuppressWarnings("unchecked")
	public List<Country> getCachedStations() throws IOCacheException {
		Map<String, Object> params = new HashMap<>();
		params.put(RedisMemoryCache.OBJECT_NAME, RestClient.STATIONS_CACHE_KEY);
		params.put(RedisMemoryCache.IGNORE_AGE, true);
		params.put(RedisMemoryCache.UPDATE_DELAY, Config.getCacheStationsUpdateDelay());
		params.put(RedisMemoryCache.UPDATE_TASK, new StationsUpdateTask());
		return (List<Country>) cache.read(params);
	}
	
	public List<Country> getStations() {
		Request request = createRequest(STATIONS_LIST);
		try {
			return getResult(searchTemplate, request).getResult().getCountry();
		} catch (ResponseError e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Result getCachedTrains(String from, String to, Date date) throws ResponseError, IOCacheException {
		Map<String, Object> params = new HashMap<>();
		params.put(RedisMemoryCache.OBJECT_NAME, getTrainsCacheKey(date, from, to));
		params.put(RedisMemoryCache.UPDATE_TASK, new TrainsUpdateTask(from, to, date));
		return (Result) checkCache(cache.read(params));
	}
	
	private Object checkCache(Object value) throws ResponseError {
		if (value instanceof ResponseError) {
			throw (ResponseError) value;
		} else {
			return value;
		}
	}
	
	public Result getTrains(String from, String to, Date date) throws ResponseError {
		Request request = createRequest(TRAINS);
		request.getParams().setCodeStationFrom(from);
		request.getParams().setCodeStationTo(to);
		request.getParams().setDate1(date);
		return getResult(searchTemplate, request).getResult();
	}
	
	public Train getCachedTrain(String from, String to, Date date, String trainNumber) throws ResponseError, IOCacheException {
		Map<String, Object> params = new HashMap<>();
		params.put(RedisMemoryCache.OBJECT_NAME, getTrainCacheKey(from, to, date, trainNumber));
		params.put(RedisMemoryCache.UPDATE_TASK, new TrainUpdateTask(from, to, date, trainNumber));
		return (Train) checkCache(cache.read(params));
	}
	
	public Train getTrain(String from, String to, Date date, String trainNumber) throws ResponseError {
		Request request = createRequest(PRICES);
		request.getParams().setCodeStationFrom(from);
		request.getParams().setCodeStationTo(to);
		request.getParams().setDate(date);
		request.getParams().setTrain(trainNumber);
		return getResult(searchTemplate, request).getResult().getTrain();
	}
	
	public Train getPlaces(String from, String to, Date date, String trainNumber, String carType, String carClass,
			String carNumber) throws ResponseError {
		Request request = createRequest(PLACES);
		request.getParams().setCodeStationFrom(from);
		request.getParams().setCodeStationTo(to);
		request.getParams().setDate(date);
		request.getParams().setTrain(trainNumber);
		request.getParams().setWagonClass(carClass);
		request.getParams().setWagonType(carType);
		request.getParams().setWagonNumber(carNumber);
		return getResult(searchTemplate, request).getResult().getTrain();
	}
	
	@SuppressWarnings("unchecked")
	public List<Country> getCachedRoute(String from, String to, Date date, String trainNumber) throws ResponseError, IOCacheException {
		Map<String, Object> params = new HashMap<>();
		params.put(RedisMemoryCache.OBJECT_NAME, getRouteCacheKey(date, trainNumber));
		params.put(RedisMemoryCache.UPDATE_TASK, new RouteUpdateTask(from, to, date, trainNumber));
		return (List<Country>) checkCache(cache.read(params));
	}
	
	public List<Country> getRoute(String from, String to, Date date, String trainNumber) throws ResponseError {
		Request request = createRequest(ROUTE);
		request.getParams().setCodeStationFrom(from);
		request.getParams().setCodeStationTo(to);
		request.getParams().setDate(date);
		request.getParams().setTrain(trainNumber);
		return getResult(searchTemplate, request).getResult().getCountries();
	}
//	
//	public Response reservation(String sessionId, String operationType, List<Customer> customers, List<Seat> seats) throws ResponseError {
//		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
//		params.add("key", Config.getKey());
//		params.add("lang", LANG_RU);
//		params.add("session_id", sessionId);
//		List<String> passengers = new ArrayList<>(customers.size());
//		for (Customer customer : customers) {
//			passengers.add(String.join(":", customer.getName(), customer.getSurname(), "", ""));
//		}
//		params.add("passengers", String.join("|", passengers));
//		params.add("auth_key", Config.getAuthKey());
//		params.add("no_clothes", "1");
//		params.add("operation_type", operationType);
//		params.add("range", String.join(",", seats.stream().map(Seat::getId).collect(Collectors.toList())));
//		return getResult(template, RESERVATION, params);
//	}
//	
//	public Train commit(String orderId, String amount, String currency) throws ResponseError {
//		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
//		params.add("key", Config.getKey());
//		params.add("lang", LANG_RU);
//		params.add("сommit_auth_key", Config.getAuthKey());
//		params.add("signature", getSignature(orderId, amount));
//		params.add("service", SERVICE);
//		params.add("order_id", orderId);
//		params.add("amount", amount);
//		params.add("currency", currency);
//		return getResult(template, COMMIT, params).getOrder();
//	}
//	
//	private String getSignature(String orderId, String amount) {
//		return StringUtil.md5(String.join("",
//				Config.getShopApiKey(), SERVICE, orderId, amount, Config.getShopSecretKey()));
//	}
//	
//	public Train getBooking(String reservationId) throws ResponseError {
//		return bookingOperation(reservationId, SHOW_BOOKING);
//	}
//	
//	public Train cancelBooking(String reservationId) throws ResponseError {
//		return bookingOperation(reservationId, CANCEL);
//	}
//	
//	private Train bookingOperation(String reservationId, String method) throws ResponseError {
//		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
//		params.add("key", Config.getKey());
//		params.add("lang", LANG_RU);
//		params.add("auth_key", Config.getAuthKey());
//		params.add("reservation_id", reservationId);
//		return getResult(template, method, params).getBooking();
//	}
//	
//	public Refund getRefundAmount(String reservationId, String passengerId) throws ResponseError {
//		return refundOperation(reservationId, passengerId, GET_REFUND_AMOUNT);
//	}
//	
//	public Refund refund(String reservationId, String passengerId) throws ResponseError {
//		return refundOperation(reservationId, passengerId, MAKE_REFUND);
//	}
//	
//	private Refund refundOperation(String reservationId, String passengerId, String method) throws ResponseError {
//		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
//		params.add("key", Config.getKey());
//		params.add("lang", LANG_RU);
//		params.add("auth_key", Config.getAuthKey());
//		params.add("reservation_id", reservationId);
//		params.add("passenger_id", passengerId);
//		return getResult(template, method, params).getRefund();
//	}
	
	private Response getResult(RestTemplate template, Request request) throws ResponseError {
		URI uri = UriComponentsBuilder.fromUriString(Config.getUrl()).build().toUri();
		ResponseEntity<Response> response = null;
		try {
			response = template.exchange(new RequestEntity<Request>(request, HttpMethod.POST, uri), Response.class);
		} catch (Exception e) {
			throw new ResponseError(e.getMessage());
		}
		if (!Objects.equals(request.getId(), response.getBody().getId())) {
			throw new ResponseError("Error. Response from other request.");
		}
		if (response.getBody().getResult() == null) {
			throw new ResponseError("Error. Empty response result.");
		}
		// проверяем ответ на ошибку
		if (response.getBody().getError() != null) {
			throw new ResponseError(response.getBody().getError().getData());
		}
		return response.getBody();
	}
	
	private Request createRequest(String method) {
		Request request = new Request();
		request.setId(StringUtil.generateUUID());
		request.setAgent(Config.getAgent());
		request.setWorkplace(Config.getWorkplace());
		request.setMethod(method);
		request.setLanguage(LANG_RU);
		request.setParams(new Params());
		return request;
	}
	
	public CacheHandler getCache() {
		return cache;
	}
	
	public static RestClientException createUnavailableMethod() {
		return new RestClientException("Method is unavailable");
	}
	
	public static String getTrainsCacheKey(Date date, String from, String to) {
		return TRAINS_CACHE_KEY + String.join(";",
				String.valueOf(DateUtils.truncate(date, Calendar.DATE).getTime()), from, to);
	}
	
	public static String getTrainCacheKey(String from, String to, Date date, String trainNumber) {
		return TRAIN_CACHE_KEY + String.join(";",
				String.valueOf(DateUtils.truncate(date, Calendar.DATE).getTime()), from, to, trainNumber);
	}
	
	public static String getRouteCacheKey(Date date, String trainNumber) {
		return ROUTE_CACHE_KEY + String.join(";",
				String.valueOf(DateUtils.truncate(date, Calendar.DATE).getTime()), trainNumber);
	}

}
