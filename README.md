# HortusFox Android App

(C) 2023 by Daniel Brendel

**Contact**: dbrendel1988(at)gmail(dot)com\
**GitHub**: https://github.com/danielbrendel

Released under the MIT license

## Description
This app is for android mobile phones or tablets in order to comfortably
use the <a href="https://github.com/danielbrendel/hortusfox-web">HortusFox Plant Management system</a>.
It is used to access the HortusFox web backend comfortably. Each user in your local environment gets
a personal build of the app. See installation section for more details.

## Installation
The build process is very quick. You just need to set two variables in order to build the app for your
users. Depending on whether you want to create a debug or release build, two seperate property files
need to be created for that. 
In order to make a debug build, please create a debug.properties file in your project root directory.
For release builds you need to create a release.properties file in your project root.
The following contents must be specified:
```sh
# This is the URL where the HortusFox webserver is running on.
BASE_URL="http://your-url-goes-here.com"

# This is the auth token for your user of this build. Check the user table in your database to see the auth tokens
AUTH_TOKEN="your_auth_token_here"
```
After that you can create an APK build and ship it to your users. After installing on an Android device,
the user can then just launch the app and then should see the dashboard when successfully started the app.

## System requirements
- Android 7.0+ (Android 12.0+ recommended)
- Android Studio Giraffe (recommended)