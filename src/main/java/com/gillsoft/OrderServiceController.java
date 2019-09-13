package com.gillsoft;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.gillsoft.abstract_rest_service.AbstractOrderService;
import com.gillsoft.client.Company;
import com.gillsoft.client.Cost;
import com.gillsoft.client.Order;
import com.gillsoft.client.OrderDocument;
import com.gillsoft.client.OrderIdModel;
import com.gillsoft.client.OrderResult;
import com.gillsoft.client.OrderTrain;
import com.gillsoft.client.RestClient;
import com.gillsoft.client.Ticket;
import com.gillsoft.client.Train;
import com.gillsoft.client.TripIdModel;
import com.gillsoft.client.Wagon;
import com.gillsoft.model.CalcType;
import com.gillsoft.model.Carriage;
import com.gillsoft.model.Commission;
import com.gillsoft.model.Currency;
import com.gillsoft.model.Customer;
import com.gillsoft.model.Lang;
import com.gillsoft.model.Locality;
import com.gillsoft.model.Organisation;
import com.gillsoft.model.Price;
import com.gillsoft.model.ResponseError;
import com.gillsoft.model.RestError;
import com.gillsoft.model.Seat;
import com.gillsoft.model.Segment;
import com.gillsoft.model.ServiceItem;
import com.gillsoft.model.Tariff;
import com.gillsoft.model.ValueType;
import com.gillsoft.model.Vehicle;
import com.gillsoft.model.request.OrderRequest;
import com.gillsoft.model.response.OrderResponse;
import com.gillsoft.util.StringUtil;

@RestController
public class OrderServiceController extends AbstractOrderService {
	
	@Autowired
	private RestClient client;
	
	@Autowired
	private SearchServiceController search;

	@Override
	public OrderResponse createResponse(OrderRequest request) {
		
		// формируем ответ
		OrderResponse response = new OrderResponse();
		response.setCustomers(request.getCustomers());
		
		// копия для определения пассажиров
		List<ServiceItem> items = new ArrayList<>();
		items.addAll(request.getServices());
		
		Map<String, Organisation> organisations = new HashMap<>();
		Map<String, Locality> localities = new HashMap<>();
		Map<String, Vehicle> vehicles = new HashMap<>();
		Map<String, Segment> segments = new HashMap<>();
		List<ServiceItem> resultItems = new ArrayList<>();
		
		// список билетов
		OrderIdModel orderId = new OrderIdModel();
		orderId.setOrders(new HashMap<>());
		for (Entry<String, List<ServiceItem>> order : getTripItems(request).entrySet()) {
			String[] params = order.getKey().split(";");
			TripIdModel idModel = new TripIdModel().create(params[0]);
			try {
				// создаем заказ
				OrderResult result = reservation(idModel, order.getValue(), request.getCustomers());
				
				// получаем поезд и вагон
				Train train = result.createTrain();
				Wagon wagon = result.createWagon();
				Carriage carriage = search.createCarriage(wagon);
				carriage.setNumber(wagon.getNumber() + " " + wagon.getType().getCode()
						+ (wagon.getClas().getCode() != null ? (" - " + wagon.getClas().getCode()) : ""));
				carriage.setId(idModel.asString());
				
				String segmentId = search.addSegment(vehicles, localities, organisations, segments, train, Collections.singletonList(wagon));
				Segment segment = new Segment(segmentId);
				
				// создаем ид заказов
				List<String> serviceIds = new ArrayList<>(order.getValue().size());
				
				// устанавливаем данные в сервисы
				for (ServiceItem item : order.getValue()) {
					Customer customer = request.getCustomers().get(item.getCustomer().getId());
					try {
						OrderDocument document = getDocument(result.getDocuments(), customer);
						addInsurance(organisations, segments.get(segmentId), document.getInsurance());
						serviceIds.add(document.getUid());
						
						item.setId(document.getUid());
						item.setAdditionals(new HashMap<>(1));
						item.getAdditionals().put("uid", document.getUid());
						item.setNumber(result.isElectronic() ? document.getOrdernumber() : result.getOrdernumber());
						item.setExpire(getExpiredDate(result));
						
						// рейс
						item.setSegment(segment);
						
						// вагон
						item.setCarriage(carriage);
						
						// стоимость
						item.setPrice(createPrice(document));
						
						// устанавливаем место
						item.setSeat(createSeat(document));
						resultItems.add(item);
					} catch (ResponseError e) {
						item.setError(new RestError(e.getMessage()));
						resultItems.add(item);
					}
				}
				orderId.getOrders().put(result.getId(), serviceIds);
			} catch (ResponseError e) {
				for (ServiceItem item : order.getValue()) {
					item.setError(new RestError(e.getMessage()));
					resultItems.add(item);
				}
			}
		}
		response.setOrderId(orderId.asString());
		response.setLocalities(localities);
		response.setVehicles(vehicles);
		response.setOrganisations(organisations);
		response.setSegments(segments);
		response.setServices(resultItems);
		return response;
	}
	
