/* constants */

const video = document.getElementById('video');

$(document).ready(function () {

});


/* window unloaded */
$(window).on("unload", function (e) {

});

/* functions */
function startIncomingStream() {
    console.log("getting stream from server");
    let socket = openGetSocket();

    const chunks = [];
    let mimeType;
    socket.onmessage = function (message) {
        console.info("got data from get-socket  size, type", message.data.size, message.data.type);
        mimeType = message.data.type;
        chunks.push(message.data);
    };

    socket.onclose = function (closeEvent) {
        if (chunks.length > 0) {
            let blob = new Blob(chunks, { "type": mimeType });
            video.src = window.URL.createObjectURL(blob);
            console.info("playing stream from server");
            video.play();
        }
    }
}


function openGetSocket() {
    let webSocket;
    try {
        webSocket = new WebSocket(((window.location.protocol === "https:") ? "wss://" : "ws://") + window.location.host + "/ws/get");
        console.debug("GET-WebSocket  open initiated", webSocket);

        // Log errors
        webSocket.onerror = function (error) {
            console.error('GET-WebSocket Error', error);
        };
        return webSocket;
    } catch (error) {
        console.error('GET-WebSocket Error ', error);
        if (webSocket) {
            webSocket.close();
        }
    }
}
