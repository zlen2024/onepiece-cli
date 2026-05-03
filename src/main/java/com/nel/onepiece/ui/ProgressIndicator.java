package com.nel.onepiece.ui;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Progress indicator for long-running operations.
 * Supports spinners, progress bars, and multi-step progress tracking.
 */
@ApplicationScoped
public class ProgressIndicator {

    @Inject
    ColorFormatter formatter;

    private static final String[] SPINNER_FRAMES = {
        "⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"
    };

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicInteger frameIndex = new AtomicInteger(0);
    private Thread spinnerThread;

    /**
     * Start a spinner with a message
     */
    public void startSpinner(String message) {
        if (running.get()) {
            stopSpinner();
        }

        running.set(true);
        frameIndex.set(0);

        spinnerThread = new Thread(() -> {
            while (running.get()) {
                formatter.clearLine();
                String frame = SPINNER_FRAMES[frameIndex.get() % SPINNER_FRAMES.length];
                formatter.print(formatter.warning(frame + " " + message + "..."));
                
                frameIndex.incrementAndGet();
                
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        
        spinnerThread.setDaemon(true);
        spinnerThread.start();
    }

    /**
     * Stop the spinner
     */
    public void stopSpinner() {
        running.set(false);
        if (spinnerThread != null) {
            try {
                spinnerThread.join(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        formatter.clearLine();
    }

    /**
     * Show a success message after spinner
     */
    public void success(String message) {
        stopSpinner();
        formatter.println(formatter.successMessage(message));
    }

    /**
     * Show an error message after spinner
     */
    public void error(String message) {
        stopSpinner();
        formatter.println(formatter.errorMessage(message));
    }

    /**
     * Show a progress bar
     */
    public void showProgressBar(String label, int current, int total) {
        int percentage = (int) ((current * 100.0) / total);
        int barLength = 20;
        int filled = (int) ((current * barLength) / total);
        
        StringBuilder bar = new StringBuilder();
        bar.append("[");
        for (int i = 0; i < barLength; i++) {
            if (i < filled) {
                bar.append("█");
            } else {
                bar.append("░");
            }
        }
        bar.append("]");
        
        formatter.clearLine();
        formatter.print(String.format("%s %s %d%%", label, bar, percentage));
        
        if (current >= total) {
            formatter.println("");
        }
    }

    /**
     * Show multi-step progress
     */
    public void showStepProgress(String[] steps, int currentStep) {
        formatter.println(formatter.bold("Progress:"));
        
        for (int i = 0; i < steps.length; i++) {
            String prefix;
            String step = steps[i];
            
            if (i < currentStep) {
                prefix = formatter.success("├─ ✓");
                formatter.println(prefix + " " + formatter.muted(step));
            } else if (i == currentStep) {
                prefix = formatter.warning("├─ ⏳");
                formatter.println(prefix + " " + step);
            } else {
                prefix = formatter.muted("└─");
                formatter.println(prefix + " " + formatter.muted(step));
            }
        }
    }

    /**
     * Show a simple loading message
     */
    public void loading(String message) {
        formatter.println(formatter.progressMessage(message));
    }

    /**
     * Show completion message
     */
    public void complete(String message) {
        formatter.println("");
        formatter.println(formatter.successMessage(message));
    }

    /**
     * Show a list of completed items
     */
    public void showCompletedList(String title, String[] items) {
        formatter.println("");
        formatter.println(formatter.bold(title));
        for (String item : items) {
            formatter.println(formatter.successMessage(item));
        }
    }

    /**
     * Show deployment logs with formatting
     */
    public void showLog(String timestamp, String message, LogLevel level) {
        String formatted = String.format("[%s] %s", timestamp, message);
        
        switch (level) {
            case ERROR:
                formatter.println(formatter.error(formatted));
                break;
            case WARNING:
                formatter.println(formatter.warning(formatted));
                break;
            case INFO:
                formatter.println(formatter.info(formatted));
                break;
            case DEBUG:
                formatter.println(formatter.muted(formatted));
                break;
            default:
                formatter.println(formatted);
        }
    }

    public enum LogLevel {
        ERROR, WARNING, INFO, DEBUG
    }
}

// Made with Bob
