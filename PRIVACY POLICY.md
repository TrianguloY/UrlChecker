
## Introduction
This privacy policy covers the use of the 'URLCheck' (https://github.com/TrianguloY/UrlChecker) Android application.

It may not be applicable to other software produced or released by TrianguloY (https://github.com/TrianguloY).

URLCheck when running does not collect any statistics, personal information, or analytics from its users, other than Android operating system built in mechanisms that are present for all the mobile applications.

URLCheck does not contain any advertising sdk, nor tracker of the user or his device.

Cookies are not stored at any point. VirusTotal authentication credentials can be stored optionally on the user's local device upon the user's explicit request. Developer does not have access to any such information.

All external interactions require user action (pressing a button) unless explicitly configured to automatically do so, which is always disabled by default.

## Third party cloud service dependencies

Note that URLCheck:

* Relies on The ClearUrl Database (https://docs.clearurls.xyz/1.23.0/specs/rules/) to retrieve information required for the operation of the url cleanup module, only if the user accepts it explicitly. Used directly on the user's device. For this purpose only, processed without sending any data related to the user, their device or their use of these;
* has the ability, at the user's request, to retrieve the URLs passing through the app by relying on the logging module. This is done without storing any identifiable user information, the data is only stored locally on the user's device;
* allows online url scanning, upon user activation, relying on VirusTotal cloud service. VirusTotal user credentials are stored locally on the user’s device (API key) and are only used for authentication with the official endpoints. Optionally, this service may store user information and data allowing identification; Please refer to VirusTotal's privacy policy (https://support.virustotal.com/hc/en-us/articles/115002168385-Privacy-Policy) for details on how they handle user data.
 
 <!-- URLCheck specific licenses of libraries used in the application can be accessed from About section. - Not useful actually -->

## Android permissions requested by the application
Note that URLCheck application requires the following android platform permissions:

* “network” android permission in order to be able to perform status retrieval, parsing or checking URLs, downloading or updating the ClearUrl database. Only at the explicit request of the user or automatically if configured to do so, which is always disabled by default.
