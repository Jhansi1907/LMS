package com.lms.controller;

import com.lms.dto.CourseRequest;
import com.lms.model.Course;
import com.lms.model.Enrollment;
import com.lms.service.CourseService;
import com.lms.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;
    private final UserService userService;

    @Autowired
    public CourseController(CourseService courseService, UserService userService) {
        this.courseService = courseService;
        this.userService = userService;
    }

    @GetMapping("/public")
    public ResponseEntity<Page<Course>> getPublicCourses(Pageable pageable) {
        return ResponseEntity.ok(courseService.searchCourses("", pageable));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<Course>> getAllCourses(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return ResponseEntity.ok(courseService.searchCourses(search, pageable));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<Course> createCourse(@Valid @RequestBody CourseRequest request) {
        Course course = Course.builder()
            .code(request.getCode())
            .title(request.getTitle())
            .description(request.getDescription())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .maxStudents(request.getMaxStudents())
            .instructor(userService.getCurrentUser())
            .build();
            
        return ResponseEntity.ok(courseService.createCourse(course));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isInstructorOfCourse(#id)")
    public ResponseEntity<Course> updateCourse(@PathVariable Long id, 
                                             @Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(courseService.updateCourse(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isInstructorOfCourse(#id)")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/enroll")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Enrollment> enrollInCourse(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.enrollStudent(id, userService.getCurrentUser()));
    }

    @PostMapping("/{id}/unenroll")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> unenrollFromCourse(@PathVariable Long id) {
        courseService.unenrollStudent(id, userService.getCurrentUser().getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/enrollments")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isInstructorOfCourse(#id)")
    public ResponseEntity<Page<Enrollment>> getCourseEnrollments(
            @PathVariable Long id, 
            Pageable pageable) {
        return ResponseEntity.ok(courseService.getCourseEnrollments(id, pageable));
    }
}
