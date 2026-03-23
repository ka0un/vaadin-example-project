package com.example.notes.service;

import com.example.notes.data.entity.Note;
import com.example.notes.data.entity.SharedNote;
import com.example.notes.data.entity.SharedNoteWithGroup;
import com.example.notes.data.entity.User;
import com.example.notes.data.entity.UserGroup;
import com.example.notes.data.entity.NoteShareToken;
import com.example.notes.data.repository.NoteRepository;
import com.example.notes.data.repository.NoteShareTokenRepository;
import com.example.notes.data.repository.SharedNoteRepository;
import com.example.notes.data.repository.SharedNoteWithGroupRepository;
import com.example.notes.data.repository.UserGroupRepository;
import com.example.notes.data.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final SharedNoteRepository sharedNoteRepository;
    private final NoteShareTokenRepository shareTokenRepository;
    private final UserRepository userRepository;
    private final SharedNoteWithGroupRepository sharedNoteWithGroupRepository;
    private final UserGroupRepository userGroupRepository;

    public NoteService(NoteRepository noteRepository,
                      SharedNoteRepository sharedNoteRepository,
                      NoteShareTokenRepository shareTokenRepository,
                      UserRepository userRepository,
                      SharedNoteWithGroupRepository sharedNoteWithGroupRepository,
                      UserGroupRepository userGroupRepository) {
        this.noteRepository = noteRepository;
        this.sharedNoteRepository = sharedNoteRepository;
        this.shareTokenRepository = shareTokenRepository;
        this.userRepository = userRepository;
        this.sharedNoteWithGroupRepository = sharedNoteWithGroupRepository;
        this.userGroupRepository = userGroupRepository;
    }

    public List<Note> getNotes(User user) {
        return noteRepository.findByUser(user);
    }

    public List<Note> getAllAccessibleNotes(User user) {
        List<UserGroup> userGroups = userGroupRepository.findByMembersContaining(user);
        return noteRepository.findAllAccessibleNotes(user, userGroups);
    }

    public List<Note> getSharedNotes(User user) {
        return noteRepository.findSharedNotesByUser(user);
    }

    public void saveNote(Note note) {
        noteRepository.save(note);
    }

    public void deleteNote(Note note) {
        noteRepository.delete(note);
    }

    public Optional<Note> getNoteById(Long id) {
        return noteRepository.findById(id);
    }

    // Sharing functionality (User)
    @Transactional
    public void shareNoteWithUser(Note note, String username, SharedNote.SharePermission permission) {
        User sharedWithUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        if (sharedWithUser.equals(note.getUser())) {
            throw new IllegalArgumentException("Cannot share note with yourself");
        }

        // Check if already shared
        if (sharedNoteRepository.existsByNoteAndSharedWithUser(note, sharedWithUser)) {
            // Update permission if already shared
            SharedNote existing = sharedNoteRepository.findByNoteAndSharedWithUser(note, sharedWithUser).get();
            existing.setPermission(permission);
            sharedNoteRepository.save(existing);
            return;
        }

        SharedNote sharedNote = new SharedNote(note, sharedWithUser, note.getUser());
        sharedNote.setPermission(permission);
        sharedNoteRepository.save(sharedNote);
    }

    @Transactional
    public void unshareNoteWithUser(Note note, User user) {
        Optional<SharedNote> sharedNote = sharedNoteRepository.findByNoteAndSharedWithUser(note, user);
        sharedNote.ifPresent(sharedNoteRepository::delete);
    }

    public List<SharedNote> getSharedUsers(Note note) {
        return sharedNoteRepository.findByNote(note);
    }

    // Sharing functionality (Group)
    @Transactional
    public void shareNoteWithGroup(Note note, UserGroup group, SharedNote.SharePermission permission) {
        Optional<SharedNoteWithGroup> existing = sharedNoteWithGroupRepository.findByNote(note).stream()
                .filter(sng -> sng.getUserGroup().equals(group))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setPermission(permission);
            sharedNoteWithGroupRepository.save(existing.get());
        } else {
            SharedNoteWithGroup groupShare = new SharedNoteWithGroup(note, group, note.getUser(), permission);
            sharedNoteWithGroupRepository.save(groupShare);
        }
    }

    @Transactional
    public void unshareNoteWithGroup(Note note, UserGroup group) {
        sharedNoteWithGroupRepository.findByNote(note).stream()
                .filter(sng -> sng.getUserGroup().equals(group))
                .findFirst()
                .ifPresent(sharedNoteWithGroupRepository::delete);
    }

    public List<SharedNoteWithGroup> getSharedGroups(Note note) {
        return sharedNoteWithGroupRepository.findByNote(note);
    }

    // Public sharing functionality
    @Transactional
    public NoteShareToken createPublicShareLink(Note note) {
        // Deactivate existing token if any
        shareTokenRepository.findByNoteAndActiveTrue(note)
                .ifPresent(token -> {
                    token.setActive(false);
                    shareTokenRepository.save(token);
                });

        NoteShareToken newToken = new NoteShareToken(note);
        return shareTokenRepository.save(newToken);
    }

    @Transactional
    public NoteShareToken createPublicShareLinkWithExpiry(Note note, LocalDateTime expiresAt) {
        // Deactivate existing token if any
        shareTokenRepository.findByNoteAndActiveTrue(note)
                .ifPresent(token -> {
                    token.setActive(false);
                    shareTokenRepository.save(token);
                });

        NoteShareToken newToken = new NoteShareToken(note, expiresAt);
        return shareTokenRepository.save(newToken);
    }

    @Transactional
    public void deactivatePublicShareLink(Note note) {
        shareTokenRepository.findByNoteAndActiveTrue(note)
                .ifPresent(token -> {
                    token.setActive(false);
                    shareTokenRepository.save(token);
                });
    }

    public Optional<Note> getNoteByShareToken(String token) {
        return shareTokenRepository.findByTokenAndActiveTrue(token)
                .filter(t -> !t.isExpired())
                .map(NoteShareToken::getNote);
    }

    public boolean canUserAccessNote(User user, Note note) {
        // Owner can always access
        if (note.getUser().equals(user)) {
            return true;
        }

        // Check if publicly shared
        if (note.isPublic()) {
            return true;
        }

        // Check if shared with user directly
        if (sharedNoteRepository.existsByNoteAndSharedWithUser(note, user)) {
            return true;
        }

        // Check if shared with any of user's groups
        List<UserGroup> userGroups = userGroupRepository.findByMembersContaining(user);
        return sharedNoteWithGroupRepository.findByNote(note).stream()
                .anyMatch(sng -> userGroups.contains(sng.getUserGroup()));
    }

    public boolean canUserEditNote(User user, Note note) {
        // Owner can always edit
        if (note.getUser().equals(user)) {
            return true;
        }

        // Check if shared with user directly with edit permission
        boolean directEdit = sharedNoteRepository.findByNoteAndSharedWithUser(note, user)
                .map(sharedNote -> sharedNote.getPermission() == SharedNote.SharePermission.EDIT)
                .orElse(false);
        if (directEdit) return true;

        // Check if shared with any of user's groups with edit permission
        List<UserGroup> userGroups = userGroupRepository.findByMembersContaining(user);
        return sharedNoteWithGroupRepository.findByNote(note).stream()
                .filter(sng -> sng.getPermission() == SharedNote.SharePermission.EDIT)
                .anyMatch(sng -> userGroups.contains(sng.getUserGroup()));
    }

    /** Returns a list of users whose username contains the query (case-insensitive). */
    public List<User> searchUsers(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return userRepository.findByUsernameContainingIgnoreCase(query);
    }
}
