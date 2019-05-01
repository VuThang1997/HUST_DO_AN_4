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

import edu.hust.model.Class;
import edu.hust.enumData.IsTeaching;
import edu.hust.model.Account;
import edu.hust.model.ReportError;
import edu.hust.model.TeacherClass;
import edu.hust.service.AccountService;
import edu.hust.service.ClassService;
import edu.hust.service.RoomService;
import edu.hust.service.StudentClassService;
import edu.hust.service.TeacherClassService;
import edu.hust.utils.JsonMapUtil;
import edu.hust.utils.ValidationAccountData;
import edu.hust.utils.ValidationRoomData;
import edu.hust.utils.ValidationTeacherClassData;

@RestController
public class TeacherClassController {

	private TeacherClassService teacherClassService;
	private StudentClassService studentClassService;
	private AccountService accountService;
	private ValidationTeacherClassData validationTeacherClassData;
	private ClassService classService;
	private ValidationAccountData validationAccountData;
	private ValidationRoomData validationRoomData;
	private JsonMapUtil jsonMapUtil;
	private RoomService roomService;

	public TeacherClassController() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Autowired
	public TeacherClassController(@Qualifier("TeacherClassServiceImpl1") TeacherClassService teacherClassService,
			@Qualifier("StudentClassServiceImpl1") StudentClassService studentClassService,
			@Qualifier("AccountServiceImpl1") AccountService accountService,
			@Qualifier("JsonMapUtilImpl1") JsonMapUtil jsonMapUtil,
			@Qualifier("ValidationTeacherClassDataImpl1") ValidationTeacherClassData validationTeacherClassData,
			@Qualifier("ValidationRoomDataImpl1") ValidationRoomData validationRoomData,
			@Qualifier("ClassServiceImpl1") ClassService classService,
			// @Qualifier("ValidationStudentClassDataImpl1") ValidationStudentClassData
			// validationStudentClassData,
			@Qualifier("ValidationAccountDataImpl1") ValidationAccountData validationAccountData,
			@Qualifier("RoomServiceImpl1") RoomService roomService) {
		super();
		this.teacherClassService = teacherClassService;
		this.studentClassService = studentClassService;
		this.accountService = accountService;
		this.roomService = roomService;
		this.validationRoomData = validationRoomData;
		this.validationTeacherClassData = validationTeacherClassData;
		this.classService = classService;
		// this.validationStudentClassData = validationStudentClassData;
		this.validationAccountData = validationAccountData;
		this.jsonMapUtil = jsonMapUtil;

	}

