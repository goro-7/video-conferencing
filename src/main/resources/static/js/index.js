/* constants */
/* window loaded */
$(document).ready(function () {
    setUpVideo();
});


/* window unloaded */
$(window).on("unload", function (e) {
  closeCamera();
});

function startStreams(){
  // startIncomingStream();
   startOutgoingStream();
}