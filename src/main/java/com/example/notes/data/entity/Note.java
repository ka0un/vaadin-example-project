package com.example.notes.data.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String content;
    
    // Add title field for better note management
    @Column(nullable = false, length = 200)
    private String title;
    
    // Add timestamps
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    // New fields for sharing functionality
    private boolean isPublic = false;
    
    // One-to-many relationship for internal sharing
    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SharedNote> sharedWith = new HashSet<>();
    
    // One-to-many relationship for public share tokens
    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<NoteShareToken> shareTokens = new HashSet<>();

    // Constructors
    public Note() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Note(String title, String content, User user) {
        this.title = title;
        this.content = content;
        this.user = user;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isPublic = false;
    }
    
    // Keep original constructor for backward compatibility
    public Note(String content, User user) {
        this("Untitled", content, user);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
        this.updatedAt = LocalDateTime.now();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public boolean isPublic() {
        return isPublic;
    }
    
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
    
    public Set<SharedNote> getSharedWith() {
        return sharedWith;
    }
    
    public void setSharedWith(Set<SharedNote> sharedWith) {
        this.sharedWith = sharedWith;
    }
    
    public Set<NoteShareToken> getShareTokens() {
        return shareTokens;
    }
    
    public void setShareTokens(Set<NoteShareToken> shareTokens) {
        this.shareTokens = shareTokens;
    }
    
    // Helper method to add shared user
    public void addSharedUser(SharedNote sharedNote) {
        sharedWith.add(sharedNote);
        sharedNote.setNote(this);
    }
    
    // Helper method to remove shared user
    public void removeSharedUser(SharedNote sharedNote) {
        sharedWith.remove(sharedNote);
        sharedNote.setNote(null);
    }
    
    // Helper method to add share token
    public void addShareToken(NoteShareToken token) {
        shareTokens.add(token);
        token.setNote(this);
    }
    
    // Helper method to remove share token
    public void removeShareToken(NoteShareToken token) {
        shareTokens.remove(token);
        token.setNote(null);
    }
}