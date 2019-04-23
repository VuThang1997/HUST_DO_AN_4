package edu.hust.utils;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import edu.hust.enumData.IsLearning;
import edu.hust.enumData.IsTeaching;
import edu.hust.model.Class;
import edu.hust.model.ClassRoom;
import edu.hust.model.Room;

@Component
@Qualifier("ValidationDataImpl1")
public class ValidationDataImpl1 implements ValidationData {

	private ValidationAccountData validationAccountData;
	private ValidationUserData validationUserData;
	private ValidationSemesterData validationSemesterData;
	private ValidationCourseData validationCourseData;

	public ValidationDataImpl1() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Autowired
	public ValidationDataImpl1(@Qualifier("ValidationAccountDataImpl1") ValidationAccountData validationAccountData,
			@Qualifier("ValidationUserDataImpl1") ValidationUserData validationUserData,
			@Qualifier("ValidationSemesterDataImpl1") ValidationSemesterData validationSemesterData,
			@Qualifier("ValidationCourseDataImpl1") ValidationCourseData validationCourseData) {
		super();
		this.validationAccountData = validationAccountData;
		this.validationUserData = validationUserData;
		this.validationSemesterData = validationSemesterData;
		this.validationCourseData = validationCourseData;
	}

	@Override
	public String validateAccountData(Map<String, Object> mapKeys) {
		String errorMessage = null;

		if (mapKeys.containsKey("ID")) {
			int id = Integer.parseInt(mapKeys.get("ID").toString());
			errorMessage = this.validationAccountData.validateIdData(id);
		}

		if (errorMessage == null && mapKeys.containsKey("Username")) {
			errorMessage = this.validationAccountData.validateUsernameData(mapKeys.get("Username").toString());
		}

		if (errorMessage == null && mapKeys.containsKey("Password")) {
			;
			errorMessage = this.validationAccountData.validatePasswordData(mapKeys.get("Password").toString());
		}

		if (errorMessage == null && mapKeys.containsKey("Imei")) {
			errorMessage = this.validationAccountData.validateImeiData(mapKeys.get("Imei").toString());
		}

		if (errorMessage == null && mapKeys.containsKey("Email")) {
			errorMessage = this.validationAccountData.validateEmailData(mapKeys.get("Email").toString());
		}

		if (errorMessage == null && mapKeys.containsKey("Role")) {
			int role = Integer.parseInt(mapKeys.get("Role").toString());
			errorMessage = this.validationAccountData.validateRoleData(role);
		}

		return errorMessage;
	}

	@Override
	public String validateUserData(Map<String, Object> mapKeys) {
		String errorMessage = null;

		if (mapKeys.containsKey("ID")) {
			int id = Integer.parseInt(mapKeys.get("ID").toString());
			errorMessage = this.validationUserData.validateIdData(id);
		}

		if (errorMessage == null && mapKeys.containsKey("Address")) {
			errorMessage = this.validationUserData.validateAddressData(mapKeys.get("Address").toString());
		}
		
		if (errorMessage == null && mapKeys.containsKey("FullName")) {
			errorMessage = this.validationUserData.validateFullNameData(mapKeys.get("FullName").toString());
		}

		if (errorMessage == null && mapKeys.containsKey("Birthday")) {
			LocalDate birthday = LocalDate.parse(mapKeys.get("Birthday").toString());
			errorMessage = this.validationUserData.validateBirthdayData(birthday);
		}

		if (errorMessage == null && mapKeys.containsKey("Phone")) {
			errorMessage = this.validationUserData.validatePhoneData(mapKeys.get("Phone").toString());
		}

		return errorMessage;
	}
	
