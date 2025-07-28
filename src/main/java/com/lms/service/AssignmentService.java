package com.lms.service;

import com.lms.model.Assignment;
import com.lms.model.Course;
import com.lms.model.Submission;
import com.lms.repository.AssignmentRepository;
import com.lms.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;
    private final EmailService emailService;

    @Autowired
    public AssignmentService(AssignmentRepository assignmentRepository,
                           CourseRepository courseRepository,
                           EmailService emailService) {
        this.assignmentRepository = assignmentRepository;
        this.courseRepository = courseRepository;
        this.emailService = emailService;
    }

    public Assignment createAssignment(Long courseId, Assignment assignment) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));

        assignment.setCourse(course);
        Assignment savedAssignment = assignmentRepository.save(assignment);
        
        // Notify enrolled students
        course.getEnrollments().forEach(enrollment ->
            emailService.sendNewAssignmentNotification(enrollment.getStudent(), savedAssignment));
        
        return savedAssignment;
    }

    public Assignment updateAssignment(Long assignmentId, Assignment assignmentDetails) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new RuntimeException("Assignment not found"));

        assignment.setTitle(assignmentDetails.getTitle());
        assignment.setDescription(assignmentDetails.getDescription());
        assignment.setDueDate(assignmentDetails.getDueDate());
        assignment.setTotalPoints(assignmentDetails.getTotalPoints());

        Assignment updatedAssignment = assignmentRepository.save(assignment);
        
        // Notify students of changes
        assignment.getCourse().getEnrollments().forEach(enrollment ->
            emailService.sendAssignmentUpdateNotification(enrollment.getStudent(), updatedAssignment));
        
        return updatedAssignment;
    }

    public void deleteAssignment(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new RuntimeException("Assignment not found"));
            
        // Notify students of deletion
        assignment.getCourse().getEnrollments().forEach(enrollment ->
            emailService.sendAssignmentDeletedNotification(enrollment.getStudent(), assignment));
            
        assignmentRepository.delete(assignment);
    }

    public List<Assignment> getUpcomingAssignments(Long courseId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));
            
        return assignmentRepository.findUpcomingAssignments(course, LocalDateTime.now());
    }

    public Page<Assignment> getCourseAssignments(Long courseId, Pageable pageable) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));
            
        return assignmentRepository.findByCourse(course, pageable);
    }

    public Double calculateAverageScore(Long assignmentId) {
        return assignmentRepository.calculateAverageScore(assignmentId);
    }

    private void validateAssignment(Assignment assignment) {
        if (assignment.getDueDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Due date must be in the future");
        }
        if (assignment.getTotalPoints().doubleValue() <= 0) {
            throw new RuntimeException("Total points must be greater than 0");
        }
    }
}
