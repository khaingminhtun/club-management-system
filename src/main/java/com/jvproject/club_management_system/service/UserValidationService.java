package com.jvproject.club_management_system.service;

import com.jvproject.club_management_system.exception.AccountNotActiveException;
import com.jvproject.club_management_system.exception.ResourceNotFoundException;
import com.jvproject.club_management_system.exception.VerificationRequiredException;
import com.jvproject.club_management_system.model.User;
import com.jvproject.club_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserValidationService {

    private final UserRepository userRepository;

    public User getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User account was not found"
                        )
                );
    }

    public User getVerifiedStudentByEmail(String email) {
        User user = getUserByEmail(email);

        if (!user.isEnabled()) {
            throw new AccountNotActiveException(
                    "Your account is not active"
            );
        }

        if (user.getStudentVerifiedAt() == null) {
            throw new VerificationRequiredException(
                    "Student verification is required"
            );
        }

        return user;
    }
}