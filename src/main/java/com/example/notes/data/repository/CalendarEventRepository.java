package com.example.notes.data.repository;

import com.example.notes.data.entity.CalendarEvent;
import com.example.notes.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {
    List<CalendarEvent> findByUserAndEventDateBetweenOrderByEventDateAscStartTimeAsc(
            User user, LocalDate from, LocalDate to);
    List<CalendarEvent> findByUserAndEventDateOrderByStartTimeAsc(User user, LocalDate date);
}