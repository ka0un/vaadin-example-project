package com.example.notes.service;

import com.example.notes.data.entity.User;
import com.example.notes.data.entity.UserGroup;
import com.example.notes.data.repository.UserGroupRepository;
import com.example.notes.data.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class UserGroupService {

    private final UserGroupRepository userGroupRepository;
    private final UserRepository userRepository;

    public UserGroupService(UserGroupRepository userGroupRepository, UserRepository userRepository) {
        this.userGroupRepository = userGroupRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public UserGroup createUserGroup(String name, User owner) {
        UserGroup group = new UserGroup(name, owner);
        return userGroupRepository.save(group);
    }

    @Transactional
    public void deleteUserGroup(UserGroup group) {
        userGroupRepository.delete(group);
    }

    @Transactional
    public void addUserToGroup(UserGroup group, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        group.getMembers().add(user);
        userGroupRepository.save(group);
    }

    @Transactional
    public void removeUserFromGroup(UserGroup group, User user) {
        group.getMembers().remove(user);
        userGroupRepository.save(group);
    }

    public List<UserGroup> getUserGroups(User user) {
        List<UserGroup> owned = userGroupRepository.findByOwner(user);
        List<UserGroup> memberOf = userGroupRepository.findByMembersContaining(user);
        return Stream.concat(owned.stream(), memberOf.stream())
                .distinct()
                .collect(Collectors.toList());
    }

    public List<UserGroup> getOwnedGroups(User user) {
        return userGroupRepository.findByOwner(user);
    }
}
