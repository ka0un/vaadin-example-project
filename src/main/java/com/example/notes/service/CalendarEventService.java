package com.example.notes.service;

import com.example.notes.data.entity.CalendarEvent;
import com.example.notes.data.entity.User;
import com.example.notes.data.repository.CalendarEventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CalendarEventService {

    private final CalendarEventRepository repo;

    public CalendarEventService(CalendarEventRepository repo) {
        this.repo = repo;
    }

    public List<CalendarEvent> getEventsForMonth(User user, LocalDate monthStart) {
        LocalDate from = monthStart.withDayOfMonth(1);
        LocalDate to = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
        return repo.findByUserAndEventDateBetweenOrderByEventDateAscStartTimeAsc(user, from, to);
    }

    public List<CalendarEvent> getEventsForDay(User user, LocalDate date) {
        return repo.findByUserAndEventDateOrderByStartTimeAsc(user, date);
    }

    public void save(CalendarEvent event) {
        repo.save(event);
    }

    public void delete(CalendarEvent event) {
        repo.delete(event);
    }
}