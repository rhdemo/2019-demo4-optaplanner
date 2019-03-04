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

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#unpauze" ).click(function() { unpauze(); });
    $( "#pauze" ).click(function() { pauze(); });
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

function unpauze() {
    stompClient.send("/app/unpauze", {});
}

function pauze() {
    stompClient.send("/app/pauze", {});
}

function addMechanic() {
    stompClient.send("/app/addMechanic", {});
}

function removeMechanic() {
    stompClient.send("/app/removeMechanic", {});
}

function processResponse(response) {
    $("#machines").append("<tr><td>" + response.responseType + "</td></tr>");
}
