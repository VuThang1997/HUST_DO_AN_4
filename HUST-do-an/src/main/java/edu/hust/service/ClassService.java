package edu.hust.service;

import java.time.LocalDate;

import edu.hust.model.Class;

public interface ClassService {

	boolean addNewClass(Class classInstance);

	Class findClassByID(int classID);

	boolean updateClassInfo(Class classInstance);

	boolean deleteClassInfo(int classID);

	boolean checkAddingTime(LocalDate addingDate, int semesterId);

}
