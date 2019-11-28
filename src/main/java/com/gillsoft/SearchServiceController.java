package com.gillsoft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;

import com.gillsoft.abstract_rest_service.SimpleAbstractTripSearchService;
import com.gillsoft.cache.CacheHandler;
import com.gillsoft.cache.IOCacheException;
import com.gillsoft.client.Country;
import com.gillsoft.client.RestClient;
import com.gillsoft.client.Result;
import com.gillsoft.client.Train;
import com.gillsoft.client.TripIdModel;
import com.gillsoft.client.Value;
import com.gillsoft.client.Wagon;
import com.gillsoft.model.Carriage;
import com.gillsoft.model.CarriageClass;
import com.gillsoft.model.Document;
import com.gillsoft.model.Lang;
import com.gillsoft.model.Locality;
import com.gillsoft.model.Organisation;
import com.gillsoft.model.Price;
import com.gillsoft.model.RequiredField;
import com.gillsoft.model.ResponseError;
import com.gillsoft.model.RestError;
import com.gillsoft.model.ReturnCondition;
import com.gillsoft.model.Route;
import com.gillsoft.model.RoutePoint;
import com.gillsoft.model.Seat;
import com.gillsoft.model.SeatStatus;
import com.gillsoft.model.SeatType;
import com.gillsoft.model.SeatsScheme;
import com.gillsoft.model.Segment;
import com.gillsoft.model.SimpleTripSearchPackage;
import com.gillsoft.model.Tariff;
import com.gillsoft.model.Trip;
import com.gillsoft.model.TripContainer;
import com.gillsoft.model.Vehicle;
import com.gillsoft.model.request.TripSearchRequest;
import com.gillsoft.model.response.TripSearchResponse;
import com.gillsoft.util.StringUtil;

@RestController
public class SearchServiceController extends SimpleAbstractTripSearchService<SimpleTripSearchPackage<Map<String, Train>>> {
	
	@Autowired
	private RestClient client;
	
	@Autowired
	@Qualifier("RedisMemoryCache")
	private CacheHandler cache;
	
	@Autowired
	private SeatsSchemeController schemaController;

	@Override
	public TripSearchResponse initSearchResponse(TripSearchRequest request) {
		return simpleInitSearchResponse(cache, request);
	}
	
	@Override
	public void addInitSearchCallables(List<Callable<SimpleTripSearchPackage<Map<String, Train>>>> callables,
			TripSearchRequest request) {
		callables.add(() -> {
			SimpleTripSearchPackage<Map<String, Train>> searchPackage = new SimpleTripSearchPackage<>();
			searchPackage.setSearchResult(new HashMap<>());
			searchPackage.setRequest(request);
			searchTrips(searchPackage);
			return searchPackage;
		});
	}
	
	private void searchTrips(SimpleTripSearchPackage<Map<String, Train>> searchPackage) {
		searchPackage.setInProgress(false);
		try {
			TripSearchRequest request = searchPackage.getRequest();
			Result result = client.getCachedTrains(request.getLocalityPairs().get(0)[0], request.getLocalityPairs().get(0)[1],
					request.getDates().get(0));
			List<Train> trains = result.getTrains();
			for (Train train : trains) {
				try {
					// получаем каждый поезд по отдельности со стоимостью и вагонами
					if (!searchPackage.getSearchResult().containsKey(train.getNumber())) {
						
						// запускаем формаирование маршрута
//						try {
//							client.getCachedRoute(result.getStationFrom().getCode(), result.getStationTo().getCode(),
//									train.getDepartureDate(), train.getNumber());
//						} catch (Exception e) {
//						}
						Train details = client.getCachedTrain(result.getStationFrom().getCode(), result.getStationTo().getCode(),
								train.getDepartureDate(), train.getNumber());
						train.setStationFrom(result.getStationFrom());
						train.setStationTo(result.getStationTo());
						train.setWagons(details.getWagons());
						searchPackage.getSearchResult().putIfAbsent(details.getNumber(), train);
					}
				} catch (IOCacheException e) {
					searchPackage.setInProgress(true);
				} catch (ResponseError e) {
				}
			}
		} catch (IOCacheException e) {
			searchPackage.setInProgress(true);
		} catch (ResponseError e) {
			searchPackage.setException(e);
		}
	}

