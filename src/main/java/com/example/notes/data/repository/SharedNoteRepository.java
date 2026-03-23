package com.example.notes.data.repository;

import com.example.notes.data.entity.SharedNote;
import com.example.notes.data.entity.User;
import com.example.notes.data.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SharedNoteRepository extends JpaRepository<SharedNote, Long> {
    List<SharedNote> findBySharedWithUser(User user);
    List<SharedNote> findByNote(Note note);
    Optional<SharedNote> findByNoteAndSharedWithUser(Note note, User sharedWithUser);
    boolean existsByNoteAndSharedWithUser(Note note, User sharedWithUser);
}