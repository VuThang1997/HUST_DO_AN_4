package edu.hust.controller;

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

import edu.hust.model.Course;
import edu.hust.model.ReportError;
import edu.hust.service.CourseService;
import edu.hust.utils.ValidationCourseData;
import edu.hust.utils.ValidationData;

@RestController
public class CourseController {

	private ValidationData validationData;
	private CourseService courseService;
	private ValidationCourseData validationCourseData;

	@Autowired
	public CourseController(@Qualifier("CourseServiceImpl1") CourseService courseService,
			@Qualifier("ValidationDataImpl1") ValidationData validationData, 
			@Qualifier("ValidationCourseDataImpl1") ValidationCourseData validationCourseData) {
		this.courseService = courseService;
		this.validationData = validationData;
		this.validationCourseData = validationCourseData;
	}

	@RequestMapping(value = "/courses", method = RequestMethod.POST)
	public ResponseEntity<?> addNewCourse(@RequestBody String courseInfo) {
		String errorMessage = null;
		ObjectMapper objectMapper = null;
		Map<String, Object> jsonMap = null;
		Course course = null;
		ReportError report = null;
		
		try {
			objectMapper = new ObjectMapper();
			course = objectMapper.readValue(courseInfo, Course.class);
			jsonMap = new HashMap<>();
			
			jsonMap.put("courseName", course.getCourseName());
			//jsonMap.put("Description", course.getDescription());

			errorMessage = this.validationData.validateCourseData(jsonMap);
			if (errorMessage != null) {
				report = new ReportError(40, "Adding course failed because " + errorMessage);
				return ResponseEntity.badRequest().body(report);
			}

			//if request has courseID -> must be removed
			course.setCourseID(-1);
			
			if (course.getDescription().isBlank()) {
				course.setDescription(null);
			}
			
			if (this.courseService.addNewCourse(course)) {
				return ResponseEntity.ok("Adding course success!");
			}

			report = new ReportError(41, "Duplicate course name!");
			return ResponseEntity.badRequest().body(report);
		} catch (Exception e) {
			e.printStackTrace();
			report = new ReportError(2, "Error happened when jackson deserialization info!");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, report.toString());
		}
	}

	@RequestMapping(value = "/courses", method = RequestMethod.GET)
	public ResponseEntity<?> getCourseInfo(@RequestParam(value = "courseID", required = true) int id) {
		ReportError report = null;
		String errorMessage = this.validationCourseData.validateIdData(id);
		if (errorMessage != null) {
			report = new ReportError(42, "Getting course failed because " + errorMessage);
			return ResponseEntity.badRequest().body(report);
		}

		Course course = this.courseService.getCourseInfo(id);

		if (course != null) {
			return ResponseEntity.ok(course);
		}

		report = new ReportError(43, "This course do not exist!");
		return new ResponseEntity<>(report, HttpStatus.NOT_FOUND);
	}

	@RequestMapping(value = "/courses", method = RequestMethod.PUT)
	public ResponseEntity<?> updateCourseInfo(@RequestBody String courseInfo) {
		String errorMessage = null;
		ObjectMapper objectMapper = null;
		Map<String, Object> jsonMap = null;
		Course course = null;
		ReportError report = null;
		
		try {
			objectMapper = new ObjectMapper();
			course = objectMapper.readValue(courseInfo, Course.class);
			jsonMap = new HashMap<>();
			
			jsonMap.put("id", course.getCourseID());
			jsonMap.put("courseName", course.getCourseName());

			errorMessage = this.validationData.validateCourseData(jsonMap);
			if (errorMessage != null) {
				report = new ReportError(44, "Updating course failed because " + errorMessage);
				return ResponseEntity.badRequest().body(report);
			}
			
			if (this.courseService.getCourseInfo(course.getCourseID()) == null) {
				report = new ReportError(43, "This course do not exist!");
				return new ResponseEntity<>(report, HttpStatus.NOT_FOUND);
			}

			if (this.courseService.updateCourseInfo(course)) {
				return ResponseEntity.ok("Updating course success!");
			}

			report = new ReportError(41, "Duplicate course name!");
			return ResponseEntity.badRequest().body(report);
		} catch (Exception e) {
			e.printStackTrace();
			report = new ReportError(2, "Error happened when jackson deserialization info!");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, report.toString());
		}
	}

	@RequestMapping(value = "/courses", method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteCourseInfo(@RequestParam(value = "courseID", required = true) int id) {
		ReportError report = null;
		String errorMessage = this.validationCourseData.validateIdData(id);
		if (errorMessage != null) {
			report = new ReportError(42, "Getting course failed because " + errorMessage);
			return ResponseEntity.badRequest().body(report);
		}
		
		if (this.courseService.getCourseInfo(id) == null) {
			report = new ReportError(43, "This course do not exist!");
			return new ResponseEntity<>(report, HttpStatus.NOT_FOUND);
		}

		if (this.courseService.deleteCourse(id)) {
			return ResponseEntity.ok("Deleting course success!");
		}
		
		report = new ReportError(45, "This course still has dependant!");
		return ResponseEntity.badRequest().body(report);
	}
}
