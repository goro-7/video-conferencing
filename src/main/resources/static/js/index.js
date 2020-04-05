// manage display of request or offer form
function unHideElement(id) {
    let requestElement = document.getElementById(id);
    requestElement.classList.remove("d-none");
    requestElement.classList.add("d-block");
}

function hideElement(id) {
    let requestElement = document.getElementById(id);
    requestElement.classList.add("d-none");
    requestElement.classList.remove("d-block");
}

function newRequestFormFLinkClicked() {
    let element = document.getElementById('requestLink');
    //console.log("element : ", element)

    hideElement("newOfferForm")
    unHideElement("newRequestFormDiv");
}

function newOfferFormLinkClicked() {
    let element = document.getElementById('offerLink');
    //console.log("element : ", element)
    hideElement("newRequestFormDiv")
    unHideElement("newOfferForm");
}


$(document).ready(function () {
    newRequestFormFLinkClicked();
    //connectToSSE();
    //connectToWS();
});

// manage submission of forms
$(document).ready(function () {

    $("#newRequestForm").submit(function (event) {

        var form = document.getElementById("newRequestForm");
        //stop submit the form, we will post it manually.
        event.preventDefault();

        // collect the form data while iterating over the inputs
        var data = {};
        for (var i = 0, ii = form.length; i < ii; ++i) {
            var input = form[i];
            if (input.name) {
                data[input.name] = input.value;
            }
        }

        //add item dtos
        let itemList = document.getElementById("itemList");
        let itemListElements = itemList.getElementsByTagName("li");
        let itemObjectArray = [];
        for (let i = 0; i < itemListElements.length; i++) {
            let inputElements = itemListElements[i].getElementsByTagName("input");
            console.log("inputElements : ?", inputElements);
            let itemInputName = inputElements.namedItem("itemName");
            let itemInputQuantity = inputElements.namedItem("itemQuantity");
            console.log("itemName : ?, itemQuantity : ?", itemInputName, itemInputQuantity);
            let itemObject = {name: itemInputName.value, quantity: itemInputQuantity.valueAsNumber};
            console.log("itemObject : ?", itemObject);
            itemObjectArray.push(itemObject);
        }
        data["itemDtos"] = itemObjectArray;

        // add requestor name and email
        let requestorName = document.getElementById("requestorName");
        let requestorEmail = document.getElementById("requestorEmail");
        let requestorObject = {name: requestorName.value, email: requestorEmail.value};
        data["requestor"] = requestorObject;
        console.log("req data ", data);

        // construct an HTTP request


        $.ajax({
            type: form.method,
            url: form.action,
            data: JSON.stringify(data),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: apiSuccess,
            failure: function (errMsg) {
                alert(errMsg);
            }
        });
    });

});

function apiSuccess(response) {
    console.log("Response : ", response);
    $('#dialogPara').text("Request Submitted");
    $("#dialog").dialog(
        {
            classes: "alert",
            buttons: [
                {
                    text: "Ok",
                    icon: "ui-icon-newwin",
                    textAlign: "center",
                    click: function () {
                        $(this).dialog("close");
                    },
                    // Uncommenting the following line would hide the text,
                    // resulting in the label being used as a tooltip
                    showText: false
                }
            ]
        });
};


let source;

/* connect to SSE */
function connectToSSE() {

    if (!!window.EventSource) {
        source = new EventSource("/sse");

        source.addEventListener('message', function (event) {
            try {
                //var data = JSON.parse(event.data);
                console.log("SSE -> ", event.data);
            } catch (err) {
                console.log(" ", err);
            }
        }, false);

        source.addEventListener('open', function (event) {
            // Connection was opened.
            console.log("open SSE ", event);
        }, false);

        source.addEventListener('error', function (event) {
            if (event.readyState == EventSource.CLOSED) {
                // Connection was closed.
            }
            console.log("error from SSE ", event)
        }, false);
    } else {
        console.log("EventSource not supported !!");
    }

}


$(window).on("unload", function (e) {
    if (source) {
        console.log("closing SSE connection");
        source.close();
    }
    ;
});


function connectToWS() {
    let webSocket;
    try {
        webSocket = new WebSocket(((window.location.protocol === "https:") ? "wss://" : "ws://") + window.location.host + "/ws");
        console.log("webSocket", webSocket);
    } catch (error) {
        console.log('WebSocket Error ', error);
        $("#openMessage").text("websocket error : " + error);
    }

    webSocket.onopen = function () {
        webSocket.send("Hi from client");
        console.log("websocket connected");
        $("#openMessage").text("websocket open");
    };

    // Log errors
    webSocket.onerror = function (error) {
        console.log('WebSocket Error', error);
        $("#openMessage").text("websocket error : " + error);
    };

    // Log messages from the server
    webSocket.onmessage = function (event) {
        console.log('WebSocket Server: ', event);
        $("#message").text("websocket message : " + event.data);
        playBlobAsVideo(event.data)
    };
}