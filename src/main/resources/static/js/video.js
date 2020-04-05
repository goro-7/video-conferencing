function toFile(blob) {
    console.log("writing file");
    const file = new File([blob], 'video.mp4', {type: 'video/mp4'});
    console.log("file written", file);
    const element = document.getElementById('videoWin');
    element.setAttribute('src', 'video.mp4');
    element.play();
}
function playBlobAsVideo(blob) {
    const video = document.getElementById("videoWin");
    video.src = window.URL.createObjectURL(blob);
    video.play();
}