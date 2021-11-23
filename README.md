# Estonian-ID-card-mobile-authenticator-POC

This is a proof-of-concept project for creating an Android app for authenticating yourself using an NFC-enabled Estonian ID card. This project will be created for the University of Tartu course "Software project".

### Requirements to use the application
* The smartphone's operating system must be Android 8.0 or newer
* The smartphone must support NFC technology and it must be enabled
* The user must have an Estonian ID card with NFC capability (issued since 2018)

### Installing the application on the phone
The first option is to open the MobileAuthApp folder of the project on the Android Studio and use the smartphone instead of an emulator (the application does not work with emulators because real ID card has to be scanned, which an emulator can not do) to run the application. This way the application gets installed on the phone automatically.   

More information about using real devices with Android studio: https://developer.android.com/studio/run/device  

The second and more reliable option is to get the .apk that is generated under the Artifacts of GitHub Actions when the project is built. Download the .apk file and move it to the smartphone and install it (phone permissions might have to be changed because it is not installed through Google Play). After the application has been installed it should open as any other application.

More info about installing third party applications on the Android phones: https://www.androidauthority.com/how-to-install-apks-31494/  

**NB! Before using the application make sure that the NFC is enabled on the phone, otherwise information can not be read from the ID card.**

### Testing the application
The project comes with a test mobile application and a test web application that can be used to try the MobileAuthApp authentication feature even if you don't have any web applications or mobile applications that require user authentication. Both projects come with a README file that help with a setup.
The mobile authentication application, when launched by the user not a website or some other application, can also read card holder's information, which can be used to verify whether the application reads the information from the ID card correctly.

### Wiki pages relevant for the "Software project" subject
* [Project Vision](https://github.com/TanelOrumaa/Estonian-ID-card-mobile-authenticator-POC/wiki/Project-Vision)
* [Release Notes](https://github.com/TanelOrumaa/Estonian-ID-card-mobile-authenticator-POC/wiki/Release-notes)
* [Project tasks](https://tvp-mobile-authentication.atlassian.net/jira/software/projects/MOB/boards/1/backlog) (Ask Tanel for JIRA permissions if needed).
* [Project plan](https://github.com/TanelOrumaa/Estonian-ID-card-mobile-authenticator-POC/wiki/Project-plan)
* [Use Cases](https://github.com/TanelOrumaa/Estonian-ID-card-mobile-authenticator-POC/wiki/Use-Cases)
* [User stories](https://github.com/TanelOrumaa/Estonian-ID-card-mobile-authenticator-POC/wiki/User-stories)
* [Use Case Tests](https://github.com/TanelOrumaa/Estonian-ID-card-mobile-authenticator-POC/wiki/Use-Case-Tests)