	@Override
	public TripSearchResponse getSearchResultResponse(String searchId) {
		return simpleGetSearchResponse(cache, searchId);
	}
	
	@Override
	public void addNextGetSearchCallablesAndResult(List<Callable<SimpleTripSearchPackage<Map<String, Train>>>> callables,
			Map<String, Vehicle> vehicles, Map<String, Locality> localities, Map<String, Organisation> organisations,
			Map<String, Segment> segments, List<TripContainer> containers,
			SimpleTripSearchPackage<Map<String, Train>> result) {
		
		// добавляем уже найденные поезда
		addResult(vehicles, localities, organisations, segments, containers, result);
		
		// продолжаем поиск, если он еще не окончен
		if (result.isInProgress()) {
			callables.add(() -> {
				searchTrips(result);
				return result;
			});
		}
	}
	
	private void addResult(Map<String, Vehicle> vehicles, Map<String, Locality> localities,
			Map<String, Organisation> organisations, Map<String, Segment> segments, List<TripContainer> containers,
			SimpleTripSearchPackage<Map<String, Train>> result) {
		TripContainer container = new TripContainer();
		container.setRequest(result.getRequest());
		if (result.getSearchResult() != null) {
			List<Trip> trips = new ArrayList<>();
			for (Train train : result.getSearchResult().values()) {
				
				// проверяем возвращен уже этот поезд или нет 
				if (!train.isAdded()) {
					
					// классы вагонов обьединяем в отдельные рейсы
					Map<String, List<Wagon>> wagons = train.getWagons().stream().collect(Collectors.groupingBy(
							w -> String.join(";", w.getCost().getValue().toString(), w.getType().getCode(), w.getClas().getCode()),
							Collectors.toList()));
					for (List<Wagon> wagonsList : wagons.values()) {
						String segmentId = addSegment(vehicles, localities, organisations, segments, train, wagonsList);
						if (segmentId != null) {
							Trip resTrip = new Trip();
							resTrip.setId(segmentId);
							trips.add(resTrip);
						}
					}
					train.setAdded(true);
				}
			}
			container.setTrips(trips);
		}
		if (result.getException() != null) {
			container.setError(new RestError(result.getException().getMessage()));
		}
		containers.add(container);
	}
	
	public String addSegment(Map<String, Vehicle> vehicles, Map<String, Locality> localities,
			Map<String, Organisation> organisations, Map<String, Segment> segments, Train train, List<Wagon> wagons) {
		Segment segment = new Segment();
		segment.setNumber(getTrainNumber(train.getNumber(), train.getClas().getCode(), train.getCategory(), false));
		segment.setDepartureDate(train.getDepartureDate());
		segment.setArrivalDate(train.getArrivalDate());
		segment.setFreeSeatsCount(getFreeSeatsCount(wagons));
		
		segment.setDeparture(createLocality(localities, train.getStationFrom()));
		segment.setArrival(createLocality(localities, train.getStationTo()));
		
		segment.setVehicle(addVehicle(vehicles, train.getNumber()));
		
		Wagon first = wagons.get(0);
		segment.setPrice(createPrice(first));
		
		TripIdModel id = new TripIdModel(first.getNumber(), first.getClas().getCode(), first.getType().getCode(),
				train.getNumber(), train.getStationFrom().getCode(), train.getStationTo().getCode(), train.getDepartureDate());
		
		segment.setCarriages(new ArrayList<>(wagons.size()));
		for (Wagon wagon : wagons) {
			Carriage carriage = createCarriage(wagon);
			id.setCar(wagon.getNumber());
			carriage.setId(id.asString());
			segment.getCarriages().add(carriage);
		}
		if (!segment.getCarriages().isEmpty()) {
			id.setCar(null);
			String key = id.asString();
			segments.put(key, segment);
			
			// получаем маршрут
			try {
				List<Country> route = client.getCachedRoute(train.getStationFrom().getCode(), train.getStationTo().getCode(),
						train.getDepartureDate(), train.getNumber());
				segment.setRoute(createRoute(route, localities));
			} catch (Exception e) {
			}
			return key;
		}
		return null;
	}
	
