package com.gillsoft;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.gillsoft.model.SeatType;
import com.gillsoft.model.SeatsScheme;

@Service
public class SeatsSchemeController {
	
	private static Logger LOGGER = LogManager.getLogger(SeatsSchemeController.class);
	
	private Map<String, CarriageScheme> schemes = new ConcurrentHashMap<>();
	
	private ObjectReader reader = new ObjectMapper().readerFor(CarriageScheme.class);
	
	public SeatsScheme getScheme(String scheme, String type) {
		CarriageScheme carriageScheme = getScheme(getSchemeId(scheme, type));
		if (carriageScheme == null) {
			return null;
		} else {
			SeatsScheme seatsScheme = new SeatsScheme();
			seatsScheme.setScheme(carriageScheme.getScheme());
			return seatsScheme;
		}
	}
	
	private String getSchemeId(String scheme, String type) {
		return type == null ? scheme : type + "_" + scheme;
	}
	
	private CarriageScheme getScheme(String schemeId) {
		if (!schemes.containsKey(schemeId)) {
			return loadScheme(schemeId);
		} else {
			return schemes.get(schemeId);
		}
	}
	
	private CarriageScheme loadScheme(String schemeId) {
		synchronized (schemeId.intern()) {
			try {
				Resource resource = new ClassPathResource("scheme/" + schemeId + ".json");
				CarriageScheme carriageScheme = reader.readValue(resource.getFile());
				schemes.put(schemeId, carriageScheme);
				return carriageScheme;
			} catch (IOException e) {
				LOGGER.error("Error when get carriage scheme", e);
				return null;
			}
		}
	}
	
	public Map<String, SeatType> getCarriageSeats(String scheme, String type) {
		CarriageScheme carriageScheme = getScheme(getSchemeId(scheme, type));
		if (carriageScheme == null) {
			return null;
		} else {
			return carriageScheme.getSeats();
		}
	}

}