	private void addInsurance(Map<String, Organisation> organisations, Segment segment, Company insurance) {
		if (insurance != null) {
			Organisation organisation = new Organisation();
			organisation.setName(Lang.UA, insurance.getName());
			organisation.setAddress(Lang.UA, insurance.getAddress());
			organisation.setPhones(Collections.singletonList(insurance.getTelephone()));
			organisations.putIfAbsent(StringUtil.md5(insurance.getName()), organisation);
			segment.setInsurance(new Organisation(StringUtil.md5(insurance.getName())));
		}
	}
	
	private Price createPrice(OrderDocument document) {
		Map<String, Cost> costs = document.getCosts();
		Price price = new Price();
		BigDecimal total = document.getRsb() != null && document.getRsb().getCommission() != null ?
				document.getRsb().getCommission().getTotal() : BigDecimal.ZERO;
		BigDecimal totalVat = total.divide(new BigDecimal("6"), 2, RoundingMode.HALF_UP);
		for (Cost cost : costs.values()) {
			total = total.add(cost.getCost());
			totalVat = totalVat.add(cost.getVat());
		}
		price.setAmount(total);
		price.setVat(totalVat);
		price.setCurrency(Currency.UAH);
		
		// тариф расчитываем исходя из примера
		// "insurance": "0.74 ", "carrier": "759.86 ", "commission_vat": "3.67 ", "carrier_vat": "122.86 ", "commission": "18.33 "
		// I-759.86ГРН=ТАР.614.26+КЗБ.18.33+ПДВ.126.53+СТР.0.74
		Cost main = costs.get("0");
		Tariff tariff = new Tariff();
		tariff.setValue(main.getCarrier().subtract(main.getCommission())
				.subtract(main.getCommissionVat()).subtract(main.getInsurance()));
		tariff.setVat(main.getCarrierVat());
		tariff.setId(document.getDetail().getKind());
		price.setTariff(tariff);
		
		price.setCommissions(new ArrayList<>());
		
		// страховой сбор
		Commission insurance = new Commission();
		insurance.setCode("СТР");
		insurance.setName(Lang.UA, "Страховий збір");
		insurance.setValue(main.getInsurance());
		insurance.setValueCalcType(CalcType.OUT);
		insurance.setType(ValueType.FIXED);
		price.getCommissions().add(insurance);
		
		// кассовый сбор
		Commission ksb = new Commission();
		ksb.setCode("КЗБ");
		ksb.setName(Lang.UA, "Касовий збір");
		ksb.setValue(main.getCommission().add(main.getCommissionVat()));
		ksb.setVat(main.getCommissionVat());
		ksb.setVatCalcType(CalcType.IN);
		ksb.setValueCalcType(CalcType.OUT);
		ksb.setType(ValueType.FIXED);
		price.getCommissions().add(ksb);
		
		if (main.getFee() != null) {
			
			// дополнительный сбор
			Commission sb = new Commission();
			sb.setCode("ЗБ");
			sb.setName(Lang.UA, "Збір");
			sb.setValue(main.getFee().add(main.getFeeVat()));
			sb.setVat(main.getFeeVat());
			sb.setVatCalcType(CalcType.IN);
			sb.setValueCalcType(CalcType.OUT);
			sb.setType(ValueType.FIXED);
			price.getCommissions().add(sb);
		}
		// сервисный сбор
		if (costs.containsKey("1")) {
			Cost serv = costs.get("1");
			Commission service = new Commission();
			service.setCode("СКА");
			service.setName(Lang.UA, "Cплата користувача агенту");
			service.setValue(serv.getCost());
			service.setVat(serv.getVat());
			service.setVatCalcType(CalcType.IN);
			service.setValueCalcType(CalcType.OUT);
			service.setType(ValueType.FIXED);
			price.getCommissions().add(service);
		}
		// сервисный сбор
		if (costs.containsKey("2")) {
			Cost military = costs.get("2");
			Commission service = new Commission();
			service.setCode("ВМ");
			service.setName(Lang.UA, "Військова вимога");
			service.setValue(military.getCost());
			service.setVat(military.getVat());
			service.setVatCalcType(CalcType.IN);
			service.setValueCalcType(CalcType.OUT);
			service.setType(ValueType.FIXED);
			price.getCommissions().add(service);
		}
		// другие сборы посредника
		if (document.getRsb() != null && document.getRsb().getCommission() != null) {
			Commission agent = new Commission();
			agent.setCode("АГН");
			agent.setName(Lang.UA, "Agent");
			agent.setValue(document.getRsb().getCommission().getAgent());
			agent.setVat(document.getRsb().getCommission().getAgent()
					.divide(new BigDecimal("6"), 2, RoundingMode.HALF_UP));
			agent.setVatCalcType(CalcType.IN);
			agent.setValueCalcType(CalcType.OUT);
			agent.setType(ValueType.FIXED);
			price.getCommissions().add(agent);
			
			Commission provider = new Commission();
			provider.setCode("ПР");
			provider.setName(Lang.UA, "Provider");
			provider.setValue(document.getRsb().getCommission().getProvider());
			provider.setVat(document.getRsb().getCommission().getProvider()
					.divide(new BigDecimal("6"), 2, RoundingMode.HALF_UP));
			provider.setVatCalcType(CalcType.IN);
			provider.setValueCalcType(CalcType.OUT);
			provider.setType(ValueType.FIXED);
			price.getCommissions().add(provider);
		}
		return price;
	}
	
