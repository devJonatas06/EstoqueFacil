package com.example.EstoqueFacil.repository;

import com.example.EstoqueFacil.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    @Query("""
            SELECT r FROM Role r
            ORDER BY r.name
            """)
    List<Role> findAllOrdered();

}