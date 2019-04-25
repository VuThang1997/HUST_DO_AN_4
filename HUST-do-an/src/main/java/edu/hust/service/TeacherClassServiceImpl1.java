package edu.hust.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import edu.hust.enumData.IsTeaching;
import edu.hust.model.Class;
import edu.hust.model.ClassRoom;
import edu.hust.model.TeacherClass;
import edu.hust.repository.ClassRepository;
import edu.hust.repository.ClassRoomRepository;
import edu.hust.repository.TeacherClassRepository;
import edu.hust.utils.GeneralValue;

@Service
@Transactional
@Qualifier("TeacherClassServiceImpl1")
public class TeacherClassServiceImpl1 implements TeacherClassService {

	private ClassRoomService classRoomService;
	private ClassRepository classRepository;
	private ClassRoomRepository classRoomRepository;
	private TeacherClassRepository teacherClassRepository;

	public TeacherClassServiceImpl1() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Autowired
	public TeacherClassServiceImpl1(@Qualifier("ClassRoomServiceImpl1") ClassRoomService classRoomService,
			ClassRoomRepository classRoomRepository, TeacherClassRepository teacherClassRepository,
			ClassRepository classRepository) {
		super();
		this.classRoomRepository = classRoomRepository;
		this.teacherClassRepository = teacherClassRepository;
		this.classRoomService = classRoomService;
		this.classRepository = classRepository;
	}

	@Override
	public List<ClassRoom> getTimeTable(int teacherID, int semesterID) {
		List<Integer> listClassID = this.teacherClassRepository.getListClass(teacherID, semesterID);
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
	public boolean checkTeacherHasAuthority(int teacherID, int classID) {
		Optional<TeacherClass> teacherClass = this.teacherClassRepository.findByTeacherIDAndClassIDAndStatus(teacherID,
				classID, IsTeaching.TEACHING.getValue());

		if (teacherClass.isEmpty()) {
			return false;
		}

		return true;
	}

	@Override
	public boolean checkGenerateTimeValid(int weekday, LocalTime generateTime, int classID, int roomID) {
		LocalTime beginTime = null;
		LocalTime finishTime = null;
		String isChecked = null;
		ClassRoom classRoom = null;

		// check if this classroom exists
		classRoom = this.classRoomService.getInfoClassRoom(classID, roomID, weekday, generateTime);
		if (classRoom == null) {
			return false;
		}

		beginTime = classRoom.getBeginAt();
		finishTime = classRoom.getFinishAt();
		isChecked = classRoom.getClassInstance().getIsChecked();

		// 1 lesson has only one roll call request
		// check if this class has been request to roll call
		if (isChecked != null) {
			String[] dateAndTime = isChecked.split("-");
			int year = Integer.parseInt(dateAndTime[0]);
			int dayOfYear = Integer.parseInt(dateAndTime[1]);
			int secondOfDay = Integer.parseInt(dateAndTime[2]);

			LocalDate checkedDate = LocalDate.ofYearDay(year, dayOfYear);
			LocalTime checkedTime = LocalTime.ofSecondOfDay(secondOfDay);

			// check if a day has more than one lesson of a class
			if (LocalDate.now().isEqual(checkedDate) && checkedTime.isAfter(classRoom.getBeginAt())
					&& checkedTime.isBefore(classRoom.getFinishAt())) {
				return false;
			}
		}

		// check if generateTime is in limit
		// 10 minutes is time limit to roll call; offset 5 minutes for insurance
		finishTime = finishTime.minusMinutes(15);
		if (generateTime.isBefore(beginTime) || generateTime.isAfter(finishTime)) {
			return false;
		}
		return true;
	}

	@Override
	public String generateIdentifyString(int classID, int roomID, int weekday, String inputMd5) {
		LocalTime checkTime = LocalTime.now();
		ClassRoom classRoom = this.classRoomService.getInfoClassRoom(classID, roomID, weekday, checkTime);
		Class classInstance = null;
		MessageDigest md = null;
		byte[] messageDigest;
		String identifyString = null;
		String eventDynamicName = null;
		// String timeString = null;

		// check if this classroom exists
		if (classRoom == null) {
			return null;
		}

		// check if class's identifyString has already been set or this class has been
		// roll called once in this lesson
		classInstance = this.classRepository.findById(classRoom.getClassInstance().getId()).get();
		if (classInstance.getIdentifyString() != null) {
			return null;
		}

		try {
			// generate md5 string (just take the first 10 characters)
			md = MessageDigest.getInstance("MD5");
			messageDigest = md.digest(inputMd5.getBytes());
			identifyString = messageDigest.toString().substring(0, 10);
			classInstance.setIdentifyString(identifyString);

			String isChecked = null;
			LocalDateTime rollCallAt = LocalDateTime.now();
			isChecked = rollCallAt.getYear() + "-" + rollCallAt.getDayOfYear() + "-"
					+ rollCallAt.toLocalTime().toSecondOfDay();
			classInstance.setIsChecked(isChecked);
			this.classRepository.save(classInstance);

			// create new event to set identifyString = null after 10 minutes
			// user Class.numberOfEvents to avoid conflict
			Class.numberOfEvents += 1;
			eventDynamicName = "generateIdentifyString" + Class.numberOfEvents;
			this.classRepository.setNullIdentifyString(classID, eventDynamicName, classRoom.getId());

			// this.classRepository.setIsCheckFalse(classID, timeString, eventDynamicName);
			return identifyString;

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean rollCall(LocalDateTime rollCallAt, int teacherID, int classID) {
		Optional<TeacherClass> teacherClass = this.teacherClassRepository.findByTeacherIDAndClassIDAndStatus(teacherID,
				classID, IsTeaching.TEACHING.getValue());
		TeacherClass instance = null;
		Class classInstance = null;
		String newValue = null;
		String listRollCall = null;

		if (teacherClass.isEmpty()) {
			return false;
		}

		instance = teacherClass.get();
		classInstance = instance.getClassInstance();
		listRollCall = instance.getListRollCall();

		newValue = "" + rollCallAt.getYear();
		newValue += "-" + rollCallAt.getDayOfYear();
		newValue += "-" + rollCallAt.toLocalTime().toSecondOfDay() + GeneralValue.regexForSplitListRollCall;

		if (listRollCall == null) {
			listRollCall = newValue;
		} else {
			listRollCall += newValue;
		}

		instance.setListRollCall(listRollCall);
		this.teacherClassRepository.save(instance);

		classInstance.setCurrentLesson(classInstance.getCurrentLesson() + 1);
		this.classRepository.save(classInstance);

		return true;

	}

	@Override
	public List<TeacherClass> findByClassID(int id) {
		List<TeacherClass> listInstance = this.teacherClassRepository.getListTeacherClass(id);
		if (listInstance == null || listInstance.isEmpty()) {
			return null;
		}
		return listInstance;
	}

	@Override
	public TeacherClass findCurrentTeacherByClassID(int classID) {
		Optional<TeacherClass> teacherClass = this.teacherClassRepository.findByClassIDAndStatus(classID,
				IsTeaching.TEACHING.getValue());
		if (teacherClass.isEmpty()) {
			return null;
		}
		return teacherClass.get();
	}

	@Override
	public boolean updateTeacherClass(TeacherClass teacherClass) {
		this.teacherClassRepository.save(teacherClass);
		return false;
	}

}