	@Override
	public String validateSemesterData(Map<String, Object> mapKeys) {
		String errorMessage = null;
		String semesterName = null;
		LocalDate beginDate = null;
		LocalDate endDate = null;
		
		if (mapKeys.containsKey("ID")) {
			int id = Integer.parseInt(mapKeys.get("ID").toString());
			errorMessage = this.validationSemesterData.validateIdData(id);
		}
		
		if (errorMessage == null) {
			try {
				semesterName = mapKeys.get("SemesterName").toString();
				beginDate = LocalDate.parse(mapKeys.get("BeginDate").toString());
				endDate = LocalDate.parse(mapKeys.get("EndDate").toString());
				errorMessage = this.validationSemesterData.validateSemesterNameData(semesterName);
				if (errorMessage == null) {
					int validYear = Integer.parseInt(semesterName.substring(0, 4));
					errorMessage = this.validationSemesterData.validateBeginDateData(beginDate, validYear);
				}
				if (errorMessage == null) {
					int sequenceOfSemester = Integer.parseInt(semesterName.substring(4));
					errorMessage = this.validationSemesterData.validateEndDateData(endDate, beginDate, sequenceOfSemester);
				}
				
			} catch (DateTimeParseException e) {
				e.printStackTrace();
				return "Cannot parse BeginDate info!";
			}
		}
		
		return errorMessage;
	}
	
	@Override
	public String validateCourseData(Map<String, Object> mapKeys) {
		String errorMessage = null;

		if (mapKeys.containsKey("ID")) {
			int id = Integer.parseInt(mapKeys.get("ID").toString());
			errorMessage = this.validationCourseData.validateIdData(id);
		}

		if (errorMessage == null && mapKeys.containsKey("CourseName")) {
			errorMessage = this.validationCourseData.validateCourseNameData(mapKeys.get("CourseName").toString());
		}
		
		return errorMessage;
	}

	@Override
	public boolean validateClassData(Class target) {
		if (target.getClassName() == null || target.getMaxStudent() <= 0 || target.getSemester() == null
				|| target.getCourse() == null) {
			return false;
		}
		return true;
	}

	@Override
	public boolean validateRoomData(Room room) {
		double gpsLa = room.getGpsLatitude();
		double gpsLong = room.getGpsLongitude();
		if (room.getAddress() == null || gpsLa < GeneralValue.minLatitude || gpsLa > GeneralValue.maxLatitude
				|| gpsLong < GeneralValue.minLongitude || gpsLong > GeneralValue.maxLongitude) {
			return false;
		}
		return true;
	}

	@Override
	public boolean validateClassRoomData(ClassRoom classRoom) {
		if (classRoom.getWeekday() < 2 || classRoom.getWeekday() > 6) {
			return false;
		}

		/*
		 * //check if timeSlots is valid String timeSlots = classRoom.getTimeSlots(); if
		 * (timeSlots == null) { return false; } else { String[] slots =
		 * timeSlots.split(","); try { int[] numberSlots =
		 * Arrays.stream(slots).mapToInt(Integer::parseInt).toArray(); int element = -1;
		 * for (int i = 0; i < numberSlots.length; i++) { element = numberSlots[i]; if
		 * (element < 1 || element > 12) { return false; } } } catch(Exception e) {
		 * e.printStackTrace(); return false; } }
		 */

		Class classInstance = classRoom.getClassInstance();
		if (classInstance == null || classInstance.getId() <= 0) {
			return false;
		}

		Room room = classRoom.getRoom();
		if (room == null || room.getId() < 1) {
			return false;
		}

		return true;
	}

	@Override
	public boolean validateTeacherClassData(int teacherID, int classID, int isTeaching) {
		if (teacherID < 1 || classID < 1) {
			return false;
		}
		boolean validFlag = false;
		for (IsTeaching value : IsTeaching.values()) {
			if (isTeaching == value.getValue()) {
				validFlag = true;
				break;
			}
		}

		if (validFlag == true) {
			return true;
		}
		return false;
	}

	@Override
	public boolean validateStudentClassData(int studentID, int classID, int isLearning) {
		if (studentID < 1 || classID < 1) {
			return false;
		}

		boolean validFlag = false;
		for (IsLearning value : IsLearning.values()) {
			if (isLearning == value.getValue()) {
				validFlag = true;
				break;
			}
		}

		if (validFlag == true) {
			return true;
		}

		return false;
	}
}
