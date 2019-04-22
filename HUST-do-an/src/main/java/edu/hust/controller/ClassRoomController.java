
package edu.hust.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.hust.model.ClassRoom;
import edu.hust.service.ClassRoomService;
import edu.hust.utils.ValidationData;

@RestController
public class ClassRoomController {

	private ClassRoomService classRoomService;
	private ValidationData validationData;

	public ClassRoomController() {
		super();
		// TODO Auto-generated constructorstub
	}

	@Autowired
	public ClassRoomController(@Qualifier("ClassRoomServiceImpl1") ClassRoomService classRoomService,
			@Qualifier("ValidationDataImpl1") ValidationData validationData) {
		super();
		this.classRoomService = classRoomService;
		this.validationData = validationData;
	}

	@PostMapping("/classrooms")
	public ResponseEntity<?> addNewClassRoom(@RequestBody String info) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			ClassRoom classRoom = objectMapper.readValue(info, ClassRoom.class);

			if (!this.validationData.validateClassRoomData(classRoom)) {
				return ResponseEntity.badRequest().body("Not enough info or wrong input context!");
			}

			if (this.classRoomService.addNewClassRoom(classRoom)) {
				return ResponseEntity.ok().build();
			}
			return new ResponseEntity<>("Class info or room info is invalid!", HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error happened!");
		}
	}

	@GetMapping("/classrooms")
	public ResponseEntity<?> getInfoClassRoom(@RequestParam(value = "classID", required = true) int classID,
			@RequestParam(value = "roomID", required = true) int roomID) {

		if (classID <= 0 || roomID <= 0) {
			return ResponseEntity.badRequest().body("These id are invalid!");
		}

		List<ClassRoom> classRooms = this.classRoomService.getListClassRoom(classID, roomID);
		if (classRooms != null && !classRooms.isEmpty()) {
			return ResponseEntity.ok(classRooms);
		}

		return new ResponseEntity<>("Those record are not exist!", HttpStatus.NOT_FOUND);
	}

	/*@PutMapping("/classrooms")
	public ResponseEntity<?> updateInfoClassRoom(@RequestParam(value = "classID", required = true) int classID,
			@RequestParam(value = "roomID", required = true) int roomID) {

		if (classID <= 0 || roomID <= 0) {
			return ResponseEntity.badRequest().body("These id are invalid!");
		}

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			ClassRoom classRoom = objectMapper.readValue(info, ClassRoom.class);

			if (!this.validationData.validateClassRoomData(classRoom)) {
				return ResponseEntity.badRequest().body("Not enough info or wrong input context!");
			}
			
			if (this.classRoomService.updateClassRoomInfo(roomID, classID, classRoom)) {
				return ResponseEntity.ok().build();
			}
			
			return ResponseEntity.badRequest().body("Not enough info!");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error happened!");
		}*/

	//}

}
