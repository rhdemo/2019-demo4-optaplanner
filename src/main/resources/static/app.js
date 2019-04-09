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

const USE_WEBSOCKET = false;

const sendToServer = USE_WEBSOCKET ? sendViaWebSocket : postAndForget;

const DAMAGE_AMOUNT = 0.2;
const SHOW_NEXT_VISITS = 3;
const HEALTH_TEXT_OFFSET = 20;
const MECHANIC_RADIUS = 20; 
const MACHINE_SPOT_RADIUS = 3;
const NEAR_BY_RADIUS = 30;

const FIRST_VISIT_STYLE = '#7094db';
const NEXT_VISIT_STYLE = '#bf8040';
const BACKGROUND_FOG = '0.6'; //0 = fully saturated image, 1.0 = white background

const DEBUG_ENABLED = true;

const ResponseType  = {
    CONNECT : 'CONNECT',
    ADD_MECHANIC : 'ADD_MECHANIC',
    REMOVE_MECHANIC : 'REMOVE_MECHANIC',
    DISPATCH_MECHANIC: 'DISPATCH_MECHANIC',
    UPDATE_MACHINE_HEALTHS: 'UPDATE_MACHINE_HEALTHS',
    UPDATE_FUTURE_VISITS : "UPDATE_FUTURE_VISITS"
};

const MechanicState = {
    TRAVELLING : 1,
    FIXING: 2,
    DONE: 3,
    REMOVED: 4
};

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#reset" ).click(function() { reset(); });
    $( "#pauze" ).click(function() { pauze(); });
    $( "#unpauze" ).click(function() { unpauze(); });
    $( "#addMechanic" ).click(function() { addMechanic(); });
    $( "#removeMechanic" ).click(function() { removeMechanic(); });
    $( "#start-simulation" ).click(function() { startSimulation() });
    $( "#stop-simulation" ).click(function() { stopSimulation() });
    $( "#canvas" ).click(function(event) { damageMachine(event) });
    $( "#canvas" ).contextmenu(function(event) { healMachine(event) });
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
        sendToServer("/app/connect");
    });
}

function reset() {
    sendToServer("/app/reset");
}

function sendViaWebSocket(endpoint) {
    stompClient.send(endpoint, {});
}

function postAndForget(endpoint) {
    $.post(endpoint, {}, function(data, status, jqXHR) { console.log('sent post to ' + endpoint) });
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
}

function pauze() {
    sendToServer("/app/pauze");
    $("#pauze").prop("disabled", true);
    $("#unpauze").prop("disabled", false);
}

function unpauze() {
    sendToServer("/app/unpauze");
    $("#pauze").prop("disabled", false);
    $("#unpauze").prop("disabled", true);
}

function addMechanic() {
    sendToServer("/app/addMechanic");
}

function removeMechanic() {
    sendToServer("/app/removeMechanic");
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
        console.log('damaging a machine ' + machineIndex);
        dealDamage(machineIndex);
    }
}

function healMachine(event) {
    event.preventDefault();
    let rect = canvas.getBoundingClientRect();
    let x = event.clientX - rect.left;
    let y = event.clientY - rect.top;

    console.log('right clicking on ' + x + ':' + y);
    let machineIndex = findMachineNearTo(x, y);
    if (machineIndex != null) {
        console.log('healing a machine ' + machineIndex);
        heal(machineIndex);
    }
}

function findMachineNearTo(x, y) {
    for (let i = 0; i < machines.length; i++) {
        let position = getPositionOfMachine(i);
        if (distance(x, y, position.x, position.y) < NEAR_BY_RADIUS) {
            return i;
        }
    }
}

function distance(x1, y1, x2, y2) {
    let squareX = Math.pow((x1 - x2), 2);
    let squareY = Math.pow((y1 - y2), 2);
    return Math.sqrt(squareX + squareY);
}

function dealDamage(machineIndex) {
    $.ajax({
            'type': 'POST',
            'url': "/simulation/damage",
            'contentType': 'application/json',
            'data': JSON.stringify({ "machineIndex" : machineIndex, "amount" : DAMAGE_AMOUNT }),
            'dataType': 'json',
            'success': function(data, status, jqXHR) { console.log('sent post damage machine ' + machineIndex) }
    });
}

function heal(machineIndex) {
    $.ajax({
            'type': 'POST',
            'url': "/simulation/heal",
            'contentType': 'application/json',
            'data': JSON.stringify({ "machineIndex" : machineIndex }),
            'dataType': 'json',
            'success': function(data, status, jqXHR) { console.log('sent post heal machine ' + machineIndex) }
     });
}

