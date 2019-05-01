package edu.hust.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import edu.hust.enumData.IsLearning;
import edu.hust.model.ClassRoom;
import edu.hust.model.StudentClass;
import edu.hust.repository.ClassRoomRepository;
import edu.hust.repository.StudentClassRepository;
import edu.hust.utils.GeneralValue;

@Service
@Qualifier("StudentClassServiceImpl1")
public class StudentClassServiceImpl1 implements StudentClassService {

	private ClassRoomService classRoomService;
	private BlacklistService blacklistService;
	private ClassRoomRepository classRoomRepository;
	private StudentClassRepository studentClassRepository;

	public StudentClassServiceImpl1() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Autowired
	public StudentClassServiceImpl1(@Qualifier("ClassRoomServiceImpl1") ClassRoomService classRoomService,
			@Qualifier("BlacklistServiceImpl1") BlacklistService blacklistService,
			ClassRoomRepository classRoomRepository, StudentClassRepository studentClassRepository) {
		super();
		this.classRoomRepository = classRoomRepository;
		this.studentClassRepository = studentClassRepository;
		this.classRoomService = classRoomService;
		this.blacklistService = blacklistService;
	}

	@Override
	public List<ClassRoom> getTimeTable(int studentID, int semesterID) {
		List<Integer> listClassID = this.studentClassRepository.getListClass(studentID, semesterID);

		if (listClassID == null || listClassID.isEmpty()) {
			return null;
		}

		List<ClassRoom> listClassRoom = this.classRoomRepository.getListClassRoom(listClassID);
		if (listClassRoom == null || listClassRoom.isEmpty()) {
			return null;
		}
		return listClassRoom;
	}

	@Override
	public boolean checkStudentHasAuthority(int studentID, int classID, int roomID, String identifyString,
			String imei) {
		Optional<StudentClass> studentClass = this.studentClassRepository.findByStudentIDAndClassIDAndStatus(studentID,
				classID, IsLearning.LEARNING.getValue());

		if (studentClass.isEmpty()) {
			System.out.println("\n\nMile 1");
			return false;
		}

		StudentClass instance = studentClass.get();
		String classIdentifyString = instance.getClassInstance().getIdentifyString();
		//String studentImei = instance.getAccount().getImei();
		
		// check if identifyString is incorrect
		if (!classIdentifyString.equals(identifyString)) {
			System.out.println("\n\nMile 2");
			return false;
		}

		// check if ClassRoom exists
		//// Notice: weekday of java = weekday of mySQL - 1
		int weekday = LocalDate.now().getDayOfWeek().getValue() + 1;
		LocalTime checkTime = LocalTime.now();
		ClassRoom classRoom = this.classRoomService.getInfoClassRoom(classID, roomID, weekday, checkTime);
		if (classRoom == null) {
			System.out.println("\n\nMile 3");
			return false;
		}

		// check if student has already roll call
		// dateAndTime co dinh dang: Year - dayInYear - secondInDay
		String isChecked = instance.getIsChecked();
		
		if (!checkIsCheckedValid(isChecked, classRoom.getBeginAt(), classRoom.getFinishAt())) {
			System.out.println("\n\nMile 4");
			return false;
		}

		return true;
	}

	@Override
	public String rollCall(int classID, int studentID, LocalDateTime rollCallAt, String imei) {
		Optional<StudentClass> studentClass = this.studentClassRepository.findByStudentIDAndClassIDAndStatus(studentID,
				classID, IsLearning.LEARNING.getValue());
		StudentClass instance = null;
		String newValue = null;
		String listRollCall = null;
		String isChecked = null;

		if (studentClass.isEmpty()) {
			return "Not found student-class";
		}

		instance = studentClass.get();
		listRollCall = instance.getListRollCall();

		newValue = "" + rollCallAt.getYear();
		newValue += GeneralValue.regexForSplitDate + rollCallAt.getDayOfYear();
		newValue += GeneralValue.regexForSplitDate + rollCallAt.toLocalTime().toSecondOfDay()
				+ GeneralValue.regexForSplitListRollCall;

		if (listRollCall == null) {
			listRollCall = newValue;
		} else {
			listRollCall += newValue;
		}

		instance.setListRollCall(listRollCall);
		
		isChecked = rollCallAt.getYear() + GeneralValue.regexForSplitDate + rollCallAt.getDayOfYear()
				+ GeneralValue.regexForSplitDate + rollCallAt.toLocalTime().toSecondOfDay();
		instance.setIsChecked(isChecked);
		this.studentClassRepository.save(instance);

		//create a blacklist's record if imei is different
		if (!instance.getAccount().getEmail().equals(imei)) {
			this.blacklistService.createNewRecord(instance, imei);
			return "Warning: System has created a record in blacklist for your incorrect IMEI!";
		}
		return null;

	}

	@Override
	public List<StudentClass> findByClassID(int id) {
		List<StudentClass> listInstance = this.studentClassRepository.getListStudentClass(id);
		if (listInstance == null || listInstance.isEmpty()) {
			return null;
		}
		return listInstance;
	}

	@Override
	public boolean updateStudentClassInfo(StudentClass studentClass) {
		this.studentClassRepository.save(studentClass);
		return true;
	}

	@Override
	public List<StudentClass> findCurrentStudentsByClassID(int classID) {
		List<StudentClass> listInstance = this.studentClassRepository.getListCurrentStudentClass(classID,
				IsLearning.LEARNING.getValue());
		if (listInstance == null || listInstance.isEmpty()) {
			return null;
		}
		return listInstance;
	}

	@Override
	public boolean checkStudentIsLearning(int studentID, int classID) {
		Optional<StudentClass> studentClass = this.studentClassRepository.findByStudentIDAndClassIDAndStatus(studentID,
				classID, IsLearning.LEARNING.getValue());
		if (studentClass.isEmpty()) {
			return false;
		}

		return true;
	}

	@Override
	public boolean checkIsCheckedValid(String isChecked, LocalTime beginAt, LocalTime finishAt) {
		if (isChecked != null) {
			String[] dateAndTime = isChecked.split(GeneralValue.regexForSplitDate);
			int year = Integer.parseInt(dateAndTime[0]);
			int dayOfYear = Integer.parseInt(dateAndTime[1]);
			int secondOfDay = Integer.parseInt(dateAndTime[2]);

			LocalDate checkedDate = LocalDate.ofYearDay(year, dayOfYear);
			LocalTime checkedTime = LocalTime.ofSecondOfDay(secondOfDay);

			// check if a day has more than one lesson of a class
			if (LocalDate.now().isEqual(checkedDate) && checkedTime.isAfter(beginAt)
					&& checkedTime.isBefore(finishAt)) {
				return false;
			}

			return true;
		}

		return true;
	}

	@Override
	public boolean checkStudentIsLearning(String studentEmail, int classID) {
		Optional<StudentClass> studentClass = this.studentClassRepository.findByStudentEmailAndClassIDAndStatus(studentEmail,
				classID, IsLearning.LEARNING.getValue());
		if (studentClass.isEmpty()) {
			return false;
		}

		return true;
	}

}
