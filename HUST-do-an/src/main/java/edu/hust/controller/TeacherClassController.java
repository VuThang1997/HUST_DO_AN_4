package edu.hust.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.Random;

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
import edu.hust.service.TeacherClassService;
import edu.hust.utils.JsonMapUtil;
import edu.hust.utils.ValidationData;
import edu.hust.utils.ValidationRoomData;
import edu.hust.utils.ValidationTeacherClassData;

@RestController
public class TeacherClassController {
	
	private TeacherClassService teacherClassService;
	private ValidationData validationData;
	private ValidationTeacherClassData validationTeacherClassData;
	private ValidationRoomData validationRoomData;
	private JsonMapUtil jsonMapUtil;

	public TeacherClassController() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Autowired
	public TeacherClassController(
			@Qualifier("TeacherClassServiceImpl1") TeacherClassService teacherClassService,
			@Qualifier("ValidationDataImpl1") ValidationData validationData, 
			@Qualifier("JsonMapUtilImpl1") JsonMapUtil jsonMapUtil,
			@Qualifier("ValidationTeacherClassDataImpl1") ValidationTeacherClassData validationTeacherClassData,
			@Qualifier("ValidationRoomDataImpl1") ValidationRoomData validationRoomData) {
		super();
		this.teacherClassService = teacherClassService;
		this.validationData = validationData;
		this.validationRoomData = validationRoomData;
		this.validationTeacherClassData = validationTeacherClassData;
		this.jsonMapUtil = jsonMapUtil;
		
	}

	@PostMapping(value = "/teacherRollCall")
	public ResponseEntity<?> rollCall(@RequestBody String info) {
		int teacherID = 0;
		int classID = 0;
		int roomID = 0;
		int weekday = 0;
		String inputMd5 = null;
		String result = null;
		String errorMessage = null;
		LocalDateTime rollCallAt = null;
		LocalTime generateTime = null;
		ObjectMapper objectMapper = null;
		Map<String, Object> jsonMap = null;
		ReportError report = null;
		
		try {
			objectMapper = new ObjectMapper();
			jsonMap = objectMapper.readValue(info, new TypeReference<Map<String, Object>>() {});

			//check request body has enough info in right JSON format
			if (!this.jsonMapUtil.checkKeysExist(jsonMap, "teacherID", "classID", "roomID")) {
				report = new ReportError(1, "Json dynamic map lacks necessary key(s)!");
				return ResponseEntity.badRequest().body(report);
			}
			
			teacherID = Integer.parseInt(jsonMap.get("teacherID").toString());
			errorMessage = this.validationTeacherClassData.validateIdData(teacherID);
			if (errorMessage != null) {
				report = new ReportError(80, "Teacher roll call failed because " + errorMessage);
				return ResponseEntity.badRequest().body(report);
			}
			
			classID = Integer.parseInt(jsonMap.get("classID").toString());
			errorMessage = this.validationTeacherClassData.validateIdData(classID);
			if (errorMessage != null) {
				report = new ReportError(80, "Teacher roll call failed because " + errorMessage);
				return ResponseEntity.badRequest().body(report);
			}
			
			roomID = Integer.parseInt(jsonMap.get("roomID").toString());
			errorMessage = this.validationRoomData.validateIdData(roomID);
			if (errorMessage != null) {
				report = new ReportError(80, "Teacher roll call failed because " + errorMessage);
				return ResponseEntity.badRequest().body(report);
			}

			//check teacher has authority to roll call this class
			if (!this.teacherClassService.checkTeacherHasAuthority(teacherID, classID)) {
				report = new ReportError(11, "Authentication has failed or has not yet been provided!");
				return new ResponseEntity<>(report, HttpStatus.UNAUTHORIZED);
			}

			//Check teacher generate time in valid limit
			//Notice: weekday of java = weekday of mySQL - 1
			generateTime = LocalTime.now();
			weekday = LocalDate.now().getDayOfWeek().getValue() + 1;
			if (!this.teacherClassService.checkGenerateTimeValid(weekday, generateTime, classID, roomID)) {
				report = new ReportError(81, "Teacher roll call failed because timing is not in valid range!");
				return ResponseEntity.badRequest().body(report);
			}
			
			//generate md5 code
			inputMd5 = generateTime.toString() + classID + generateTime.getMinute() + (new Random()).nextInt(1000);
			result = this.teacherClassService.generateIdentifyString(classID, roomID, weekday, inputMd5);
			if (result == null) {
				report = new ReportError(82, "Cannot generate identify string!");
				return ResponseEntity.badRequest().body(report);
			}
			
			//add generate time and date to teacher's listRollCall
			rollCallAt = LocalDateTime.now();
			this.teacherClassService.rollCall(rollCallAt, teacherID, classID);
			
			report = new ReportError(200, result);
			return ResponseEntity.ok(report);

		} catch (NumberFormatException e) {
			e.printStackTrace();
			report = new ReportError(83, "Teacher roll call failed because an ID is not a number");
			return ResponseEntity.badRequest().body(report);
		} catch (Exception e) {
			e.printStackTrace();
			report = new ReportError(2, "Error happened when jackson deserialization info!");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, report.toString());
		}
	}
}
