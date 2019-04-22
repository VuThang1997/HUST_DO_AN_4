package edu.hust.service;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import edu.hust.repository.SemesterRepository;

@Service
@Qualifier("SemesterServiceImpl1")
public class SemesterServiceImpl1 implements SemesterService {

	private SemesterRepository semesterRepository;

	public SemesterServiceImpl1() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Autowired
	public SemesterServiceImpl1(SemesterRepository semesterRepository) {
		super();
		this.semesterRepository = semesterRepository;
	}
	
	@Override
	public int getSemesterIDByDate(Date date) {
		Optional<Integer> semesterID = this.semesterRepository.getSemesterIDByTime(date);
		if (semesterID.isPresent()) {
			return semesterID.get().intValue();
		}
		return -1;
	}

	

}