	public String getTrainNumber(String number, String clas, String category, boolean firm) {
		if (firm) {
			number += " ФІРМ";
		} else {
			switch (clas) {
				case "4":
					number += " ФІРМ";
					break;
				case "8":
					number += " ЕПК";
					break;
				default:
					break;
				}
		}
		switch (category) {
		case "1":
			number += " ІС+";
			break;
		case "2":
			number += " ІС";
			break;
		case "3":
			number += " РЕ";
			break;
		case "4":
			number += " Р";
			break;
		case "5":
			number += " НЕ";
			break;
		case "6":
			number += " НШ";
			break;
		case "7":
			number += " НП";
			break;
		default:
			break;
		}
		return number;
	}
	
	public Carriage createCarriage(Wagon wagon) {
		Carriage carriage = new Carriage();
		carriage.setNumber(wagon.getNumber());
		carriage.setClas(getCarClass(wagon.getClas().getCode(), wagon.getType().getCode()));
		if (wagon.getPlaces() != null) {
			carriage.setFreeLowerPlaces(wagon.getPlaces().getLower().getValue());
			carriage.setFreeLowerSidePlaces(wagon.getPlaces().getLower().getSide());
			carriage.setFreeTopPlaces(wagon.getPlaces().getTop().getValue());
			carriage.setFreeTopSidePlaces(wagon.getPlaces().getTop().getSide());
		}
		return carriage;
	}
	
	private int getFreeSeatsCount(List<Wagon> wagons) {
		int count = 0;
		for (Wagon wagon : wagons) {
			if (wagon.getPlaces() != null) {
				count += wagon.getPlaces().getLower().getSide()
						+ wagon.getPlaces().getLower().getValue()
						+ wagon.getPlaces().getTop().getSide()
						+ wagon.getPlaces().getTop().getValue();
			}
		}
		return count;
	}
	
	private CarriageClass getCarClass(String clas, String type) {
		switch (type) {
		case "Л":
			return CarriageClass.FIRST;
		case "К":
			return CarriageClass.SECOND;
		case "П":
			return CarriageClass.THIRD;
		case "С":
			switch (clas) {
			case "1":
				return CarriageClass.RESERVED_FIRST;
			case "2":
				return CarriageClass.RESERVED_SECOND;
			case "3":
				return CarriageClass.RESERVED_THIRD;
			}
		case "О":
			return CarriageClass.NON_RESERVED;
		case "М":
			return CarriageClass.COMFORTABLE;
		default:
			return null;
		}
	}
	
	public Locality createLocality(Map<String, Locality> localities, Value value) {
		String key = value.getCode();
		Locality station = new Locality();
		station.setName(Lang.RU, value.getValue());
		if (localities == null) {
			station.setId(key);
			return station;
		}
		Locality locality = localities.get(key);
		if (locality == null) {
			localities.put(key, station);
		}
		return new Locality(key);
	}
	
	public Vehicle addVehicle(Map<String, Vehicle> vehicles, String number) {
		if (number == null) {
			return null;
		}
		String key = StringUtil.md5(number);
		Vehicle vehicle = vehicles.get(key);
		if (vehicle == null) {
			vehicle = new Vehicle();
			vehicle.setNumber(number);
			vehicles.put(key, vehicle);
		}
		return new Vehicle(key);
	}
	
	private Price createPrice(Wagon wagon) {
		if (wagon.getCost() == null) {
			return null;
		}
		// тариф
		Tariff tariff = new Tariff();
		tariff.setId("0");
		tariff.setValue(wagon.getCost().getValue());
		
		// стоимость
		Price price = new Price();
		price.setCurrency(wagon.getCost().getCurrency());
		price.setAmount(wagon.getCost().getValue());
		price.setTariff(tariff);
		return price;
	}

	@Override
	public Route getRouteResponse(String tripId) {
		TripIdModel idModel = new TripIdModel().create(tripId);
		try {
			List<Country> route = client.getCachedRoute(idModel.getFrom(), idModel.getTo(), idModel.getDate(), idModel.getTrain());
			return createRoute(route, null);
		} catch (IOCacheException | ResponseError e) {
			throw new RestClientException(e.getMessage());
		}
	}
	
