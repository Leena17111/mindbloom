package com.mindbloom.dao;

import java.util.List;

import com.mindbloom.model.Post;

public interface PostDao {

    void save(Post post);

    void delete(int id);

    Post findById(int id);

    List<Post> findAll();

    List<Post> findByStudentId(int studentId);

    List<Post> findByCategory(String category);

    List<Post> searchByTitle(String keyword);
}