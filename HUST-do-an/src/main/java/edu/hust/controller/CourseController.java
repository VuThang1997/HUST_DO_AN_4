package edu.hust.controller;

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
import edu.hust.utils.ValidationData;

@RestController
public class CourseController {

	private CourseService courseService;
	private ValidationData validationData;

	@Autowired
	public CourseController(@Qualifier("CourseServiceImpl1") CourseService courseService,
			@Qualifier("ValidationDataImpl1") ValidationData validationData) {
		this.courseService = courseService;
		this.validationData = validationData;
	}

	@RequestMapping(value = "/courses", method = RequestMethod.POST)
	public ResponseEntity<?> addNewCourse(@RequestBody String courseInfo) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			Course course = objectMapper.readValue(courseInfo, Course.class);

			if (!this.validationData.validateCourseData(course)) {
				return ResponseEntity.badRequest().body("Not enough info!");
			}

			if (this.courseService.addNewCourse(course)) {
				return ResponseEntity.ok().build();
			}

			return ResponseEntity.badRequest().body("This course's name is already registed!");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error happened!");
		}
	}

	@RequestMapping(value = "/courses", method = RequestMethod.GET)
	public ResponseEntity<?> getCourseInfo(@RequestParam(value = "courseID", required = true) int id) {
		if (id <= 0) {
			return ResponseEntity.badRequest().body("This id is invalid!");
		}

		Course course = this.courseService.getCourseInfo(id);

		if (course != null) {
			return ResponseEntity.ok(course);
		}

		return new ResponseEntity<>("This course is not exist!", HttpStatus.NOT_FOUND);

	}

	@RequestMapping(value = "/courses", method = RequestMethod.PUT)
	public ResponseEntity<?> updateCourseInfo(@RequestParam(value = "courseID", required = true) int id,
			@RequestBody String courseInfo) {
		if (id <= 0) {
			return ResponseEntity.badRequest().body("This id is invalid!");
		}

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			Course course = objectMapper.readValue(courseInfo, Course.class);

			if (course.getCourseID() <= 0) {
				course.setCourseID(id);
			}

			if (this.courseService.updateCourseInfo(course)) {
				return ResponseEntity.ok().build();
			}

			return new ResponseEntity<>("This course is not exist!", HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error happened!");
		}
	}

	@RequestMapping(value = "/courses", method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteCourseInfo(@RequestParam(value = "courseID", required = true) int id) {
		if (id <= 0) {
			return ResponseEntity.badRequest().body("This id is invalid!");
		}

		if (this.courseService.deleteCourse(id)) {
			return ResponseEntity.ok().build();
		}
		return ResponseEntity.badRequest().body("This course is not exist or still have classes link to!!");
	}
}
