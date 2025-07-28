package com.lms.controller;

import com.lms.dto.GradeSubmissionRequest;
import com.lms.model.Submission;
import com.lms.service.SubmissionService;
import com.lms.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/assignments/{assignmentId}/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;
    private final UserService userService;

    @Autowired
    public SubmissionController(SubmissionService submissionService, UserService userService) {
        this.submissionService = submissionService;
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT') and @securityUtils.canSubmitToAssignment(#assignmentId)")
    public ResponseEntity<Submission> submitAssignment(
            @PathVariable Long assignmentId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(submissionService.submitAssignment(
            assignmentId, 
            userService.getCurrentUser(), 
            file
        ));
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.canAccessSubmission(#id)")
    public ResponseEntity<Resource> downloadSubmission(
            @PathVariable Long assignmentId,
            @PathVariable Long id) {
        Submission submission = submissionService.getSubmission(id);
        Resource resource = submissionService.loadSubmissionFile(submission.getFilePath());
        
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + submission.getFileName() + "\"")
            .body(resource);
    }

    @PutMapping("/{id}/grade")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isInstructorOfSubmission(#id)")
    public ResponseEntity<Submission> gradeSubmission(
            @PathVariable Long assignmentId,
            @PathVariable Long id,
            @Valid @RequestBody GradeSubmissionRequest request) {
        return ResponseEntity.ok(submissionService.gradeSubmission(
            id,
            request.getScore(),
            request.getFeedback()
        ));
    }

    @GetMapping("/student")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<Submission>> getStudentSubmissions() {
        return ResponseEntity.ok(submissionService.getStudentSubmissions(
            userService.getCurrentUser()
        ));
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("@securityUtils.canAccessSubmission(#id)")
    public ResponseEntity<Resource> downloadSubmission(
            @PathVariable Long assignmentId,
            @PathVariable Long id) throws IOException {
        byte[] file = submissionService.downloadSubmission(id);
        ByteArrayResource resource = new ByteArrayResource(file);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=submission")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .contentLength(file.length)
            .body(resource);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<List<Submission>> getPendingSubmissions() {
        return ResponseEntity.ok(submissionService.getPendingGradingByInstructor(
            userService.getCurrentUser()
        ));
    }

    @GetMapping("/student/average")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Double> getStudentAverageScore() {
        return ResponseEntity.ok(submissionService.calculateStudentAverageScore(
            userService.getCurrentUser()
        ));
    }
}
