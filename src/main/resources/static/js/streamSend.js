/* constants */
const loopRate = 6000;
const constraints = {
    video: true,
    audio: true
};
const options = {
   // audioBitsPerSecond: 128000,
   // videoBitsPerSecond: 2500000,
    mimeType: 'video/webm'
};
let recording = false;
let webSocket;
let camera;

function generateUserId() {
    let userId = uuidv4();
    console.info(`using userId ${userId}`);
    self.window.name = userId.toString();
}

function uuidv4() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => {
        const r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}


function getUserId() {
    return self.window.name;
}

/* document events */
$(document).ready(() => generateUserId());


$(window).on("unload", () => {
    recording = false;
    stopOutgoingStream();
});

/* functions */
async function startOutgoingStream() {
    console.log("starting stream to server");
    webSocket = openSendSocket();
    try {
        camera = await navigator.mediaDevices.getUserMedia(constraints);
        recordAndSend(camera);
        //startIncomingStream(webSocket);
        startIncomingStream(webSocket);
    } catch (err) {
        console.error("failed to get camera", err);
    }
}


function openSendSocket() {
    try {
        const userId = getUserId();
        const url = ((window.location.protocol === "https:") ? "wss://" : "ws://") + window.location.host + "/ws/send/" + getUserId();
        console.info(`connecting ${url}`);
        let webSocket = new WebSocket(url);
        console.debug("webSocket open initiated", webSocket);
        webSocket.onerror = errorEvent => {
            console.error("socket error", errorEvent);
            stopOutgoingStream();
            alert("socket error, reload page");
        };

        webSocket.onclose = closeEvent => {
            console.debug("socket closed", closeEvent);
            stopOutgoingStream();
            alert("socket closed, reload page");

        };

        return webSocket;
    } catch (error) {
        console.error('WebSocket Error ', error);
    }
}

function recordAndSend(camera) {
    let mediaRecorder = new MediaRecorder(camera, options);
    try {
        mediaRecorder.start();
    } catch (e) {
        console.warn("media start ", e);
        return;
    }
    recording = true;
    const buffer = [];
    mediaRecorder.ondataavailable = event => buffer.push(event.data);
    mediaRecorder.onstop = event => {
        let blob = new Blob(buffer);
        sendData(blob);
        blob = null;
        buffer.length = 0;
    };
    setTimeout(() => {
            if (mediaRecorder.state === 'recording') {
                mediaRecorder.stop();
            }
            recordAndSend(camera);
        },
        loopRate);
}

function sendData(data) {
    if (recording === false) {
        return
    }
    if (data.size > 0) {
        if (webSocket.readyState === WebSocket.OPEN) {
            console.info("sending data of size ", data.size);
            try {
                webSocket.send(data);
            } catch (error) {
                console.warn("Failed to send date", error);
            }

        } else {
            console.warn("Socket not open yet ", webSocket);
        }
        data = null;
    }
}

/*
function notSupported() {
    if (!!(navigator.mediaDevices && navigator.mediaDevices.getUserMedia)) {
        alert('getUserMedia() is not supported by your browser');
        return false;
    } else {
        return true;
    }
}*/

function stopCamera() {
    if (camera) {
        camera.getTracks().forEach(element => {
            element.stop();
        });
    }
}

function stopOutgoingStream() {
    console.info("closing camera");
    recording = false;
    stopCamera();
    if (webSocket) {
        webSocket.close(1000, "User clicked disconnect");
    }
}

function isRecording() {
    return recording === true;
}