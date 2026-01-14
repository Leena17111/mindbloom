package com.mindbloom.dao;

import java.util.List;

import com.mindbloom.model.Dass21Question;

public interface Dass21QuestionDao {

    List<Dass21Question> findAll();

    Dass21Question findById(int id);
}
