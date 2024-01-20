# HortusFox Android App

(C) 2023 - 2024 by Daniel Brendel

**Contact**: dbrendel1988(at)gmail(dot)com\
**GitHub**: https://github.com/danielbrendel

Released under the MIT license

## Description
This app is for android mobile phones or tablets in order to comfortably
use the <a href="https://github.com/danielbrendel/hortusfox-web">HortusFox Plant Management system</a>.
This way a more native mobile experience should be provided.

## Installation
The build process is very quick. You just need to set the URL to your HortusFox instance in order to build 
the app for your users. Depending on whether you want to create a debug or release build, two separate property files
need to be created for that. 
In order to make a debug build, please create a debug.properties file in your project root directory.
For release builds you need to create a release.properties file in your project root.
Example of a properties file:
```sh
# This is the URL where the HortusFox web application instance is running on.
BASE_URL="http://your-url-goes-here.com"
```
After that you can create an APK build and ship it to your users. After installing on an Android device, the user 
can then just launch the app and then should see the login page where they can login with their credentials.

## System requirements
- Android 7.0+ (Android 12.0+ recommended)
- Android Studio Giraffe (recommended)