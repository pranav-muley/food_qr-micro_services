package com.festora.authservice.controller;

import com.festora.authservice.dto.OpenSessionRequest;
import com.festora.authservice.service.QrSessionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/session")
public class QrSessionController {

    private final QrSessionService sessionService;

    public QrSessionController(QrSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping("/open")
    public ResponseEntity<?> open(@Valid @RequestBody OpenSessionRequest req) {
        var res = sessionService.openSession(req);
        return ResponseEntity.ok(Map.of(
                "sessionId", res.sessionId(),
                "restaurantId", res.restaurantId(),
                "tableNumber", res.tableNumber(),
                "sessionToken", res.sessionToken(),
                "expiresAt", res.expiresAt()
        ));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<?> get(@PathVariable String sessionId) {
        var res = sessionService.getSession(sessionId);
        if (res == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of(
                "sessionId", res.sessionId(),
                "restaurantId", res.restaurantId(),
                "tableNumber", res.tableNumber(),
                "expiresAt", res.expiresAt()
        ));
    }
}
