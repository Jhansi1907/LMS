package com.lms.repository;

import com.lms.model.Assignment;
import com.lms.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    
    List<Assignment> findByCourse(Course course);
    
    Page<Assignment> findByCourse(Course course, Pageable pageable);
    
    @Query("SELECT a FROM Assignment a WHERE a.course = :course AND a.dueDate > :currentDate")
    List<Assignment> findUpcomingAssignments(@Param("course") Course course, 
                                           @Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT a FROM Assignment a WHERE a.course = :course AND " +
           "a.dueDate BETWEEN :startDate AND :endDate")
    List<Assignment> findAssignmentsInDateRange(@Param("course") Course course, 
                                              @Param("startDate") LocalDateTime startDate, 
                                              @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(s) FROM Assignment a JOIN a.submissions s WHERE a.id = :assignmentId")
    long countSubmissions(@Param("assignmentId") Long assignmentId);
    
    @Query("SELECT AVG(s.score) FROM Assignment a JOIN a.submissions s WHERE a.id = :assignmentId")
    Double calculateAverageScore(@Param("assignmentId") Long assignmentId);
}
