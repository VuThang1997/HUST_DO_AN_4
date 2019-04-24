package edu.hust.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;

import edu.hust.model.TeacherClass;

public interface CustomTeacherClassRepository {

	@Query("SELECT tc.classInstance.id FROM TeacherClass tc WHERE tc.account.id = ?1 AND tc.classInstance.semester.semesterID = ?2")
	List<Integer> getListClass(int teacherID, int semesterID);

	@Query("SELECT tc FROM TeacherClass tc WHERE tc.isTeaching = ?3 AND tc.account.id = ?1 AND tc.classInstance.id = ?2")
	Optional<TeacherClass> findByTeacherIDAndClassIDAndStatus(int teacherID, int classID, int isTeaching);
	
	@Query("SELECT tc FROM TeacherClass tc WHERE tc.classInstance.id = ?1")
	List<TeacherClass> getListTeacherClass(int id);
}
