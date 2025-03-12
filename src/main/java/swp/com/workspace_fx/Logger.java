package swp.com.workspace_fx;

import jdk.internal.icu.lang.UCharacterDirection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Logger {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static final String LOG_DIR = "logs";
    private static final String APP_NAME = "FileWorkspace";
    private static final Logger INSTANCE = new Logger();

    private final Path logDir;
    private final Path logFile;
    private final ConcurrentLinkedQueue<String> logQueue = new ConcurrentLinkedQueue<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private Logger() {
        this.logDir = Paths.get(LOG_DIR);

        try {
            // Create logs directory if it doesn't exist
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }

            // Create a session-specific log file with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            this.logFile = logDir.resolve(String.format("%s_session_%s.log", APP_NAME, timestamp));

            // Create and initialize the log file with header
            Files.createFile(logFile);
            String header = String.format("=== %s APPLICATION LOG - SESSION STARTED AT %s ===\n\n",
                    APP_NAME, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            Files.write(logFile, header.getBytes(), StandardOpenOption.APPEND);

            // Log system information
            logSystemInfo();
        } catch (IOException e) {
            System.err.println("Failed to initialize logging: " + e.getMessage());
            throw new RuntimeException("Failed to initialize logging system", e);
        }

        // Schedule log flushing every 2 seconds
        scheduler.scheduleAtFixedRate(this::flushLogs, 2, 2, TimeUnit.SECONDS);

        // Add shutdown hook to ensure logs are flushed
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            info("Application shutdown initiated");
            close();
        }));

        info("Logging system initialized");
    }

    public static Logger getInstance() {
        return INSTANCE;
    }

    public void info(String message) {
        log("INFO", message);
    }

    public void operation(String operation) {
        log("OPERATION", operation);
    }

    public void error(String message) {
        log("ERROR", message);
    }

    public void error(String message, Throwable throwable) {
        StringBuilder sb = new StringBuilder(message);
        sb.append(": ").append(throwable.getMessage());
        sb.append("\nStack trace:");
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append("\n    ").append(element.toString());
        }
        log("ERROR", sb.toString());
    }

    public void debug(String message) {
        log("DEBUG", message);
    }

    private void log(String level, String message) {
        LocalDateTime now = LocalDateTime.now();
        String logEntry = String.format("[%s %s] [%s] %s",
                now.format(DATE_FORMAT),
                now.format(TIME_FORMAT),
                level,
                message);

        logQueue.add(logEntry);

        // Also print to console for debugging purposes
        if (level.equals("ERROR")) {
            System.err.println(logEntry);
        } else {
            System.out.println(logEntry);
        }
    }

    private void logSystemInfo() {
        info("Operating System: " + System.getProperty("os.name") + " " +
                System.getProperty("os.version") + " " + System.getProperty("os.arch"));
        info("Java Version: " + System.getProperty("java.version"));
        info("User Directory: " + System.getProperty("user.dir"));
        info("Log file created at: " + logFile.toAbsolutePath());
    }

    private void flushLogs() {
        if (logQueue.isEmpty()) {
            return;
        }

        try {
            StringBuilder sb = new StringBuilder();
            String entry;
            while ((entry = logQueue.poll()) != null) {
                sb.append(entry).append(System.lineSeparator());
            }

            Files.write(logFile, sb.toString().getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Failed to write logs: " + e.getMessage());

            // If writing fails, try to re-add entries to the queue
            try {
                StringBuilder sb = new StringBuilder();
                String[] entries = sb.toString().split(System.lineSeparator());
                for (String entry : entries) {
                    if (!entry.isEmpty()) {
                        logQueue.add(entry);
                    }
                }
            } catch (Exception ex) {
                System.err.println("Fatal error in logger - some log entries may be lost: " + ex.getMessage());
            }
        }
    }

    public void close() {
        info("Closing logger and flushing remaining logs");
        flushLogs();

        try {
            String footer = String.format("\n=== %s APPLICATION LOG - SESSION ENDED AT %s ===\n",
                    APP_NAME, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            Files.write(logFile, footer.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Failed to write log footer: " + e.getMessage());
        }

        scheduler.shutdown();
        try {
            scheduler.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public Path getLogFilePath() {
        return logFile;
    }
}