	private Route createRoute(List<Country> trainRoute, Map<String, Locality> localities) {
		Route route = new Route();
		List<RoutePoint> path = new ArrayList<>();
		for (Country country : trainRoute) {
			for (com.gillsoft.client.Locality station : country.getStations()) {
				RoutePoint point = new RoutePoint();
				point.setDepartureTime(station.getDepartureTime());
				point.setArrivalTime(station.getArrivalTime());
				point.setLocality(createLocality(localities, new Value(station.getCode(), station.getName())));
				path.add(point);
			}
		}
		route.setPath(path);
		return route;
	}

	@Override
	public SeatsScheme getSeatsSchemeResponse(String tripId) {
		Wagon wagon = getWagon(tripId);
		String code = wagon.getType().getCode();
		return schemaController.getScheme(getScheme(wagon), "С".equals(code) ? getCarCode(code) : null);
	}
	
	private String getScheme(Wagon wagon) {
		String code = wagon.getType().getCode();
		
		// сидячий вагон анализируем дополнительно
		return "С".equals(code) ?
				(wagon.getClas().getCode() + "_" + wagon.getSubtype()) :
					(getCarCode(code) + "_" + wagon.getSubtype());
	}
	
	private Wagon getWagon(String tripId) {
		try {
			TripIdModel idModel = new TripIdModel().create(tripId);
			Train train = client.getCachedPlaces(idModel.getFrom(), idModel.getTo(), idModel.getDate(), idModel.getTrain(),
					idModel.getType(), idModel.getClas(), idModel.getCar());
			return train.getWagons().get(0);
		} catch (ResponseError e) {
			throw new RestClientException(e.getMessage());
		}
	}
	
	private String getCarCode(String type) {
		switch (type) {
		case "Л":
			return "1";
		case "К":
			return "2";
		case "П":
			return "3";
		case "С":
			return "4";
		case "О":
			return "5";
		case "М":
			return "6";
		default:
			return "";
		}
	}
	
	@Override
	public List<Seat> getSeatsResponse(String tripId) {
		Wagon wagon = getWagon(tripId);
		String code = wagon.getType().getCode();
		Map<String, SeatType> types = schemaController.getCarriageSeats(getScheme(wagon), "С".equals(code) ? getCarCode(code) : null);
		
		List<Seat> newSeats = new ArrayList<>();
		
		List<String> seats = Arrays.asList(wagon.getSeats().split(",")).stream()
				.map(s -> s.replaceFirst("^0+", "")).collect(Collectors.toList());
		if (types != null) {
			for (Entry<String, SeatType> entry : types.entrySet()) {
				Seat newSeat = new Seat();
				newSeat.setType(entry.getValue());
				newSeat.setId(entry.getKey());
				newSeat.setNumber(entry.getKey());
				if (SeatType.SEAT == entry.getValue()) {
					if (seats.contains(entry.getKey())) {
						newSeat.setStatus(SeatStatus.FREE);
					} else {
						newSeat.setStatus(SeatStatus.SALED);
					}
				}
				newSeats.add(newSeat);
			}
		} else {
			for (String seat : seats) {
				Seat newSeat = new Seat();
				newSeat.setType(SeatType.SEAT);
				newSeat.setId(seat);
				newSeat.setNumber(seat);
				newSeat.setStatus(SeatStatus.FREE);
				newSeats.add(newSeat);
			}
		}
		return newSeats;
	}

	@Override
	public List<Tariff> getTariffsResponse(String tripId) {
		throw RestClient.createUnavailableMethod();
	}

	@Override
	public List<RequiredField> getRequiredFieldsResponse(String tripId) {
		List<RequiredField> required = new ArrayList<>(4);
		required.add(RequiredField.NAME);
		required.add(RequiredField.SURNAME);
		required.add(RequiredField.PHONE);
		required.add(RequiredField.EMAIL);
		return required;
	}

	@Override
	public List<Seat> updateSeatsResponse(String tripId, List<Seat> seats) {
		throw RestClient.createUnavailableMethod();
	}

	@Override
	public List<ReturnCondition> getConditionsResponse(String tripId, String tariffId) {
		throw RestClient.createUnavailableMethod();
	}

	@Override
	public List<Document> getDocumentsResponse(String tripId) {
		throw RestClient.createUnavailableMethod();
	}

}
