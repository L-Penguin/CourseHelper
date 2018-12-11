package com.tiancai.app.lib;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;



public class Course implements Serializable {

    private String courseName;
    private String courseNum;
    private List<List<Course>> prerequisites;

    public Course(String setCourseName, String setCourseNum) {
        courseName = setCourseName;
        courseNum = setCourseNum;
        prerequisites = new ArrayList<>();
    }

    public Course(String setCourseName, String setCourseNum, List<List<Course>> setPre) {
        this(setCourseName, setCourseNum);
        prerequisites = setPre;
    }

    public void appendPrerequisite(List<Course> toAdd) {
        prerequisites.add(toAdd);
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseNum() {
        return courseNum;
    }

    public void setCourseNum(String courseNum) {
        this.courseNum = courseNum;
    }

    public List<List<Course>> getPrerequisites() {
        return prerequisites;
    }

    public void setPrerequisites(List<List<Course>> setPre) {
        prerequisites = setPre;
    }
}
