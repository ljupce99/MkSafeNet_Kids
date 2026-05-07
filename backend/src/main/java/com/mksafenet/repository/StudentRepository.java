package com.mksafenet.repository;

import com.mksafenet.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, String> {
    List<Student> findBySessionId(Long sessionId);

    @Query("SELECT s FROM Student s WHERE s.session.school.id = :schoolId")
    List<Student> findBySchoolId(Long schoolId);

    @Query("SELECT COUNT(s) FROM Student s WHERE s.session.school.id = :schoolId AND s.completed = true")
    long countCompletedBySchoolId(Long schoolId);

    @Query("SELECT AVG(s.score) FROM Student s WHERE s.session.school.id = :schoolId AND s.completed = true")
    Double avgScoreBySchoolId(Long schoolId);
}
