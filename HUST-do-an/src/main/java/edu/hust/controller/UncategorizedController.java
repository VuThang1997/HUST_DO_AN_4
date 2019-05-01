package edu.hust.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.hust.enumData.AccountRole;
import edu.hust.enumData.AfternoonTimeFrame;
import edu.hust.model.ClassRoom;
import edu.hust.model.ReportError;
import edu.hust.model.StudentClass;
import edu.hust.model.TeacherClass;
import edu.hust.service.ClassRoomService;
import edu.hust.service.SemesterService;
import edu.hust.service.StudentClassService;
import edu.hust.service.TeacherClassService;
import edu.hust.utils.GeneralValue;
import edu.hust.utils.ValidationAccountData;
import edu.hust.utils.ValidationSemesterData;

@RestController
public class UncategorizedController {

	private StudentClassService studentClassService;
	private TeacherClassService teacherClassService;
	private SemesterService semesterService;
	private ClassRoomService classRoomService;
	private ValidationAccountData validationAccountData;
	private ValidationSemesterData validationSemesterData;

	public UncategorizedController() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Autowired
	public UncategorizedController(@Qualifier("StudentClassServiceImpl1") StudentClassService studentClassService,
			@Qualifier("TeacherClassServiceImpl1") TeacherClassService teacherClassService,
			@Qualifier("SemesterServiceImpl1") SemesterService semesterService,
			@Qualifier("ValidationSemesterDataImpl1") ValidationSemesterData validationSemesterData,
			@Qualifier("ValidationAccountDataImpl1") ValidationAccountData validationAccountData,
			@Qualifier("ClassRoomServiceImpl1") ClassRoomService classRoomService) {
		super();
		this.studentClassService = studentClassService;
		this.teacherClassService = teacherClassService;
		this.semesterService = semesterService;
		this.validationAccountData = validationAccountData;
		this.validationSemesterData = validationSemesterData;
		this.classRoomService = classRoomService;
	}

	@GetMapping("/timetable")
	public ResponseEntity<?> getTimeTable(@RequestParam(value = "role", required = true) int role,
			@RequestParam(value = "accountID", required = true) int accountID,
			@RequestParam(value = "semesterID", required = true) int semesterID) {

		ReportError report = null;
		String errorMessage = null;

		errorMessage = this.validationAccountData.validateIdData(accountID);
		if (errorMessage != null) {
			report = new ReportError(100, "Getting timetable failed because " + errorMessage);
			return ResponseEntity.badRequest().body(report);
		}

		errorMessage = this.validationAccountData.validateRoleData(role);
		if (errorMessage != null) {
			report = new ReportError(100, "Getting timetable failed because " + errorMessage);
			return ResponseEntity.badRequest().body(report);
		}

		errorMessage = this.validationSemesterData.validateIdData(semesterID);
		if (errorMessage != null) {
			report = new ReportError(100, "Getting timetable failed because " + errorMessage);
			return ResponseEntity.badRequest().body(report);
		}

		// truong hop vua login xong thi lay server time de tim semester hien tai
		if (semesterID == 0) {
			LocalDate currentDate = LocalDate.now();
			semesterID = this.semesterService.getSemesterIDByDate(currentDate);
			if (semesterID == -1) {
				report = new ReportError(33, "This semester do not exist yet!");
				return new ResponseEntity<>(report, HttpStatus.NOT_FOUND);
			}
		}

		List<ClassRoom> listClassRoom = null;
		if (role == AccountRole.STUDENT.getValue()) {
			listClassRoom = this.studentClassService.getTimeTable(accountID, semesterID);

		} else if (role == AccountRole.TEACHER.getValue()) {
			listClassRoom = this.teacherClassService.getTimeTable(accountID, semesterID);
		}

		if (listClassRoom == null) {
			report = new ReportError(101, "No record for this semester!");
			return new ResponseEntity<>(report, HttpStatus.NOT_FOUND);
		}

		return ResponseEntity.ok(listClassRoom);
	}