	private Date getExpiredDate(OrderResult result) {
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		try {
			Date time = timeFormat.parse(result.getWaitTime());
			Calendar cTime = Calendar.getInstance();
			cTime.setTime(time);
			
			Calendar sys = Calendar.getInstance();
			sys.setTime(result.getSysdate());
			
			sys.add(Calendar.HOUR, cTime.get(Calendar.HOUR));
			sys.add(Calendar.MINUTE, cTime.get(Calendar.MINUTE));
			sys.add(Calendar.SECOND, cTime.get(Calendar.SECOND));
			
			return sys.getTime();
		} catch (ParseException e) {
		}
		return null;
	}
	
	private OrderDocument getDocument(List<OrderDocument> documents, Customer customer) throws ResponseError {
		if (documents.stream().anyMatch(d ->
				Objects.equals(customer.getName(), d.getPassport().getFirstname())
						&& Objects.equals(customer.getSurname(), d.getPassport().getLastname()))) {
			return documents.stream().filter(d ->
					Objects.equals(customer.getName(), d.getPassport().getFirstname())
							&& Objects.equals(customer.getSurname(), d.getPassport().getLastname())).findAny().get();
		}
		throw new ResponseError("Can not find in response customer " + customer.getSurname() + " " + customer.getName());
	}
	
	private Seat createSeat(OrderDocument document) throws ResponseError {
		Seat seat = new Seat();
		seat.setId(document.getDetail().getPlaces().getValue());
		seat.setNumber(document.getDetail().getPlaces().getValue());
		return seat;
	}
	
