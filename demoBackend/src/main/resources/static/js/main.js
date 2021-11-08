const POLLING_INTERVAL = 1000;
const POLLING_RETRIES = 120;

function launchAuthApp(action) {
    if (!isAndroid()) {
        alert("Functionality only available for Android devices.")
        return null
    }

    // Fetch challenge.
    httpGetAsync(originUrl + challengeUrl, (body) => {
        let data = JSON.parse(body);
        let challenge = data.nonce;
        let intent = createParametrizedIntentUrl(challenge, action); // TODO: Error handling.
        console.log(intent);
        window.location.href = intent;
        pollForAuth(POLLING_INTERVAL, challenge);
    })
}

function pollForAuth(timeout, challenge) {
    console.log("Polling for auth");
    let requestUrl = originUrl + authenticationRequestUrl + "?challenge=" + challenge;

    let counter = 0;
    let timer = setInterval(() => {
        // Fetch authentication object.
        httpGetAsync(requestUrl, (body) => {
            console.log(body);
            // If this is a successful request, stop the polling.
            clearInterval(timer);
            window.location.href = originUrl + loggedInUrl;
        });
        counter++;
        if (counter > POLLING_RETRIES) {
            clearInterval(timer); // Stop polling after some time.
            let loginErrorAlert = document.getElementById("loginErrorAlert");
            loginErrorAlert.classList.remove("d-none")
        }
    }, timeout)

}

function createParametrizedIntentUrl(challenge, action) {
    if (action == null) {
        console.error("There has to be an action for intent.")
    }
    else if (challenge == null) {
        console.error("Challenge missing, can't authenticate without it.")
    } else {
        return intentUrl + "?" + "action=" + action + "&challenge=" + challenge + "&authUrl=" + originUrl + authenticationRequestUrl;
    }
}

function isAndroid() {
    // Check if using Android device.
    const ua = navigator.userAgent.toLowerCase();
    return ua.indexOf("android") > -1;
}

function httpGetAsync(theUrl, callback) {
    console.log("Sending a request.")
    const xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState === 4 && xmlHttp.status === 200) {
            callback(xmlHttp.responseText);
        }
    }
    xmlHttp.open("GET", theUrl, true); // true for asynchronous
    xmlHttp.send(null);
}