package com.vitorcamprubi.OMS_Lite.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Health", description = "Endpoints de health-check")
public class PingController {

    @GetMapping("/ping")
    @Operation(summary = "Health check", description = "Retorna \"pong\" se a aplicação estiver de pé.")
    public String ping() {
        return "pong";
    }
}
