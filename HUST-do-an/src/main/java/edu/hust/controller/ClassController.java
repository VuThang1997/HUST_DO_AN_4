package edu.hust.controller;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.hust.model.Class;
import edu.hust.service.ClassService;
import edu.hust.utils.ValidationData;

@RestController
public class ClassController {

	private ClassService classService;
	private ValidationData validationData;

	public ClassController() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Autowired
	public ClassController(@Qualifier("ClassServiceImpl1") ClassService classService,
			@Qualifier("ValidationDataImpl1") ValidationData validationData) {
		super();
		this.classService = classService;
		this.validationData = validationData;
	}

	@RequestMapping(value = "/classes", method = RequestMethod.POST)
	public ResponseEntity<?> addNewClass(@RequestBody String infoClass) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			Class classInstance = objectMapper.readValue(infoClass, Class.class);

			if (this.validationData.validateClassData(classInstance)) {
				return ResponseEntity.badRequest().body("Not enough info!");
			}

			if (this.classService.addNewClass(classInstance)) {
				return ResponseEntity.ok().build();
			}

			return new ResponseEntity<>("Not found this course or this semester", HttpStatus.NOT_FOUND);

		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error happened!");
		}
	}

	@RequestMapping(value = "/classes", method = RequestMethod.GET)
	public ResponseEntity<?> getInfoClass(@RequestParam(value = "classID", required = true) int classID) {
		if (classID <= 0) {
			return ResponseEntity.badRequest().body("This id is invalid!");
		}

		Class classInstance = this.classService.getClassInfo(classID);
		if (classInstance != null) {
			return ResponseEntity.ok(classInstance);
		}
		return new ResponseEntity<>("This class is not exist!", HttpStatus.NOT_FOUND);
	}

	@PutMapping("/classes")
	public ResponseEntity<?> updateInfoClass(@RequestParam(value = "classID", required = true) int classID,
			@RequestBody String classInfo) {
		if (classID <= 0) {
			return ResponseEntity.badRequest().body("This id is invalid!");
		}

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			Class classInstance = objectMapper.readValue(classInfo, Class.class);

			if (!this.validationData.validateClassData(classInstance)) {
				return ResponseEntity.badRequest().body("Not enough info!");
			}

			if (classInstance.getId() <= 0) {
				classInstance.setId(classID);
			}

			if (this.classService.updateClassInfo(classInstance)) {
				return ResponseEntity.ok().build();
			}

			return new ResponseEntity<>("This class is not exist!", HttpStatus.NOT_FOUND);

		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error happened!");
		}
	}

	@DeleteMapping("/classes")
	public ResponseEntity<?> deleteInfoClass(@RequestParam(value = "classID", required = true) int classID) {
		if (classID <= 0) {
			return ResponseEntity.badRequest().body("This id is invalid!");
		}

		if (this.classService.deleteClassInfo(classID)) {
			return ResponseEntity.ok().build();
		}
		return ResponseEntity.badRequest().body("This clas is not exist or still have something link to!!");
	}
}
