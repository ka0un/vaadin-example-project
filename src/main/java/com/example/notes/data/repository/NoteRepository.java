package com.example.notes.data.repository;

import com.example.notes.data.entity.Note;
import com.example.notes.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByUser(User user);
}
