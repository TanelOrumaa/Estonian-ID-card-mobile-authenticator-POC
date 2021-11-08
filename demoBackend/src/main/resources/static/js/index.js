window.onload = () => {
    // Add event listener for login button.
    let loginButton = document.getElementById("loginButton");

    if (loginButton != null) {
        loginButton.addEventListener("click", () => {
            let action = loginButton.getAttribute("data-action");
            loginButton.setAttribute("disabled", "true");
            loginButton.textContent = "Logging in";
            launchAuthApp(action);
        })
    }
}

