
package edu.hust.service;

import edu.hust.model.Room;

public interface RoomService {

	boolean deleteRoom(int roomID);

	Room getInfo(int id);

	boolean updateRoom(Room room);

	boolean addNewRoom(Room room);

	boolean checkMacAddress(int roomID, String macAddr);

	double calculateDistanceBetween2GPSCoord(int roomID, double gpsLong, double gpsLa);

}
