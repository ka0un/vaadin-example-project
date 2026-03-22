package com.example.notes.service;

import com.example.notes.data.entity.Note;
import com.example.notes.data.entity.User;
import com.example.notes.data.repository.NoteRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NoteService {

    private final NoteRepository noteRepository;

    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    public List<Note> getNotes(User user) {
        return noteRepository.findByUserOrderByIdDesc(user);
    }
    //clarify
    public Note saveNote(Note note) {
        return noteRepository.save(note);
    }

    public void deleteNote(Note note) {
        noteRepository.delete(note);
    }
}
