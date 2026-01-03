package com.mindbloom.dao;

import java.util.List;

import com.mindbloom.model.StudentResourceProgress;

public interface StudentResourceProgressDao {

    void save(StudentResourceProgress progress);

    StudentResourceProgress findByStudentAndResource(int studentId, int resourceId);

    List<StudentResourceProgress> findByStudentId(int studentId);
}
