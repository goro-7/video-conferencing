/* constants */

let mimeType = "video/webm;codecs=opus,vp8";

/* functions */
function startIncomingStream(socket) {
    console.log("getting stream from server");
    if (socket === undefined) {
        socket = openGetSocket();
    }

    let mediaSource = new MediaSource();
    let video = document.getElementById('video');
    video.src = window.URL.createObjectURL(mediaSource);
    video.onloadeddata = e => console.info("video - data loaded %o", video);
    video.onplay = e => console.info("video - playing begun %o", video);
    video.onemptied = e => console.info("video - is now empty %o", video);
    video.onwaiting = e => console.info("video - is now waiting for data ... %o", video);
    video.onended = event => info("video ended %o", video);

    let videoBuffer;
    let serverBuffer = [];

    mediaSource.onsourceopen = event => {
        console.info("'source opened' is mime %s supported %o", mimeType, MediaSource.isTypeSupported(mimeType));
        videoBuffer = mediaSource.addSourceBuffer(mimeType);
        videoBuffer.onupdateend = event => transferData(videoBuffer, video, serverBuffer);
        socket.onmessage = async message => {
            debug("received data from server, size - %o ", message.data);
            /*  if (serverBuffer.length > 30) {
                  warn("dropping data from socket serverBuffer");
                  let data = serverBuffer.splice(0, 5);
              }*/
            let data = await message.data.arrayBuffer();
            serverBuffer.push(data);
        }
    }
}

function transferData(videoBuffer, video, serverBuffer) {
    info("videoBuffer onupdateend");
    if (videoBuffer && videoBuffer.updating === false) {
        videoBuffer.appendBuffer(serverBuffer.splice(0, 1));
    }

    if (video.paused && video.readyState >= 2) {
        video.play();
    }

}

function setUpVideo(serverBuffer) {
    const video = document.getElementById('video');
    let mediaSource = new MediaSource();
    mediaSource.onsourceopen = event => {
        info("source opened >> %o", event);
        const sourceBuffer = mediaSource.addSourceBuffer(mimeType);
        if (mediaSource.readyState === "open" && sourceBuffer && sourceBuffer.updating === false) {
            sourceBuffer.appendBuffer(serverBuffer.splice(0, 1));
        }
        video.play();
        //sourceBuffer.onupdateend = event => appendToSourceBuffer(mediaSource, sourceBuffer, video, serverBuffer);
    };
    video.src = window.URL.createObjectURL(mediaSource);

}

function appendToSourceBuffer(mediaSource, sourceBuffer, video, serverBuffer) {
    if (mediaSource.readyState === "open" && sourceBuffer && sourceBuffer.updating === false) {
        sourceBuffer.appendBuffer(serverBuffer.splice(0, 1));
    }
    // Limit the total serverBuffer size to 20 minutes , This way we don't run out of RAM
    if (video.buffered.length && video.buffered.end(0) - video.buffered.start(0) > 1200) {
        sourceBuffer.remove(0, video.buffered.end(0) - 1200)
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
