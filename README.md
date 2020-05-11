# Android UIDAI RD-Service Manager
Android library to easily integrate fingerprint device support in your app (for UIDAI based secure Aadhaar authentication in India). Build your own AePS (Aadhaar based Cash-Out) or eKYC services for Android.

<img alt="JitPack" src="https://img.shields.io/jitpack/v/github/ekoindia/android-uidai-rdservice-manager"></a>
<a href="https://github.com/ekoindia/android-uidai-rdservice-manager/issues">![GitHub issues](https://img.shields.io/github/issues/ekoindia/android-uidai-rdservice-manager)</a>
<a href="https://github.com/ekoindia/android-uidai-rdservice-manager/blob/master/LICENSE">![GitHub license](https://img.shields.io/github/license/ekoindia/android-uidai-rdservice-manager)</a>
<a href="https://eko.in" target="_blank">![Develop With Eko.in](https://img.shields.io/badge/Develop%20with-Eko.in-brightgreen)</a>
<a href="https://twitter.com/intent/tweet?text=Wow:&url=https%3A%2F%2Fgithub.com%2Fekoindia%2Fandroid-uidai-rdservice-manager" target="_blank"><img alt="Twitter" src="https://img.shields.io/twitter/url?style=social&url=https%3A%2F%2Fgithub.com%2Fekoindia%2Fandroid-uidai-rdservice-manager"></a>
<a href="https://twitter.com/intent/follow?screen_name=ekospeaks" target="_blank">![Twitter Follow](https://img.shields.io/twitter/follow/ekospeaks?label=Follow&style=social)</a>

---

## Introduction

As per [UIDAI](https://uidai.gov.in) (Aadhaar) guidelines, only registered biometric devices can be used for Aadhaar Authentication & eKYC transactions. These devices come with RDService drivers (usually available on PlayStore) that exposes a standard API.

This library makes it easy to work with all such devices so that your app can search for installed drivers and get the fingerprint data after a scan.

For reference, you may check out the latest [Aadhaar Registered Devices Technical Specification v2 by UIDAI](https://uidai.gov.in/images/resource/Aadhaar_Registered_Devices_2_0_4.pdf)

## Usage

### STEP 1: Add library in your Android project

Add jitpack in your root build.gradle file:
```gradle
allprojects {
  repositories {
    // ...
    maven { url 'https://jitpack.io' }
  }
}
```

Add UIDAI-RDService-Manager library to your app build.gradle file:
```gradle
Dependencies {
  // ...
  implementation 'com.github.ekoindia:android-uidai-rdservice-manager:<latest-version>'
}
```

### STEP 2: Use the library in your Activity

Implement RDServiceEvent interface:
```java
import in.eko.uidai_rdservice_manager_lib.RDServiceEvents;
import in.eko.uidai_rdservice_manager_lib.RDServiceManager;

public class MyActivity extends AppCompatActivity implements RDServiceEvents {
```

Initiate RDServiceManager in your activity's _onCreate()_:
```java
private RDServiceManager rdServiceManager;

@Override
protected void onCreate(Bundle savedInstanceState)
{
  // ...
  rdServiceManager = new RDServiceManager.Builder(this).create();
}
```

Setup RDService event callbacks:
```java
@Override
public void onRDServiceDriverDiscovery(String rdServiceInfo, String rdServicePackage) {
  // Called when an installed driver is discovered
}

@Override
public void onRDServiceCaptureResponse(String pidData, String rdServicePackage) {
  // Called when fingerprint is successfully captured
}

@Override
public void onRDServiceDriverDiscoveryFailed(int resultCode, Intent data, String rdServicePackage, String reason) {
  // Called when a discovered driver fails to provide a proper status information 
}

@Override
public void onRDServiceDriverNotFound() {
  // Called when no installed driver is found
}

@Override
public void onRDServiceCaptureFailed(int resultCode, Intent data, String rdServicePackage) {
  // Called when fingerprint capture fails
}
```

Search for installed RDService drivers:
```java
rdServiceManager.discoverRdService();
```

Initiate fingerprint capture:
```java
rdServiceManager.captureRdService(data);
```

## Configuration
_TODO_

## Javadocs
https://ekoindia.github.io/android-uidai-rdservice-manager/

## Examples
_TODO_
