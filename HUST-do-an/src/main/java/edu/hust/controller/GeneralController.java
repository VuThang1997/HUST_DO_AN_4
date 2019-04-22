package edu.hust.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.hust.enumData.AccountRole;
import edu.hust.model.ClassRoom;
import edu.hust.service.SemesterService;
import edu.hust.service.StudentClassService;
import edu.hust.service.TeacherClassService;

@RestController
public class GeneralController {

	private StudentClassService studentClassService;
	private TeacherClassService teacherClassService;
	private SemesterService semesterService;

	public GeneralController() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Autowired
	public GeneralController(@Qualifier("StudentClassServiceImpl1") StudentClassService studentClassService,
			@Qualifier("TeacherClassServiceImpl1") TeacherClassService teacherClassService,
			@Qualifier("SemesterServiceImpl1") SemesterService semesterService) {
		super();
		this.studentClassService = studentClassService;
		this.teacherClassService = teacherClassService;
		this.semesterService = semesterService;
	}

	@GetMapping("/timetable")
	public ResponseEntity<?> getTimeTable(@RequestParam(value = "role", required = true) int role,
			@RequestParam(value = "accountID", required = true) int accountID,
			@RequestParam(value = "semesterID", required = true) int semesterID) {

		if (accountID <= 0 || semesterID < 0) {
			return ResponseEntity.badRequest().body("This id is invalid!");
		}

		// truong hop vua login xong thi lay server time de tim semester hien tai
		if (semesterID == 0) {
			Calendar calendar = new GregorianCalendar();
			Date currentTime = calendar.getTime();
			System.out.println("\n\ncurrent time = " + currentTime + "\n\n");
			semesterID = this.semesterService.getSemesterIDByDate(currentTime);
			if (semesterID == -1) {
				return new ResponseEntity<>("No record is found!", HttpStatus.NOT_FOUND);
			}
		}

		List<ClassRoom> listClassRoom = null;
		if (role == AccountRole.STUDENT.getValue()) {
			listClassRoom = this.studentClassService.getTimeTable(accountID, semesterID);

		} else if (role == AccountRole.TEACHER.getValue()) {
			listClassRoom = this.teacherClassService.getTimeTable(accountID, semesterID);
		}

		if (listClassRoom == null) {
			return new ResponseEntity<>("This room is not existed", HttpStatus.NOT_FOUND);
		}

		return ResponseEntity.ok(listClassRoom);
	}
}
