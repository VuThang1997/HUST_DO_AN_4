-- phpMyAdmin SQL Dump
-- version 4.8.4
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Apr 25, 2019 at 08:10 AM
-- Server version: 10.1.37-MariaDB
-- PHP Version: 7.3.1

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `roll_call_system_2`
--

-- --------------------------------------------------------

--
-- Table structure for table `account`
--

CREATE TABLE `account` (
  `ID` int(1) UNSIGNED NOT NULL,
  `Email` varchar(30) COLLATE utf8_unicode_ci NOT NULL,
  `IMEI` varchar(20) COLLATE utf8_unicode_ci DEFAULT NULL,
  `IsActive` int(1) UNSIGNED NOT NULL,
  `Password` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `Role` int(1) UNSIGNED NOT NULL,
  `UserInfo` varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL,
  `Username` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `UpdateImeiCounter` int(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `account`
--

INSERT INTO `account` (`ID`, `Email`, `IMEI`, `IsActive`, `Password`, `Role`, `UserInfo`, `Username`, `UpdateImeiCounter`) VALUES
(1, 'studentTest1@gmail.com', '12345678', 2, '12345', 3, 'V H T+DH BKHN+1234567890+1979-01-01', 'testTeacher', 1),
(2, 'teacher1@gmail.com', '123456', 2, '123', 2, 'V H T 3+DH BKHN+1234567890+1997-10-01', 'testTeacher', 0),
(4, 'emailStudent1@gmail.com', '123456789', 2, '1234', 3, NULL, 'student2', 0),
(5, 'emailStudent3@gmail.com', '123456', 2, '12345', 3, NULL, 'student3', 0);

-- --------------------------------------------------------

--
-- Table structure for table `blacklist`
--

CREATE TABLE `blacklist` (
  `ID` int(1) NOT NULL,
  `ClassID` int(1) UNSIGNED NOT NULL,
  `StudentID` int(1) UNSIGNED NOT NULL,
  `FakeIMEI` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `CommitDate` date NOT NULL,
  `CommitTime` time NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `blacklist`
--

INSERT INTO `blacklist` (`ID`, `ClassID`, `StudentID`, `FakeIMEI`, `CommitDate`, `CommitTime`) VALUES
(2, 2, 1, '123456', '2019-04-25', '13:06:43');

-- --------------------------------------------------------

--
-- Table structure for table `class`
--

CREATE TABLE `class` (
  `ID` int(1) UNSIGNED NOT NULL,
  `CurrentLesson` int(1) NOT NULL,
  `IdentifyString` varchar(10) COLLATE utf8_unicode_ci DEFAULT NULL,
  `IsChecked` varchar(16) COLLATE utf8_unicode_ci DEFAULT NULL,
  `MaxStudent` int(1) NOT NULL,
  `ClassName` varchar(30) COLLATE utf8_unicode_ci NOT NULL,
  `NumberOfLessons` int(1) UNSIGNED NOT NULL,
  `CourseID` int(1) UNSIGNED NOT NULL,
  `SemesterID` int(1) UNSIGNED NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `class`
--

INSERT INTO `class` (`ID`, `CurrentLesson`, `IdentifyString`, `IsChecked`, `MaxStudent`, `ClassName`, `NumberOfLessons`, `CourseID`, `SemesterID`) VALUES
(2, 17, NULL, '2019-115-46921', 14, 'class1', 31, 1, 1),
(3, 0, NULL, NULL, 20, 'classTest2', 34, 1, 9);

-- --------------------------------------------------------

--
-- Table structure for table `class_room`
--

CREATE TABLE `class_room` (
  `ID` int(1) UNSIGNED NOT NULL,
  `BeginAt` time NOT NULL,
  `FinishAt` time NOT NULL,
  `Weekday` int(1) UNSIGNED NOT NULL,
  `ClassID` int(1) UNSIGNED NOT NULL,
  `RoomID` int(1) UNSIGNED NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `class_room`
--

INSERT INTO `class_room` (`ID`, `BeginAt`, `FinishAt`, `Weekday`, `ClassID`, `RoomID`) VALUES
(1, '11:00:00', '23:00:00', 2, 2, 1),
(2, '15:30:00', '17:00:00', 6, 2, 1),
(3, '07:00:00', '09:00:00', 5, 2, 1),
(4, '14:15:00', '15:00:00', 4, 2, 1),
(5, '16:00:00', '18:00:00', 5, 2, 1),
(6, '12:30:00', '14:00:00', 5, 2, 1),
(7, '20:00:00', '22:00:00', 5, 2, 1);

-- --------------------------------------------------------

--
-- Table structure for table `course`
--

CREATE TABLE `course` (
  `ID` int(1) UNSIGNED NOT NULL,
  `CourseName` varchar(30) COLLATE utf8_unicode_ci NOT NULL,
  `Description` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `course`
--

INSERT INTO `course` (`ID`, `CourseName`, `Description`) VALUES
(1, 'course1', NULL),
(2, 'Kĩ thuật máy tính', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `room`
--

CREATE TABLE `room` (
  `ID` int(1) UNSIGNED NOT NULL,
  `Address` varchar(60) COLLATE utf8_unicode_ci NOT NULL,
  `GPSLatitude` double NOT NULL,
  `GPSLongitude` double NOT NULL,
  `MacAddress` varchar(12) COLLATE utf8_unicode_ci NOT NULL,
  `RoomName` varchar(30) COLLATE utf8_unicode_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `room`
--

INSERT INTO `room` (`ID`, `Address`, `GPSLatitude`, `GPSLongitude`, `MacAddress`, `RoomName`) VALUES
(1, 'Nha D3, DH BKHN', 21.004694, 105.844997, '11:14:22:01', 'D3-503'),
(2, 'Nha D3, DH BKHN', 21.003752, 105.84423, '00:14:22:01', 'D3-501'),
(3, 'Nha D3, DH BKHN', 21.004692, 105.845112, '00:15:22:01', 'D3-502');

-- --------------------------------------------------------

--
-- Table structure for table `semester`
--

CREATE TABLE `semester` (
  `ID` int(1) UNSIGNED NOT NULL,
  `BeginDate` date NOT NULL,
  `EndDate` date NOT NULL,
  `SemesterName` varchar(10) COLLATE utf8_unicode_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `semester`
--

INSERT INTO `semester` (`ID`, `BeginDate`, `EndDate`, `SemesterName`) VALUES
(1, '2018-01-01', '2018-05-02', '20181'),
(6, '2018-09-01', '2019-01-15', '20182'),
(8, '2017-01-01', '2017-01-15', '20172'),
(9, '2019-04-30', '2019-06-15', '20191');

-- --------------------------------------------------------

--
-- Table structure for table `student_class`
--

CREATE TABLE `student_class` (
  `ID` int(1) UNSIGNED NOT NULL,
  `IsChecked` varchar(16) COLLATE utf8_unicode_ci DEFAULT NULL,
  `IsLearning` int(11) NOT NULL,
  `listRollCall` varchar(500) COLLATE utf8_unicode_ci DEFAULT NULL,
  `StudentID` int(1) UNSIGNED NOT NULL,
  `ClassID` int(1) UNSIGNED NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `student_class`
--

INSERT INTO `student_class` (`ID`, `IsChecked`, `IsLearning`, `listRollCall`, `StudentID`, `ClassID`) VALUES
(1, '2019-115-47203', 1, '2019-115-28800;2019-115-46800p;2019-115-47203;', 1, 2),
(2, NULL, 1, '2019-115-28800;2019-115-46973y;2019-115-i;2019-115-i;', 4, 2),
(3, '2019-115-46894', 1, '2019-115-x;2019-115-46800;2019-115-61200;2019-115-i;', 5, 2);

-- --------------------------------------------------------

--
-- Table structure for table `teacher_class`
--

CREATE TABLE `teacher_class` (
  `ID` int(1) UNSIGNED NOT NULL,
  `IsTeaching` int(1) UNSIGNED NOT NULL,
  `ListRollCall` varchar(500) COLLATE utf8_unicode_ci DEFAULT NULL,
  `TeacherID` int(1) UNSIGNED NOT NULL,
  `ClassID` int(1) UNSIGNED NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `teacher_class`
--

INSERT INTO `teacher_class` (`ID`, `IsTeaching`, `ListRollCall`, `TeacherID`, `ClassID`) VALUES
(1, 1, '2019-115-28800;2019-115-46921;', 2, 2);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `account`
--
ALTER TABLE `account`
  ADD PRIMARY KEY (`ID`),
  ADD UNIQUE KEY `UK_ltaacihrmn3qk7g85jspnyo6s` (`Email`);

--
-- Indexes for table `blacklist`
--
ALTER TABLE `blacklist`
  ADD PRIMARY KEY (`ID`);

--
-- Indexes for table `class`
--
ALTER TABLE `class`
  ADD PRIMARY KEY (`ID`),
  ADD KEY `FK_class_semester` (`SemesterID`),
  ADD KEY `FK_class_course` (`CourseID`);

--
-- Indexes for table `class_room`
--
ALTER TABLE `class_room`
  ADD PRIMARY KEY (`ID`),
  ADD KEY `FK_classRoom_class` (`ClassID`),
  ADD KEY `FK_classRoom_room` (`RoomID`);

--
-- Indexes for table `course`
--
ALTER TABLE `course`
  ADD PRIMARY KEY (`ID`),
  ADD UNIQUE KEY `UK_njr1siano8jsig6xckt4kl4yb` (`CourseName`);

--
-- Indexes for table `room`
--
ALTER TABLE `room`
  ADD PRIMARY KEY (`ID`),
  ADD UNIQUE KEY `UK_342lvox58bu66dypyvh5bd8cb` (`MacAddress`),
  ADD UNIQUE KEY `UK_ixynujpupl0sfknw9yru7bvym` (`RoomName`);

--
-- Indexes for table `semester`
--
ALTER TABLE `semester`
  ADD PRIMARY KEY (`ID`),
  ADD UNIQUE KEY `SemesterName` (`SemesterName`);

--
-- Indexes for table `student_class`
--
ALTER TABLE `student_class`
  ADD PRIMARY KEY (`ID`),
  ADD KEY `FK_studentClass_student` (`StudentID`),
  ADD KEY `FK_studentClass_class` (`ClassID`);

--
-- Indexes for table `teacher_class`
--
ALTER TABLE `teacher_class`
  ADD PRIMARY KEY (`ID`),
  ADD KEY `FK_teacherClass_teacher` (`TeacherID`),
  ADD KEY `FK_teacherClass_class` (`ClassID`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `account`
--
ALTER TABLE `account`
  MODIFY `ID` int(1) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `blacklist`
--
ALTER TABLE `blacklist`
  MODIFY `ID` int(1) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `class`
--
ALTER TABLE `class`
  MODIFY `ID` int(1) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `class_room`
--
ALTER TABLE `class_room`
  MODIFY `ID` int(1) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `course`
--
ALTER TABLE `course`
  MODIFY `ID` int(1) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `room`
--
ALTER TABLE `room`
  MODIFY `ID` int(1) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `semester`
--
ALTER TABLE `semester`
  MODIFY `ID` int(1) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT for table `student_class`
--
ALTER TABLE `student_class`
  MODIFY `ID` int(1) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `teacher_class`
--
ALTER TABLE `teacher_class`
  MODIFY `ID` int(1) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `class`
--
ALTER TABLE `class`
  ADD CONSTRAINT `FK_class_course` FOREIGN KEY (`CourseID`) REFERENCES `course` (`ID`),
  ADD CONSTRAINT `FK_class_semester` FOREIGN KEY (`SemesterID`) REFERENCES `semester` (`ID`);

--
-- Constraints for table `class_room`
--
ALTER TABLE `class_room`
  ADD CONSTRAINT `FK_classRoom_class` FOREIGN KEY (`ClassID`) REFERENCES `class` (`ID`),
  ADD CONSTRAINT `FK_classRoom_room` FOREIGN KEY (`RoomID`) REFERENCES `room` (`ID`);

--
-- Constraints for table `student_class`
--
ALTER TABLE `student_class`
  ADD CONSTRAINT `FK_studentClass_class` FOREIGN KEY (`ClassID`) REFERENCES `class` (`ID`),
  ADD CONSTRAINT `FK_studentClass_student` FOREIGN KEY (`StudentID`) REFERENCES `account` (`ID`);

--
-- Constraints for table `teacher_class`
--
ALTER TABLE `teacher_class`
  ADD CONSTRAINT `FK_teacherClass_class` FOREIGN KEY (`ClassID`) REFERENCES `class` (`ID`),
  ADD CONSTRAINT `FK_teacherClass_teacher` FOREIGN KEY (`TeacherID`) REFERENCES `account` (`ID`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
