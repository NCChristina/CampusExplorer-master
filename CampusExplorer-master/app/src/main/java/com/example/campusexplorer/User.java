package com.example.campusexplorer;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String userId;
    private String firstName;
    private String lastName;
    private String studentId;
    private String department;
    private String email;
    private Integer points;  // Added for points tracking
    private Map<String, Boolean> challenges;  // Added for challenge tracking

//    public User() {
//        // Initialize default values
//        this.points = 0;
//        this.challenges = new HashMap<>();
//    }

    public User(String userId, String firstName, String lastName, String studentId, String department, String email) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.studentId = studentId;
        this.department = department;
        this.email = email;
        this.points = 0;  // Default to 0 points

        // Initialize challenges with default values (incomplete)
        this.challenges = new HashMap<>();
        this.challenges.put("GLOBAL_LOUNGE", false);
        this.challenges.put("CLIFTON_TRAIL", false);
        this.challenges.put("CAREER_FAIR", false);
    }

    // Existing getters and setters...
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // New getters and setters for points and challenges
    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Map<String, Boolean> getChallenges() {
        return challenges;
    }

    public void setChallenges(Map<String, Boolean> challenges) {
        this.challenges = challenges;
    }

    // method to check if a specific challenge is completed
    public boolean isChallengeCompleted(String challengeId) {
        if (challenges == null || !challenges.containsKey(challengeId)) {
            return false;
        }
        return challenges.get(challengeId);
    }

    // method to mark a challenge as completed
    public void completeChallenge(String challengeId) {
        if (challenges == null) {
            challenges = new HashMap<>();
        }
        challenges.put(challengeId, true);
    }

    // method to add points
    public void addPoints(int pointsToAdd) {
        if (points == null) {
            points = 0;
        }
        points += pointsToAdd;
    }
}