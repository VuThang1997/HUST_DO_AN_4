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

import edu.hust.enumData.IsTeaching;
import edu.hust.service.TeacherClassService;
import edu.hust.utils.JsonMapUtil;
import edu.hust.utils.ValidationData;

@RestController
public class TeacherClassController {
	
	private TeacherClassService teacherClassService;
	private ValidationData validationData;
	private JsonMapUtil jsonMapUtil;

	public TeacherClassController() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Autowired
	public TeacherClassController(
			@Qualifier("TeacherClassServiceImpl1") TeacherClassService teacherClassService,
			@Qualifier("ValidationDataImpl1") ValidationData validationData, 
			@Qualifier("JsonMapUtilImpl1") JsonMapUtil jsonMapUtil) {
		super();
		this.teacherClassService = teacherClassService;
		this.validationData = validationData;
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
		LocalDateTime rollCallAt = null;
		LocalTime generateTime = null;
		ObjectMapper objectMapper = null;
		Map<String, Object> jsonMap = null;
		
		try {
			objectMapper = new ObjectMapper();
			jsonMap = objectMapper.readValue(info, new TypeReference<Map<String, Object>>() {});

			//check request body has enough info in right JSON format
			if (!this.jsonMapUtil.checkKeysExist(jsonMap, "TeacherID", "ClassID", "RoomID")) {
				return ResponseEntity.badRequest().body("Not enough info!");
			}

			teacherID = Integer.parseInt(jsonMap.get("TeacherID").toString());
			classID = Integer.parseInt(jsonMap.get("ClassID").toString());
			roomID = Integer.parseInt(jsonMap.get("RoomID").toString());
			
			//check no ID has value less than or equal 0 -> save a bit of performance
			if (!this.validationData.validateTeacherClassData(teacherID, classID, IsTeaching.TEACHING.getValue())) {
				return ResponseEntity.badRequest().body("Some info are not valid!");
			}

			//check teacher has authority to roll call this class
			if (!this.teacherClassService.checkTeacherHasAuthority(teacherID, classID)) {
				return new ResponseEntity<>("Authentication has failed or has not yet been provided!",
						HttpStatus.UNAUTHORIZED);
			}

			//Check teacher generate time in valid limit
			//Notice: weekday of java = weekday of mySQL - 1
			generateTime = LocalTime.now();
			weekday = LocalDate.now().getDayOfWeek().getValue() + 1;
			if (!this.teacherClassService.checkGenerateTimeValid(weekday, generateTime, classID, roomID)) {
				return ResponseEntity.badRequest().body("Not in valid time!");
			}
			
			//generate md5 code
			inputMd5 = generateTime.toString() + classID + generateTime.getMinute() + (new Random()).nextInt(1000);
			result = this.teacherClassService.generateIdentifyString(classID, roomID, weekday, inputMd5);
			if (result == null) {
				return ResponseEntity.badRequest().body("Error happened!");
			}
			
			//add generate time and date to teacher's listRollCall
			rollCallAt = LocalDateTime.now();
			this.teacherClassService.rollCall(rollCallAt, teacherID, classID);
			
			return ResponseEntity.ok(result);

		} catch (NumberFormatException e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().body("An ID is not a number!");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error happened!");
		}
	}
}
