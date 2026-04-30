package com.buffalotraining.schedule.service;

import com.buffalotraining.schedule.dto.ClassSessionResponse;
import com.buffalotraining.schedule.dto.CreateClassSessionRequest;
import com.buffalotraining.schedule.dto.UpdateClassSessionRequest;
import com.buffalotraining.schedule.dto.UpdateClassSessionStatusRequest;
import com.buffalotraining.schedule.entity.ClassSession;
import com.buffalotraining.schedule.entity.ClassSessionStatus;
import com.buffalotraining.schedule.repository.ClassSessionRepository;
import com.buffalotraining.user.entity.User;
import com.buffalotraining.user.entity.UserStatus;
import com.buffalotraining.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassSessionService {

    private final ClassSessionRepository classSessionRepository;
    private final UserRepository userRepository;

    public ClassSessionResponse createClassSession(CreateClassSessionRequest request) {
        validateTimeRange(request.getStartTime(), request.getEndTime());

        User coach = userRepository.findById(request.getCoachId())
                .orElseThrow(() -> new IllegalArgumentException("Coach not found with id: " + request.getCoachId()));

        validateCoach(coach);

        ClassSession classSession = ClassSession.builder()
                .className(request.getClassName().trim())
                .description(request.getDescription())
                .classDate(request.getClassDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .capacity(request.getCapacity())
                .reservedSpots(0)
                .coach(coach)
                .status(ClassSessionStatus.OPEN)
                .notes(request.getNotes())
                .build();

        ClassSession savedClassSession = classSessionRepository.save(classSession);

        return ClassSessionResponse.fromEntity(savedClassSession);
    }

    public List<ClassSessionResponse> getAllClassSessions() {
        return classSessionRepository.findByOrderByClassDateDescStartTimeAsc()
                .stream()
                .map(ClassSessionResponse::fromEntity)
                .toList();
    }

    public ClassSessionResponse getClassSessionById(Long id) {
        ClassSession classSession = findClassSessionEntityById(id);
        return ClassSessionResponse.fromEntity(classSession);
    }

    public List<ClassSessionResponse> getClassSessionsByDate(java.time.LocalDate date) {
        return classSessionRepository.findByClassDateOrderByStartTimeAsc(date)
                .stream()
                .map(ClassSessionResponse::fromEntity)
                .toList();
    }

    public List<ClassSessionResponse> getClassSessionsByStatus(ClassSessionStatus status) {
        return classSessionRepository.findByStatusOrderByClassDateDescStartTimeAsc(status)
                .stream()
                .map(ClassSessionResponse::fromEntity)
                .toList();
    }

    public List<ClassSessionResponse> getClassSessionsByCoach(Long coachId) {
        return classSessionRepository.findByCoachIdOrderByClassDateDescStartTimeAsc(coachId)
                .stream()
                .map(ClassSessionResponse::fromEntity)
                .toList();
    }

    public ClassSessionResponse updateClassSession(Long id, UpdateClassSessionRequest request) {
        ClassSession classSession = findClassSessionEntityById(id);

        LocalTime finalStartTime = request.getStartTime() != null ? request.getStartTime() : classSession.getStartTime();
        LocalTime finalEndTime = request.getEndTime() != null ? request.getEndTime() : classSession.getEndTime();

        validateTimeRange(finalStartTime, finalEndTime);

        if (request.getClassName() != null && !request.getClassName().isBlank()) {
            classSession.setClassName(request.getClassName().trim());
        }

        if (request.getDescription() != null) {
            classSession.setDescription(request.getDescription());
        }

        if (request.getClassDate() != null) {
            classSession.setClassDate(request.getClassDate());
        }

        if (request.getStartTime() != null) {
            classSession.setStartTime(request.getStartTime());
        }

        if (request.getEndTime() != null) {
            classSession.setEndTime(request.getEndTime());
        }

        if (request.getCapacity() != null) {
            if (request.getCapacity() < classSession.getReservedSpots()) {
                throw new IllegalArgumentException("Capacity cannot be lower than current reserved spots");
            }

            classSession.setCapacity(request.getCapacity());

            if (classSession.getReservedSpots() < request.getCapacity()
                    && classSession.getStatus() == ClassSessionStatus.FULL) {
                classSession.setStatus(ClassSessionStatus.OPEN);
            }
        }

        if (request.getCoachId() != null) {
            User coach = userRepository.findById(request.getCoachId())
                    .orElseThrow(() -> new IllegalArgumentException("Coach not found with id: " + request.getCoachId()));

            validateCoach(coach);
            classSession.setCoach(coach);
        }

        if (request.getNotes() != null) {
            classSession.setNotes(request.getNotes());
        }

        ClassSession updatedClassSession = classSessionRepository.save(classSession);

        return ClassSessionResponse.fromEntity(updatedClassSession);
    }

    public ClassSessionResponse updateClassSessionStatus(Long id, UpdateClassSessionStatusRequest request) {
        ClassSession classSession = findClassSessionEntityById(id);

        classSession.setStatus(request.getStatus());

        ClassSession updatedClassSession = classSessionRepository.save(classSession);

        return ClassSessionResponse.fromEntity(updatedClassSession);
    }

    public void deleteClassSession(Long id) {
        ClassSession classSession = findClassSessionEntityById(id);

        if (classSession.getReservedSpots() > 0) {
            throw new IllegalArgumentException("Cannot delete a class session with existing reservations");
        }

        classSessionRepository.delete(classSession);
    }

    private ClassSession findClassSessionEntityById(Long id) {
        return classSessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Class session not found with id: " + id));
    }

    private void validateCoach(User user) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("Coach user is not active");
        }

        String roleName = user.getRole().getName();

        if (!"COACH".equals(roleName) && !"ADMIN".equals(roleName)) {
            throw new IllegalArgumentException("Selected user is not a coach or admin");
        }
    }

    private void validateTimeRange(LocalTime startTime, LocalTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
    }
}