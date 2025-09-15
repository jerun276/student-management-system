package com.student_management_system.teacher.repository;

import com.student_management_system.teacher.model.StudyMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyMaterialRepository extends JpaRepository<StudyMaterial, Long> {
    // Find all materials for a specific subject
    List<StudyMaterial> findBySubjectId(Long subjectId);
}