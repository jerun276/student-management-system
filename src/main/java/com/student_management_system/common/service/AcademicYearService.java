package com.student_management_system.common.service;

import com.student_management_system.common.model.AcademicYear;
import com.student_management_system.common.repository.AcademicYearRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AcademicYearService {
    
    @Autowired
    private AcademicYearRepository academicYearRepository;
    
    /**
     * Get all academic years
     */
    public List<AcademicYear> getAllAcademicYears() {
        return academicYearRepository.findAllOrderByStartDateDesc();
    }
    
    /**
     * Get academic year by ID
     */
    public Optional<AcademicYear> getAcademicYearById(Long id) {
        return academicYearRepository.findById(id);
    }
    
    /**
     * Get the currently active academic year
     */
    public Optional<AcademicYear> getCurrentAcademicYear() {
        return academicYearRepository.findByIsActiveTrue();
    }
    
    /**
     * Get academic year by name
     */
    public Optional<AcademicYear> getAcademicYearByName(String name) {
        return academicYearRepository.findByName(name);
    }
    
    /**
     * Create a new academic year
     */
    public AcademicYear createAcademicYear(String name, LocalDate startDate, LocalDate endDate) {
        if (academicYearRepository.existsByName(name)) {
            throw new IllegalArgumentException("Academic year with name '" + name + "' already exists");
        }
        
        AcademicYear academicYear = new AcademicYear();
        academicYear.setName(name);
        academicYear.setStartDate(startDate);
        academicYear.setEndDate(endDate);
        academicYear.setActive(false); // New academic years are not active by default
        
        return academicYearRepository.save(academicYear);
    }
    
    /**
     * Update an academic year
     */
    public AcademicYear updateAcademicYear(AcademicYear academicYear) {
        return academicYearRepository.save(academicYear);
    }
    
    /**
     * Set an academic year as active (and deactivate others)
     */
    public AcademicYear setActiveAcademicYear(Long academicYearId) {
        // First, deactivate all academic years
        List<AcademicYear> allAcademicYears = academicYearRepository.findAll();
        for (AcademicYear ay : allAcademicYears) {
            ay.setActive(false);
            academicYearRepository.save(ay);
        }
        
        // Then activate the specified one
        Optional<AcademicYear> academicYearOpt = academicYearRepository.findById(academicYearId);
        if (academicYearOpt.isPresent()) {
            AcademicYear academicYear = academicYearOpt.get();
            academicYear.setActive(true);
            return academicYearRepository.save(academicYear);
        } else {
            throw new IllegalArgumentException("Academic year with ID " + academicYearId + " not found");
        }
    }
    
    /**
     * Delete an academic year
     */
    public void deleteAcademicYear(Long id) {
        Optional<AcademicYear> academicYearOpt = academicYearRepository.findById(id);
        if (academicYearOpt.isPresent()) {
            AcademicYear academicYear = academicYearOpt.get();
            if (academicYear.isActive()) {
                throw new IllegalStateException("Cannot delete the active academic year");
            }
            academicYearRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("Academic year with ID " + id + " not found");
        }
    }
    
    /**
     * Check if an academic year name is available
     */
    public boolean isNameAvailable(String name) {
        return !academicYearRepository.existsByName(name);
    }
}
