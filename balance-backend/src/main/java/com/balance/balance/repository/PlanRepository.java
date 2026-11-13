package com.balance.balance.repository;

import com.balance.balance.model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, Long> {

    Optional<Plan> findByNombre(String nombre);

    boolean existsByNombre(String nombre);
}
