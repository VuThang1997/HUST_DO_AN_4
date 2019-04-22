package edu.hust.utils;

import java.time.LocalTime;

public interface ValidationSemesterData {

	String validateIdData(int id);
	
	String validateSemesterNameData(String semesterName);
	
	String validateBeginDateData(LocalTime beginDate);
	
	String validateEndDateData(LocalTime endDate);
}
