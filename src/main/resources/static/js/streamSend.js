/* constants */
const loopRate = 5000;
const mediaRecorderRate = '${mediaRecorderRate}';
const constraints = {
    video: true,
    audio: true
};
const options = {
    // audioBitsPerSecond: 128000,
    // videoBitsPerSecond: 2500000,
    mimeType: 'video/webm'
};
let streaming = false;
let webSocket;

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
    stopStreaming();
});

/* functions */
async function startStreaming() {
    console.log("starting stream to server");
    streaming = true;
    webSocket = openSendSocket();
    try {
        let camera = await navigator.mediaDevices.getUserMedia(constraints);
        recordAndSend(camera);
        startIncomingStream(webSocket);
    } catch (err) {
        console.error("failed to get camera", err);
    }
}


function recordAndSend(camera) {
    let mediaRecorder = new MediaRecorder(camera, options);

    try {
        mediaRecorder.start(loopRate);
    } catch (e) {
        console.warn("media start ", e);
        return;
    }

    mediaRecorder.ondataavailable = event => sendData(mediaRecorder, event, camera);
    mediaRecorder.onstop = event => sendData(mediaRecorder, event, camera);

    /*    setTimeout(() => {

                if (mediaRecorder.state === 'streaming' || mediaRecorder.state === 'recording') {
                    mediaRecorder.stop();
                }

                recordAndSend(camera);
            },
            loopRate);*/
}

function sendData(mediaRecorder, event, camera) {
    if (streaming === false) {
        debug("got data but streaming flag is off so not sending it");
        return;
    }

    if (event.type !== 'dataavailable') {
        return;
    }

    let data = event.data;

    if (data.size > 0) {
        if (webSocket.readyState === WebSocket.OPEN) {
            console.debug("sending data of size ", data.size);
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
    if(mediaRecorder.state === "recording"){
        mediaRecorder.stop();
    }

    recordAndSend(camera);
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

async function stopCamera() {
    let camera = await navigator.mediaDevices.getUserMedia(constraints);
    if (camera) {
        info("closing camera");
        camera.getTracks().forEach(element => {
            element.stop();
        });
    }
}

function stopStreaming() {
    info("stopping in & out streaming");
    streaming = false;
    stopCamera();
    if (webSocket) {
        webSocket.close(1000, "User clicked disconnect");
    }
}

function streamingStopped() {
    return streaming === false;
}