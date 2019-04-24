package edu.hust.controller;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.hust.model.Class;
import edu.hust.model.Course;
import edu.hust.model.Semester;
import edu.hust.service.ClassService;
import edu.hust.service.CourseService;
import edu.hust.service.SemesterService;
import edu.hust.utils.JsonMapUtil;
import edu.hust.utils.ValidationClassData;
import edu.hust.utils.ValidationData;

@RestController
public class ClassController {

	private ClassService classService;
	private ValidationData validationData;
	private ValidationClassData validationClassData;
	private JsonMapUtil jsonMapUtil;
	private SemesterService semesterService;
	private CourseService courseService;

	public ClassController() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Autowired
	public ClassController(@Qualifier("ClassServiceImpl1") ClassService classService,
			@Qualifier("ValidationDataImpl1") ValidationData validationData,
			@Qualifier("JsonMapUtilImpl1") JsonMapUtil jsonMapUtil,
			@Qualifier("SemesterServiceImpl1") SemesterService semesterService,
			@Qualifier("CourseServiceImpl1") CourseService courseService,
			@Qualifier("ValidationClassDataImpl1") ValidationClassData validationClassData) {
		super();
		this.classService = classService;
		this.validationData = validationData;
		this.jsonMapUtil = jsonMapUtil;
		this.semesterService = semesterService;
		this.courseService = courseService;
		this.validationClassData = validationClassData;
	}

	@RequestMapping(value = "/classes", method = RequestMethod.POST)
	public ResponseEntity<?> addNewClass(@RequestBody String infoClass) {
		Map<String, Object> jsonMap = null;
		ObjectMapper objectMapper = null;
		String className = null;
		String errorMessage = null;
		Course course = null;
		Semester semester = null;
		LocalDate addingDate = null;
		Class classInstance = null;
		int maxStudent = -1;
		int numberOfLessons = -1;
		int courseID = -1;
		int semesterId = -1;

		try {
			objectMapper = new ObjectMapper();
			jsonMap = objectMapper.readValue(infoClass, new TypeReference<Map<String, Object>>() {
			});

			// check request body has enough info in right JSON format
			if (!this.jsonMapUtil.checkKeysExist(jsonMap, "ClassName", "MaxStudent", "NumberOfLessons", "CourseID",
					"SemesterID")) {
				return ResponseEntity.badRequest()
						.body("Error code: 01; Content: Json dynamic map lacks necessary key(s)!");
			}

			errorMessage = this.validationData.validateClassData(jsonMap);
			if (errorMessage != null) {
				return ResponseEntity.badRequest()
						.body("Error code: 60; Content: Adding new class failed because " + errorMessage);
			}

			courseID = Integer.parseUnsignedInt(jsonMap.get("CourseID").toString());
			course = this.courseService.getCourseInfo(courseID);
			if (course == null) {
				return new ResponseEntity<>("Error code: 43; Content: This course do not exist!", HttpStatus.NOT_FOUND);
			}

			semesterId = Integer.parseUnsignedInt(jsonMap.get("SemesterID").toString());
			semester = this.semesterService.findSemesterById(semesterId);
			if (semester == null) {
				return ResponseEntity.badRequest().body("Error code: 33; Content: This semester do not exist!");
			}

			addingDate = LocalDate.now();
			if (addingDate.isAfter(semester.getBeginDate())) {
				return ResponseEntity.badRequest()
						.body("Error code: 61; Content: Adding new class do not work after semester begins!");
			}

			numberOfLessons = Integer.parseUnsignedInt(jsonMap.get("NumberOfLessons").toString());
			maxStudent = Integer.parseUnsignedInt(jsonMap.get("MaxStudent").toString());
			className = jsonMap.get("ClassName").toString();
			

			classInstance = new Class(className, maxStudent, numberOfLessons);
			classInstance.setCurrentLesson(0);
			classInstance.setIdentifyString(null);
			classInstance.setIsChecked(null);
			classInstance.setCourse(course);
			classInstance.setSemester(semester);

			this.classService.addNewClass(classInstance);
			return ResponseEntity.ok("Adding new class successes!");

		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Error code: 02; Content: Error happened when jackson deserialization info !");
		}
	}

