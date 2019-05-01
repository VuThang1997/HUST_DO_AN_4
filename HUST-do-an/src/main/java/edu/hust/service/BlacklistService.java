package edu.hust.service;

import edu.hust.model.StudentClass;

public interface BlacklistService {

	void createNewRecord(StudentClass studentClass, String fakeImei);

}
