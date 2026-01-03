package com.festora.authservice.controller;

import com.festora.authservice.dto.SessionStartRequest;
import com.festora.authservice.dto.SessionStartResponse;
import com.festora.authservice.service.CustomerSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/session")
@RequiredArgsConstructor
public class SessionController {

    private final CustomerSessionService sessionService;

    @PostMapping("/start")
    public SessionStartResponse start(@RequestBody SessionStartRequest request) {
        return sessionService.startSession(request.getQrId());
    }
}

