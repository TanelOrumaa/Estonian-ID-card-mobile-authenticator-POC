# Demo backend + website for mobile authentication project.

## How to run.


### 1. Clone GIT repository
### 2. Setup HTTPS
Since Web eID only works over HTTPS connection, you'll need to serve the backend and website with an HTTPS certificate. A suitable tool for that is ngrok (https://ngrok.com/).
To use ngrok, download it and then run the command (may need administrator rights) 
```ngrok http 8080```
and you should see something like this:
```
ngrok by @inconshreveable                                                                               (Ctrl+C to quit)

Session Status                online                                                                                    
Account                       TanelOrumaa (Plan: Free)                                                                  
Version                       2.3.40                                                                                    
Region                        United States (us)                                                                        
Web Interface                 http://127.0.0.1:4040                                                                     
Forwarding                    http://somethinghere.ngrok.io -> http://localhost:8080                              
Forwarding                    https://somethinghere.ngrok.io -> http://localhost:8080                             
                                                                                                                        
Connections                   ttl     opn     rt1     rt5     p50     p90                                               
                              1508    0       0.00    0.00    2.31    75.59                                             
                                                                                                                        
HTTP Requests                                                                                                           
-------------                                                                                  
```

Copy the second forwarding link (the one with https) and put it in ```com.tarkvaratehnika.demobackend.config.ApplicationConfiguration.kt``` as ```val WEBSITE_ORIGIN_URL = "https://yourlinkhere.com"```

### 3. Run the project
Use your favourite IDE or just run it via commandline with ```./mvnw spring-boot:run```

On your browser (Android to test out from Android device or desktop to try out ID-card reader or QR-code capability) navigate to the url you copied earlier and you should see the website landing page. If you have the mobile authentication app installed, you should be able to log into the website with your Estonian ID-card.


## Credits...
...go out to creators of https://github.com/web-eid/web-eid-spring-boot-example. That example project was used in some parts as an example (files where inspiration was taken are correctly annotated with the appropriate license text).