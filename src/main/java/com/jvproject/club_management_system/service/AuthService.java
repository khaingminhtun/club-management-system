package com.jvproject.club_management_system.service;

import com.jvproject.club_management_system.dto.auth.AuthResponse;
import com.jvproject.club_management_system.dto.auth.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);
}