function processResponse(response) {
    if (response.responseType === ResponseType.CONNECT) {
        console.log("Connected to a server");
        locations = response.locations;
        mechanics = response.mechanics;
    } else if (response.responseType === ResponseType.ADD_MECHANIC) {
        mechanics.push(response.mechanic);
        console.log("Adding a mechanic");
    } else if (response.responseType === ResponseType.REMOVE_MECHANIC) {
        let mechanicIndex = response.mechanicIndex;
        if (mechanicIndex >= 0) {
            console.log("Removing a mechanic index: " + mechanicIndex);
            mechanics.splice(mechanicIndex, 1);
        }
    } else if (response.responseType === ResponseType.UPDATE_MACHINE_HEALTHS) {
        machines = response.machines;
    } else if (response.responseType === ResponseType.DISPATCH_MECHANIC) {
        let mechanic = response.mechanic;
        handleDispatchMechanic(mechanic);
        console.log("Dispatching a mechanic: " + mechanic.mechanicIndex + " to a machine: " + mechanic.focusMachineIndex);
    } else if (response.responseType === ResponseType.UPDATE_FUTURE_VISITS) {
        console.log("Future visits for a mechanic: " + response.mechanicIndex + " received");
        let mechanic = mechanics[response.mechanicIndex];
        if (mechanic != null && mechanic.state !== MechanicState.REMOVED) {
            mechanic.futureMachineIndexes = response.futureMachineIndexes;
            console.log("Future visits for a mechanic: " + response.mechanicIndex + " successfully applied");
        }
    } else {
        console.log("Uknown response type: " + response.responseType);
    }

    draw(drawGame);
}

function handleDispatchMechanic(mechanic) {
    mechanic.state = MechanicState.TRAVELLING;
    if (mechanics.length <= mechanic.mechanicIndex) {
        mechanics.push(mechanic);
    } else {
        mechanics[mechanic.mechanicIndex] = mechanic;
    }
    
    let travelTime = mechanic.focusTravelDurationMillis;
    let fixTime = mechanic.focusFixDurationMillis;
    setTimeout(function() {
        updateMechanicState(mechanic, MechanicState.FIXING);
        draw(drawGame);
    }, travelTime);

    setTimeout(function() {
        updateMechanicState(mechanic, MechanicState.DONE);
        draw(drawGame); 
    }, travelTime + fixTime);    
}

function updateMechanicState(mechanic, state) {
    if (mechanic != null) { // in case the mechanic was already removed
        mechanic.state = state;
    }
}

/*       -------------       DRAWING THE GAME    -------------------            */


function drawGame(ctx) {
    let canvas = document.getElementById('canvas');
    let backgroundImage = new Image();
    backgroundImage.src = "/machines.png";

    backgroundImage.onload = function() {
        ctx.drawImage(backgroundImage, 0, 0);
        ctx.fillStyle = 'rgba(225, 225, 225, ' + BACKGROUND_FOG + ')';
        ctx.fillRect(0,0, canvas.width, canvas.height);
        drawMachines(ctx);
        drawMechanics(ctx);
    }
}

function draw(drawFunction) {
    let canvas = document.getElementById('canvas');
    let originalCtx = canvas.getContext('2d');
    drawFunction(originalCtx);
}

function drawMachines(ctx) {
    for (var i = 0; i < machines.length; i++) {
        drawMachine(ctx, machines[i]);
    }
}

function drawMechanics(ctx) {
    for (var i = 0; i < mechanics.length; i++) {
        drawMechanic(ctx, mechanics[i]);
    }

    $( "#mechanicCount" ).val('' + mechanics.length);
}

function drawMachine(ctx, machine) {
    let position =  getPositionOfMachine(machine.machineIndex);
    let positionX = position.x;
    let positionY = position.y;

    let machineHealth = machine.health * 100;

    let textPositionX = positionX + HEALTH_TEXT_OFFSET;
    let textPositionY = positionY - HEALTH_TEXT_OFFSET;
    ctx.fillStyle = 'white';
    let borderSizeX = 43;
    let borderSizeY = 20;
    let borderStartX = textPositionX - 3;
    let borderStartY = textPositionY - 15;
    ctx.fillRect(borderStartX, borderStartY, borderSizeX, borderSizeY);

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
    ctx.fillText(healthString, textPositionX, textPositionY);

    // draw the spot for the mechanic
    ctx.beginPath();
    ctx.arc(positionX, positionY, MACHINE_SPOT_RADIUS, 0, 2 * Math.PI, false);
    ctx.fillStyle = 'black';
    ctx.fill();
    ctx.stroke();

    if (DEBUG_ENABLED) {
        ctx.fillStyle = 'black';
        let machineString = 'machine: ' + machine.machineIndex;
        ctx.fillText(machineString, position.x + 2 * HEALTH_TEXT_OFFSET, position.y - 2 * HEALTH_TEXT_OFFSET);
    }
}

