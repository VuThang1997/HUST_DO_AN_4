package edu.hust.utils;

import java.time.LocalTime;

public interface ValidationClassRoomData {

	String validateIdData(int id);
	
	//String validateBeginDate(LocalDate beginAt);
	
	String validateFinishDate(LocalTime finishAt, LocalTime beginAt);
	
	String validateWeekday(int weekday);
}