	private OrderResult reservation(TripIdModel idModel, List<ServiceItem> services, Map<String, Customer> customersMap) throws ResponseError {
		List<Customer> customers = services.stream()
				.map(s -> customersMap.get(s.getCustomer().getId())).collect(Collectors.toList());
		String places = services.stream().map(s -> s.getSeat().getId()).collect(Collectors.joining(","));
		return client.reservation(idModel, places, customers);
	}
	
	/*
	 * В заказе ресурса можно оформить максимум 4 пассажира в одном заказе.
	 */
	private Map<String, List<ServiceItem>> getTripItems(OrderRequest request) {
		Map<String, List<ServiceItem>> trips = new HashMap<>();
		for (ServiceItem item : request.getServices()) {
			
			// добавляем номер вагона в ид рейса
			String tripId = item.getCarriage().getId();
			List<ServiceItem> items = trips.get(tripId);
			if (items == null) {
				items = new ArrayList<>();
				trips.put(tripId, items);
			}
			if (items.size() == 4) {
				trips.put(String.join(";", tripId, StringUtil.generateUUID()), trips.get(tripId));
				items = new ArrayList<>();
				trips.put(tripId, items);
			}
			items.add(item);
		}
		return trips;
	}

	@Override
	public OrderResponse addServicesResponse(OrderRequest request) {
		throw RestClient.createUnavailableMethod();
	}

	@Override
	public OrderResponse removeServicesResponse(OrderRequest request) {
		throw RestClient.createUnavailableMethod();
	}

	@Override
	public OrderResponse updateCustomersResponse(OrderRequest request) {
		throw RestClient.createUnavailableMethod();
	}

