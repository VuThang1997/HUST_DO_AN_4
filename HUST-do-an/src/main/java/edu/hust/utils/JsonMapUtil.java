package edu.hust.utils;

import java.util.Map;

public interface JsonMapUtil {

	/**
	 * @param jsonMap - a Map contains some keys
	 * @param args - list of keys that need check
	 * @return true if the map contains all keys; 
	 */
	boolean checkKeysExist(Map<String, Object> jsonMap, String... args);
}
