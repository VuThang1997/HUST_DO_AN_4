package edu.hust.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.hust.model.TeacherClass;

@Repository
public interface TeacherClassRepository extends JpaRepository<TeacherClass, Integer>, CustomTeacherClassRepository{

}
