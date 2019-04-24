package edu.hust.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.hust.model.StudentClass;

@Repository
public interface StudentClassRepository extends JpaRepository<StudentClass, Integer>, CustomStudentClassRepository{

	

}
