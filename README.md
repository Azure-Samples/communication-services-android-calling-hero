# communication-services-android-calling-hero-pr

This project demonstrates the integration of [communication-services](https://docs.microsoft.com/en-us/azure/communication-services/quickstarts/voice-video-calling/calling-client-samples?pivots=platform-android) for Android Java application.

## Features

- Initialize the CallClient, create a CallAgent, and access the DeviceManager
- Start a new group call
- Join an existing group call
- Render remote participant video streams dynamically
- Swtich layout between different call cases: only-local video view, one-on-one call view and group call with multiple participants
- Turning local video stream from camera on/off
- Mute/unmute local microphone audio

## Prerequisites

- An Azure account with an active subscription. [Create an account for free](https://azure.microsoft.com/free/?WT.mc_id=A261C142F).
- An OS running [Android Studio](https://developer.android.com/studio).
- A deployed Communication Services resource. [Create a Communication Services resource](https://docs.microsoft.com/en-us/azure/communication-services/quickstarts/create-communication-resource).
- An Authentication Endpoint that will return the Azure Communication Services Token. See [example](https://docs.microsoft.com/en-us/azure/communication-services/tutorials/trusted-service-tutorial)

## Steps to Run

1. Open Android Studio and select `Open an Existing Project`.
2. Select folder `ACSCall`.
3. Expand app/assets to update `appSettings.properties`. Set the value for the key `acsTokenFetchUrl` to be the URL for your Authentication Endpoint.
4. Build/Run.

## Securing Authentication Endpoint

For simple demonstration purposes, this sample uses a publicly accessible endpoint by default to fetch an ACS token. For production scenarios, it is recommended that the ACS token is returned from a secured endpoint.  

With additional configuration, this sample also supports connecting to an **Azure Active Directory** (AAD) protected endpoint so that user login is required for the app to fetch an ACS token. See steps below:

1. Enable Azure Active Directory authentication in your app.
   - [Register your app under Azure Active Directory (using Android platform settings)](https://docs.microsoft.com/en-us/azure/active-directory/develop/tutorial-v2-android)
   - [Configure your App Service or Azure Functions app to use Azure AD login](https://docs.microsoft.com/en-us/azure/app-service/configure-authentication-provider-aad)
2. Go to your registered app overview page under Azure Active Directory App Registrations. Take note of the `Package name`, `Signature hash`, `MSAL Configutaion`
   ![](./docs/images/androidConfigurationImage.png)
3. Edit `ACSCall/app/src/main/assets/appSettings.properties` and set `isAADAuthEnabled` to enable Azure Active Directory
4. Edit `AndroidManifest.xml` and set `android:path` to keystore signature hash. (Optional. The current value uses hash from bundled debug.keystore. If different keystore is used, this must be updated.)
   ```
   <activity android:name="com.microsoft.identity.client.BrowserTabActivity">
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data
                    android:host="com.azure.samples.acscall"
                    android:path="/Signature hash" <!-- do not remove /. The current hash in AndroidManifest.xml is for debug.keystore. -->
                    android:scheme="msauth" />
            </intent-filter>
        </activity>
   ```
5. Copy MSAL Android configuration from Azure portal and paste to `ACSCall/app/src/main/res/raw/auth_config_single_account.json`. Include "account_mode" : "SINGLE"
   ```
      {
         "client_id": "",
         "authorization_user_agent": "DEFAULT",
         "redirect_uri": "",
         "account_mode" : "SINGLE",
         "authorities": [
            {
               "type": "AAD",
               "audience": {
               "type": "AzureADMyOrg",
               "tenant_id": ""
               }
            }
         ]
      }
   ```

6. Edit `ACSCall/app/src/main/assets/appSettings.properties` and set the value for the key `acsTokenFetchUrl` to be the URL for your secure Authentication Endpoint.
7. Edit `ACSCall/app/src/main/assets/appSettings.properties` and set the value for the key `aadScopes` from `Azure Active Directory` `Expose an API` scopes
