/* constants */
const constraints = {
    video: true,
    audio: true
};

const video1 = document.getElementById('video1');

/* functions */
function startOutgoingStream() {
    console.log("starting stream to server");

    // connect to web cam & microphone
    navigator.mediaDevices.getUserMedia(constraints).then((stream) => {
        video1.srcObject = stream
    });

}

function notSupported() {
    if (!!(navigator.mediaDevices && navigator.mediaDevices.getUserMedia)) {
        alert('getUserMedia() is not supported by your browser');
        return false;
    } else {
        return true;
    }

}