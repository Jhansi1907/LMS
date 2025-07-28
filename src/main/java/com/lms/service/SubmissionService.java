package com.lms.service;

import com.lms.model.Assignment;
import com.lms.model.Submission;
import com.lms.model.User;
import com.lms.repository.AssignmentRepository;
import com.lms.repository.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final FileStorageService fileStorageService;
    private final EmailService emailService;

    @Autowired
    public SubmissionService(SubmissionRepository submissionRepository,
                           AssignmentRepository assignmentRepository,
                           FileStorageService fileStorageService,
                           EmailService emailService) {
        this.submissionRepository = submissionRepository;
        this.assignmentRepository = assignmentRepository;
        this.fileStorageService = fileStorageService;
        this.emailService = emailService;
    }

    public Submission submitAssignment(Long assignmentId, User student, MultipartFile file) 
            throws IOException {
        Assignment assignment = assignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if (LocalDateTime.now().isAfter(assignment.getDueDate())) {
            throw new RuntimeException("Assignment submission deadline has passed");
        }

        // Check if student already submitted
        if (submissionRepository.existsByAssignmentAndStudent(assignment, student)) {
            throw new RuntimeException("You have already submitted this assignment");
        }

        // Store the file
        String fileUrl = fileStorageService.storeFile(file);

        Submission submission = new Submission();
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setFileUrl(fileUrl);
        submission.setSubmissionDate(LocalDateTime.now());

        Submission savedSubmission = submissionRepository.save(submission);
        
        // Notify instructor
        emailService.sendNewSubmissionNotification(assignment.getCourse().getInstructor(), savedSubmission);
        
        return savedSubmission;
    }

    public Submission gradeSubmission(Long submissionId, BigDecimal score, String feedback) {
        Submission submission = submissionRepository.findById(submissionId)
            .orElseThrow(() -> new RuntimeException("Submission not found"));

        if (score.compareTo(submission.getAssignment().getTotalPoints()) > 0) {
            throw new RuntimeException("Score cannot exceed total points");
        }

        submission.setScore(score);
        submission.setFeedback(feedback);
        
        Submission gradedSubmission = submissionRepository.save(submission);
        
        // Notify student
        emailService.sendGradingNotification(submission.getStudent(), gradedSubmission);
        
        return gradedSubmission;
    }

    public List<Submission> getPendingGradingByInstructor(User instructor) {
        return submissionRepository.findPendingGradingByInstructor(instructor);
    }

    public List<Submission> getStudentSubmissions(User student) {
        return submissionRepository.findByStudent(student);
    }

    public Double calculateStudentAverageScore(User student) {
        return submissionRepository.calculateStudentAverageScore(student);
    }

    public byte[] downloadSubmission(Long submissionId) throws IOException {
        Submission submission = submissionRepository.findById(submissionId)
            .orElseThrow(() -> new RuntimeException("Submission not found"));
            
        return fileStorageService.getFile(submission.getFileUrl());
    }
}
