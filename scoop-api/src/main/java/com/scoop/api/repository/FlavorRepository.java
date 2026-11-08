package com.scoop.api.repository;

import com.scoop.api.entity.Flavor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlavorRepository extends JpaRepository<Flavor, Long> {
    List<Flavor> findAllByOrderByNameAsc();
}
