package edu.hust.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import edu.hust.model.ClassRoom;
import edu.hust.model.StudentClass;

/**
 * @author BePro
 *
 */
public interface StudentClassService {

	/**
	 * @param studentID
	 * @param semesterID
	 * @return a list of ClassRoom that is learned in this semester by student
	 */
	List<ClassRoom> getTimeTable(int studentID, int semesterID);

	/**
	 * @param studentID
	 * @param classID
	 * @param identifyString
	 * @return true if server can find a record with the above fields
	 */
	boolean checkStudentHasAuthority(int studentID, int classID, int roomID, String identifyString, String imei);

	//boolean checkGenerateTimeValid(int weekday, LocalTime generateTime, int classID, int roomID);
	
	String rollCall(int classID, int studentID, LocalDateTime rollCallAt, String imei);

	List<StudentClass> findByClassID(int id);
	
	boolean updateStudentClassInfo(StudentClass studentClass);

	List<StudentClass> findCurrentStudentsByClassID(int id);

	boolean checkStudentIsLearning(int studentID, int classID);
	
	boolean checkIsCheckedValid(String isChecked, LocalTime beginAt, LocalTime finishAt);

	boolean checkStudentIsLearning(String studentEmail, int classID);
}
