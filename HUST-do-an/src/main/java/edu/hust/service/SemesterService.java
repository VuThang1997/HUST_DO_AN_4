package edu.hust.service;

import java.time.LocalDate;
import java.util.Date;

import edu.hust.model.Semester;

public interface SemesterService {
	
	int getSemesterIDByDate(Date date);

	Semester findSemesterByName(String semesterName);

	void addNewSemester(Semester semester);

	boolean checkSemesterTimeDuplicate(LocalDate beginDate);

	boolean checkSemesterDependant(String semesterName);

	void deleteSemester(String semesterName);

	Semester findSemesterById(int id);

	void updateSemesterInfo(Semester semester);
}
