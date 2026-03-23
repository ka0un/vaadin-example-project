package com.example.notes.data.repository;

import com.example.notes.data.entity.User;
import com.example.notes.data.entity.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {
    List<UserGroup> findByOwner(User owner);
    List<UserGroup> findByMembersContaining(User member);
}
