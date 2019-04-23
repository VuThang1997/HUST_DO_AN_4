package edu.hust.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import edu.hust.model.Course;
import edu.hust.repository.CourseRepository;

@Service
@Qualifier("CourseServiceImpl1")
public class CourseServiceImpl1 implements CourseService {

	private CourseRepository courseRepository;
	// private ClassRepository classRepository;

	public CourseServiceImpl1() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Autowired
	public CourseServiceImpl1(CourseRepository courseRepository) {
		super();
		this.courseRepository = courseRepository;
		// this.classRepository = classRepository;
	}

	@Override
	public boolean addNewCourse(Course course) {
		// check if another course has use this name
		if (this.courseRepository.findByCourseName(course.getCourseName()).isPresent()) {
			return false;
		}
		this.courseRepository.save(course);
		return true;
	}

	@Override
	public Course getCourseInfo(int id) {
		Optional<Course> course = this.courseRepository.findById(id);
		if (course.isPresent()) {
			return course.get();
		}
		return null;
	}

	@Override
	public boolean updateCourseInfo(Course course) {
		Optional<Course> courseInfo = this.courseRepository.findById(course.getCourseID());
		if (courseInfo.isPresent()) {
			// course name must be unique
			Optional<Course> duplicateCourse = this.courseRepository.findByCourseName(course.getCourseName());
			if (duplicateCourse.isPresent()) {
				return false;
			}

			this.courseRepository.save(course);
			return true;
		}
		return false;
	}

	@Override
	public boolean deleteCourse(int id) {
		try {
			this.courseRepository.deleteById(id);
			return true;
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
