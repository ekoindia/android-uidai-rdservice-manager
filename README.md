# Android UIDAI RD-Service Manager
Android helper library library to easily integrate any fingerprint device in your app (for UIDAI based secure Aadhaar authentication in India)

[![GitHub issues](https://img.shields.io/github/issues/ekoindia/android-uidai-rdservice-manager)](https://github.com/ekoindia/android-uidai-rdservice-manager/issues)  [![GitHub license](https://img.shields.io/github/license/ekoindia/android-uidai-rdservice-manager)](https://github.com/ekoindia/android-uidai-rdservice-manager/blob/master/LICENSE)
<a href="https://twitter.com/intent/follow?screen_name=ekospeaks" target="_blank">![Twitter Follow](https://img.shields.io/twitter/follow/ekospeaks?label=Follow&style=social)</a>

---

## Usage

### STEP 1: Add library in your Android project

Add jitpack in your root build.gradle at the end of repositories:
```java
allprojects {
  repositories {
    // ...
    maven { url 'https://jitpack.io' }
  }
}
```

Add UIDAI-RDService-Manager library to your app build.gradle file:
```java
Dependencies {
  // ...
  implementation 'com.github.ekodevelops:eko-gateway-android-sdk:1.2.0'
}
```

### STEP 2: Use the library in your Activity

Implement RDServiceEvent interface:
```java
public class MyActivity extends AppCompatActivity implements RDServiceEvents
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
}

@Override
public void onRDServiceCaptureResponse(String pidData) {
}

@Override
public void onRDServiceDriverDiscoveryFailed(int resultCode, Intent data, String pkg, String reason) { reason);
}

@Override
public void onRDServiceDriverNotFound() {
}

@Override
public void onRDServiceCaptureFailed(int resultCode, Intent data, String pkg) {
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

## Examples
_TODO_