	@PostMapping(value = "/teacherRollCall")
	public ResponseEntity<?> rollCall(@RequestBody String info) {
		int teacherID = 0;
		int classID = 0;
		int roomID = 0;
		int weekday = 0;
		double gpsLong;
		double gpsLa;
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
			jsonMap = objectMapper.readValue(info, new TypeReference<Map<String, Object>>() {
			});

			// check request body has enough info in right JSON format
			if (!this.jsonMapUtil.checkKeysExist(jsonMap, "teacherID", "classID", "roomID", "gpsLong", "gpsLa")) {
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

			gpsLong = Double.parseDouble(jsonMap.get("gpsLong").toString());
			gpsLa = Double.parseDouble(jsonMap.get("gpsLa").toString());
			if (gpsLong < -180 || gpsLong > 180 || gpsLa < -90 || gpsLa > 90) {
				report = new ReportError(84, "Longitude/Latitude is out of practical range!");
				return ResponseEntity.badRequest().body(report);
			}

			// check teacher has authority to roll call this class
			if (!this.teacherClassService.checkTeacherHasAuthority(teacherID, classID)) {
				report = new ReportError(11, "Authentication has failed or has not yet been provided!");
				return new ResponseEntity<>(report, HttpStatus.UNAUTHORIZED);
			}

			// check if device is in distance limit - 50m
			if (this.roomService.calculateDistanceBetween2GPSCoord(roomID, gpsLong, gpsLa) > 50) {
				report = new ReportError(85, "Device is out of valid distance to classroom!");
				return ResponseEntity.badRequest().body(report);
			}

			// Check teacher generate time in valid limit
			// Notice: weekday of java = weekday of mySQL - 1
			generateTime = LocalTime.now();
			weekday = LocalDate.now().getDayOfWeek().getValue() + 1;
			if (!this.teacherClassService.checkGenerateTimeValid(weekday, generateTime, classID, roomID)) {
				report = new ReportError(81, "Teacher roll call failed because timing is not in valid range!");
				return ResponseEntity.badRequest().body(report);
			}

			// generate md5 code
			inputMd5 = generateTime.toString() + classID + generateTime.getMinute() + (new Random()).nextInt(1000);
			result = this.teacherClassService.generateIdentifyString(classID, roomID, weekday, inputMd5);
			if (result == null) {
				report = new ReportError(82, "Cannot generate identify string!");
				return ResponseEntity.badRequest().body(report);
			}

			// add generate time and date to teacher's listRollCall
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

	@PostMapping(value = "/rollCallStudentWithPermission")
	public ResponseEntity<?> rollCallStudentWithPermission(@RequestBody String info) {
		int teacherID = 0;
		int classID = 0;
		int roomID = 0;
		double gpsLong;
		double gpsLa;
		int reason = -1;
		String studentEmail = null;
		String studentPassword = null;
		String errorMessage = null;
		ObjectMapper objectMapper = null;
		Map<String, Object> jsonMap = null;
		ReportError report = null;

		try {
			objectMapper = new ObjectMapper();
			jsonMap = objectMapper.readValue(info, new TypeReference<Map<String, Object>>() {
			});

			// check request body has enough info in right JSON format
			if (!this.jsonMapUtil.checkKeysExist(jsonMap, "teacherID", "classID", "roomID", "gpsLong", "gpsLa",
					"studentEmail", "studentPassword", "reason")) {
				report = new ReportError(1, "Json dynamic map lacks necessary key(s)!");
				return ResponseEntity.badRequest().body(report);
			}
			
			reason = Integer.parseInt(jsonMap.get("reason").toString());
			errorMessage = this.teacherClassService.checkReasonValid(reason);
			if (errorMessage != null) {
				report = new ReportError(80, "Teacher roll call failed because " + errorMessage);
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

			studentEmail = jsonMap.get("studentEmail").toString();
			errorMessage = this.validationAccountData.validateEmailData(studentEmail);
			if (errorMessage != null) {
				report = new ReportError(86, "Roll call with permission failed because " + errorMessage);
				return ResponseEntity.badRequest().body(report);
			}

			roomID = Integer.parseInt(jsonMap.get("roomID").toString());
			errorMessage = this.validationRoomData.validateIdData(roomID);
			if (errorMessage != null) {
				report = new ReportError(80, "Teacher roll call failed because " + errorMessage);
				return ResponseEntity.badRequest().body(report);
			}

			gpsLong = Double.parseDouble(jsonMap.get("gpsLong").toString());
			gpsLa = Double.parseDouble(jsonMap.get("gpsLa").toString());
			if (gpsLong < -180 || gpsLong > 180 || gpsLa < -90 || gpsLa > 90) {
				report = new ReportError(84, "Longitude/Latitude is out of practical range!");
				return ResponseEntity.badRequest().body(report);
			}

			// check student's email and password are valid
			studentPassword = jsonMap.get("studentPassword").toString();
			if (this.accountService.findAccountByEmailAndPassword(studentEmail, studentPassword) == null) {
				report = new ReportError(11, "Authentication has failed or has not yet been provided!");
				return new ResponseEntity<>(report, HttpStatus.UNAUTHORIZED);
			}

			// check student is learning in this class
			if (!this.studentClassService.checkStudentIsLearning(studentEmail, classID)) {
				report = new ReportError(87,
						"Roll call with permission failed because this student is not in list student");
				return ResponseEntity.badRequest().body(report);
			}

			// check teacher has authority to roll call this class
			if (!this.teacherClassService.checkTeacherHasAuthority(teacherID, classID)) {
				report = new ReportError(11, "Authentication has failed or has not yet been provided!");
				return new ResponseEntity<>(report, HttpStatus.UNAUTHORIZED);
			}

			// check if device is in distance limit - 50m
			if (this.roomService.calculateDistanceBetween2GPSCoord(roomID, gpsLong, gpsLa) > 50) {
				report = new ReportError(85, "Device is out of valid distance to classroom!");
				return ResponseEntity.badRequest().body(report);
			}

			if (!this.teacherClassService.rollCallStudentWithPermission(studentEmail, classID, roomID, reason)) {
				report = new ReportError(88, "This student has rolled call already!!");
				return ResponseEntity.badRequest().body(report);
			}

			return ResponseEntity.ok("Roll call student with permission successes!");

		} catch (NumberFormatException e) {
			e.printStackTrace();
			report = new ReportError(83, "Teacher roll call student failed because an ID is not a number");
			return ResponseEntity.badRequest().body(report);
		} catch (Exception e) {
			e.printStackTrace();
			report = new ReportError(2, "Error happened when jackson deserialization info!");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, report.toString());
		}
	}

	@PostMapping(value = "/teacherClass")
	public ResponseEntity<?> addTeacherClass(@RequestBody String info) {
		int teacherID = 0;
		int classID = 0;
		Account teacherAccount = null;
		Class classInstance = null;
		TeacherClass teacherClass = null;
		String errorMessage = null;
		ObjectMapper objectMapper = null;
		Map<String, Object> jsonMap = null;
		ReportError report = null;

		try {
			objectMapper = new ObjectMapper();
			jsonMap = objectMapper.readValue(info, new TypeReference<Map<String, Object>>() {
			});

			// check request body has enough info in right JSON format
			if (!this.jsonMapUtil.checkKeysExist(jsonMap, "teacherID", "classID")) {
				report = new ReportError(1, "Json dynamic map lacks necessary key(s)!");
				return ResponseEntity.badRequest().body(report);
			}

			teacherID = Integer.parseInt(jsonMap.get("teacherID").toString());
			errorMessage = this.validationTeacherClassData.validateIdData(teacherID);
			if (errorMessage != null) {
				report = new ReportError(89, "Adding teacher-class failed because " + errorMessage);
				return ResponseEntity.badRequest().body(report);
			}

			classID = Integer.parseInt(jsonMap.get("classID").toString());
			errorMessage = this.validationTeacherClassData.validateIdData(classID);
			if (errorMessage != null) {
				report = new ReportError(89, "Adding teacher-class roll call failed because " + errorMessage);
				return ResponseEntity.badRequest().body(report);
			}

			// check if the student and the class exist
			teacherAccount = this.accountService.findAccountByID(teacherID);
			if (teacherAccount == null) {
				report = new ReportError(110, "Adding teacher-class roll call failed because teacherID is invalid ");
				return ResponseEntity.badRequest().body(report);
			}

			classInstance = this.classService.findClassByID(classID);
			if (classInstance == null) {
				report = new ReportError(111, "Adding teacher-class roll call failed because classID is invalid ");
				return ResponseEntity.badRequest().body(report);
			}

			teacherClass = this.teacherClassService.findCurrentTeacherByClassID(classID);
			if (teacherClass != null) {
				if (teacherClass.getAccount().getId() == teacherID) {
					report = new ReportError(113, "The teacher is teaching this class. Nothing need be added!");
				}
				
				report = new ReportError(114, "Another teacher is teaching this class!");
				return ResponseEntity.badRequest().body(report);
			}

			if (!this.teacherClassService.checkTimetableConflict(teacherID, classID)) {
				report = new ReportError(112, "There are conflicts with current timetable!");
				return new ResponseEntity<>(report, HttpStatus.CONFLICT);
			}

			teacherClass = new TeacherClass(classInstance, teacherAccount);
			teacherClass.setIsTeaching(IsTeaching.TEACHING.getValue());
			teacherClass.setListRollCall(null);
			
			this.teacherClassService.addNewTeacherClass(teacherClass);
			return ResponseEntity.ok("Adding new teacher-class successes!");

		} catch (NumberFormatException e) {
			e.printStackTrace();
			report = new ReportError(115, "Adding new teacher-class failed because an ID is not a number");
			return ResponseEntity.badRequest().body(report);
		} catch (Exception e) {
			e.printStackTrace();
			report = new ReportError(2, "Error happened when jackson deserialization info!");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, report.toString());
		}
	}
}
