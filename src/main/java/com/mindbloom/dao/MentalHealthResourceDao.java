package com.mindbloom.dao;

import java.util.List;

import com.mindbloom.model.MentalHealthResource;

public interface MentalHealthResourceDao {

    void save(MentalHealthResource resource);

    void delete(int id);

    MentalHealthResource findById(int id);

    List<MentalHealthResource> findAll();

    List<MentalHealthResource> searchByTitle(String keyword);

    List<MentalHealthResource> findByCategory(String category);
}
