package edu.hust.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("JsonMapUtilImpl1")
public class JsonMapUtilImpl1 implements JsonMapUtil {

	@Override
	public boolean checkKeysExist(Map<String, Object> jsonMap, String... args) {
		List<String> listKeys = new ArrayList<>();
		for (String arg : args) {
			listKeys.add(arg);
		}

		if (!jsonMap.keySet().containsAll(listKeys)) {
			return false;
		}
		return true;
	}

}
