package edu.hust.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;

import edu.hust.model.StudentClass;

public interface CustomStudentClassRepository {

	@Query("SELECT sc.classInstance.id FROM StudentClass sc WHERE sc.account.id = ?1 AND sc.classInstance.semester.semesterID = ?2")
	List<Integer> getListClass(int studentID, int semesterID);
	
	@Query("SELECT sc FROM StudentClass sc WHERE sc.isLearning = ?3 AND sc.account.id = ?1 AND sc.classInstance.id = ?2")
	Optional<StudentClass> findByStudentIDAndClassIDAndStatus(int studentID, int classID, int isLearning);
}
