package com.buffalotraining.schedule.repository;

import com.buffalotraining.schedule.entity.ClassSession;
import com.buffalotraining.schedule.entity.ClassSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ClassSessionRepository extends JpaRepository<ClassSession, Long> {

    List<ClassSession> findByOrderByClassDateDescStartTimeAsc();

    List<ClassSession> findByClassDateOrderByStartTimeAsc(LocalDate classDate);

    List<ClassSession> findByStatusOrderByClassDateDescStartTimeAsc(ClassSessionStatus status);

    List<ClassSession> findByCoachIdOrderByClassDateDescStartTimeAsc(Long coachId);
}