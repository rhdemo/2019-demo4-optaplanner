/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var stompClient = null;
var machines = [];
var mechanics = [];
var locations = [];

const SHOW_NEXT_VISITS = 3;
const HEALTH_TEXT_OFFSET = 50;
const MECHANIC_SIZE = 20;
const NEAR_BY_RADIUS = 30;

const ResponseType  = {
    MACHINE_LOCATIONS : 'MACHINE_LOCATIONS',
    ADD_MECHANIC : 'ADD_MECHANIC',
    REMOVE_MECHANIC : 'REMOVE_MECHANIC',
    DISPATCH_MECHANIC: 'DISPATCH_MECHANIC',
    UPDATE_MACHINE_HEALTHS: 'UPDATE_MACHINE_HEALTHS'
};

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#pauze" ).click(function() { pauze(); });
    $( "#unpauze" ).click(function() { unpauze(); });
    $( "#addMechanic" ).click(function() { addMechanic(); });
    $( "#removeMechanic" ).click(function() { removeMechanic(); });
    $( "#start-simulation" ).click(function() { startSimulation() });
    $( "#stop-simulation" ).click(function() { stopSimulation() });
    $( "#canvas" ).click(function(event) { damageMachine(event) });
});

function connect() {
    var socket = new SockJS('/roster-websocket');
    stompClient = Stomp.over(socket);
    stompClient.debug = null;
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/roster', function (roster) {
            processResponse(JSON.parse(roster.body));
        });
        stompClient.send("/app/locations", {});
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
    }
    $("#machines").html("");
}

function pauze() {
    stompClient.send("/app/pauze", {});
    $("#pauze").prop("disabled", true);
    $("#unpauze").prop("disabled", false);
}

function unpauze() {
    stompClient.send("/app/unpauze", {});
    $("#pauze").prop("disabled", false);
    $("#unpauze").prop("disabled", true);
}

function addMechanic() {
    stompClient.send("/app/addMechanic", {});
}

function removeMechanic() {
    stompClient.send("/app/removeMechanic", {});
}

function startSimulation() {
    console.log('starting simulation');
    $.post('/simulation/start', {}, function(data, status, jqXHR) { console.log('sent post start simulation') });
    $("#start-simulation").prop("disabled", true);
    $("#stop-simulation").prop("disabled", false);
}

function stopSimulation() {
    console.log('stopping simulation');
    $.post('/simulation/stop', {}, function(data, status, jqXHR) { console.log('sent post stop simulation') });
    $("#start-simulation").prop("disabled", false);
    $("#stop-simulation").prop("disabled", true);
}

function damageMachine(event) {
    let rect = canvas.getBoundingClientRect();
    let x = event.clientX - rect.left;
    let y = event.clientY - rect.top;

    console.log('clicking on ' + x + ':' + y);
    let machineIndex = findMachineNearTo(x, y);
    if (machineIndex != null) {
        console.log('clicking on machine ' + machineIndex);
        dealDamage(machineIndex);
    }
}

function findMachineNearTo(x, y) {
    for (let i = 0; i < machines.length; i++) {
        let position = getPositionOfMachineComponent(i);
        if (distance(x, y, position.x, position.y) < NEAR_BY_RADIUS) {
            return i;
        }
    }
}

function distance(x1, y1, x2, y2) {
    let squareX = Math.pow((x1 - x2), 2);
    let squareY = Math.pow((y1 - y2), 2);
    return Math.sqrt(squareX, squareY);
}

function dealDamage(machineIndex) {
    $.ajax({
            'type': 'POST',
            'url': "/simulation/damage",
            'contentType': 'application/json',
            'data': JSON.stringify({ "machineIndex" : machineIndex }),
            'dataType': 'json',
            'success': function(data, status, jqXHR) { console.log('sent post damage machine ' + machineIndex) }
    });
}

function processResponse(response) {
    if (response.responseType === ResponseType.MACHINE_LOCATIONS) {
        console.log("Machines locations");
        locations = response.locations;
    } else if (response.responseType === ResponseType.ADD_MECHANIC) {
        console.log("Adding a mechanic");
        let mechanic = {
            mechanicIndex: response.mechanicIndex
        };
        mechanics.push(mechanic);
    } else if (response.responseType === ResponseType.REMOVE_MECHANIC) {
        let mechanicIndex = response.mechanicIndex;
        if (mechanicIndex >= 0) {
            console.log("Removing a mechanic index: " + mechanicIndex)
            mechanics.splice(mechanicIndex, 1);
        }
    } else if (response.responseType === ResponseType.UPDATE_MACHINE_HEALTHS) {
        machines = response.machines;
    } else if (response.responseType === ResponseType.DISPATCH_MECHANIC) {
        let mechanic = response.mechanic
        mechanics[mechanic.mechanicIndex] = mechanic;
        console.log("Dispatching a mechanic: " + mechanic.mechanicIndex + " to a machine: " + mechanic.focusMachineIndex);
    } else {
        console.log("Uknown response type: " + response.responseType);
    }

    drawGame();
}