	@RequestMapping(value = "/classes", method = RequestMethod.GET)
	public ResponseEntity<?> getInfoClass(@RequestParam(value = "classID", required = true) int classID) {
		String errorMessage = this.validationClassData.validateIdData(classID);
		if (errorMessage != null) {
			return ResponseEntity.badRequest().body("Error code: 62; Content: Getting class info failed because " + errorMessage);
		}

		Class classInstance = this.classService.findClassByID(classID);
		if (classInstance != null) {
			return new ResponseEntity<>("Error code: 63; Content: This class do not exist!", HttpStatus.NOT_FOUND);
		}

		return ResponseEntity.ok(classInstance);
	}
	
	@DeleteMapping("/classes")
	public ResponseEntity<?> deleteInfoClass(@RequestParam(value = "classID", required = true) int classID) {
		String errorMessage = this.validationClassData.validateIdData(classID);
		if (errorMessage != null) {
			return ResponseEntity.badRequest().body("Error code: 62; Content: Getting class info failed because " + errorMessage);
		}
		
		Class classInstance = this.classService.findClassByID(classID);
		if (classInstance == null) {
			return new ResponseEntity<>("Error code: 63; Content: This class do not exist!", HttpStatus.NOT_FOUND);
		}
		
		if (this.classService.deleteClassInfo(classID)) {
			return ResponseEntity.ok("Deleting class successes!");
		}
		
		return ResponseEntity.badRequest().body("Error code: 66; Content: This class still has dependant!");
	}

	@PutMapping("/classes")
	public ResponseEntity<?> updateInfoClass(@RequestBody String infoClass) {
		Map<String, Object> jsonMap = null;
		ObjectMapper objectMapper = null;
		String className = null;
		String errorMessage = null;
		Course course = null;
		Semester semester = null;
		Class classInstance = null;
		int id = -1;
		int maxStudent = -1;
		int numberOfLessons = -1;
		int courseID = -1;
		int semesterId = -1;

		try {
			objectMapper = new ObjectMapper();
			jsonMap = objectMapper.readValue(infoClass, new TypeReference<Map<String, Object>>() {
			});

			// check request body has enough info in right JSON format
			if (!this.jsonMapUtil.checkKeysExist(jsonMap, "ID", "ClassName", "MaxStudent", "NumberOfLessons",
					"CourseID", "SemesterID")) {
				return ResponseEntity.badRequest()
						.body("Error code: 01; Content: Json dynamic map lacks necessary key(s)!");
			}

			errorMessage = this.validationData.validateClassData(jsonMap);
			if (errorMessage != null) {
				return ResponseEntity.badRequest()
						.body("Error code: 64; Content: Updating class info failed because " + errorMessage);
			}

			id = Integer.parseUnsignedInt(jsonMap.get("ID").toString());
			classInstance = this.classService.findClassByID(id);
			if (classInstance == null) {
				return new ResponseEntity<>("Error code: 63; Content: This class do not exist!", HttpStatus.NOT_FOUND);
			}

			courseID = Integer.parseUnsignedInt(jsonMap.get("CourseID").toString());
			if (classInstance.getCourse().getCourseID() != courseID) {
				course = this.courseService.getCourseInfo(courseID);
				if (course == null) {
					return new ResponseEntity<>("Error code: 43; Content: This course do not exist!",
							HttpStatus.NOT_FOUND);
				}
			} else {
				course = classInstance.getCourse();
			}

			semesterId = Integer.parseUnsignedInt(jsonMap.get("SemesterID").toString());
			if (classInstance.getSemester().getSemesterID() != semesterId) {
				semester = this.semesterService.findSemesterById(semesterId);
				if (semester == null) {
					return ResponseEntity.badRequest().body("Error code: 33; Content: This semester do not exist!");
				}
			} else {
				semester = classInstance.getSemester();
			}
			
			LocalDate updateDate = LocalDate.now();
			if (updateDate.isAfter(semester.getBeginDate())) {
				return ResponseEntity.badRequest().body("Error code: 65; Content: Updating class info do not work after semester begins!");
			}

			numberOfLessons = Integer.parseUnsignedInt(jsonMap.get("NumberOfLessons").toString());
			maxStudent = Integer.parseUnsignedInt(jsonMap.get("MaxStudent").toString());
			className = jsonMap.get("ClassName").toString();

			classInstance = new Class(className, maxStudent, numberOfLessons);
			classInstance.setCourse(course);
			classInstance.setSemester(semester);

			this.classService.updateClassInfo(classInstance);
			return ResponseEntity.ok("Updating class info successes!");

		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Error code: 02; Content: Error happened when jackson deserialization info !");
		}
	}
}
