package com.lms.repository;

import com.lms.model.Course;
import com.lms.model.Enrollment;
import com.lms.model.Enrollment.EnrollmentStatus;
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
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    
    List<Enrollment> findByStudent(User student);
    
    List<Enrollment> findByCourse(Course course);
    
    Optional<Enrollment> findByStudentAndCourse(User student, Course course);
    
    boolean existsByStudentAndCourse(User student, Course course);
    
    List<Enrollment> findByStudentAndStatus(User student, EnrollmentStatus status);
    
    Page<Enrollment> findByCourseAndStatus(Course course, EnrollmentStatus status, Pageable pageable);
    
    @Query("SELECT e FROM Enrollment e WHERE e.course = :course AND e.status = :status")
    List<Enrollment> findEnrollmentsByCourseAndStatus(@Param("course") Course course, 
                                                     @Param("status") EnrollmentStatus status);
    
    @Query("SELECT AVG(e.grade) FROM Enrollment e WHERE e.course = :course AND e.status = 'COMPLETED'")
    Double calculateAverageGradeForCourse(@Param("course") Course course);
    
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course = :course AND e.status = :status")
    long countByCourseAndStatus(@Param("course") Course course, @Param("status") EnrollmentStatus status);
}