function drawGame() {
    $( "#mechanicCount" ).value='' + mechanics.length;

    let canvas = document.getElementById('canvas');
    let ctx = canvas.getContext('2d');

    let backgroundImage = new Image();
    backgroundImage.src = "/machines.png";

    backgroundImage.onload = function() {
        ctx.drawImage(backgroundImage, 0, 0);
        drawMachine(ctx);
        drawMechanics(ctx);
    }
}

function drawMachine(ctx) {
    for (i = 0; i < machines.length; i++) {
        drawMachineComponent(ctx, i)
    }
}

function drawMechanics(ctx) {
    for (i = 0; i < mechanics.length; i++) {
        drawMechanic(ctx, mechanics[i].mechanicIndex)
    }
}

function drawMachineComponent(ctx, index) {
    let position =  getPositionOfMachineComponent(index);
    let positionX = position.x;
    let positionY = position.y;

    let machineHealth = machines[index].health * 100;

    if (machineHealth > 80) {
        ctx.fillStyle = 'rgb(122, 163, 76)'; // green
    } else if (machineHealth > 50) { 
        ctx.fillStyle = 'rgb(242, 214, 36)'; //yellow
    } else if (machineHealth > 0) {
        ctx.fillStyle = 'rgb(183, 13, 1)'; // red
    } else {
        ctx.fillStyle = 'rgb(0, 0, 0)'; // black - the component has been broken
    }

    ctx.font = "15px Georgia";
    let healthString = Math.round(machineHealth) + ' %';
    ctx.fillText(healthString, positionX + HEALTH_TEXT_OFFSET / 2, positionY - HEALTH_TEXT_OFFSET);
}

function getPositionOfMachineComponent(machineIndex) {
    let position = {
        x: locations[machineIndex].x,
        y: locations[machineIndex].y
    };

    return position;
}

function drawMechanic(ctx, index) {
    var positionX, positionY;
    let machineIndex = mechanics[index].focusMachineIndex;
    if (machineIndex != null) {
        let position = getPositionOfMachineComponent(machineIndex);
        positionX = position.x;
        positionY = position.y;
    } else {
        positionX = MECHANIC_SIZE;
        positionY = MECHANIC_SIZE;
    }

    ctx.beginPath();
    ctx.arc(positionX, positionY, MECHANIC_SIZE, 0, 2 * Math.PI, false);
    ctx.fillStyle = 'green';
    ctx.fill();
    ctx.lineWidth = 2;
    ctx.strokeStyle = '#003300';
    ctx.setLineDash([]);
    ctx.stroke();

    drawNextVisits(ctx, index)
}

function drawNextVisits(ctx, mechanicIndex) {
    let futureIndexes = mechanics[mechanicIndex].futureMachineIndexes;
    let previousMachineIndex = futureIndexes[0];
    let nextMachineIndex;
    for (i = 1; i < futureIndexes.length; i++) {
        if (i > SHOW_NEXT_VISITS) { // show only next N visits
            break;
        }

        nextMachineIndex = futureIndexes[i];
        drawPathBetweenTwoMachines(ctx, previousMachineIndex, nextMachineIndex);
        previousMachineIndex = nextMachineIndex;
    }
}

function drawPathBetweenTwoMachines(ctx, machine1, machine2) {
    let position1 = getPositionOfMachineComponent(machine1);
    let position2 = getPositionOfMachineComponent(machine2);

    ctx.beginPath();
    ctx.moveTo(position1.x, position1.y);
    ctx.lineTo(position2.x, position2.y);
    ctx.setLineDash([5, 15]);
    ctx.lineWidth = 2;
    ctx.strokeStyle = '#AA0000';
    ctx.stroke();

    ctx.beginPath();
    ctx.arc(position2.x, position2.y, MECHANIC_SIZE/2, 0, 2 * Math.PI, false);
    ctx.fillStyle = '#AA0000';
    ctx.setLineDash([]);
    ctx.fill();
    ctx.stroke();
}
