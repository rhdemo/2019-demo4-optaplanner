package com.redhat.demo.optaplanner.websocket;

import com.redhat.demo.optaplanner.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class OptaPlannerWebSocketController {

    private GameService gameService;

    @Autowired
    public OptaPlannerWebSocketController(GameService gameService) {
        this.gameService = gameService;
    }

    @MessageMapping("/pauze")
    public void pauze() {
        gameService.pauseGame();
    }

    @MessageMapping("/unpauze")
    public void unpauze() {
        gameService.resumeGame();
    }

    @MessageMapping("/addMechanic")
    public void addMechanic() {
        gameService.addMechanic();
    }

    @MessageMapping("/removeMechanic")
    public void removeMechanic() {
        gameService.removeMechanic();
    }
}
