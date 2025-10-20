package com.example.taskmanager.controller;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskExecution;
import com.example.taskmanager.repository.TaskRepository;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class TaskExecutionController {

    private final TaskRepository taskRepository;
    private final KubernetesClient k8s;

    public TaskExecutionController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
        Config config = Config.autoConfigure(null);
        this.k8s = new DefaultKubernetesClient(config);
    }

    // Execute task.command by creating a short-lived pod (busybox) in the same cluster/namespace.
    @PutMapping("/tasks/{id}/execute")
    public ResponseEntity<?> executeTaskInPod(@PathVariable String id) {
        Optional<Task> maybe = taskRepository.findById(id);
        if (maybe.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Task task = maybe.get();
        String cmd = task.getCommand();
        if (cmd == null || cmd.isBlank()) {
            return ResponseEntity.badRequest().body("task.command is empty");
        }

        // Basic validation - extend as needed
        if (cmd.contains("rm -") || cmd.contains(":/") || cmd.contains("&&") || cmd.contains(";") || cmd.contains("|")) {
            return ResponseEntity.badRequest().body("command contains disallowed tokens");
        }

        String runId = "task-run-" + UUID.randomUUID().toString().substring(0, 8);
        String namespace = System.getenv().getOrDefault("KUBERNETES_NAMESPACE", "default");

        Pod pod = new PodBuilder()
                .withNewMetadata()
                    .withName(runId)
                    .addToLabels("app", "task-run")
                    .addToLabels("taskId", id)
                .endMetadata()
                .withNewSpec()
                    .addNewContainer()
                        .withName("runner")
                        .withImage("busybox:1.36")
                        // run the user command via sh -c
                        .withCommand("sh", "-c", cmd)
                    .endContainer()
                    .withRestartPolicy("Never")
                .endSpec()
                .build();

        Date start = new Date();
        try {
            Pod created = k8s.pods().inNamespace(namespace).create(pod);
            String podName = created.getMetadata().getName();

            // Wait until Succeeded or Failed (timeout 2 minutes)
            Pod finishedPod = k8s.pods()
                    .inNamespace(namespace)
                    .withName(podName)
                    .waitUntilCondition(p -> {
                        if (p == null || p.getStatus() == null) return false;
                        String phase = p.getStatus().getPhase();
                        return "Succeeded".equals(phase) || "Failed".equals(phase);
                    }, 2, TimeUnit.MINUTES);

            String logs = finishedPod == null ? "" : k8s.pods().inNamespace(namespace).withName(podName).getLog();

            Date end = new Date();
            TaskExecution exec = new TaskExecution();
            exec.setStartTime(start);
            exec.setEndTime(end);
            exec.setOutput(logs == null ? "" : logs);

            // append execution and save
            task.getTaskExecutions().add(exec);
            taskRepository.save(task);

            // cleanup pod
            try { k8s.pods().inNamespace(namespace).withName(podName).delete(); } catch (Exception ignored) {}

            return ResponseEntity.ok(exec);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("failed to run pod: " + e.getMessage());
        }
    }
}