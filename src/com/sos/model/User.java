package com.sos.model;

import com.sos.util.Json;

public class User implements Identifiable, Jsonable {
    private int id;
    private final String name;
    private final String email;
    private final String password;   // plain text: this is a college simulation (no hashing in syllabus)

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    @Override public int getId() { return id; }
    @Override public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }

    @Override
    public String toJson() {   // password is never sent to the browser
        return "{\"id\":" + id + ",\"name\":" + Json.quote(name) + ",\"email\":" + Json.quote(email) + "}";
    }
}
