
package edu.hust.service;

import edu.hust.model.Room;

public interface RoomService {

	boolean deleteRoom(int roomID);

	boolean updateRoom(Room room);

	boolean addNewRoom(Room room);

	boolean checkMacAddress(int roomID, String macAddr);

	double calculateDistanceBetween2GPSCoord(int roomID, double gpsLong, double gpsLa);

	/**
	 * @param roomName
	 * @return false if another room has this name; true if none exists
	 */
	boolean checkRoomNameDuplicate(String roomName);

	/**
	 * @param macAddress
	 * @return false if another room has this MAC address; true if none exists
	 */
	boolean checkMacAddrDuplicate(String macAddress);

	Room findRoomById(int id);

}
