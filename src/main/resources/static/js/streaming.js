/* constants */

let mimeType = "video/webm";

/* functions */
function startIncomingStream(socket) {
    console.log("getting stream from server");
    const buffer = [];
    setUpVideo(buffer);
    if (socket === undefined) {
        socket = openGetSocket();
    }
    socket.onmessage = message => {
        console.info("got message from get-socket ", message);
        //let blob = new Blob(message.data.a, {"type": mimeType});
        buffer.push(message.data);
        // blob = null;
    };
}

function setUpVideo(buffer) {
    // set up video
    const video = document.getElementById('video');
    video.onloadeddata = e => console.info("video - data loaded");
    video.onplay = e => console.info("video - playing begun");
    video.onemptied = e => console.info("video - is now empty");
    video.onwaiting = e => {
        console.info("video - is now waiting for data ... ");
        loadAndPlay(video, buffer).then(r => console.debug("loadAndPlay completed"));
    };
    video.onended = event => loadAndPlay(video, buffer);
    video.play();
}

async function loadAndPlay(video, buffer) {

    if (buffer.length > 0) {
        let slice = buffer.splice(0, 1);
        console.info("playing stream from server slice ", slice);
        let blob = new Blob(slice, {"type": mimeType});
        try {
            let currentTime = video.currentTime;
            video.src = window.URL.createObjectURL(blob);
            video.currentTime = currentTime;
            video.play();
            // URL.revokeObjectURL(objectURL);
        } catch (err) {
            console.warn("during loadAndPlay ", err);
        }
    } else {
        console.info("buffer was empty, waiting ...");
        setTimeout(() => loadAndPlay(video, buffer), loopRate);
    }
}


function openGetSocket() {
    let webSocket;
    try {
        webSocket = new WebSocket(((window.location.protocol === "https:") ? "wss://" : "ws://") + window.location.host + "/ws/get");
        console.debug("GET-WebSocket  open initiated", webSocket);

        // Log errors
        webSocket.onerror = error => console.error('GET-WebSocket Error', error);
        return webSocket;
    } catch (error) {
        console.error('GET-WebSocket Error ', error);
        if (webSocket) {
            webSocket.close();
        }
    }
}
