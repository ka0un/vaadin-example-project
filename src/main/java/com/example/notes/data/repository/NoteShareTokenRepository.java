package com.example.notes.data.repository;

import com.example.notes.data.entity.NoteShareToken;
import com.example.notes.data.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NoteShareTokenRepository extends JpaRepository<NoteShareToken, Long> {
    Optional<NoteShareToken> findByTokenAndActiveTrue(String token);
    Optional<NoteShareToken> findByNoteAndActiveTrue(Note note);
}