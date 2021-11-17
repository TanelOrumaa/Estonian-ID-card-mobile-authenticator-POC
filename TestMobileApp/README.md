# TestMobileApp overview
### The purpose
The TestMobileApp was created in order to demonstrate how a different application on the Android smartphone could use the MobileAuthApp for user authentication purposes.
### Installing the application
The application installation process is the same as with the MobileAuthApp. Check the guide in the project's [main readme file](https://github.com/TanelOrumaa/Estonian-ID-card-mobile-authenticator-POC#installing-the-application-on-the-phone). 
### Using the application
In order to use this application a backend server must be running that can issue challenges and verify the token created by the MobileAuthApp.  
Use demoBackend application that is included in the project. Follow the demoBackend setup guide and once you have a backend running take the https address of the backend
and add it in the TestMobileApp's MainActivty.kt file as the new value for the constant variable BASE_URL (this is easly noticeable in the class as it is pointed out with a comment).
Now the app can be used.
