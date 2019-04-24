
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

import edu.hust.model.ReportError;
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
		ReportError report = null;
		
		try {
			objectMapper = new ObjectMapper();
			room = objectMapper.readValue(roomInfo, Room.class);
			jsonMap = new HashMap<>();
			
			jsonMap.put("roomName", room.getRoomName());
			jsonMap.put("address", room.getAddress());
			jsonMap.put("gpsLa", room.getGpsLatitude());
			jsonMap.put("gpsLong", room.getGpsLongitude());
			jsonMap.put("macAddress", room.getMacAddress());
			
			errorMessage = this.validationData.validateRoomData(jsonMap);
			if (errorMessage != null) {
				report = new ReportError(50, "Adding room failed because " + errorMessage);
				return ResponseEntity.badRequest().body(report);
			}
			
			if (!this.roomService.checkRoomNameDuplicate(room.getRoomName())) {
				report = new ReportError(51, "Duplicate room name!");
				return ResponseEntity.badRequest().body(report);
			}
			
			if (!this.roomService.checkMacAddrDuplicate(room.getMacAddress())) {
				report = new ReportError(52, "Duplicate MAC address!");
				return ResponseEntity.badRequest().body(report);
			}
			
			this.roomService.addNewRoom(room);
			return ResponseEntity.ok("Adding new room successes!");
			
		} catch (IOException e) {
			e.printStackTrace();
			report = new ReportError(2, "Error happened when jackson deserialization info!");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, report.toString());
		}
	}

	@RequestMapping(value = "/rooms", method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteRoom(@RequestParam(value = "roomID", required = true) int id) {
		String errorMessage = this.validationRoomData.validateIdData(id);
		ReportError report = null;
		if (errorMessage != null) {
			report = new ReportError(54, "Deleting room failed because " + errorMessage);
			return ResponseEntity.badRequest().body(report);
		}
		
		Room room = this.roomService.findRoomById(id);
		if (room == null) {
			report = new ReportError(53, "This room do not exist!");
			return new ResponseEntity<>(report, HttpStatus.NOT_FOUND);
		}
		
		if (this.roomService.deleteRoom(id)) {
			return ResponseEntity.ok("Deleting room successes!");
		}
		
		report = new ReportError(55, "This room still has dependant!");
		return ResponseEntity.badRequest().body(report);
	}

	@RequestMapping(value = "/rooms", method = RequestMethod.GET)
	public ResponseEntity<?> readInfoRoom(@RequestParam(value = "roomID", required = true) int id) {
		String errorMessage = this.validationRoomData.validateIdData(id);
		ReportError report = null;
		if (errorMessage != null) {
			report = new ReportError(57, "Getting room failed because " + errorMessage);
			return ResponseEntity.badRequest().body(report);
		}
		
		Room room = this.roomService.findRoomById(id);
		if (room == null) {
			report = new ReportError(53, "This room do not exist!");
			return new ResponseEntity<>(report, HttpStatus.NOT_FOUND);
		}

		return ResponseEntity.ok(room);
	}

	@RequestMapping(value = "/rooms", method = RequestMethod.PUT)
	public ResponseEntity<?> updateInfoRoom(@RequestBody String infoRoom) {
		String errorMessage = null;
		ObjectMapper objectMapper = null;
		Map<String, Object> jsonMap = null;
		Room room = null;
		ReportError report = null;
		
		try {
			objectMapper = new ObjectMapper();
			room = objectMapper.readValue(infoRoom, Room.class);
			jsonMap = new HashMap<>();
			
			jsonMap.put("id", room.getId());
			jsonMap.put("roomName", room.getRoomName());
			jsonMap.put("address", room.getAddress());
			jsonMap.put("gpsLa", room.getGpsLatitude());
			jsonMap.put("gpsLong", room.getGpsLongitude());
			jsonMap.put("macAddress", room.getMacAddress());
			
			errorMessage = this.validationData.validateRoomData(jsonMap);
			if (errorMessage != null) {
				report = new ReportError(56, "Updating room failed because " + errorMessage);
				return ResponseEntity.badRequest().body(report);
			}
			
			if (this.roomService.findRoomById(room.getId()) == null) {
				report = new ReportError(53, "This room do not exist!");
				return new ResponseEntity<>(report, HttpStatus.NOT_FOUND);
			}
			
			if (!this.roomService.checkRoomNameDuplicate(room.getRoomName())) {
				report = new ReportError(51, "Duplicate room name!");
				return ResponseEntity.badRequest().body(report);
			}
			
			if (!this.roomService.checkMacAddrDuplicate(room.getMacAddress())) {
				report = new ReportError(52, "Duplicate MAC address!");
				return ResponseEntity.badRequest().body(report);
			}
			
			this.roomService.updateRoom(room);
			return ResponseEntity.ok("Updating room info successes!");
		} catch (Exception e) {
			e.printStackTrace();
			report = new ReportError(2, "Error happened when jackson deserialization info!");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, report.toString());
		}
	}
}
