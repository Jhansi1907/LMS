package com.lms.controller;

import com.lms.dto.AssignmentRequest;
import com.lms.model.Assignment;
import com.lms.service.AssignmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses/{courseId}/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    @Autowired
    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping
    @PreAuthorize("@securityUtils.canAccessCourse(#courseId)")
    public ResponseEntity<Page<Assignment>> getCourseAssignments(
            @PathVariable Long courseId, 
            Pageable pageable) {
        return ResponseEntity.ok(assignmentService.getCourseAssignments(courseId, pageable));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isInstructorOfCourse(#courseId)")
    public ResponseEntity<Assignment> createAssignment(
            @PathVariable Long courseId,
            @Valid @RequestBody AssignmentRequest request) {
        Assignment assignment = Assignment.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .dueDate(request.getDueDate())
            .totalPoints(request.getTotalPoints())
            .build();
            
        return ResponseEntity.ok(assignmentService.createAssignment(courseId, assignment));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isInstructorOfAssignment(#id)")
    public ResponseEntity<Assignment> updateAssignment(
            @PathVariable Long courseId,
            @PathVariable Long id,
            @Valid @RequestBody AssignmentRequest request) {
        Assignment assignment = Assignment.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .dueDate(request.getDueDate())
            .totalPoints(request.getTotalPoints())
            .build();
            
        return ResponseEntity.ok(assignmentService.updateAssignment(id, assignment));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isInstructorOfAssignment(#id)")
    public ResponseEntity<Void> deleteAssignment(
            @PathVariable Long courseId,
            @PathVariable Long id) {
        assignmentService.deleteAssignment(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/upcoming")
    @PreAuthorize("@securityUtils.canAccessCourse(#courseId)")
    public ResponseEntity<List<Assignment>> getUpcomingAssignments(
            @PathVariable Long courseId) {
        return ResponseEntity.ok(assignmentService.getUpcomingAssignments(courseId));
    }

    @GetMapping("/{id}/average-score")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isInstructorOfAssignment(#id)")
    public ResponseEntity<Double> getAverageScore(
            @PathVariable Long courseId,
            @PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.calculateAverageScore(id));
    }
}
