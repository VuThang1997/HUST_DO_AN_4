package edu.hust.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import edu.hust.model.Class;
import edu.hust.model.Course;
import edu.hust.model.Semester;
import edu.hust.repository.ClassRepository;
import edu.hust.repository.CourseRepository;
import edu.hust.repository.SemesterRepository;

@Service
@Qualifier("ClassServiceImpl1")
public class ClassServiceImpl1 implements ClassService {

	private ClassRepository classRepository;
	private CourseRepository courseRepository;
	private SemesterRepository semesterRepository;

	public ClassServiceImpl1() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	@Autowired
	public ClassServiceImpl1(ClassRepository classRepository, CourseRepository courseRepository,
			SemesterRepository semesterRepository) {
		super();
		this.classRepository = classRepository;
		this.courseRepository = courseRepository;
		this.semesterRepository = semesterRepository;
	}

	@Override
	public boolean addNewClass(Class classInstance) {
		int courseID = classInstance.getCourse().getCourseID();
		int semesterID = classInstance.getSemester().getSemesterID();
		
		if (!this.courseRepository.existsById(courseID) || !this.semesterRepository.existsById(semesterID)) {
			return false;
		}

		this.classRepository.save(classInstance);
		return true;
		
	}

	@Override
	public Class getClassInfo(int classID) {
		Optional<Class> classInstance = this.classRepository.findById(classID);
		return (classInstance.isPresent()) ? classInstance.get() : null;
	}

	@Override
	public boolean updateClassInfo(Class classInstance) {
		Optional<Class> oldInfo = this.classRepository.findById(classInstance.getId());
		if (oldInfo.isPresent()) {
			Class target = oldInfo.get();
			
			if (classInstance.getClassName() != null) {
				target.setClassName(classInstance.getClassName());
			}
			
			if (classInstance.getMaxStudent() > 0) {
				target.setMaxStudent(classInstance.getMaxStudent());
			}
			
			if (classInstance.getSemester() != null) {
				Semester semester = classInstance.getSemester();
				if (this.semesterRepository.existsById(semester.getSemesterID())) {
					target.setSemester(semester);
				}
			}
			
			if (classInstance.getCourse() != null) {
				Course course = classInstance.getCourse();
				if (this.courseRepository.existsById(course.getCourseID())) {
					target.setCourse(course);
				}
			}
			
			//ko check va update list classroom
			
			this.classRepository.save(target);
			return true;
		}
		return false;
	}

	@Override
	public boolean deleteClassInfo(int classID) {
		if (this.classRepository.existsById(classID)) {
			try {
				this.classRepository.deleteById(classID);
				return true;
			} catch(Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}

}
