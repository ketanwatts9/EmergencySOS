package com.sos.service;

import com.sos.exception.AppException;
import com.sos.exception.AuthenticationException;
import com.sos.exception.DuplicateException;
import com.sos.exception.NotFoundException;
import com.sos.model.User;
import com.sos.repository.UserRepository;
import com.sos.util.Validator;

public class AuthService {
    private final UserRepository userRepository;
    private final SessionManager sessions;

    public AuthService(UserRepository userRepository, SessionManager sessions) {
        this.userRepository = userRepository;
        this.sessions = sessions;
    }

    public synchronized void register(String name, String email, String password) throws AppException {
        name = Validator.toTitleCase(Validator.requireText(name, "Name", 50));
        email = Validator.validateEmail(email);
        Validator.validatePassword(password);

        if (userRepository.findByEmail(email) != null)
            throw new DuplicateException("This email is already registered");

        userRepository.save(new User(name, email, password));
    }

    /** Returns a session token when email and password match. */
    public String login(String email, String password) throws AppException {
        email = Validator.validateEmail(email);
        User user = userRepository.findByEmail(email);
        if (user == null || password == null || !user.getPassword().equals(password))
            throw new AuthenticationException("Invalid email or password");
        return sessions.createSession(user.getId());
    }

    public void logout(String token) { sessions.invalidate(token); }

    public User getUser(int userId) throws NotFoundException {
        User user = userRepository.findById(userId);
        if (user == null) throw new NotFoundException("User not found");
        return user;
    }
}
