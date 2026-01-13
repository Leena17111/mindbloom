package com.mindbloom.security;

import com.mindbloom.dao.PersonDao;
import com.mindbloom.model.Person;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private PersonDao personDao;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        Person person = personDao.findByEmail(email);

        if (person == null) {
            throw new UsernameNotFoundException("User not found");
        }

        // ✅ IMPORTANT: return Person directly
        return person;
    }
}