package com.mksafenet.repository;

import com.mksafenet.model.Scenario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScenarioRepository extends JpaRepository<Scenario, Long> {
	List<Scenario> findByTypeOfScenario(int typeOfScenario);
}
