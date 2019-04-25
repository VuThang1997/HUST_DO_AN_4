package edu.hust.service;

import java.time.LocalDate;

import edu.hust.model.Semester;

public interface SemesterService {
	
	int getSemesterIDByDate(LocalDate currentDate);

	Semester findSemesterByName(String semesterName);

	void addNewSemester(Semester semester);

	boolean checkSemesterTimeDuplicate(LocalDate beginDate);

	boolean checkSemesterDependant(String semesterName);

	void deleteSemester(String semesterName);

	Semester findSemesterById(int id);

	void updateSemesterInfo(Semester semester);
}
