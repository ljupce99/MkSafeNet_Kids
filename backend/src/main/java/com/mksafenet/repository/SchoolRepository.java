package com.mksafenet.repository;

import com.mksafenet.model.School;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolRepository extends JpaRepository<School, Long> {
    boolean existsByName(String name);
}
