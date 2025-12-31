package com.devPath.auth;

import com.devPath.user.domain.User;
import java.util.UUID;

public interface AuthService {
    //TODO : implementation AuthService
    User getCurrentUser();
    UUID getCurrentUserId();

}
