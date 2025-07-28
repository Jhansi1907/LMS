package com.lms.controller;

import com.lms.dto.DashboardResponse;
import com.lms.model.Assignment;
import com.lms.model.Course;
import com.lms.model.User;
import com.lms.service.AssignmentService;
import com.lms.service.CourseService;
import com.lms.service.SubmissionService;
import com.lms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final CourseService courseService;
    private final AssignmentService assignmentService;
    private final SubmissionService submissionService;
    private final UserService userService;

    @Autowired
    public DashboardController(CourseService courseService,
                             AssignmentService assignmentService,
                             SubmissionService submissionService,
                             UserService userService) {
        this.courseService = courseService;
        this.assignmentService = assignmentService;
        this.submissionService = submissionService;
        this.userService = userService;
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardResponse> getAdminDashboard() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userService.countAllUsers());
        stats.put("totalCourses", courseService.countAllCourses());
        stats.put("activeUsers", userService.countActiveUsers());
        stats.put("recentlyJoinedUsers", userService.getRecentlyJoinedUsers());

        return ResponseEntity.ok(new DashboardResponse(stats));
    }

    @GetMapping("/instructor")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<DashboardResponse> getInstructorDashboard() {
        User instructor = userService.getCurrentUser();
        Map<String, Object> stats = new HashMap<>();
        
        List<Course> activeCourses = courseService.findActiveCoursesForInstructor(instructor);
        List<Assignment> upcomingAssignments = assignmentService.findUpcomingAssignmentsForInstructor(instructor);
        
        stats.put("activeCourses", activeCourses);
        stats.put("totalStudents", courseService.countTotalStudentsForInstructor(instructor));
        stats.put("upcomingAssignments", upcomingAssignments);
        stats.put("pendingGrading", submissionService.getPendingGradingByInstructor(instructor));

        return ResponseEntity.ok(new DashboardResponse(stats));
    }

    @GetMapping("/student")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<DashboardResponse> getStudentDashboard() {
        User student = userService.getCurrentUser();
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("enrolledCourses", courseService.getEnrolledCourses(student));
        stats.put("upcomingAssignments", assignmentService.getUpcomingAssignmentsForStudent(student));
        stats.put("recentGrades", submissionService.getRecentGrades(student));
        stats.put("overallProgress", submissionService.calculateStudentAverageScore(student));

        return ResponseEntity.ok(new DashboardResponse(stats));
    }
}
