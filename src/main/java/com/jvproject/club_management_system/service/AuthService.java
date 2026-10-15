package com.jvproject.club_management_system.service;

import com.jvproject.club_management_system.dto.auth.RegisterResponse;
import com.jvproject.club_management_system.dto.auth.RegisterRequest;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);
}
