package com.lms.service;

import com.lms.model.Course;
import com.lms.model.Enrollment;
import com.lms.model.User;
import com.lms.repository.CourseRepository;
import com.lms.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EmailService emailService;

    @Autowired
    public CourseService(CourseRepository courseRepository, 
                        EnrollmentRepository enrollmentRepository,
                        EmailService emailService) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.emailService = emailService;
    }

    public Course createCourse(Course course) {
        validateCourse(course);
        Course savedCourse = courseRepository.save(course);
        emailService.sendCourseCreationNotification(course.getInstructor(), savedCourse);
        return savedCourse;
    }

    public Course updateCourse(Long courseId, Course courseDetails) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));

        course.setTitle(courseDetails.getTitle());
        course.setDescription(courseDetails.getDescription());
        course.setStartDate(courseDetails.getStartDate());
        course.setEndDate(courseDetails.getEndDate());
        course.setMaxStudents(courseDetails.getMaxStudents());

        return courseRepository.save(course);
    }

    public void deleteCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));
            
        // Notify enrolled students
        course.getEnrollments().forEach(enrollment -> 
            emailService.sendCourseDeletedNotification(enrollment.getStudent(), course));
            
        courseRepository.delete(course);
    }

    public Enrollment enrollStudent(Long courseId, User student) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));

        if (enrollmentRepository.existsByStudentAndCourse(student, course)) {
            throw new RuntimeException("Student already enrolled in this course");
        }

        if (enrollmentRepository.countByCourseAndStatus(course, Enrollment.EnrollmentStatus.ACTIVE) 
            >= course.getMaxStudents()) {
            throw new RuntimeException("Course is full");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setStatus(Enrollment.EnrollmentStatus.ACTIVE);

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        emailService.sendEnrollmentConfirmation(student, course);
        
        return savedEnrollment;
    }

    public void unenrollStudent(Long courseId, Long studentId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));

        Enrollment enrollment = enrollmentRepository.findByStudentAndCourse(
                new User().builder().id(studentId).build(), course)
            .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        enrollment.setStatus(Enrollment.EnrollmentStatus.DROPPED);
        enrollmentRepository.save(enrollment);
        
        emailService.sendUnenrollmentNotification(enrollment.getStudent(), course);
    }

    public List<Course> findActiveCourses() {
        return courseRepository.findActiveCourses(LocalDate.now());
    }

    public Page<Course> searchCourses(String searchTerm, Pageable pageable) {
        return courseRepository.searchCourses(searchTerm, pageable);
    }

    private void validateCourse(Course course) {
        if (course.getStartDate().isAfter(course.getEndDate())) {
            throw new RuntimeException("Start date must be before end date");
        }
        if (course.getMaxStudents() < 1) {
            throw new RuntimeException("Maximum students must be at least 1");
        }
        if (courseRepository.existsByCode(course.getCode())) {
            throw new RuntimeException("Course code already exists");
        }
    }
}
