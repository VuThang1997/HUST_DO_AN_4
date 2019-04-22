package edu.hust.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.hust.model.Course;

public interface CourseRepository extends JpaRepository<Course, Integer>{

	Optional<Course> findByCourseName(String courseName);

}
