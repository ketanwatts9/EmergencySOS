package com.sos;

import com.sos.exception.AppException;
import com.sos.repository.AlertRepository;
import com.sos.repository.ContactRepository;
import com.sos.repository.FileAlertRepository;
import com.sos.repository.FileContactRepository;
import com.sos.repository.FileUserRepository;
import com.sos.repository.UserRepository;
import com.sos.server.AlertHandler;
import com.sos.server.AppServer;
import com.sos.server.AuthHandler;
import com.sos.server.ContactHandler;
import com.sos.server.StaticFileHandler;
import com.sos.service.AlertService;
import com.sos.service.AuthService;
import com.sos.service.ContactService;
import com.sos.service.DispatcherThread;
import com.sos.service.Notification;
import com.sos.service.SessionManager;
import com.sos.service.SharedQueue;

public class Main {
    public static void main(String[] args) {
        // The program finds web/ and data/ relative to the folder it is started from.
        if (!new java.io.File("web/index.html").isFile()) {
            System.out.println("ERROR: cannot find web/index.html.");
            System.out.println("Current folder: " + new java.io.File(".").getAbsoluteFile().getParent());
            System.out.println("Start the program from the project folder (the one that contains the 'web' folder),");
            System.out.println("or set the 'Working directory' of your IDE run configuration to that folder.");
            return;
        }
        try {
            // 1. Repositories. Stage 2: replace these 3 lines with Jdbc repositories.
            UserRepository userRepo = new FileUserRepository("data/users.csv");
            ContactRepository contactRepo = new FileContactRepository("data/contacts.csv");
            AlertRepository alertRepo = new FileAlertRepository("data/alerts.csv");

            // 2. Services
            SessionManager sessions = new SessionManager();
            SharedQueue<Notification> queue = new SharedQueue<>(50);
            AuthService authService = new AuthService(userRepo, sessions);
            ContactService contactService = new ContactService(contactRepo);
            AlertService alertService = new AlertService(alertRepo, contactService, authService, queue);

            // 3. Background thread that "sends" the SOS messages
            new DispatcherThread(queue, alertRepo).start();

            // 4. Web server
            AppServer server = new AppServer(8080,
                    new AuthHandler(authService, sessions),
                    new ContactHandler(contactService, sessions),
                    new AlertHandler(alertService, sessions),
                    new StaticFileHandler("web"));
            server.start();
            System.out.println("Emergency SOS running at http://localhost:8080");

        } catch (AppException e) {
            System.out.println("Startup problem: " + e.getMessage());
        } catch (java.io.IOException e) {
            System.out.println("Could not start the web server: " + e.getMessage());
        }
    }
}
