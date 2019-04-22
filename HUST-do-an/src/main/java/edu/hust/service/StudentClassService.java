package edu.hust.service;

import java.time.LocalDateTime;
import java.util.List;

import edu.hust.model.ClassRoom;

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
	
	boolean rollCall(int classID, int studentID, LocalDateTime rollCallAt);
}
