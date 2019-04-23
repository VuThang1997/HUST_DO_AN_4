package edu.hust.utils;

import java.util.Map;

import edu.hust.model.Class;
import edu.hust.model.ClassRoom;
import edu.hust.model.Course;
import edu.hust.model.Room;

/**
 * @author BePro
 *
 */
public interface ValidationData {
	
	String validateAccountData(Map<String, Object> mapKeys);
	
	String validateUserData(Map<String, Object> mapKeys);
	
	String validateSemesterData(Map<String, Object> mapKeys);
	
	boolean validateClassData(Class target);
	
	boolean validateRoomData(Room room);

	boolean validateCourseData(Course course);
	
	boolean validateClassRoomData(ClassRoom classRoom);
	
	boolean validateTeacherClassData(int teacherID, int classID, int isTeaching);

	boolean validateStudentClassData(int studentID, int classID, int isLearning);
	
	

}
