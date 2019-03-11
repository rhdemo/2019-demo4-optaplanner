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

const START_X = 50;
const START_Y = 50;
const REFRESH_RATE = 40; // ms
const ROWS = 5;
const COMPONENT_SIZE = 50;
const MECHANIC_SIZE = 20;
const DISTANCE = 100;

const ResponseType  = {
    SETUP_UI : 'SETUP_UI',
    ADD_MECHANIC : 'ADD_MECHANIC',
    REMOVE_MECHANIC : 'REMOVE_MECHANIC',
    DISPATCH_MECHANIC: 'DISPATCH_MECHANIC'
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
});

function connect() {
    var socket = new SockJS('/roster-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/roster', function (roster) {
            processResponse(JSON.parse(roster.body));
        });
        stompClient.send("/app/setupUI", {});
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

function processResponse(response) {
    $("#machines").append("<tr><td>" + response.responseType + "</td></tr>");

    if (response.responseType === ResponseType.SETUP_UI) {
        machines = response.machines;
        mechanics = response.mechanics;    
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
    } else if (response.responseType === ResponseType.DISPATCH_MECHANIC) {
        mechanics[response.mechanicIndex].machineIndex = response.toMachineIndex;
        machines[response.toMachineIndex].health = 1.0;
        console.log("Dispatching a mechanic: " + response.mechanicIndex + " to a machine: " + response.toMachineIndex);
    }

    drawGame();
}

function drawGame() {
    $( "#mechanicCount" ).value='' + mechanics.length;

    let canvas = document.getElementById('canvas');
    let ctx = canvas.getContext('2d');
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    drawMachine(ctx);
    drawMechanics(ctx);
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
    ctx.fillRect(positionX, positionY, COMPONENT_SIZE, COMPONENT_SIZE);

    // put a health above the component
    ctx.fillStyle = 'black';
    ctx.font = "15px Georgia";
    let healthString = Math.round(machineHealth) + ' %';
    ctx.fillText(healthString, positionX + COMPONENT_SIZE / 2, positionY - 5);
}

function getPositionOfMachineComponent(machineIndex) {
    let effectiveDistance = COMPONENT_SIZE + DISTANCE;
    let positionX = START_X + (machineIndex % ROWS) * effectiveDistance;
    let positionY = START_Y + Math.floor(machineIndex / ROWS) * effectiveDistance;
    let position = {
        x: positionX,
        y: positionY
    };

    return position;
}

function drawMechanic(ctx, index) {
    var positionX, positionY;
    let machineIndex = mechanics[index].machineIndex;
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
    ctx.stroke();
}
