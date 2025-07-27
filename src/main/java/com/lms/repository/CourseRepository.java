package com.lms.repository;

import com.lms.model.Course;
import com.lms.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    List<Course> findByInstructor(User instructor);
    
    Page<Course> findByInstructor(User instructor, Pageable pageable);
    
    Optional<Course> findByCode(String code);
    
    boolean existsByCode(String code);
    
    @Query("SELECT c FROM Course c WHERE c.startDate <= :date AND c.endDate >= :date")
    List<Course> findActiveCourses(@Param("date") LocalDate date);
    
    @Query("SELECT c FROM Course c WHERE " +
           "LOWER(c.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Course> searchCourses(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT c FROM Course c WHERE c.instructor = :instructor AND " +
           "c.startDate <= :currentDate AND c.endDate >= :currentDate")
    List<Course> findActiveCoursesForInstructor(@Param("instructor") User instructor, 
                                               @Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT COUNT(e) FROM Course c JOIN c.enrollments e WHERE c.id = :courseId")
    long countEnrollments(@Param("courseId") Long courseId);
}
