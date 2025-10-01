package com.student_management_system.common.service;

import com.student_management_system.common.model.AcademicYear;
import com.student_management_system.common.repository.AcademicYearRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AcademicYearService
 * This demonstrates service layer testing with mocked dependencies
 */
@ExtendWith(MockitoExtension.class)
class AcademicYearServiceTest {

    @Mock
    private AcademicYearRepository academicYearRepository;

    @InjectMocks
    private AcademicYearService academicYearService;

    private AcademicYear testAcademicYear;

    @BeforeEach
    void setUp() {
        testAcademicYear = new AcademicYear();
        testAcademicYear.setId(1L);
        testAcademicYear.setName("2024-2025");
        testAcademicYear.setStartDate(LocalDate.of(2024, 9, 1));
        testAcademicYear.setEndDate(LocalDate.of(2025, 8, 31));
        testAcademicYear.setActive(false);
    }

    @Test
    void createAcademicYear_ValidData_ShouldCreateAndReturnAcademicYear() {
        // ARRANGE
        String name = "2025-2026";
        LocalDate startDate = LocalDate.of(2025, 9, 1);
        LocalDate endDate = LocalDate.of(2026, 8, 31);

        when(academicYearRepository.existsByName(name)).thenReturn(false);
        when(academicYearRepository.save(any(AcademicYear.class))).thenReturn(testAcademicYear);

        // ACT
        AcademicYear result = academicYearService.createAcademicYear(name, startDate, endDate);

        // ASSERT
        assertThat(result).isNotNull();
        verify(academicYearRepository).existsByName(name);
        verify(academicYearRepository).save(any(AcademicYear.class));
    }

    @Test
    void createAcademicYear_DuplicateName_ShouldThrowException() {
        // ARRANGE
        String duplicateName = "2024-2025";
        LocalDate startDate = LocalDate.of(2024, 9, 1);
        LocalDate endDate = LocalDate.of(2025, 8, 31);

        when(academicYearRepository.existsByName(duplicateName)).thenReturn(true);

        // ACT & ASSERT
        assertThatThrownBy(() -> 
            academicYearService.createAcademicYear(duplicateName, startDate, endDate))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Academic year with name");

        verify(academicYearRepository, never()).save(any(AcademicYear.class));
    }

    @Test
    void createAcademicYear_ValidDates_ShouldCreateSuccessfully() {
        // ARRANGE
        String name = "2025-2026";
        LocalDate startDate = LocalDate.of(2025, 9, 1);
        LocalDate endDate = LocalDate.of(2026, 8, 31);

        when(academicYearRepository.existsByName(name)).thenReturn(false);
        when(academicYearRepository.save(any(AcademicYear.class))).thenReturn(testAcademicYear);

        // ACT
        AcademicYear result = academicYearService.createAcademicYear(name, startDate, endDate);

        // ASSERT
        assertThat(result).isNotNull();
        verify(academicYearRepository).save(any(AcademicYear.class));
    }

    @Test
    void setActiveAcademicYear_ValidYear_ShouldActivateYearAndDeactivateOthers() {
        // ARRANGE
        Long yearId = 1L;
        AcademicYear currentActive = new AcademicYear();
        currentActive.setId(2L);
        currentActive.setActive(true);
        
        List<AcademicYear> allYears = Arrays.asList(testAcademicYear, currentActive);

        when(academicYearRepository.findAll()).thenReturn(allYears);
        when(academicYearRepository.findById(yearId)).thenReturn(Optional.of(testAcademicYear));
        when(academicYearRepository.save(any(AcademicYear.class))).thenReturn(testAcademicYear);

        // ACT
        academicYearService.setActiveAcademicYear(yearId);

        // ASSERT
        assertThat(testAcademicYear.isActive()).isTrue();
        assertThat(currentActive.isActive()).isFalse();
        
        verify(academicYearRepository, times(3)).save(any(AcademicYear.class)); // 2 deactivations + 1 activation
    }

    @Test
    void setActiveAcademicYear_NonExistentYear_ShouldThrowException() {
        // ARRANGE
        Long nonExistentId = 999L;
        when(academicYearRepository.findAll()).thenReturn(Arrays.asList());
        when(academicYearRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThatThrownBy(() -> 
            academicYearService.setActiveAcademicYear(nonExistentId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Academic year with ID 999 not found");

        verify(academicYearRepository, never()).save(any(AcademicYear.class));
    }

    @Test
    void getCurrentAcademicYear_ActiveYearExists_ShouldReturnActiveYear() {
        // ARRANGE
        testAcademicYear.setActive(true);
        when(academicYearRepository.findByIsActiveTrue()).thenReturn(Optional.of(testAcademicYear));

        // ACT
        Optional<AcademicYear> result = academicYearService.getCurrentAcademicYear();

        // ASSERT
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testAcademicYear);
        assertThat(result.get().isActive()).isTrue();
    }

    @Test
    void getCurrentAcademicYear_NoActiveYear_ShouldReturnEmpty() {
        // ARRANGE
        when(academicYearRepository.findByIsActiveTrue()).thenReturn(Optional.empty());

        // ACT
        Optional<AcademicYear> result = academicYearService.getCurrentAcademicYear();

        // ASSERT
        assertThat(result).isEmpty();
    }

    @Test
    void getAllAcademicYears_ShouldReturnAllYearsOrderedByStartDate() {
        // ARRANGE
        AcademicYear year1 = createAcademicYear("2023-2024", LocalDate.of(2023, 9, 1));
        AcademicYear year2 = createAcademicYear("2024-2025", LocalDate.of(2024, 9, 1));
        List<AcademicYear> allYears = Arrays.asList(year2, year1); // Reverse order

        when(academicYearRepository.findAllOrderByStartDateDesc()).thenReturn(allYears);

        // ACT
        List<AcademicYear> result = academicYearService.getAllAcademicYears();

        // ASSERT
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(year2, year1); // Should maintain repository order
    }

    @Test
    void deleteAcademicYear_InactiveYear_ShouldDeleteSuccessfully() {
        // ARRANGE
        Long yearId = 1L;
        testAcademicYear.setActive(false);
        
        when(academicYearRepository.findById(yearId)).thenReturn(Optional.of(testAcademicYear));

        // ACT
        academicYearService.deleteAcademicYear(yearId);

        // ASSERT
        verify(academicYearRepository).deleteById(yearId);
    }

    @Test
    void deleteAcademicYear_ActiveYear_ShouldThrowException() {
        // ARRANGE
        Long yearId = 1L;
        testAcademicYear.setActive(true);
        
        when(academicYearRepository.findById(yearId)).thenReturn(Optional.of(testAcademicYear));

        // ACT & ASSERT
        assertThatThrownBy(() -> 
            academicYearService.deleteAcademicYear(yearId))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Cannot delete the active academic year");

        verify(academicYearRepository, never()).deleteById(any(Long.class));
    }

    // Helper method
    private AcademicYear createAcademicYear(String name, LocalDate startDate) {
        AcademicYear year = new AcademicYear();
        year.setName(name);
        year.setStartDate(startDate);
        year.setEndDate(startDate.plusMonths(11));
        return year;
    }
}
