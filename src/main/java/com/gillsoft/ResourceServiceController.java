package com.gillsoft;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.RestController;

import com.gillsoft.abstract_rest_service.AbstractResourceService;
import com.gillsoft.model.Method;
import com.gillsoft.model.MethodType;
import com.gillsoft.model.Ping;
import com.gillsoft.model.Resource;

@RestController
public class ResourceServiceController extends AbstractResourceService {

	@Override
	public List<Method> getAvailableMethodsResponse() {
		List<Method> methods = new ArrayList<>();
		
		// resource
		addMethod(methods, "Resource activity check", Method.PING, MethodType.GET);
		addMethod(methods, "Information about resource", Method.INFO, MethodType.GET);
		addMethod(methods, "Available methods", Method.METHOD, MethodType.GET);
		
		// localities
		addMethod(methods, "All available resource localities", Method.LOCALITY_ALL, MethodType.POST);
		addMethod(methods, "All used resource localities", Method.LOCALITY_USED, MethodType.POST);
		
		// search
		addMethod(methods, "Init search", Method.SEARCH, MethodType.POST);
		addMethod(methods, "Return search result", Method.SEARCH, MethodType.GET);
		addMethod(methods, "Return trip route", Method.SEARCH_TRIP_ROUTE, MethodType.GET);
		addMethod(methods, "Return free seats on trip", Method.SEARCH_TRIP_SEATS, MethodType.GET);
		addMethod(methods, "Return the scheme of seats on trip", Method.SEARCH_TRIP_SEATS_SCHEME, MethodType.GET);
		addMethod(methods, "Return required fields to create order", Method.SEARCH_TRIP_REQUIRED, MethodType.GET);

		// order
		addMethod(methods, "Create new order", Method.ORDER, MethodType.POST);
		addMethod(methods, "Confirm order", Method.ORDER_CONFIRM, MethodType.POST);
		addMethod(methods, "Get order information", Method.ORDER, MethodType.GET);
		addMethod(methods, "Get service information", Method.ORDER_SERVICE, MethodType.GET);
		addMethod(methods, "Cancel order", Method.ORDER_CANCEL, MethodType.POST);
		return methods;
	}

	@Override
	public Resource getInfoResponse() {
		Resource resource = new Resource();
		resource.setCode("TICKETS-RAILWAY");
		resource.setName("API Tickets Travel Network");
		return resource;
	}

	@Override
	public Ping pingResponse(String id) {
		return createPing(id);
	}

}
