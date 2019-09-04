package com.gillsoft.client;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import org.apache.commons.lang.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.datetime.FastDateFormat;
import org.joda.time.LocalDate;
import org.joda.time.Years;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.ParameterizedTypeReference;
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
import com.gillsoft.model.Customer;
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
	
	// 0–оформлений, 1–відмінений, 2–викуплений, 3–документ відмінено чи він не існує, 6–погашений, 9–використаний, 20–роздрукований
	public static final String RESERVETION_STATUS = "0";
	public static final String PAIED_STATUS = "2;9;20";
	public static final String CANCEL_STATUS = "1;3;6";
	
	private static final String LANG_RU = "ru";
	private static final int MAX_AGE = 14;
	private static final String FULL = "full";
	private static final String CHILD = "child";
	private static final String TRANSACTION_TYPE = "3";
	
	private static final String STATIONS_LIST = "stations list";
	private static final String TRAINS = "trains";
	private static final String PRICES = "prices";
	private static final String PLACES = "places";
	private static final String ROUTE = "train_route";
	private static final String RESERVATION = "reserve";
	private static final String PAY = "pay";
	private static final String STATUS_DOC = "status_doc";
	private static final String CANCEL = "cancel";
	private static final String REVOCATION = "revocation";
	private static final String RETURN_DOC = "return_doc";
	private static final String RETURN_DOC_CONFIRM = "return_doc_confirm";
	private static final String GET_RESPONSE = "get response";
	
	private static final String RETRY_ERROR_CODE = "4214";
	
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
			return getResult(searchTemplate, request, new ParameterizedTypeReference<Response<Result>>() {}).getResult().getCountry();
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
		return getResult(searchTemplate, request, new ParameterizedTypeReference<Response<Result>>() {}).getResult();
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
		return getResult(searchTemplate, request, new ParameterizedTypeReference<Response<Result>>() {}).getResult().getTrain();
	}
	
	public Train getCachedPlaces(String from, String to, Date date, String trainNumber, String carType, String carClass,
			String carNumber) throws ResponseError {
		Map<String, Object> params = new HashMap<>();
		params.put(RedisMemoryCache.OBJECT_NAME, getPlacesCacheKey(from, to, date, trainNumber, carType, carClass, carNumber));
		try {
			return (Train) checkCache(cache.read(params));
		} catch (ResponseError e) {
			throw e;
		} catch (IOCacheException e) {
			params.put(RedisMemoryCache.TIME_TO_LIVE, 30000l);
			Train train = getPlaces(from, to, date, trainNumber, carType, carClass, carNumber);
			try {
				cache.write(train, params);
			} catch (IOCacheException ex) {
			}
			return train;
		}
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
		return getResult(searchTemplate, request, new ParameterizedTypeReference<Response<Result>>() {}).getResult().getTrain();
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
		return getResult(searchTemplate, request, new ParameterizedTypeReference<Response<Result>>() {}).getResult().getCountries();
	}
	
	public OrderResult reservation(TripIdModel idModel, String places, List<Customer> customers) throws ResponseError {
		Request request = createRequest(RESERVATION);
		request.getParams().setCodeStationFrom(idModel.getFrom());
		request.getParams().setCodeStationTo(idModel.getTo());
		request.getParams().setDate(idModel.getDate());
		request.getParams().setTrain(idModel.getTrain());
		request.getParams().setWagonClass(idModel.getClas());
		request.getParams().setWagonType(idModel.getType());
		request.getParams().setWagonNumber(idModel.getCar());
		request.getParams().setPlaces(places);
		request.getParams().setDocuments(new HashMap<>());
		List<Document> documents = new ArrayList<>();
		int number = 1;
		for (Customer customer : customers) {
			Document document = new Document();
			document.setNumber(number++);
			document.setCountPlace(1);
			document.setFirstname(customer.getName());
			document.setLastname(customer.getSurname());
			if (customer.getBirthday() != null
					&& Years.yearsBetween(new LocalDate(idModel.getDate()), new LocalDate(customer.getBirthday())).getYears() < MAX_AGE) {
				document.setChild(customer.getBirthday());
				document.setKind(CHILD);
			} else {
				document.setKind(FULL);
			}
			documents.add(document);
		}
		request.getParams().getDocuments().put("document", documents);
		return getResult(template, request, new ParameterizedTypeReference<Response<OrderResult>>() {}).getResult();
	}
	
	public CancelResult revocation(String reserveId) throws ResponseError {
		return cancelOperation(reserveId, REVOCATION);
	}
	
	public CancelResult cancel(String reserveId) throws ResponseError {
		return cancelOperation(reserveId, CANCEL);
	}
	
	private CancelResult cancelOperation(String reserveId, String method) throws ResponseError {
		Request request = createRequest(method);
		request.getParams().setReserveId(reserveId);
		return getResult(template, request, new ParameterizedTypeReference<Response<CancelResult>>() {}).getResult();
	}
	
	public List<Order> pay(String reserveId) throws ResponseError {
		Request request = createRequest(PAY);
		request.getParams().setReserveId(reserveId);
		request.getParams().setTransactionType(TRANSACTION_TYPE);
		return getResult(template, request, new ParameterizedTypeReference<Response<OrderResult>>() {}).getResult().getOrder();
	}
	
	public OrderDocument getDocStatus(String uid) throws ResponseError {
		Request request = createRequest(STATUS_DOC);
		request.getParams().setUid(uid);
		return getResult(template, request, new ParameterizedTypeReference<Response<OrderResult>>() {}).getResult().getDocument();
	}
	
	public List<Order> getReturnAmount(String uid, Document passport) throws ResponseError {
		return returnOperation(uid, passport, RETURN_DOC);
	}
	
	public List<Order> returnConfirm(String uid, Document passport) throws ResponseError {
		return returnOperation(uid, passport, RETURN_DOC_CONFIRM);
	}
	
	private List<Order> returnOperation(String uid, Document passport, String method) throws ResponseError {
		Request request = createRequest(method);
		request.getParams().setUid(uid);
		request.getParams().setPassport(passport);
		return getResult(template, request, new ParameterizedTypeReference<Response<OrderResult>>() {}).getResult().getOrder();
	}
	
	public List<Order> getPayResponse(String reserveId) throws ResponseError {
		Request request = createRequest(GET_RESPONSE);
		request.getParams().setSrcId(reserveId);
		return getResult(template, request, new ParameterizedTypeReference<Response<OrderResult>>() {}).getResult().getOrder();
	}
	
	private <T> Response<T> getResult(RestTemplate template, Request request,
			ParameterizedTypeReference<Response<T>> type) throws ResponseError {
		int tryCount = 0;
		do {
			URI uri = UriComponentsBuilder.fromUriString(Config.getUrl()).build().toUri();
			ResponseEntity<Response<T>> response = null;
			try {
				response = template.exchange(new RequestEntity<Request>(request, HttpMethod.POST, uri), type);
			} catch (Exception e) {
				LOGGER.error("Error when execute request: " + uri.toString(), e);
				throw new ResponseError(e.getMessage());
			}
			if (!Objects.equals(request.getId(), response.getBody().getId())) {
				throw new ResponseError("Error. Response from other request.");
			}
			// проверяем ответ на ошибку
			if (response.getBody().getError() != null) {
				if (RETRY_ERROR_CODE.equals(response.getBody().getError().getCode())) {
					request.setId(StringUtil.generateUUID());
					tryCount++;
					try {
						Thread.sleep(new Random().nextInt(2000) + 1000l);
					} catch (Exception e) {
					}
					continue;
				}
				throw new ResponseError(response.getBody().getError().getMessage()
						+ " " + response.getBody().getError().getData());
			}
			if (response.getBody().getResult() == null) {
				throw new ResponseError("Error. Empty response result.");
			}
			return response.getBody();
		} while (tryCount < 10);
		throw new ResponseError("Error. Can not send request.");
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
	
	public static String getPlacesCacheKey(String from, String to, Date date, String trainNumber, String carType,
			String carClass, String carNumber) {
		return TRAIN_CACHE_KEY + String.join(";",
				String.valueOf(DateUtils.truncate(date, Calendar.DATE).getTime()), from, to, trainNumber, carType, carClass, carNumber);
	}
	
	public static String getRouteCacheKey(Date date, String trainNumber) {
		return ROUTE_CACHE_KEY + String.join(";",
				String.valueOf(DateUtils.truncate(date, Calendar.DATE).getTime()), trainNumber);
	}
	
}
