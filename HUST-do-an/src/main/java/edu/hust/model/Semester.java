package edu.hust.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "semester")
public class Semester implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private int semesterID;

	@Column(name = "SemesterName", nullable = false, length = 10, unique = true)
	private String semesterName;

	@Column(name = "BeginDate", nullable = false)
	@Temporal(TemporalType.DATE)
	private Date beginDate;

	@Column(name = "EndDate", nullable = false)
	@Temporal(TemporalType.DATE)
	private Date endDate;

	public Semester() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Semester(String name, Date beginDate, Date endDate) {
		super();
		this.semesterName = name;
		this.beginDate = beginDate;
		this.endDate = endDate;
	}

	public String getName() {
		return semesterName;
	}

	public void setName(String name) {
		this.semesterName = name;
	}

	public Date getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(Date beginDate) {
		this.beginDate = beginDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public int getSemesterID() {
		return semesterID;
	}

	public void setSemesterID(int semesterID) {
		this.semesterID = semesterID;
	}

}
