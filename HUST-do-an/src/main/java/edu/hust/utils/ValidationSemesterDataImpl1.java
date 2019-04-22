package edu.hust.utils;

import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("ValidationSemesterDataImpl1")
public class ValidationSemesterDataImpl1 implements ValidationSemesterData {

	@Override
	public String validateIdData(int id) {
		if (id < 1) {
			return "Id can not be less than 1!";
		}
		return null;
	}

	@Override
	public String validateSemesterNameData(String semesterName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String validateBeginDateData(LocalTime beginDate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String validateEndDateData(LocalTime endDate) {
		// TODO Auto-generated method stub
		return null;
	}

}
