
package edu.hust.controller;

import java.io.IOException;

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

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.hust.model.Room;
import edu.hust.service.RoomService;
import edu.hust.utils.ValidationData;

@RestController
public class RoomController {

	private RoomService roomService;
	private ValidationData validationData;

	@Autowired
	public RoomController(@Qualifier("RoomServiceImpl1") RoomService roomService,
			@Qualifier("ValidationDataImpl1") ValidationData validationData) {
		this.roomService = roomService;
		this.validationData = validationData;
	}

	@RequestMapping(value = "/rooms", method = RequestMethod.POST)
	public ResponseEntity<?> addNewRoom(@RequestBody String roomInfo) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			Room room = mapper.readValue(roomInfo, Room.class);
			
			if (!this.validationData.validateRoomData(room)) {
				return ResponseEntity.badRequest().body("Not enough info!");
			}
			
			if (this.roomService.addNewRoom(room)) {
				return ResponseEntity.ok().build();
			} else {
				return new ResponseEntity<>("This room is not existed", HttpStatus.NOT_FOUND);
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error happened!");
		}
	}

	@RequestMapping(value = "/rooms", method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteRoom(@RequestParam(value = "roomID", required = true) int roomID) {
		if (this.roomService.deleteRoom(roomID)) {
			return ResponseEntity.ok().build();
		} else {
			return new ResponseEntity<>("This room is not existed", HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(value = "/rooms", method = RequestMethod.GET)
	public ResponseEntity<?> readInfoRoom(@RequestParam(value = "roomID", required = true) int roomID) {
		Room room = this.roomService.getInfo(roomID);

		if (room == null) {
			return new ResponseEntity<>("This room is not existed", HttpStatus.NOT_FOUND);
		} else {
			return ResponseEntity.ok(room);
		}
	}

	@RequestMapping(value = "/rooms", method = RequestMethod.PUT)
	public ResponseEntity<?> updateInfoRoom(@RequestBody String infoRoom) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			Room updateInfo = mapper.readValue(infoRoom, Room.class);
			if (this.roomService.updateRoom(updateInfo)) {
				return ResponseEntity.ok().build();
			} else {
				return new ResponseEntity<>("This room is not existed", HttpStatus.NOT_FOUND);
			}
		} catch (JsonGenerationException e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong input content!");
		} catch (JsonMappingException e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mapping JSON failed!");
		} catch (IOException e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error happened!");
		}
	}
}
