package com.example.notes.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.notes.data.entity.Note;
import com.example.notes.data.entity.User;

public interface NoteRepository extends JpaRepository<Note, Long> {
    java.util.List<Note> findByUserOrderByIdDesc(User user);
}
