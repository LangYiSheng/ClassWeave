package com.lanchen.classweave.service;

import com.lanchen.classweave.common.ErrorCode;
import com.lanchen.classweave.domain.entity.UserEntity;
import com.lanchen.classweave.domain.repository.UserRepository;
import com.lanchen.classweave.dto.user.UpdateCurrentUserRequest;
import com.lanchen.classweave.dto.user.UserResponse;
import com.lanchen.classweave.exception.BusinessException;
import com.lanchen.classweave.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserPrincipal loadPrincipalById(Long userId) {
        return new UserPrincipal(findById(userId));
    }

    public UserEntity findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "用户不存在"));
    }

    public UserResponse getCurrentUser(Long userId) {
        return toUserResponse(findById(userId));
    }

    @Transactional
    public UserResponse updateCurrentUser(Long userId, UpdateCurrentUserRequest request) {
        UserEntity user = findById(userId);
        String value = request.displayName();
        user.setDisplayName(value == null || value.isBlank() ? null : value.trim());
        return toUserResponse(user);
    }

    public UserResponse toUserResponse(UserEntity user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getDisplayName());
    }
}
