package edu.hust.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import edu.hust.model.Class;

@Repository
public interface ClassRepository extends JpaRepository<Class, Integer>, CustomClassRepository{

	@Query("SELECT cl FROM Class cl WHERE cl.semester.semesterName = ?1")
	List<Class> findBySemesterName(String semesterName);
}
