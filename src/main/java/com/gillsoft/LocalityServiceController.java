package com.gillsoft;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;

import com.gillsoft.abstract_rest_service.AbstractLocalityService;
import com.gillsoft.cache.IOCacheException;
import com.gillsoft.client.Country;
import com.gillsoft.client.RestClient;
import com.gillsoft.model.Lang;
import com.gillsoft.model.Locality;
import com.gillsoft.model.request.LocalityRequest;

@RestController
public class LocalityServiceController extends AbstractLocalityService {
	
	public static List<Locality> all;
	
	@Autowired
	private RestClient client;

	@Override
	public List<Locality> getAllResponse(LocalityRequest request) {
		createLocalities();
		return all;
	}

	@Override
	public List<Locality> getUsedResponse(LocalityRequest request) {
		createLocalities();
		return all;
	}

	@Override
	public Map<String, List<String>> getBindingResponse(LocalityRequest request) {
		throw RestClient.createUnavailableMethod();
	}
	
	@Scheduled(initialDelay = 60000, fixedDelay = 900000)
	public void createLocalities() {
		if (LocalityServiceController.all == null) {
			synchronized (LocalityServiceController.class) {
				if (LocalityServiceController.all == null) {
					boolean cacheError = true;
					do {
						try {
							List<Country> countries = client.getCachedStations();
							List<Locality> all = new CopyOnWriteArrayList<>();
							for (Country country : countries) {
								Map<String, String> railways = new HashMap<>();
								if (country.getRailways() != null) {
									for (com.gillsoft.client.Locality railway : country.getRailways()) {
										railways.put(railway.getCode(), railway.getNameLt());
									}
								}
								Locality parent = createLocality(country);
								all.add(parent);
								if (country.getStations() != null) {
									for (com.gillsoft.client.Locality station : country.getStations()) {
										Locality locality = createLocality(station);
										locality.setDetails(railways.get(station.getRailway()));
										locality.setParent(new Locality(parent.getId()));
										all.add(locality);
									}
								}
							}
							LocalityServiceController.all = all;
							cacheError = false;
						} catch (IOCacheException e) {
							try {
								TimeUnit.MILLISECONDS.sleep(100);
							} catch (InterruptedException ie) {
							}
						}
					} while (cacheError);
				}
			}
		}
	}
	
	private Locality createLocality(com.gillsoft.client.Locality resLocality) {
		Locality locality = new Locality();
		locality.setId(resLocality.getCode());
		locality.setName(Lang.RU, resLocality.getNameRu());
		locality.setName(Lang.UA, resLocality.getNameUk());
		locality.setName(Lang.EN, resLocality.getNameLt());
		return locality;
	}

}
