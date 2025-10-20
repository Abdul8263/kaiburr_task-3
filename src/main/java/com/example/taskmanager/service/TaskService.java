package com.example.taskmanager.service;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskExecution;
import com.example.taskmanager.repository.TaskRepository;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskService {
    private final TaskRepository repo;

    public TaskService(TaskRepository repo) {
        this.repo = repo;
    }

    // Controller-compatible API
    public List<Task> findAll() {
        return getAllTasks();
    }

    public Optional<Task> findById(String id) {
        return getTaskById(id);
    }

    public Task create(Task task) {
        return saveTask(task);
    }

    public Optional<Task> update(String id, Task task) {
        return repo.findById(id).map(existing -> {
            existing.setName(task.getName());
            existing.setOwner(task.getOwner());
            existing.setCommand(task.getCommand());
            return repo.save(existing);
        });
    }

    public boolean delete(String id) {
        if (!repo.existsById(id)) return false;
        repo.deleteById(id);
        return true;
    }

    // Existing helpers (retained for other uses)
    public List<Task> getAllTasks() {
        return repo.findAll();
    }

    public Optional<Task> getTaskById(String id) {
        return repo.findById(id);
    }

    public Task saveTask(Task task) {
        // validate command before saving
        if (!isSafeCommand(task.getCommand())) {
            throw new IllegalArgumentException("Command contains forbidden/unsafe patterns");
        }
        return repo.save(task);
    }

    public void deleteTask(String id) {
        repo.deleteById(id);
    }

    public List<Task> findByName(String name) {
        return repo.findByNameContainingIgnoreCase(name);
    }

    // Execute command, create TaskExecution, save it into task
    public TaskExecution executeTask(String taskId) throws Exception {
        Task task = repo.findById(taskId).orElseThrow(() -> new IllegalArgumentException("Task not found"));
        String command = task.getCommand();

        if (!isSafeCommand(command)) {
            throw new IllegalArgumentException("Command contains forbidden/unsafe patterns");
        }

        Instant start = Instant.now();
        ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String output = reader.lines().collect(Collectors.joining("\n"));
        int exitCode = process.waitFor();

        // optionally append exit code to output
        if (exitCode != 0) {
            output = output + "\n[EXIT_CODE=" + exitCode + "]";
        }

        Instant end = Instant.now();
        TaskExecution exec = new TaskExecution(Date.from(start), Date.from(end), output);

        // store and return
        task.addTaskExecution(exec);
        repo.save(task);

        return exec;
    }

    // Simple blacklist validator. Expand as needed.
    private boolean isSafeCommand(String command) {
        if (command == null) return false;
        String[] blacklist = {"rm ", "rm -", "reboot", "shutdown", "mkfs", "dd ", ":", ">:","2>","|&", "sudo", "chmod", "chown"};
        String lower = command.toLowerCase();
        for (String b : blacklist) {
            if (lower.contains(b)) return false;
        }
        // allow only simple commands like echo, ls, pwd, cat (careful with cat large files), date, whoami
        return true;
    }
}
