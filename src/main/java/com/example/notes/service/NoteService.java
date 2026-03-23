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

    public Note saveNote(Note note) {
        return noteRepository.save(note);
    }

    public void deleteNote(Note note) {
        noteRepository.delete(note);
    }

    public Note saveOrUpdateNote(Note editingNote,
                                 String content,
                                 User currentUser,
                                 byte[] uploadedImageData,
                                 String uploadedImageName,
                                 String uploadedImageType,
                                 boolean removeExistingImage) {

        String trimmedContent = content != null ? content.trim() : "";

        if (trimmedContent.isBlank()) {
            throw new IllegalArgumentException("Note content cannot be empty.");
        }

        Note noteToSave = (editingNote != null) ? editingNote : new Note();
        noteToSave.setContent(trimmedContent);
        noteToSave.setUser(currentUser);

        if (uploadedImageData != null) {
            noteToSave.setImageData(uploadedImageData);
            noteToSave.setImageName(uploadedImageName);
            noteToSave.setImageType(uploadedImageType);
        } else if (removeExistingImage) {
            noteToSave.setImageData(null);
            noteToSave.setImageName(null);
            noteToSave.setImageType(null);
        }

        return noteRepository.save(noteToSave);
    }
}