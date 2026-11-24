package com.sos.service;

import com.sos.exception.AuthenticationException;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/** Remembers who is logged in: token -> userId (a HashMap). */
public class SessionManager {
    private final Map<String, Integer> sessions = new HashMap<>();
    private final Random random = new Random();

    public synchronized String createSession(int userId) {
        String token = Long.toHexString(random.nextLong()) + Long.toHexString(System.nanoTime());
        sessions.put(token, userId);
        return token;
    }

    public synchronized int getUserId(String token) throws AuthenticationException {
        Integer userId = (token == null) ? null : sessions.get(token);
        if (userId == null) throw new AuthenticationException("Please login first");
        return userId;   // auto-unboxing Integer -> int
    }

    public synchronized void invalidate(String token) {
        sessions.remove(token);
    }
}
