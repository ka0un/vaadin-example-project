package com.example.notes.data.repository;

import com.example.notes.data.entity.Note;
import com.example.notes.data.entity.SharedNoteWithGroup;
import com.example.notes.data.entity.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SharedNoteWithGroupRepository extends JpaRepository<SharedNoteWithGroup, Long> {
    List<SharedNoteWithGroup> findByNote(Note note);
    List<SharedNoteWithGroup> findByUserGroupIn(List<UserGroup> userGroups);
}
