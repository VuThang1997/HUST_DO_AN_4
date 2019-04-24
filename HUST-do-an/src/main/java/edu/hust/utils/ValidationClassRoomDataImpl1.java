package edu.hust.utils;

import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("ValidationClassRoomDataImpl1")
public class ValidationClassRoomDataImpl1 implements ValidationClassRoomData {

	@Override
	public String validateIdData(int id) {
		if (id < 1) {
			return "Id can not be less than 1!";
		}
		return null;
	}

	/*
	 * @Override public String validateBeginDate(LocalDate beginAt) { // TODO
	 * Auto-generated method stub return null; }
	 */

	@Override
	public String validateFinishDate(LocalTime finishAt, LocalTime beginAt) {
		if (finishAt.isBefore(beginAt)) {
			return "finishAt cannot be before beginAt";
		}
		return null;
	}

	@Override
	public String validateWeekday(int weekday) {
		if (weekday < 2 || weekday > 6) {
			return "weekday is out of valid range";
		}
		return null;
	}

}
