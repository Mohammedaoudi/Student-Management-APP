package com.tpvolley.backend.controllers;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tpvolley.backend.dto.FiliereStats;
import com.tpvolley.backend.entities.Student;
import com.tpvolley.backend.services.interfaces.StudentService;

@RestController
@RequestMapping("/api/v1/students")
public class StudentController {

    private final StudentService studentService;
    private final ResourceLoader resourceLoader;

    public StudentController(StudentService studentService, ResourceLoader resourceLoader) {
        this.studentService = studentService;
        this.resourceLoader = resourceLoader;
    }

    // Create a new student
    @PostMapping("/create")
    public ResponseEntity<Student> createStudent(
            @RequestParam("nom") String nom,
            @RequestParam("prenom") String prenom,
            @RequestParam("ville") String ville,
            @RequestParam("sexe") String sexe,
            @RequestParam("filiere") String filiere,

            @RequestParam(value = "image", required = false) MultipartFile image) {
        
        // Handle the image file (if provided)
        String imageUrl = image != null ? saveImage(image, filiere, nom) : null;

        // Create a new student with the image URL (if provided)
        Student student = new Student();
        student.setNom(nom);
        student.setPrenom(prenom);
        student.setVille(ville);
        student.setSexe(sexe);
        student.setImage(imageUrl);
        student.setFiliere(filiere);// Set the image URL or path if image exists

        Student createdStudent = studentService.create(student);
        return ResponseEntity.ok(createdStudent);
    }

    private String saveImage(MultipartFile image, String filiere, String nom) {
        try {
            // Generate a unique file name based on filiere and nom
            String fileName = filiere + "_" + nom + "_" + image.getOriginalFilename();
            
            // Use the project root path for the uploads directory
            String uploadDir = Paths.get("uploads").toAbsolutePath().toString() + File.separator;
    
            // Create the directory if it doesn't exist
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }
    
            // Save the file to the directory
            File destinationFile = new File(uploadDir + fileName);
            image.transferTo(destinationFile);
    
            // Return the URL for accessing the image
            return "/api/v1/students/uploads/" + fileName; // This will be accessible via a browser
        } catch (Exception e) {
            throw new RuntimeException("Failed to save image", e);
        }
    }
    
    // Get all students
    @GetMapping
    public ResponseEntity<List<Student>> getAllStudents() {
        List<Student> students = studentService.findAll();
        return ResponseEntity.ok(students);
    }

    // Get a student by ID
    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable Integer id) {
        Optional<Student> student = studentService.findStudentByid(id);
        return student.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update a student by ID
    @PutMapping("/{id}")
public ResponseEntity<Student> updateStudent(
        @PathVariable Integer id,
        @RequestParam("nom") String nom,
        @RequestParam("prenom") String prenom,
        @RequestParam("ville") String ville,
        @RequestParam("sexe") String sexe,
        @RequestParam("filiere") String filiere,
        @RequestParam(value = "image", required = false) MultipartFile image) {

    // Create a new Student object to update
    Student studentToUpdate = new Student();
    studentToUpdate.setId(id);
    studentToUpdate.setNom(nom);
    studentToUpdate.setPrenom(prenom);
    studentToUpdate.setVille(ville);
    studentToUpdate.setSexe(sexe);
    studentToUpdate.setFiliere(filiere);

    // Handle the image file (if provided)
    String imageUrl = image != null ? saveImage(image, filiere, nom) : null;
    if (imageUrl != null) {
        studentToUpdate.setImage(imageUrl);
    }

    // Call the update service
    Student updatedStudent = studentService.update(studentToUpdate);
    return ResponseEntity.ok(updatedStudent);
}


    // Delete a student by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Integer id) {
        studentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Get image by filename
    @GetMapping(value = "/uploads/{filename:.+}", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            // Construct the file path
            String filePath = Paths.get("uploads", filename).toAbsolutePath().toString();
            File file = new File(filePath);

            // Load the image as a Resource
            Resource resource = resourceLoader.getResource("file:" + filePath);
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/count-per-filiere")
    public ResponseEntity<List<FiliereStats>> countStudentsByFiliere() {
        List<FiliereStats> stats = studentService.countStudentsByFiliere();
        return ResponseEntity.ok(stats);
    }
}