	@Override
	public OrderResponse getResponse(String orderId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OrderResponse getServiceResponse(String serviceId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OrderResponse bookingResponse(String orderId) {
		throw RestClient.createUnavailableMethod();
	}

	@Override
	public OrderResponse confirmResponse(String orderId) {
		return confirmOperation(orderId, (id, document) -> {
			if (RestClient.RESERVETION_STATUS.contains(document.getStatus())) {
				List<Order> orders = client.pay(id);
				List<ServiceItem> items = new ArrayList<>();
				BigDecimal total = BigDecimal.ZERO;
				for (Order order : orders) {
					List<Ticket> tickets = null;
					if (order.isElectronic()) {
						tickets = order.getDocuments();
					} else {
						tickets = order.getTicket();
					}
					for (Ticket ticket : tickets) {
						ServiceItem item = new ServiceItem();
						item.setConfirmed(true);
						item.setId(ticket.getDocument().getUid());
						item.setAdditionals(new HashMap<>());
						Cost main = ticket.getDocument().getCosts().get("0");
						total = total.add(main.getCarrier());
						item.getAdditionals().put("cost", String.format("%.2f", main.getCarrier()));
						item.getAdditionals().put("cost_desc", String.format("ТАР(%.2f) + КЗБ(%.2f) + ПДВ(%.2f) + СТР(%.2f)",
								main.getCarrier().subtract(main.getCommission()).subtract(main.getCommissionVat())
								.subtract(main.getInsurance()).subtract(main.getCarrierVat()),
						main.getCommission(), main.getCarrierVat().add(main.getCommissionVat()), main.getInsurance()));
						item.getAdditionals().put("terminal", ticket.getDocument().getTransaction().getTerminal());
						item.getAdditionals().put("text", ticket.getDocument().getText());
						item.getAdditionals().put("train", getTrainNumber(ticket.getDocument().getDetail().getTrain()));
						item.getAdditionals().put("from_code", ticket.getDocument().getDetail().getStationFrom().getCode());
						item.getAdditionals().put("from_name", ticket.getDocument().getDetail().getStationFrom().getValue());
						item.getAdditionals().put("to_code", ticket.getDocument().getDetail().getStationTo().getCode());
						item.getAdditionals().put("to_name", ticket.getDocument().getDetail().getStationTo().getValue());
						item.getAdditionals().put("barcode", order.getBarcodeImage());
						item.getAdditionals().put("electronic", String.valueOf(order.isElectronic()));
						if (order.isElectronic()) {
							item.getAdditionals().put("qr", ticket.getQrImage());
							item.getAdditionals().put("fiscal_rro", ticket.getFiscalInfo().getRro());
							item.getAdditionals().put("fiscal_server", ticket.getFiscalInfo().getServer());
							item.getAdditionals().put("fiscal_id", ticket.getFiscalInfo().getId());
							item.getAdditionals().put("fiscal_tin", ticket.getFiscalInfo().getTin());
						}
						items.add(item);
					}
				}
				for (ServiceItem item : items) {
					item.getAdditionals().put("total", String.format("%.2f", total));
				}
				return items;
			} else if (!RestClient.PAIED_STATUS.contains(document.getStatus())) {
				throw new ResponseError("Can not pay service. Pay applied only to reserved services.");
			}
			return null;
		});
	}
	
	private String getTrainNumber(OrderTrain train) {
		String number = train.getValue();
		switch (train.getClas()) {
		case "4":
			number += " ФІРМ";
			break;
		case "8":
			number += " ЕПК";
			break;
		default:
			break;
		}
		switch (train.getCategory()) {
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

	@Override
	public OrderResponse cancelResponse(String orderId) {
		return confirmOperation(orderId, (id, document) -> {
			if (RestClient.RESERVETION_STATUS.contains(document.getStatus())) {
				client.revocation(id);
			} else if (RestClient.PAIED_STATUS.contains(document.getStatus())) {
				client.cancel(id);
			} else if (!RestClient.CANCEL_STATUS.contains(document.getStatus())) {
				throw new ResponseError("Can not cancel service. Cancel applied only to reserved or paied services.");
			}
			return null;
		});
	}
	
	private OrderResponse confirmOperation(String orderId, ConfirmOperation operation) {
		
		// формируем ответ
		OrderResponse response = new OrderResponse();
		List<ServiceItem> resultItems = new ArrayList<>();
		response.setOrderId(orderId);
		response.setServices(resultItems);
		
		// преобразовываем ид заказа в объкт
		OrderIdModel model = new OrderIdModel().create(orderId);
		
		// отменяем заказы и формируем ответ
		for (Entry<String, List<String>> entry : model.getOrders().entrySet()) {
			try {
				// получаем заказ и проверяем статус (достаточно проверить одну позицию заказа - другие будут в том же статусе)
				OrderDocument document = client.getDocStatus(entry.getValue().get(0));
					
				// выполняем подтверждение
				List<ServiceItem> confirmedItems = operation.confirm(entry.getKey(), document);
				if (confirmedItems != null) {
					resultItems.addAll(confirmedItems);
				} else {
					for (String id : entry.getValue()) {
						addServiceItem(resultItems, id, true, null);
					}
				}
			} catch (ResponseError e) {
				for (String id : entry.getValue()) {
					addServiceItem(resultItems, id, false, new RestError(e.getMessage()));
				}
			}
		}
		return response;
	}
	
	private void addServiceItem(List<ServiceItem> resultItems, String ticket, boolean confirmed,
			RestError error) {
		ServiceItem serviceItem = new ServiceItem();
		serviceItem.setId(ticket);
		serviceItem.setConfirmed(confirmed);
		serviceItem.setError(error);
		resultItems.add(serviceItem);
	}

	@Override
	public OrderResponse prepareReturnServicesResponse(OrderRequest request) {
		throw RestClient.createUnavailableMethod();
	}

	@Override
	public OrderResponse returnServicesResponse(OrderRequest request) {
		throw RestClient.createUnavailableMethod();
	}

	@Override
	public OrderResponse getPdfDocumentsResponse(OrderRequest request) {
		throw RestClient.createUnavailableMethod();
	}
	
	private interface ConfirmOperation {
		
		public List<ServiceItem> confirm(String id, OrderDocument document) throws ResponseError;
		
	}

}
