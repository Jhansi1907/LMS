package com.lms.service;

import com.lms.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Autowired
    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Async
    public void sendWelcomeEmail(User user) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", user.getFirstName());
        variables.put("role", user.getRole());
        
        sendTemplateEmail(user.getEmail(), 
            "Welcome to LMS", 
            "welcome-email",
            variables);
    }

    @Async
    public void sendCourseCreationNotification(User instructor, Course course) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("instructorName", instructor.getFirstName());
        variables.put("courseName", course.getTitle());
        variables.put("courseCode", course.getCode());
        
        sendTemplateEmail(instructor.getEmail(),
            "Course Created Successfully",
            "course-creation",
            variables);
    }

    @Async
    public void sendEnrollmentConfirmation(User student, Course course) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("studentName", student.getFirstName());
        variables.put("courseName", course.getTitle());
        variables.put("startDate", course.getStartDate());
        
        sendTemplateEmail(student.getEmail(),
            "Enrollment Confirmation",
            "enrollment-confirmation",
            variables);
    }

    @Async
    public void sendNewAssignmentNotification(User student, Assignment assignment) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("studentName", student.getFirstName());
        variables.put("assignmentTitle", assignment.getTitle());
        variables.put("dueDate", assignment.getDueDate());
        variables.put("courseName", assignment.getCourse().getTitle());
        
        sendTemplateEmail(student.getEmail(),
            "New Assignment Posted",
            "new-assignment",
            variables);
    }

    @Async
    public void sendGradingNotification(User student, Submission submission) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("studentName", student.getFirstName());
        variables.put("assignmentTitle", submission.getAssignment().getTitle());
        variables.put("score", submission.getScore());
        variables.put("totalPoints", submission.getAssignment().getTotalPoints());
        variables.put("feedback", submission.getFeedback());
        
        sendTemplateEmail(student.getEmail(),
            "Assignment Graded",
            "grading-notification",
            variables);
    }

    private void sendTemplateEmail(String to, String subject, String templateName, 
                                 Map<String, Object> variables) {
        try {
            Context context = new Context();
            variables.forEach(context::setVariable);

            String htmlContent = templateEngine.process(templateName, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
