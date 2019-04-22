
package edu.hust.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import edu.hust.model.Room;
import edu.hust.repository.RoomRepository;
import edu.hust.utils.GeneralValue;

@Service

@Qualifier("RoomServiceImpl1")
public class RoomServiceImpl1 implements RoomService {

	private RoomRepository roomRepository;

	public RoomServiceImpl1() {
		super();
	}

	@Autowired
	public RoomServiceImpl1(RoomRepository roomRepository) {
		super();
		this.roomRepository = roomRepository;
	}

	@Override
	public boolean deleteRoom(int roomID) {
		if (this.roomRepository.existsById(roomID)) {
			this.roomRepository.deleteById(roomID);
			return true;
		}
		return false;
	}

	@Override
	public Room getInfo(int id) {
		Optional<Room> room = this.roomRepository.findById(id);
		if (room.isPresent()) {
			return room.get();
		}
		return null;
	}

	@Override
	public boolean updateRoom(Room room) {
		try {
			Room infoRoom = this.roomRepository.findById(room.getId()).get();
			if (room.getAddress() != null) {
				infoRoom.setAddress(room.getAddress());
			}
			if (room.getGpsLongitude() != 0) {
				infoRoom.setGpsLongitude(room.getGpsLongitude());
			}
			if (room.getGpsLatitude() != 0) {
				infoRoom.setGpsLatitude(room.getGpsLatitude());
			}
			this.roomRepository.save(infoRoom);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	@Override
	public boolean addNewRoom(Room room) {
		if (this.roomRepository.findByAddress(room.getAddress())) {
			return false;
		}
		this.roomRepository.save(room);
		return true;
	}

	@Override
	public boolean checkMacAddress(int roomID, String macAddr) {
		Optional<Room> room = this.roomRepository.findByIdAndMacAddress(roomID, macAddr);
		return room.isPresent() ? true : false;
	}

	@Override
	public double calculateDistanceBetween2GPSCoord(int roomID, double gpsLong, double gpsLa) {
		Optional<Room> room = this.roomRepository.findById(roomID);
		if (room.isEmpty()) {
			return Integer.MAX_VALUE;
		}

		Room instance = room.get();
		double roomGpsLa = instance.getGpsLatitude();
		double roomGpsLong = instance.getGpsLongitude();
		double dlong = (gpsLong - roomGpsLong) * GeneralValue.degreeToRadiant;
		double dla = (gpsLa - roomGpsLa) * GeneralValue.degreeToRadiant;

		double a = Math.pow(Math.sin(dla / 2D), 2D) + Math.cos(roomGpsLa * GeneralValue.degreeToRadiant)
				* Math.cos(gpsLa * GeneralValue.degreeToRadiant) * Math.pow(Math.sin(dlong / 2D), 2D);
		double c = 2D * Math.atan2(Math.sqrt(a), Math.sqrt(1D - a));
		double d = GeneralValue.eQuatorialEarthRadius * c * 1000;

		return d;
	}

}
