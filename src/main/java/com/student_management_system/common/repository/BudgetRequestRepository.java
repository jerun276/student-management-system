package com.student_management_system.common.repository;

import com.student_management_system.common.model.BudgetRequest;
import com.student_management_system.common.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BudgetRequestRepository extends JpaRepository<BudgetRequest, Long> {
    // Find all requests with a specific status, ordered by date
    List<BudgetRequest> findByStatusOrderByRequestDateDesc(RequestStatus status);
}