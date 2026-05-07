package com.mksafenet.repository;

import com.mksafenet.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findByToken(String token);
    List<Session> findByTeacherId(Long teacherId);
    List<Session> findBySchoolId(Long schoolId);
}
