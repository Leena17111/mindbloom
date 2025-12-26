package com.mindbloom.dao;

import java.util.List;
import com.mindbloom.model.Person;

public interface PersonDao {

    Person findByEmail(String email);

    Person findById(Integer id);

    void save(Person person);

    List<Person> findByRole(String role);
}
