package edu.hust.service;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import edu.hust.model.Class;
import edu.hust.model.Semester;
import edu.hust.repository.ClassRepository;
import edu.hust.repository.SemesterRepository;

@Service
@Qualifier("ClassServiceImpl1")
public class ClassServiceImpl1 implements ClassService {

	private ClassRepository classRepository;
	private SemesterRepository semesterRepository;

	public ClassServiceImpl1() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Autowired
	public ClassServiceImpl1(ClassRepository classRepository, SemesterRepository semesterRepository) {
		super();
		this.classRepository = classRepository;
		this.semesterRepository = semesterRepository;
	}

	@Override
	public boolean addNewClass(Class classInstance) {
		this.classRepository.save(classInstance);
		return true;

	}

	@Override
	public Class findClassByID(int classID) {
		Optional<Class> classInstance = this.classRepository.findById(classID);
		return (classInstance.isPresent()) ? classInstance.get() : null;
	}

	@Override
	public boolean updateClassInfo(Class classInstance) {
		this.classRepository.save(classInstance);
		return true;
	}

	@Override
	public boolean deleteClassInfo(int classID) {
		try {
			this.classRepository.deleteById(classID);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean checkAddingTime(LocalDate addingDate, int semesterId) {
		// check if semesterId is valid
		Optional<Semester> semester = this.semesterRepository.findById(semesterId);
		if (semester.isEmpty()) {
			return false;
		}

		Semester instance = semester.get();
		if (addingDate.isAfter(instance.getBeginDate())) {
			return false;
		}
		return false;
	}

}
