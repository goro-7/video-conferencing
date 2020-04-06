/* constants */

const video = document.getElementById('video');
let streamOn = true

function setUpVideo() {
    // set up video
    video.onloadeddata = e => console.info("video - data loaded");
    video.onplay = e => console.info("video - playing begun");
    video.onemptied = e => console.info("video - is now empty");
    video.onwaiting = e => {
        console.info("video - is now waiting for data ... ");
    }

    video.onended = event => {
        console.info("video ended");
        startIncomingStream();
    }
}

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
            let blob = new Blob(chunks, {"type": mimeType});
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
