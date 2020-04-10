/* constants */
/* window loaded */
$(document).ready(() => {

});


/* window unloaded */
$(window).on("unload", e => {

});

function startStreams(){

}

function info(format, objects = null) {
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