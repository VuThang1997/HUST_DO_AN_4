package edu.hust.service;

import edu.hust.model.Class;

public interface ClassService {

	boolean addNewClass(Class classInstance);

	Class getClassInfo(int classID);

	boolean updateClassInfo(Class classInstance);

	boolean deleteClassInfo(int classID);

}
