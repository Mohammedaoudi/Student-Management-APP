package com.tpvolley.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.tpvolley.backend.entities.Student;

public interface StudentRepository extends  JpaRepository<Student, Integer>{

    @Query("SELECT s.filiere, COUNT(s) FROM Student s GROUP BY s.filiere")
    List<Object[]> countStudentsByFiliere();

}
