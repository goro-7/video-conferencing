/* constants */
/* window loaded */
$(document).ready(() => {
info("mediaRecorderRate :>>> %s", mediaRecorderRate);
});


/* window unloaded */
$(window).on("unload", e => {

});

function info(format, objects) {
    console.info(format, objects);
}


function debug(format, objects = null) {
    console.debug(format, objects);
}


function error(format, objects = null) {
    console.error(format, objects);
}


function warn(format, objects = null) {
    console.warn(format, objects);
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
            stopStreaming();
            alert("socket error, reload page");
        };

        webSocket.onclose = closeEvent => {
            console.debug("socket closed", closeEvent);
            stopStreaming();
            alert("socket closed, reload page");

        };

        return webSocket;
    } catch (error) {
        console.error('WebSocket Error ', error);
    }
}