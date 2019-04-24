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

import edu.hust.model.ReportError;
import edu.hust.service.RoomService;
import edu.hust.service.StudentClassService;
import edu.hust.utils.JsonMapUtil;
import edu.hust.utils.ValidationRoomData;
import edu.hust.utils.ValidationStudentClassData;

@RestController
public class StudentClassController {

	private StudentClassService studentClassService;
	private RoomService roomService;
	private JsonMapUtil jsonMapUtil;
	private ValidationRoomData validationRoomData;
	private ValidationStudentClassData validationStudentClassData;

	public StudentClassController() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Autowired
	public StudentClassController(@Qualifier("StudentClassServiceImpl1") StudentClassService studentClassService,
			@Qualifier("RoomServiceImpl1") RoomService roomService,
			@Qualifier("JsonMapUtilImpl1") JsonMapUtil jsonMapUtil,
			@Qualifier("ValidationRoomDataImpl1") ValidationRoomData validationRoomData,
			@Qualifier("ValidationStudentClassDataImpl1") ValidationStudentClassData validationStudentClassData) {
		super();
		this.studentClassService = studentClassService;
		this.roomService = roomService;
		this.validationRoomData = validationRoomData;
		this.validationStudentClassData = validationStudentClassData;
		this.jsonMapUtil = jsonMapUtil;
	}

	@PostMapping(value = "/studentRollCall")
	public ResponseEntity<?> rollCall(@RequestBody String info) {
		int studentID;
		int classID;
		int roomID;
		double gpsLong;
		double gpsLa;
		ReportError report = null;
		String imei = null;
		String macAddr = null;
		String identifyString = null;
		String errorMessage = null;
		LocalDateTime rollCallAt = null;
		ObjectMapper objectMapper = null;
		Map<String, Object> jsonMap = null;

		try {
			objectMapper = new ObjectMapper();
			jsonMap = objectMapper.readValue(info, new TypeReference<Map<String, Object>>() {
			});

			// check request body has enough info in right JSON format
			if (!this.jsonMapUtil.checkKeysExist(jsonMap, "studentID", "classID", "roomID", "gpsLong", "gpsLa",
					"macAddr", "identifyString", "imei")) {
				report = new ReportError(1, "Json dynamic map lacks necessary key(s)!");
				return ResponseEntity.badRequest().body(report);
			}

			studentID = Integer.parseInt(jsonMap.get("studentID").toString());
			errorMessage = this.validationStudentClassData.validateIdData(studentID);
			if (errorMessage != null) {
				report = new ReportError(90, "Student roll call failed because " + errorMessage);
				return ResponseEntity.badRequest().body(report);
			}
			
			classID = Integer.parseInt(jsonMap.get("classID").toString());
			errorMessage = this.validationStudentClassData.validateIdData(classID);
			if (errorMessage != null) {
				report = new ReportError(90, "Student roll call failed because " + errorMessage);
				return ResponseEntity.badRequest().body(report);
			}
			
			roomID = Integer.parseInt(jsonMap.get("roomID").toString());
			errorMessage = this.validationRoomData.validateIdData(roomID);
			if (errorMessage != null) {
				report = new ReportError(90, "Student roll call failed because " + errorMessage);
				return ResponseEntity.badRequest().body(report);
			}

			gpsLong = Double.parseDouble(jsonMap.get("gpsLong").toString());
			gpsLa = Double.parseDouble(jsonMap.get("gpsLa").toString());
			if (gpsLong < -180 || gpsLong > 180 || gpsLa < -90 || gpsLa > 90) {
				report = new ReportError(91, "Longitude/Latitude is out of practical range!");
				return ResponseEntity.badRequest().body(report);
			}
			
			imei = jsonMap.get("imei").toString();
			if (imei == null || imei.isBlank()) {
				report = new ReportError(93, "Missing IMEI info");
				return ResponseEntity.badRequest().body(report);
			}
			
			macAddr = jsonMap.get("macAddr").toString();
			if (macAddr == null || macAddr.isBlank()) {
				report = new ReportError(92, "Missing MAC address");
				return ResponseEntity.badRequest().body(report);
			}

			identifyString = jsonMap.get("identifyString").toString();
			if (identifyString == null || identifyString.isBlank()) {
				report = new ReportError(94, "Missing identifyString info");
				return ResponseEntity.badRequest().body(report);
			}
			
			// check student has authority to roll call this class
			if (!this.studentClassService.checkStudentHasAuthority(studentID, classID, roomID, identifyString, imei)) {
				report = new ReportError(11, "Authentication has failed or has not yet been provided!");
				return new ResponseEntity<>(report, HttpStatus.UNAUTHORIZED);
			}
			
			// check if MAC address is correct
			if (!this.roomService.checkMacAddress(roomID, macAddr)) {
				report = new ReportError(95, "MAC address is incorrect!");
				return ResponseEntity.badRequest().body(report);
			}

			// check if device is in distance limit - 20m
			if (this.roomService.calculateDistanceBetween2GPSCoord(roomID, gpsLong, gpsLa) > 20) {
				report = new ReportError(96, "Device is out of valid distance to classroom!");
				return ResponseEntity.badRequest().body(report);
			}

			// add rollcall time to student.listRollcall
			rollCallAt = LocalDateTime.now();
			this.studentClassService.rollCall(classID, studentID, rollCallAt);

			return ResponseEntity.ok("Roll call successful!");

		} catch (Exception e) {
			e.printStackTrace();
			report = new ReportError(2, "Error happened when jackson deserialization info!");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, report.toString());
		}
	}
}
