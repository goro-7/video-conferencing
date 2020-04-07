/* constants */
const constraints = {
    video: true,
    audio: true
};
const options = {
    audioBitsPerSecond: 128000,
    videoBitsPerSecond: 2500000,
    mimeType: 'video/webm'
};


const video1 = document.getElementById('video1');

let cameraStream;
let record = true;
let webSocket;



/* functions */
async function startOutgoingStream() {
    console.log("starting stream to server");
    // connect to web cam & microphone
    cameraStream = await navigator.mediaDevices.getUserMedia(constraints);
    recordAndSend();
}


function recordAndSend() {
    let mediaRecorder = new MediaRecorder(cameraStream, options);
    mediaRecorder.start();
    const buffer = [];
    mediaRecorder.ondataavailable = event => buffer.push(event.data);
    mediaRecorder.onstop = event => sendData(new Blob(buffer));
    setTimeout(() => {
        if (mediaRecorder.state === 'recording') {
            mediaRecorder.stop();
        }
    }, 500);
}

function sendData(data) {
    if (data.size > 0) {
        let socket = openSendSocket();
        socket.onopen = function (openEvent) {
            console.info("sending data of size ", data.size);
            socket.send(data);
            socket.close();
            if(record){
                recordAndSend();
            }
        }
    }else{
        if(record){
            recordAndSend();
        }
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

function closeCamera() {
    record = false;
    console.info("closing camera");
    if (cameraStream) {
        cameraStream.getTracks().forEach(element => {
            element.stop();
        });
    }
}

function openSendSocket() {
    try {
        webSocket = new WebSocket(((window.location.protocol === "https:") ? "wss://" : "ws://") + window.location.host + "/ws/send");
        console.debug("webSocket open initiated", webSocket);
        webSocket.onerror = function (errorEvent) {
            console.error("socket error", errorEvent);
            //mediaRecorder.pause();
        };

        return webSocket;
    } catch (error) {
        console.error('WebSocket Error ', error);
    }
}