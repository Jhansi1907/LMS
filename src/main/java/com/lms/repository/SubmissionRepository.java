package com.lms.repository;

import com.lms.model.Assignment;
import com.lms.model.Submission;
import com.lms.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    
    List<Submission> findByStudent(User student);
    
    List<Submission> findByAssignment(Assignment assignment);
    
    Optional<Submission> findByAssignmentAndStudent(Assignment assignment, User student);
    
    boolean existsByAssignmentAndStudent(Assignment assignment, User student);
    
    @Query("SELECT s FROM Submission s WHERE s.assignment = :assignment ORDER BY s.score DESC")
    Page<Submission> findTopSubmissionsByScore(@Param("assignment") Assignment assignment, Pageable pageable);
    
    @Query("SELECT AVG(s.score) FROM Submission s WHERE s.student = :student")
    Double calculateStudentAverageScore(@Param("student") User student);
    
    @Query("SELECT s FROM Submission s WHERE s.assignment = :assignment AND s.score IS NULL")
    List<Submission> findUngradeSubmissions(@Param("assignment") Assignment assignment);
    
    @Query("SELECT COUNT(s) FROM Submission s WHERE s.assignment = :assignment AND s.score >= :minScore")
    long countPassingSubmissions(@Param("assignment") Assignment assignment, @Param("minScore") double minScore);
    
    @Query("SELECT s FROM Submission s WHERE " +
           "s.assignment.course.instructor = :instructor AND " +
           "s.score IS NULL")
    List<Submission> findPendingGradingByInstructor(@Param("instructor") User instructor);
}
