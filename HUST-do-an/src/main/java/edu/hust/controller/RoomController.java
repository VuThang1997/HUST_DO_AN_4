
package edu.hust.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.hust.model.Room;
import edu.hust.service.RoomService;
import edu.hust.utils.ValidationData;
import edu.hust.utils.ValidationRoomData;

@RestController
public class RoomController {

	private RoomService roomService;
	private ValidationData validationData;
	private ValidationRoomData validationRoomData;

	@Autowired
	public RoomController(@Qualifier("RoomServiceImpl1") RoomService roomService,
			@Qualifier("ValidationDataImpl1") ValidationData validationData, 
			@Qualifier("ValidationRoomDataImpl1") ValidationRoomData validationRoomData) {
		this.roomService = roomService;
		this.validationData = validationData;
		this.validationRoomData = validationRoomData;
	}

	@RequestMapping(value = "/rooms", method = RequestMethod.POST)
	public ResponseEntity<?> addNewRoom(@RequestBody String roomInfo) {
		String errorMessage = null;
		ObjectMapper objectMapper = null;
		Map<String, Object> jsonMap = null;
		Room room = null;
		
		try {
			objectMapper = new ObjectMapper();
			room = objectMapper.readValue(roomInfo, Room.class);
			jsonMap = new HashMap<>();
			
			jsonMap.put("RoomName", room.getRoomName());
			jsonMap.put("Address", room.getAddress());
			jsonMap.put("GpsLa", room.getGpsLatitude());
			jsonMap.put("GpsLong", room.getGpsLongitude());
			jsonMap.put("MacAddress", room.getMacAddress());
			
			errorMessage = this.validationData.validateRoomData(jsonMap);
			if (errorMessage != null) {
				return ResponseEntity.badRequest().body("Error code: 50; Content: Adding room failed because " + errorMessage);
			}
			
			if (!this.roomService.checkRoomNameDuplicate(room.getRoomName())) {
				return ResponseEntity.badRequest().body("Error code: 51; Content: Duplicate room name!");
			}
			
			if (!this.roomService.checkMacAddrDuplicate(room.getMacAddress())) {
				return ResponseEntity.badRequest().body("Error code: 52; Content: Duplicate MAC address!");
			}
			
			this.roomService.addNewRoom(room);
			return ResponseEntity.ok("Adding new room successes!");
			
		} catch (IOException e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Error code: 02; Content: Error happened when jackson deserialization info !");
		}
	}

	@RequestMapping(value = "/rooms", method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteRoom(@RequestParam(value = "roomID", required = true) int id) {
		String errorMessage = this.validationRoomData.validateIdData(id);
		if (errorMessage != null) {
			return ResponseEntity.badRequest().body("Error code: 54; Content: Deleting room failed because " + errorMessage);
		}
		
		Room room = this.roomService.findRoomById(id);
		if (room == null) {
			return new ResponseEntity<>("Error code: 53; Content: This room do not exist!", HttpStatus.NOT_FOUND);
		}
		
		if (this.roomService.deleteRoom(id)) {
			return ResponseEntity.ok("Deleting room successes!");
		} 
		return ResponseEntity.badRequest().body("Error code: 55; Content: This room still has dependant!");
	}

	@RequestMapping(value = "/rooms", method = RequestMethod.GET)
	public ResponseEntity<?> readInfoRoom(@RequestParam(value = "roomID", required = true) int id) {
		String errorMessage = this.validationRoomData.validateIdData(id);
		if (errorMessage != null) {
			return ResponseEntity.badRequest().body("Error code: 57; Content: Getting room info failed because " + errorMessage);
		}
		
		Room room = this.roomService.findRoomById(id);
		if (room == null) {
			return new ResponseEntity<>("Error code: 53; Content: This room do not exist!", HttpStatus.NOT_FOUND);
		}

		return ResponseEntity.ok(room);
	}

	@RequestMapping(value = "/rooms", method = RequestMethod.PUT)
	public ResponseEntity<?> updateInfoRoom(@RequestBody String infoRoom) {
		String errorMessage = null;
		ObjectMapper objectMapper = null;
		Map<String, Object> jsonMap = null;
		Room room = null;
		
		try {
			objectMapper = new ObjectMapper();
			room = objectMapper.readValue(infoRoom, Room.class);
			jsonMap = new HashMap<>();
			
			jsonMap.put("ID", room.getId());
			jsonMap.put("RoomName", room.getRoomName());
			jsonMap.put("Address", room.getAddress());
			jsonMap.put("GpsLa", room.getGpsLatitude());
			jsonMap.put("GpsLong", room.getGpsLongitude());
			jsonMap.put("MacAddress", room.getMacAddress());
			
			errorMessage = this.validationData.validateRoomData(jsonMap);
			if (errorMessage != null) {
				return ResponseEntity.badRequest().body("Error code: 56; Content: Updating room failed because " + errorMessage);
			}
			
			if (this.roomService.findRoomById(room.getId()) == null) {
				return new ResponseEntity<>("Error code: 53; Content: This room do not exist!", HttpStatus.NOT_FOUND);
			}
			
			if (!this.roomService.checkRoomNameDuplicate(room.getRoomName())) {
				return ResponseEntity.badRequest().body("Error code: 51; Content: Duplicate room name!");
			}
			
			if (!this.roomService.checkMacAddrDuplicate(room.getMacAddress())) {
				return ResponseEntity.badRequest().body("Error code: 52; Content: Duplicate MAC address!");
			}
			
			this.roomService.updateRoom(room);
			return ResponseEntity.ok("Updating room info successes!");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Error code: 02; Content: Error happened when jackson deserialization info !");
		}
	}
}
