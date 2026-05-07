package com.mksafenet.repository;

import com.mksafenet.model.ScenarioResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ScenarioResponseRepository extends JpaRepository<ScenarioResponse, Long> {
    List<ScenarioResponse> findByStudentId(String studentId);

    @Query("SELECT sr FROM ScenarioResponse sr WHERE sr.student.session.school.id = :schoolId")
    List<ScenarioResponse> findBySchoolId(Long schoolId);

    @Query("SELECT sr.scenarioId, COUNT(sr), SUM(CASE WHEN sr.correct = true THEN 1 ELSE 0 END) FROM ScenarioResponse sr WHERE sr.student.session.school.id = :schoolId GROUP BY sr.scenarioId")
    List<Object[]> getScenarioStatsForSchool(Long schoolId);
}
