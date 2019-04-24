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

@Service
@Qualifier("StudentClassServiceImpl1")
public class StudentClassServiceImpl1 implements StudentClassService {

	private ClassRoomService classRoomService;
	private ClassRoomRepository classRoomRepository;
	private StudentClassRepository studentClassRepository;

	public StudentClassServiceImpl1() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Autowired
	public StudentClassServiceImpl1(@Qualifier("ClassRoomServiceImpl1") ClassRoomService classRoomService,
			ClassRoomRepository classRoomRepository, StudentClassRepository studentClassRepository) {
		super();
		this.classRoomRepository = classRoomRepository;
		this.studentClassRepository = studentClassRepository;
		this.classRoomService = classRoomService;
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
			return false;
		}

		StudentClass instance = studentClass.get();
		String classIdentifyString = instance.getClassInstance().getIdentifyString();
		String studentImei = instance.getAccount().getImei();

		// check if identifyString is incorrect or imei is incorrect
		if (!classIdentifyString.equals(identifyString) || !studentImei.equals(imei)) {
			return false;
		}

		// check if ClassRoom exists
		//// Notice: weekday of java = weekday of mySQL - 1
		int weekday = LocalDate.now().getDayOfWeek().getValue() + 1;
		LocalTime checkTime = LocalTime.now();
		ClassRoom classRoom = this.classRoomService.getInfoClassRoom(classID, roomID, weekday, checkTime);
		if (classRoom == null) {
			return false;
		}

		// check if student has already roll call
		// dateAndTime co dinh dang: Year - dayInYear - secondInDay
		String isChecked = instance.getIsChecked();

		if (isChecked != null) {
			String[] dateAndTime = isChecked.split("-");
			int year = Integer.parseInt(dateAndTime[0]);
			int dayOfYear = Integer.parseInt(dateAndTime[1]);
			int secondOfDay = Integer.parseInt(dateAndTime[2]);

			LocalDate checkedDate = LocalDate.ofYearDay(year, dayOfYear);
			LocalTime checkedTime = LocalTime.ofSecondOfDay(secondOfDay);
			System.out.println("\n\ncheckedDate = " + checkedDate.toString());
			System.out.println("\n\ncheckedTime = " + checkedTime.toString());
			
			//check if a day has more than one lesson of a class
			if (LocalDate.now().isEqual(checkedDate) && checkedTime.isAfter(classRoom.getBeginAt())
					&& checkedTime.isBefore(classRoom.getFinishAt())) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean rollCall(int classID, int studentID, LocalDateTime rollCallAt) {
		Optional<StudentClass> studentClass = this.studentClassRepository.findByStudentIDAndClassIDAndStatus(studentID,
				classID, IsLearning.LEARNING.getValue());
		StudentClass instance = null;
		String newValue = null;
		String listRollCall = null;

		if (studentClass.isEmpty()) {
			return false;
		}

		instance = studentClass.get();
		listRollCall = instance.getListRollCall();

		newValue = "" + rollCallAt.getYear();
		newValue += "-" + rollCallAt.getDayOfYear();
		newValue += "-" + rollCallAt.toLocalTime().toSecondOfDay() + ";";

		if (listRollCall == null) {
			listRollCall = newValue;
		} else {
			listRollCall += newValue;
		}

		instance.setListRollCall(listRollCall);

		String isChecked = null;
		isChecked = rollCallAt.getYear() + "-" + rollCallAt.getDayOfYear() + "-"
				+ rollCallAt.toLocalTime().toSecondOfDay();
		instance.setIsChecked(isChecked);
		this.studentClassRepository.save(instance);

		return true;

	}

	@Override
	public List<StudentClass> findByClassID(int id) {
		List<StudentClass> listInstance = this.studentClassRepository.getListStudentClass(id);
		if (listInstance == null || listInstance.isEmpty()) {
			return null;
		}
		return listInstance;
	}
}
