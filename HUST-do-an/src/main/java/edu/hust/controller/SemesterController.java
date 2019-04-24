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

import edu.hust.model.ReportError;
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
		ReportError report = null;

		objectMapper = new ObjectMapper();
		try {
			jsonMap = objectMapper.readValue(infoSemester, new TypeReference<Map<String, Object>>() {
			});

			// check request body has enough info in right JSON format
			if (!this.jsonMapUtil.checkKeysExist(jsonMap, "semesterName", "beginDate", "endDate")) {
				report = new ReportError(1, "Json dynamic map lacks necessary key(s)!");
				return ResponseEntity.badRequest().body(report);
			}

			errorMessage = this.validationData.validateSemesterData(jsonMap);
			if (errorMessage != null) {
				report = new ReportError(30, "Adding semester failed because " + errorMessage);
				return ResponseEntity.badRequest().body(report);
			}

			semesterName = jsonMap.get("semesterName").toString();
			semester = this.semesterService.findSemesterByName(semesterName);
			if (semester != null) {
				report = new ReportError(31, "This semester name has already been used!");
				return ResponseEntity.badRequest().body(report);
			}

			// check if semester's time begin is duplicate
			beginDate = LocalDate.parse(jsonMap.get("beginDate").toString());
			if (!this.semesterService.checkSemesterTimeDuplicate(beginDate)) {
				report = new ReportError(32, "This semester's duration violate another semester's duration!");
				return ResponseEntity.badRequest().body(report);
			}

			endDate = LocalDate.parse(jsonMap.get("endDate").toString());
			if (!this.semesterService.checkSemesterTimeDuplicate(endDate)) {
				report = new ReportError(32, "This semester's duration violate another semester's duration!");
				return ResponseEntity.badRequest().body(report);
			}

			semester = new Semester(semesterName, beginDate, endDate);

			this.semesterService.addNewSemester(semester);
			return ResponseEntity.ok("Adding new semester successfully!");
		} catch (Exception e) {
			e.printStackTrace();
			report = new ReportError(2, "Error happened when jackson deserialization info!");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, report.toString());
		}
	}

	@RequestMapping(value = "/semesters", method = RequestMethod.GET)
	public ResponseEntity<?> getSemesterInfo(
			@RequestParam(value = "semesterName", required = true) String semesterName) {
		String errorMessage = this.validationSemesterData.validateSemesterNameData(semesterName);
		ReportError report = null;
		
		if (errorMessage != null) {
			report = new ReportError(37, "Getting semester info failed because " + errorMessage);
			return ResponseEntity.badRequest().body(report);
		}
		
		Semester semester = this.semesterService.findSemesterByName(semesterName);
		if (semester == null) {
			report = new ReportError(33, "This semester do not exist yet!");
			return new ResponseEntity<>(report, HttpStatus.NOT_FOUND);
		}
		return ResponseEntity.ok(semester);
	}

	@RequestMapping(value = "/semesters", method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteSemester(
			@RequestParam(value = "semesterName", required = true) String semesterName) {
		String errorMessage = this.validationSemesterData.validateSemesterNameData(semesterName);
		ReportError report = null;
		if (errorMessage != null) {
			report = new ReportError(36, "Deleting semester failed because " + errorMessage);
			return ResponseEntity.badRequest().body(report);
		}
		
		if (this.semesterService.findSemesterByName(semesterName) == null) {
			report = new ReportError(33, "This semester do not exist yet!");
			return new ResponseEntity<>(report, HttpStatus.NOT_FOUND);
		}

		if (!this.semesterService.checkSemesterDependant(semesterName)) {
			report = new ReportError(34, "This semester still has dependant!");
			return ResponseEntity.badRequest().body(report);
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
		ReportError report = null;

		objectMapper = new ObjectMapper();
		try {
			jsonMap = objectMapper.readValue(infoSemester, new TypeReference<Map<String, Object>>() {
			});

			// check request body has enough info in right JSON format
			if (!this.jsonMapUtil.checkKeysExist(jsonMap, "id", "semesterName", "beginDate", "endDate")) {
				report = new ReportError(1, "Json dynamic map lacks necessary key(s)!");
				return ResponseEntity.badRequest().body(report);
			}

			errorMessage = this.validationData.validateSemesterData(jsonMap);
			if (errorMessage != null) {
				report = new ReportError(35, "Updating semester failed because " + errorMessage);
				return ResponseEntity.badRequest().body(report);
			}

			semesterName = jsonMap.get("semesterName").toString();
			semester = this.semesterService.findSemesterByName(semesterName);
			if (semester != null) {
				report = new ReportError(31, "This semester name has already been used!");
				return ResponseEntity.badRequest().body(report);
			}
			
			id = Integer.parseInt(jsonMap.get("id").toString());
			semester = this.semesterService.findSemesterById(id);
			if (semester == null) {
				report = new ReportError(33, "This semester do not exist yet!");
				return new ResponseEntity<>(report, HttpStatus.NOT_FOUND);
			}

			// check if semester's time begin is duplicate
			beginDate = LocalDate.parse(jsonMap.get("beginDate").toString());
			if (!this.semesterService.checkSemesterTimeDuplicate(beginDate)) {
				report = new ReportError(32, "This semester's duration violate another semester's duration!");
				return ResponseEntity.badRequest().body(report);
			}

			endDate = LocalDate.parse(jsonMap.get("endDate").toString());
			if (!this.semesterService.checkSemesterTimeDuplicate(endDate)) {
				report = new ReportError(32, "This semester's duration violate another semester's duration!");
				return ResponseEntity.badRequest().body(report);
			}

			semester = new Semester(id, semesterName, beginDate, endDate);
			this.semesterService.updateSemesterInfo(semester);
			return ResponseEntity.ok("Updating new semester successfully!");
		} catch (Exception e) {
			e.printStackTrace();
			report = new ReportError(2, "Error happened when jackson deserialization info!");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, report.toString());
		}
	}
}
