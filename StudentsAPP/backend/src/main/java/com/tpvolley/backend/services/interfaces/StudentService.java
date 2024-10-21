package com.tpvolley.backend.services.interfaces;

import java.util.List;
import java.util.Optional;

import com.tpvolley.backend.dto.FiliereStats;
import com.tpvolley.backend.entities.Student;

public interface StudentService {

    Student create(Student student);

    Optional<Student> findStudentByid(Integer id);

    List<Student> findAll();

    Student update(Student stud);

    void delete(Integer id);

    List<FiliereStats> countStudentsByFiliere();

}