function getPositionOfMachine(machineIndex) {
    let position = {
        x: locations[machineIndex].x,
        y: locations[machineIndex].y
    };

    return position;
}

function drawMechanic(ctx, mechanic) {
    if (DEBUG_ENABLED) {
        console.log(
            'drawing a mechanic ' 
            + mechanic.mechanicIndex 
            + ' at current machine ' 
            + mechanic.originalMachineIndex
            + ' and focus machine ' 
            + mechanic.focusMachineIndex);
    }
    var positionX, positionY;
    var machineIndex;
    let mechanicState = mechanic.state;
    if (mechanicState === MechanicState.TRAVELLING) {
        machineIndex = mechanic.originalMachineIndex;
    } else {
        machineIndex = mechanic.focusMachineIndex;
    }

    if (machineIndex != null) {
        let position = getPositionOfMachine(machineIndex);
        positionX = position.x;
        positionY = position.y;
    } else {
        positionX = MECHANIC_RADIUS;
        positionY = MECHANIC_RADIUS;
    }

    let mechanicStyle = getMechanicColorByState(mechanicState);

    ctx.beginPath();
    ctx.arc(positionX, positionY, MECHANIC_RADIUS, 0, 2 * Math.PI, false);
    ctx.fillStyle = mechanicStyle;
    ctx.fill();
    ctx.lineWidth = 2;
    ctx.strokeStyle = mechanicStyle;
    ctx.setLineDash([]);
    ctx.stroke();

    drawNextVisits(ctx, mechanic)
}

function getMechanicColorByState(mechanicState) {
    switch (mechanicState) {
        case MechanicState.TRAVELLING:
            return FIRST_VISIT_STYLE;
        case MechanicState.FIXING:
            return 'yellow';
        case MechanicState.DONE:
            return 'green';
    }
}

function drawNextVisits(ctx, mechanic) {
    let isTravelling = mechanic.state === MechanicState.TRAVELLING;
    let futureIndexes = mechanic.futureMachineIndexes;
    let previousMachineIndex = isTravelling ? mechanic.originalMachineIndex : mechanic.focusMachineIndex;
    let nextMachineIndex;

    if (futureIndexes == null) {
        return;
    }
    for (i = 0; i < futureIndexes.length; i++) {
        if (i > SHOW_NEXT_VISITS) { // show only next N visits
            break;
        }

        nextMachineIndex = futureIndexes[i];

        let travellingAndNotLastConnection = isTravelling && i < SHOW_NEXT_VISITS;
        if (travellingAndNotLastConnection || !isTravelling) {
            drawPathBetweenTwoMachines(ctx, mechanic, previousMachineIndex, nextMachineIndex, i);
        }

        previousMachineIndex = nextMachineIndex;
    }
}

function drawPathBetweenTwoMachines(ctx, mechanic, machineIndex1, machineIndex2, index) {
    if (machineIndex1 == machineIndex2) {
        return;
    }

    let position1 = getPositionOfMachine(machineIndex1);
    let position2 = getPositionOfMachine(machineIndex2);

    let isFirstConnection = machineIndex1 == mechanic.originalMachineIndex;
    let isTravelling = mechanic.state === MechanicState.TRAVELLING

    let style = isFirstConnection && isTravelling ? FIRST_VISIT_STYLE : NEXT_VISIT_STYLE;
    
    ctx.beginPath();
    ctx.moveTo(position1.x, position1.y);
    ctx.lineTo(position2.x, position2.y);
    ctx.setLineDash([20, 4]);
    ctx.lineWidth = 2;
    ctx.strokeStyle = style;
    ctx.stroke();
    
    ctx.beginPath();
    ctx.arc(position2.x, position2.y, MECHANIC_RADIUS/2, 0, 2 * Math.PI, false);
    ctx.fillStyle = style;
    ctx.setLineDash([]);
    ctx.fill();
    ctx.stroke();  
}
