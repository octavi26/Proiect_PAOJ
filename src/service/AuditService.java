package service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Singleton service for logging actions to a CSV file.
 * Requirement: Stage II - Audit Service.
 */
public class AuditService {
    private static AuditService instance;
    private static final String FILE_PATH = "audit.csv";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private AuditService() {}

    public static AuditService getInstance() {
        if (instance == null) {
            instance = new AuditService();
        }
        return instance;
    }

    /**
     * Logs an action with a timestamp to the audit.csv file.
     * @param actionName The name of the action performed.
     */
    public void logAction(String actionName) {
        try (FileWriter fw = new FileWriter(FILE_PATH, true);
             PrintWriter pw = new PrintWriter(fw)) {
            String timestamp = LocalDateTime.now().format(formatter);
            pw.println(actionName + ", " + timestamp);
        } catch (IOException e) {
            System.err.println("Audit log failed: " + e.getMessage());
        }
    }
}
