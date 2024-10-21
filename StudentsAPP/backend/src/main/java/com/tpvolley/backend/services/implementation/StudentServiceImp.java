package com.tpvolley.backend.services.implementation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.tpvolley.backend.dto.FiliereStats;
import com.tpvolley.backend.entities.Student;
import com.tpvolley.backend.repository.StudentRepository;
import com.tpvolley.backend.services.interfaces.StudentService;


@Service
public class StudentServiceImp implements StudentService{

    private StudentRepository studentRepository;
    
    public StudentServiceImp(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public Student create(Student student) {
        return studentRepository.save(student);
    }

    @Override
    public Optional<Student> findStudentByid(Integer id) {
         studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(String.format("Student with id (%s) could not be found", id)));
        return studentRepository.findById(id);  
    }

    @Override
    public List<Student> findAll() {
       return studentRepository.findAll();
    }

    @Override
    public Student update(Student stud) {
         Student existingStudent = studentRepository.findById(stud.getId())
         .orElseThrow(()-> new RuntimeException(String.format("Student with id (%s) could not be found", stud.getId())));

            existingStudent.setNom(stud.getNom());
            existingStudent.setPrenom(stud.getPrenom());
            existingStudent.setVille(stud.getVille());
            existingStudent.setSexe(stud.getSexe());
            existingStudent.setFiliere(stud.getFiliere());

            existingStudent.setImage(stud.getImage());
            return studentRepository.save(existingStudent);
        }

    @Override
    public void delete(Integer id) {
        studentRepository.findById(id)
        .orElseThrow(()-> new RuntimeException(String.format("Student with id (%s) could not be found", id)));    
        studentRepository.deleteById(id);
    }


    @Override
    public List<FiliereStats> countStudentsByFiliere() {
        List<Object[]> results = studentRepository.countStudentsByFiliere();
        List<FiliereStats> statsList = new ArrayList<>();

        for (Object[] result : results) {
            String filiere = (String) result[0];
            Long count = (Long) result[1];
            statsList.add(new FiliereStats(filiere, count));
        }

        return statsList;
    }
}
