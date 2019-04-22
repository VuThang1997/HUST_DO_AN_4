package edu.hust.controller;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.hust.service.RoomService;
import edu.hust.service.StudentClassService;
import edu.hust.utils.GeneralValue;
import edu.hust.utils.JsonMapUtil;

@RestController
public class StudentClassController {

	private StudentClassService studentClassService;
	private RoomService roomService;
	private JsonMapUtil jsonMapUtil;

	public StudentClassController() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Autowired
	public StudentClassController(@Qualifier("StudentClassServiceImpl1") StudentClassService studentClassService,
			@Qualifier("RoomServiceImpl1") RoomService roomService,
			@Qualifier("JsonMapUtilImpl1") JsonMapUtil jsonMapUtil) {
		super();
		this.studentClassService = studentClassService;
		this.roomService = roomService;
		// this.validationData = validationData;
		this.jsonMapUtil = jsonMapUtil;
	}

	@PostMapping(value = "/studentRollCall")
	public ResponseEntity<?> rollCall(@RequestBody String info) {
		int studentID;
		int classID;
		int roomID;
		double gpsLong;
		double gpsLa;
		String imei = null;
		String macAddr = null;
		String identifyString = null;
		LocalDateTime rollCallAt = null;
		ObjectMapper objectMapper = null;
		Map<String, Object> jsonMap = null;

		try {
			objectMapper = new ObjectMapper();
			jsonMap = objectMapper.readValue(info, new TypeReference<Map<String, Object>>() {
			});

			// check request body has enough info in right JSON format
			if (!this.jsonMapUtil.checkKeysExist(jsonMap, "StudentID", "ClassID", "RoomID", "GpsLong", "GpsLa",
					"MacAddr", "IdentifyString", "IMEI")) {
				return ResponseEntity.badRequest().body("Not enough info!");
			}

			studentID = Integer.parseInt(jsonMap.get("StudentID").toString());
			classID = Integer.parseInt(jsonMap.get("ClassID").toString());
			roomID = Integer.parseInt(jsonMap.get("RoomID").toString());
			if (studentID < 1 || classID < 1 || roomID < 1) {
				return ResponseEntity.badRequest().body("Some info are not valid!");
			}

			// check other info
			gpsLong = Double.parseDouble(jsonMap.get("GpsLong").toString());
			gpsLa = Double.parseDouble(jsonMap.get("GpsLa").toString());
			if (gpsLong < GeneralValue.minLongitude || gpsLong > GeneralValue.maxLongitude
					|| gpsLa < GeneralValue.minLatitude || gpsLa > GeneralValue.maxLatitude) {

				return ResponseEntity.badRequest().body("Some info are not practical!");
			}

			macAddr = jsonMap.get("MacAddr").toString();
			imei = jsonMap.get("IMEI").toString();
			if (macAddr == null || imei == null) {
				return ResponseEntity.badRequest().body("Some info are missing!");
			}

			// check student has authority to roll call this class
			identifyString = jsonMap.get("IdentifyString").toString();
			if (!this.studentClassService.checkStudentHasAuthority(studentID, classID, roomID, identifyString, imei)) {
				return new ResponseEntity<>("Authentication has failed or has not yet been provided!",
						HttpStatus.UNAUTHORIZED);
			}

			// check if MAC address is correct and signal strength is acceptable
			if (!this.roomService.checkMacAddress(roomID, macAddr)) {
				return ResponseEntity.badRequest().body("Something is wrong with Wifi");
			}

			// check if device is in distance limit - 20m
			if (this.roomService.calculateDistanceBetween2GPSCoord(roomID, gpsLong, gpsLa) > 20) {
				return ResponseEntity.badRequest().body("You are too far! Come closer!");
			}

			// add rollcall time to student.listRollcall
			rollCallAt = LocalDateTime.now();
			this.studentClassService.rollCall(classID, studentID, rollCallAt);

			return ResponseEntity.ok("Roll call successful!");

		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error happened!");
		}
	}
}
