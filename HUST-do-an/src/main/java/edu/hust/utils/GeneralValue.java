package edu.hust.utils;

public class GeneralValue {

	//from corner of Dai Co Viet to TC
	public static final double minLongitude = 105.841582;
	public static final double maxLongitude = 105.847237;
	public static final double minLatitude = 21.002048;
	public static final double maxLatitude = 21.007368;
	
	public static final double degreeToRadiant = (Math.PI / 180D);
	public static final double eQuatorialEarthRadius = 6378.1370D;
	
	public static final int semestersInYear = 3;
	public static final int sequenceOfSummerSemester = 3;
	public static final int lengthOfNormalSemester = 150;		//unit: day
	public static final int lengthOfSummerSemester = 30;		//unit: day
	
	public static final int minStudents = 15;			//unit: person
	public static final int maxStudents = 100;			//unit: person
	
	public static final int maxClassPerTeacher = 7;		//unit: class
	public static final int maxClassPerStudent = 7;		//unit: class
	
	public static final String regexForSplitListRollCall = ";";
	public static final String regexForSplitDate = "-";
	
	public static final String markForMissingRollCall = "x";
	public static final String markForTeacherMissing = "i";
	public static final String markForPermission = "y";
	public static final String markForNotBringPhone = "p";
	
	public static final int maxTimesForUpdatingImei = 3;
}
