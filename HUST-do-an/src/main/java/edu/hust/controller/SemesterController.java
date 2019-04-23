package edu.hust.controller;

import java.time.LocalDate;
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.hust.model.Semester;
import edu.hust.service.SemesterService;
import edu.hust.utils.JsonMapUtil;
import edu.hust.utils.ValidationData;
import edu.hust.utils.ValidationSemesterData;

@RestController
public class SemesterController {

	private JsonMapUtil jsonMapUtil;
	private ValidationData validationData;
	private SemesterService semesterService;
	private ValidationSemesterData validationSemesterData;

	public SemesterController() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Autowired
	public SemesterController(@Qualifier("SemesterServiceImpl1") SemesterService semesterService,
			@Qualifier("JsonMapUtilImpl1") JsonMapUtil jsonMapUtil,
			@Qualifier("ValidationDataImpl1") ValidationData validationData,
			@Qualifier("ValidationSemesterDataImpl1") ValidationSemesterData validationSemesterData) {
		super();
		this.semesterService = semesterService;
		this.jsonMapUtil = jsonMapUtil;
		this.validationData = validationData;
		this.validationSemesterData = validationSemesterData;
	}

	@RequestMapping(value = "/semesters", method = RequestMethod.POST)
	public ResponseEntity<?> addSemesterInfo(@RequestBody String infoSemester) {
		String errorMessage = null;
		ObjectMapper objectMapper = null;
		Map<String, Object> jsonMap = null;
		Semester semester = null;
		String semesterName = null;
		LocalDate beginDate = null;
		LocalDate endDate = null;

		objectMapper = new ObjectMapper();
		try {
			jsonMap = objectMapper.readValue(infoSemester, new TypeReference<Map<String, Object>>() {
			});

			// check request body has enough info in right JSON format
			if (!this.jsonMapUtil.checkKeysExist(jsonMap, "SemesterName", "BeginDate", "EndDate")) {
				return ResponseEntity.badRequest()
						.body("Error code: 01; Content: Json dynamic map lacks necessary key(s)!");
			}

			errorMessage = this.validationData.validateSemesterData(jsonMap);
			if (errorMessage != null) {
				return ResponseEntity.badRequest()
						.body("Error code: 30; Content: Adding semester failed because " + errorMessage);
			}

			semesterName = jsonMap.get("SemesterName").toString();
			semester = this.semesterService.findSemesterByName(semesterName);
			if (semester != null) {
				return ResponseEntity.badRequest()
						.body("Error code: 31; Content: This semester name has already been used!");
			}

			// check if semester's time begin is duplicate
			beginDate = LocalDate.parse(jsonMap.get("BeginDate").toString());
			if (!this.semesterService.checkSemesterTimeDuplicate(beginDate)) {
				return ResponseEntity.badRequest()
						.body("Error code: 32; Content: This semester's duration is in another semester");
			}

			endDate = LocalDate.parse(jsonMap.get("EndDate").toString());
			if (!this.semesterService.checkSemesterTimeDuplicate(endDate)) {
				return ResponseEntity.badRequest()
						.body("Error code: 32; Content: This semester's duration is in another semester");
			}

			semester = new Semester(semesterName, beginDate, endDate);

			this.semesterService.addNewSemester(semester);
			return ResponseEntity.ok("Adding new semester successfully!");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Error code: 03; Content: Json map get error when putting element(s) !");
		}
	}

	@RequestMapping(value = "/semesters", method = RequestMethod.GET)
	public ResponseEntity<?> getSemesterInfo(
			@RequestParam(value = "SemesterName", required = true) String semesterName) {
		Semester semester = this.semesterService.findSemesterByName(semesterName);
		if (semester == null) {
			return ResponseEntity.badRequest().body("Error code: 33; Content: This semester do not exist!");
		}
		return ResponseEntity.ok(semester);
	}

	@RequestMapping(value = "/semesters", method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteSemester(
			@RequestParam(value = "SemesterName", required = true) String semesterName) {
		String errorMessage = this.validationSemesterData.validateSemesterNameData(semesterName);
		if (errorMessage != null) {
			return ResponseEntity.badRequest()
					.body("Error code: 34; Content: Deleting semester failed because " + errorMessage);
		}
		
		if (this.semesterService.findSemesterByName(semesterName) == null) {
			return ResponseEntity.badRequest().body("Error code: 33; Content: This semester do not exist!");
		}

		if (!this.semesterService.checkSemesterDependant(semesterName)) {
			return ResponseEntity.badRequest().body("Error code: 34; Content: This semester still has dependant!");
		}

		this.semesterService.deleteSemester(semesterName);
		return ResponseEntity.ok("The semester is deleted!");
	}
	
	@RequestMapping(value = "/semesters", method = RequestMethod.PUT)
	public ResponseEntity<?> updateSemesterInfo(@RequestBody String infoSemester) {
		String errorMessage = null;
		ObjectMapper objectMapper = null;
		Map<String, Object> jsonMap = null;
		Semester semester = null;
		String semesterName = null;
		LocalDate beginDate = null;
		LocalDate endDate = null;
		int id = -1;

		objectMapper = new ObjectMapper();
		try {
			jsonMap = objectMapper.readValue(infoSemester, new TypeReference<Map<String, Object>>() {
			});

			// check request body has enough info in right JSON format
			if (!this.jsonMapUtil.checkKeysExist(jsonMap, "ID", "SemesterName", "BeginDate", "EndDate")) {
				return ResponseEntity.badRequest()
						.body("Error code: 01; Content: Json dynamic map lacks necessary key(s)!");
			}

			errorMessage = this.validationData.validateSemesterData(jsonMap);
			if (errorMessage != null) {
				return ResponseEntity.badRequest().body("Error code: 35; Content: Updating semester failed because " + errorMessage);
			}

			semesterName = jsonMap.get("SemesterName").toString();
			semester = this.semesterService.findSemesterByName(semesterName);
			if (semester != null) {
				return ResponseEntity.badRequest()
						.body("Error code: 31; Content: This semester name has already been used!");
			}
			
			id = Integer.parseInt(jsonMap.get("ID").toString());
			semester = this.semesterService.findSemesterById(id);
			if (semester == null) {
				return ResponseEntity.badRequest().body("Error code: 33; Content: This semester do not exist!");
			}

			// check if semester's time begin is duplicate
			beginDate = LocalDate.parse(jsonMap.get("BeginDate").toString());
			if (!this.semesterService.checkSemesterTimeDuplicate(beginDate)) {
				return ResponseEntity.badRequest()
						.body("Error code: 32; Content: This semester's duration is in another semester");
			}

			endDate = LocalDate.parse(jsonMap.get("EndDate").toString());
			if (!this.semesterService.checkSemesterTimeDuplicate(endDate)) {
				return ResponseEntity.badRequest()
						.body("Error code: 32; Content: This semester's duration is in another semester");
			}

			semester = new Semester(id, semesterName, beginDate, endDate);
			this.semesterService.updateSemesterInfo(semester);
			return ResponseEntity.ok("Updating new semester successfully!");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Error code: 03; Content: Json map get error when putting element(s) !");
		}
	}
}
