package com.redhat.demo.optaplanner.rest;

import com.redhat.demo.optaplanner.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping("/app")
public class OptaPlannerRestController {

    private GameService gameService;

    @Autowired
    public OptaPlannerRestController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/pauzeDispatch")
    @ResponseStatus(HttpStatus.OK)
    public void pauzeDispatch() {
        gameService.pauseGame();
    }

    @PostMapping("/unpauzeDispatch")
    @ResponseStatus(HttpStatus.OK)
    public void unpauzeDispatch() {
        gameService.resumeGame();
    }

    @PostMapping("/addMechanic")
    @ResponseStatus(HttpStatus.OK)
    public void addMechanic() {
        gameService.addMechanic();
    }

    @PostMapping("/removeMechanic")
    @ResponseStatus(HttpStatus.OK)
    public void removeMechanic() {
        gameService.removeMechanic();
    }

    @PostMapping("/locations")
    @ResponseStatus(HttpStatus.OK)
    public void machineLocations() {
        gameService.machineLocations();
    }

}
