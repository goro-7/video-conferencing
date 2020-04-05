/* window loaded */
$(document).ready(function () {

});

/* window unloaded */
$(window).on("unload", function (e) {

});

const video = document.getElementById('video');
let webSocket;
let canConnect = true;

function connect() {
 canConnect=true;
 connectStream();
 startOutgoingStream();
}

function connectStream() {

    if(!canConnect){
        console.log("Flag off , skipping connect ", canConnect);
    }

    try {
        webSocket = new WebSocket(((window.location.protocol === "https:") ? "wss://" : "ws://") + window.location.host + "/ws");
        console.log("webSocket", webSocket);
    } catch (error) {
        console.log('WebSocket Error ', error);
        $("#openMessage").text("websocket error : " + error);
        connectStream();
    }

    webSocket.onopen = function () {
        webSocket.send("Hi from client");
        console.log("websocket connected");
        $("#openMessage").text("websocket open");
    };

    // Log errors
    webSocket.onerror = function (error) {
        console.log('WebSocket Error', error);
        $("#openMessage").text("websocket error : " + error);
        console.log('Attempting reconnect ...')
        connectStream();
    };

    // Log messages from the server
    webSocket.onmessage = function (event) {
        console.log('WebSocket Server: ', event);
        $("#message").text("websocket message : " + event.data);
        playBlobAsVideo(event.data)
    };
}

function toFile(blob) {
    console.log("writing file");
    const file = new File([blob], 'video.mp4', {type: 'video/mp4'});
    console.log("file written", file);
    video.setAttribute('src', 'video.mp4');
    video.play();
}

function playBlobAsVideo(blob) {
    video.src = window.URL.createObjectURL(blob);
    video.play();
}

function disconnectStream() {
    canConnect = false;
}