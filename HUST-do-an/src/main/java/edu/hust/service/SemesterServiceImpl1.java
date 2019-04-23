package edu.hust.service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import edu.hust.model.Semester;
import edu.hust.model.Class;
import edu.hust.repository.ClassRepository;
import edu.hust.repository.SemesterRepository;

@Service
@Qualifier("SemesterServiceImpl1")
public class SemesterServiceImpl1 implements SemesterService {

	private SemesterRepository semesterRepository;
	private ClassRepository classRepository;

	public SemesterServiceImpl1() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Autowired
	public SemesterServiceImpl1(SemesterRepository semesterRepository, ClassRepository classRepository) {
		super();
		this.semesterRepository = semesterRepository;
		this.classRepository = classRepository;
	}
	
	@Override
	public int getSemesterIDByDate(Date date) {
		Optional<Integer> semesterID = this.semesterRepository.getSemesterIDByTime(date);
		if (semesterID.isPresent()) {
			return semesterID.get().intValue();
		}
		return -1;
	}

	@Override
	public Semester findSemesterByName(String semesterName) {
		Optional<Semester> semester = this.semesterRepository.findBySemesterName(semesterName);
		if (semester.isPresent()) {
			return semester.get();
		}
		return null;
	}

	@Override
	public void addNewSemester(Semester semester) {
		this.semesterRepository.save(semester);
		return;
	}

	@Override
	public boolean checkSemesterTimeDuplicate(LocalDate beginDate) {
		List<Semester> listSemester = this.semesterRepository.checkSemesterDuplicate(beginDate);
		if (listSemester == null || listSemester.isEmpty()) {
			return true;
		}
		return false;
	}

	@Override
	public boolean checkSemesterDependant(String semesterName) {
		List<Class> listClass = this.classRepository.findBySemesterName(semesterName);
		if (listClass == null || listClass.isEmpty()) {
			return true;
		}
		return false;
	}

	@Override
	public void deleteSemester(String semesterName) {
		this.semesterRepository.deleteBySemesterName(semesterName);
		return;
	}

	@Override
	public Semester findSemesterById(int id) {
		Optional<Semester> semester = this.semesterRepository.findById(id);
		if (semester.isPresent()) {
			return semester.get();
		}
		return null;
	}

	@Override
	public void updateSemesterInfo(Semester semester) {
		this.semesterRepository.save(semester);
		return;
		
	}

}
