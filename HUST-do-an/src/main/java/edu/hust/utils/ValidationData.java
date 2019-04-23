package edu.hust.utils;

import java.util.Map;

import edu.hust.model.ClassRoom;

/**
 * @author BePro
 *
 */
public interface ValidationData {
	
	String validateAccountData(Map<String, Object> mapKeys);
	
	String validateUserData(Map<String, Object> mapKeys);
	
	String validateSemesterData(Map<String, Object> mapKeys);
	
	String validateClassData(Map<String, Object> mapKeys);
	
	String validateRoomData(Map<String, Object> mapKeys);

	String validateCourseData(Map<String, Object> mapKeys);
	
	boolean validateClassRoomData(ClassRoom classRoom);
	
	boolean validateTeacherClassData(int teacherID, int classID, int isTeaching);

	boolean validateStudentClassData(int studentID, int classID, int isLearning);
	
	

}