	@PostMapping("/checkTeacherRollCallToday")
	public ResponseEntity<?> checkTeacherRollCallToday(@RequestParam(value = "role", required = true) int role) {
		int currentDay = -1;
		int indexOf = -1;
		boolean flag = false;
		LocalTime tmpTime = null;
		ReportError report = null;
		String errorMessage = null;
		String tmpString = null;
		String[] rollCallList;
		String rollCallString = null;
		String newRollCallList = null;
		String regexDate = null;
		TeacherClass teacherClass = null;
		LocalDateTime now = null;
		List<ClassRoom> listClassRoom = null;

		// only admins have permission to access this API errorMessage =
		errorMessage = this.validationAccountData.validateRoleData(role);
		if (errorMessage != null || role != AccountRole.ADMIN.getValue()) {
			report = new ReportError(11, "Authentication has failed or has not yet been provided!");
			return new ResponseEntity<>(report, HttpStatus.UNAUTHORIZED);
		}

		now = LocalDateTime.now(); // currentTime = now.toLocalTime();
		if (now.toLocalTime().isBefore(AfternoonTimeFrame.FRAME12.getValue())) {
			report = new ReportError(102, "Check roll call today failed because of current time is not valid! ");
			return ResponseEntity.badRequest().body(report);
		}

		// Notice: weekday of java = weekday of mySQL - 1
		currentDay = now.getDayOfWeek().getValue() + 1;
		listClassRoom = this.classRoomService.findClassRoomByWeekday(currentDay);
		if (listClassRoom == null) {
			report = new ReportError(103, "No class has lessons today!");
			return new ResponseEntity<>(report, HttpStatus.NOT_FOUND);
		}

		regexDate = now.getYear() + GeneralValue.regexForSplitDate + now.getDayOfYear()
				+ GeneralValue.regexForSplitDate;
		errorMessage = now.getYear() + GeneralValue.regexForSplitDate + now.getDayOfYear()
				+ GeneralValue.regexForSplitDate + GeneralValue.markForMissingRollCall
				+ GeneralValue.regexForSplitListRollCall;

		for (ClassRoom classRoom : listClassRoom) {
			flag = false;
			newRollCallList = null;
			rollCallString = null;

			teacherClass = this.teacherClassService.findCurrentTeacherByClassID(classRoom.getClassInstance().getId());
			if (teacherClass.getListRollCall() != null && !teacherClass.getListRollCall().isBlank()) {
				indexOf = teacherClass.getListRollCall().indexOf(regexDate);

				// if list has no record for today => not roll call
				if (indexOf == -1) {
					teacherClass.setListRollCall(teacherClass.getListRollCall().concat(errorMessage));
					this.teacherClassService.updateTeacherClass(teacherClass);

				} else {
					rollCallString = teacherClass.getListRollCall().substring(indexOf);
					rollCallList = rollCallString.split(GeneralValue.regexForSplitListRollCall);
					rollCallString = "";

					for (int i = 0; i < rollCallList.length; i++) {
						if (rollCallList[i].contains(GeneralValue.markForMissingRollCall)) {
							rollCallString += rollCallList[i] + GeneralValue.regexForSplitListRollCall;
							continue;
						}
						// max second of a day is 86400 = 5 number; at 7am second = 25200 = 5 number
						tmpString = rollCallList[i].substring(rollCallList[i].length() - 5);
						tmpTime = LocalTime.ofSecondOfDay(Integer.parseInt(tmpString));

						// if this lesson has been rolled call
						if (tmpTime.isAfter(classRoom.getBeginAt()) && tmpTime.isBefore(classRoom.getFinishAt())) {
							rollCallString = null;
							flag = true;
							break;

						} else if (tmpTime.isAfter(classRoom.getFinishAt())) {
							// this lessons is missing roll call
							rollCallString = rollCallString.concat(errorMessage);
							for (; i < rollCallList.length; i++) {
								rollCallString += rollCallList[i] + GeneralValue.regexForSplitListRollCall;
							}
							flag = true;
							break;

						} else {
							rollCallString += rollCallList[i] + GeneralValue.regexForSplitListRollCall;
						}
					}

					// if the missing lesson is the last frame
					if (flag == false) {
						rollCallString = rollCallString.concat(errorMessage);
					}

					System.out.println("\n\n indexOf = " + indexOf);
					// TH list rong
					if (rollCallString == null || rollCallString.isBlank()) {
						newRollCallList = null;
					} else {
						newRollCallList = teacherClass.getListRollCall().substring(0, indexOf).concat(rollCallString);
					}

					if (newRollCallList != null) {
						teacherClass.setListRollCall(newRollCallList);
						this.teacherClassService.updateTeacherClass(teacherClass);
					}
				}

			} else {
				teacherClass.setListRollCall(errorMessage);
				this.teacherClassService.updateTeacherClass(teacherClass);
			}
		}
		return ResponseEntity.ok("Check teacher roll call today complete!");
	}

