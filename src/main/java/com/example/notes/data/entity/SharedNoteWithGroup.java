package com.example.notes.data.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "shared_note_with_group")
public class SharedNoteWithGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "note_id")
    private Note note;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_group_id")
    private UserGroup userGroup;

    @ManyToOne(optional = false)
    @JoinColumn(name = "shared_by_user_id")
    private User sharedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SharedNote.SharePermission permission = SharedNote.SharePermission.READ;

    private LocalDateTime sharedAt = LocalDateTime.now();

    public SharedNoteWithGroup() {}

    public SharedNoteWithGroup(Note note, UserGroup userGroup, User sharedBy, SharedNote.SharePermission permission) {
        this.note = note;
        this.userGroup = userGroup;
        this.sharedBy = sharedBy;
        this.permission = permission;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        this.note = note;
    }

    public UserGroup getUserGroup() {
        return userGroup;
    }

    public void setUserGroup(UserGroup userGroup) {
        this.userGroup = userGroup;
    }

    public User getSharedBy() {
        return sharedBy;
    }

    public void setSharedBy(User sharedBy) {
        this.sharedBy = sharedBy;
    }

    public SharedNote.SharePermission getPermission() {
        return permission;
    }

    public void setPermission(SharedNote.SharePermission permission) {
        this.permission = permission;
    }

    public LocalDateTime getSharedAt() {
        return sharedAt;
    }

    public void setSharedAt(LocalDateTime sharedAt) {
        this.sharedAt = sharedAt;
    }
}
