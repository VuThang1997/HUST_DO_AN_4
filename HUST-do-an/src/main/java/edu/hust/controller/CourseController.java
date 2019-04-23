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
		
		try {
			objectMapper = new ObjectMapper();
			course = objectMapper.readValue(courseInfo, Course.class);
			jsonMap = new HashMap<>();
			
			jsonMap.put("CourseName", course.getCourseName());
			//jsonMap.put("Description", course.getDescription());

			errorMessage = this.validationData.validateCourseData(jsonMap);
			if (errorMessage != null) {
				return ResponseEntity.badRequest().body("Error code: 40; Content: Adding course failed because " + errorMessage);
			}

			//if request has courseID -> must be removed
			course.setCourseID(-1);
			
			if (course.getDescription().isBlank()) {
				course.setDescription(null);
			}
			
			if (this.courseService.addNewCourse(course)) {
				return ResponseEntity.ok("Adding course success!");
			}

			return ResponseEntity.badRequest().body("Error code: 41; Content: Duplicate course name!");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Error code: 02; Content: Error happened when jackson deserialization info !");
		}
	}

	@RequestMapping(value = "/courses", method = RequestMethod.GET)
	public ResponseEntity<?> getCourseInfo(@RequestParam(value = "courseID", required = true) int id) {
		String errorMessage = this.validationCourseData.validateIdData(id);
		if (errorMessage != null) {
			return ResponseEntity.badRequest().body("Error code: 42; Content: Getting course info failed because of " + errorMessage);
		}

		Course course = this.courseService.getCourseInfo(id);

		if (course != null) {
			return ResponseEntity.ok(course);
		}

		return new ResponseEntity<>("Error code: 43; Content: This course do not exist!", HttpStatus.NOT_FOUND);
	}

	@RequestMapping(value = "/courses", method = RequestMethod.PUT)
	public ResponseEntity<?> updateCourseInfo(@RequestBody String courseInfo) {
		String errorMessage = null;
		ObjectMapper objectMapper = null;
		Map<String, Object> jsonMap = null;
		Course course = null;
		
		try {
			objectMapper = new ObjectMapper();
			course = objectMapper.readValue(courseInfo, Course.class);
			jsonMap = new HashMap<>();
			
			jsonMap.put("ID", course.getCourseID());
			jsonMap.put("CourseName", course.getCourseName());

			errorMessage = this.validationData.validateCourseData(jsonMap);
			if (errorMessage != null) {
				return ResponseEntity.badRequest().body("Error code: 44; Content: Updating course failed because " + errorMessage);
			}
			
			if (this.courseService.getCourseInfo(course.getCourseID()) == null) {
				return new ResponseEntity<>("Error code: 43; Content: This course do not exist!", HttpStatus.NOT_FOUND);
			}

			if (this.courseService.updateCourseInfo(course)) {
				return ResponseEntity.ok("Updating course success!");
			}

			return ResponseEntity.badRequest().body("Error code: 41; Content: Duplicate course name!");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Error code: 02; Content: Error happened when jackson deserialization info !");
		}
	}

	@RequestMapping(value = "/courses", method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteCourseInfo(@RequestParam(value = "courseID", required = true) int id) {
		String errorMessage = this.validationCourseData.validateIdData(id);
		if (errorMessage != null) {
			return ResponseEntity.badRequest().body("Error code: 42; Content: Getting course info failed because of " + errorMessage);
		}
		
		if (this.courseService.getCourseInfo(id) == null) {
			return new ResponseEntity<>("Error code: 43; Content: This course do not exist!", HttpStatus.NOT_FOUND);
		}

		if (this.courseService.deleteCourse(id)) {
			return ResponseEntity.ok("Deleting course success!");
		}
		
		return ResponseEntity.badRequest().body("Error code: 45; Content: This course still has dependant!");
	}
}
