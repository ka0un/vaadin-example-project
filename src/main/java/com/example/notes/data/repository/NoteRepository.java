package com.example.notes.data.repository;

import com.example.notes.data.entity.Note;
import com.example.notes.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByUser(User user);

    // Find public notes
    List<Note> findByIsPublicTrue();

    // Find notes shared with a specific user
    @Query("SELECT sn.note FROM SharedNote sn WHERE sn.sharedWithUser = :user")
    List<Note> findSharedNotesByUser(@Param("user") User user);

    // Find notes shared with specific user groups
    @Query("SELECT sng.note FROM SharedNoteWithGroup sng WHERE sng.userGroup IN :groups")
    List<Note> findSharedNotesByGroups(@Param("groups") List<com.example.notes.data.entity.UserGroup> groups);

    // Find all accessible notes for a user (owned + shared + group-shared + public)
    @Query("SELECT DISTINCT n FROM Note n " +
           "LEFT JOIN SharedNote sn ON n = sn.note " +
           "LEFT JOIN SharedNoteWithGroup sng ON n = sng.note " +
           "WHERE n.user = :user OR sn.sharedWithUser = :user " +
           "OR sng.userGroup IN :groups OR n.isPublic = true")
    List<Note> findAllAccessibleNotes(@Param("user") User user, @Param("groups") List<com.example.notes.data.entity.UserGroup> groups);
}