	@SuppressWarnings("deprecation")
	@PostMapping("/checkStudentRollCallToday")
	public ResponseEntity<?> checkStudentRollCallToday(@RequestParam(value = "role", required = true) int role) {
		int currentDay = -1;
		int indexOf = -1;
		boolean isTeacheRollCalled = false;
		boolean flag = true;
		LocalTime tmpTime = null;
		ReportError report = null;
		String message1 = null;
		String message2 = null;
		String tmpString = null;
		String[] rollCallList;
		String rollCallString = null;
		String newRollCallList = null;
		String regexDate = null;
		TeacherClass teacherClass = null;
		LocalDateTime now = null;
		List<ClassRoom> listClassRoom = null;
		List<StudentClass> listStudentClass = null;
		Map<ClassRoom, Boolean> mapClassRoomRollCalled = null;
		ClassRoom instanceClassRoom = null;

		// only admins have permission to access this API
		message2 = this.validationAccountData.validateRoleData(role);
		if (message2 != null || role != AccountRole.ADMIN.getValue()) {
			report = new ReportError(11, "Authentication has failed or has not yet been provided!");
			return new ResponseEntity<>(report, HttpStatus.UNAUTHORIZED);
		}

		now = LocalDateTime.now(); // currentTime = now.toLocalTime();
		if (now.toLocalTime().isBefore(AfternoonTimeFrame.FRAME12.getValue())) {
			report = new ReportError(102, "Check roll call today failed because of current time is not valid! ");
			return ResponseEntity.badRequest().body(report);
		}

		// Notice: weekday of java = weekday of mySQL - 1
		currentDay = now.getDayOfWeek().getValue() + 1;
		listClassRoom = this.classRoomService.findClassRoomByWeekday(currentDay);
		if (listClassRoom == null) {
			report = new ReportError(103, "No class has lessons today!");
			return new ResponseEntity<>(report, HttpStatus.NOT_FOUND);
		}

		mapClassRoomRollCalled = new HashMap<>();
		regexDate = now.getYear() + GeneralValue.regexForSplitDate + now.getDayOfYear()
				+ GeneralValue.regexForSplitDate;
		message1 = now.getYear() + GeneralValue.regexForSplitDate + now.getDayOfYear() + GeneralValue.regexForSplitDate
				+ GeneralValue.markForMissingRollCall + GeneralValue.regexForSplitListRollCall;
		message2 = now.getYear() + GeneralValue.regexForSplitDate + now.getDayOfYear() + GeneralValue.regexForSplitDate
				+ GeneralValue.markForTeacherMissing + GeneralValue.regexForSplitListRollCall;

		for (ClassRoom classRoom : listClassRoom) {
			isTeacheRollCalled = false;
			rollCallString = "";

			teacherClass = this.teacherClassService.findCurrentTeacherByClassID(classRoom.getClassInstance().getId());
			indexOf = teacherClass.getListRollCall().indexOf(regexDate);

			// this API is called after checkTeacherRollCallToday => this situation
			// shouldn't occur
			if (indexOf == -1) {
				report = new ReportError(104, "This API must be called after calling checkTeacherRollCallToday API!");
				return ResponseEntity.badRequest().body(report);

			} else {
				rollCallString = teacherClass.getListRollCall().substring(indexOf);
				rollCallList = rollCallString.split(GeneralValue.regexForSplitListRollCall);

				for (int i = 0; i < rollCallList.length; i++) {
					if (rollCallList[i].contains(GeneralValue.markForMissingRollCall)) {
						continue;
					}

					// max second of a day is 86400 = 5 number; at 7am second = 25200 = 5 number
					tmpString = rollCallList[i].substring(rollCallList[i].length() - 5);
					tmpTime = LocalTime.ofSecondOfDay(Integer.parseInt(tmpString));

					// this lesson was roll called
					if (tmpTime.isAfter(classRoom.getBeginAt()) && tmpTime.isBefore(classRoom.getFinishAt())) {
						isTeacheRollCalled = true;
						break;
					}

					// it mean teacher missing roll called this lesson
					else if (tmpTime.isAfter(classRoom.getFinishAt())) {
						isTeacheRollCalled = false;
						break;
					}
				}
				mapClassRoomRollCalled.put(classRoom, new Boolean(isTeacheRollCalled));
			}
		}

		for (Map.Entry<ClassRoom, Boolean> entry : mapClassRoomRollCalled.entrySet()) {
			isTeacheRollCalled = entry.getValue().booleanValue();

			instanceClassRoom = entry.getKey();
			listStudentClass = this.studentClassService
					.findCurrentStudentsByClassID(instanceClassRoom.getClassInstance().getId());

			if (listStudentClass == null || listStudentClass.isEmpty()) {
				continue;
			}

			for (StudentClass studentClass : listStudentClass) {
				flag = false;
				// rollCallString = "";
				indexOf = studentClass.getListRollCall().indexOf(regexDate);

				// student has no roll call today
				if (indexOf == -1) {
					if (isTeacheRollCalled == false) {
						newRollCallList = studentClass.getListRollCall().concat(message2);
					} else {
						newRollCallList = studentClass.getListRollCall().concat(message1);
					}

					studentClass.setListRollCall(newRollCallList);
					this.studentClassService.updateStudentClassInfo(studentClass);

				} else {
					rollCallString = studentClass.getListRollCall().substring(indexOf);
					rollCallList = rollCallString.split(GeneralValue.regexForSplitListRollCall);
					rollCallString = "";

					for (int i = 0; i < rollCallList.length; i++) {
						if (rollCallList[i].contains(GeneralValue.markForMissingRollCall)
								|| rollCallList[i].contains(GeneralValue.markForTeacherMissing)) {
							rollCallString += rollCallList[i] + GeneralValue.regexForSplitListRollCall;
							continue;
						}

						else {
							if (rollCallList[i].contains(GeneralValue.markForPermission)
									|| rollCallList[i].contains(GeneralValue.markForNotBringPhone)) {
								// max second of a day is 86400 = 5 number; at 7am second = 25200 = 5 number
								// however, this string contains a special mark => Both beginIndex and EndIndex
								// plus 1
								tmpString = rollCallList[i].substring(rollCallList[i].length() - 6,
										rollCallList[i].length() - 1);

							} else {
								// max second of a day is 86400 = 5 number; at 7am second = 25200 = 5 number
								tmpString = rollCallList[i].substring(rollCallList[i].length() - 5);
							}

							tmpTime = LocalTime.ofSecondOfDay(Integer.parseInt(tmpString));

							// this lesson was roll called
							if (tmpTime.isAfter(instanceClassRoom.getBeginAt())
									&& tmpTime.isBefore(instanceClassRoom.getFinishAt())) {
								rollCallString = null;
								flag = true;
								break;
							}

							// this lessons is missing roll call
							else if (tmpTime.isAfter(instanceClassRoom.getFinishAt())) {
								if (isTeacheRollCalled == false) {
									rollCallString = rollCallString.concat(message2);
								} else {
									rollCallString = rollCallString.concat(message1);
								}

								for (; i < rollCallList.length; i++) {
									rollCallString += rollCallList[i] + GeneralValue.regexForSplitListRollCall;
								}
								flag = true;
								break;

							} else {
								rollCallString += rollCallList[i] + GeneralValue.regexForSplitListRollCall;
							}
						}
					}

					// if the missing lesson is the last frame
					if (flag == false) {
						if (isTeacheRollCalled == false) {
							rollCallString = rollCallString.concat(message2);
						} else {
							rollCallString = rollCallString.concat(message1);
						}
					}

					if (rollCallString == null || rollCallString.isBlank()) {
						newRollCallList = null;
					} else {
						newRollCallList = teacherClass.getListRollCall().substring(0, indexOf).concat(rollCallString);
					}

					if (newRollCallList != null) {
						studentClass.setListRollCall(newRollCallList);
						this.studentClassService.updateStudentClassInfo(studentClass);
					}
				}
			}
		}

		return ResponseEntity.ok("Check student roll call today complete!");
	}
}
