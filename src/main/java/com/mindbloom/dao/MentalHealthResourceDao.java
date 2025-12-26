package com.mindbloom.dao;

import java.util.List;

import com.mindbloom.model.MentalHealthResource;

public interface MentalHealthResourceDao {

    void save(MentalHealthResource resource);

    void delete(Long id);

    MentalHealthResource findById(Long id);

    List<MentalHealthResource> findAll();

    List<MentalHealthResource> searchByTitle(String keyword);
}
