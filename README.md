# Autobahn Android Documentation
### Last Update: February 20,2012
Changelog:
* February 20, 2020:
  * Added Documentation for autobahn android
-----

Author Information
* Herald Joel M. Santos
* hraldsantos@gmaio.com
* +639478574911

-----

### Table of Contents

INSTALLATION
* Repository
  * Overview

PROGRAMMING
* Team / Programmer Identity
* Project Config
  * File Structure
* Architecture
* Testing
  * Accessibility (Automated QA)
    * General Instructions
  * Unit Testing
* Libraries
  * Rxjava, RxAndroid and RxKotlin (Reactive)
  * Retrofit (Networking)
  * KotlinX (JSON Serializer)
  * Epoxy (List Library)
  * DaggerAndroid  (Dependency Injection)
  * Firebase (Push Notifications)

DEPLOYMENT
* Summary
* Versioning
* Source Control
* Deployment Targets
  * Firebase App Distribution
  * Google Play Store
* Internal Testing
  * Fabric / Crashlytics
* Post-Release

-----
# INSTALLATION
Create the following dummy files:

- autobahn.keystore
- keystore.properties

```
keyPassword=
keyAlias=
storePassword=
storeFile=../autobahn-keystore.jks
```

* Repository
  * Overview
  * Source Control Flow
  * Pull Requests and Code Review

## Repository

### Overview
* Source control: Git
* Source control flow: Git Flow
* URL: http://10.51.1.53/autobahn/autobahn-android
  * Access is faster when via IP Address, so just use this

### Source Control Flow

We use Git Flow to manage commits from developers. It lists rules for merging, pulling, and branching.

#### Do
* Always branch out from the right branches
* Always make a pull request when wanting to merge into develop

#### Don't
* Never rebase
* Never amend history for pushed branches

*Generally speaking, the benefits of rebasing and amending history is negligible compared to the potential problems that can result from its improper use. Make your life simpler by never using it.*

Basically, the branches in Git Flow look like this:
```
* master
  * develop
    * feature/(name_of_feature)/develop
    * feature/(name_of_feature)/subfeature1
    * feature/(name_of_feature)/subfeature2
    ...
  * refactor   
    * refactor/develop
    * refactor/(name_of_feature)/subfeature1
    * refactor/(name_of_feature)/subfeature2
    ...
  * release 
    * release-1.0.1
  ...
  * release-<major version>.<minor version>.<patch version>
```

#### Branch Types

* master
* develop/feature/develop
* feature/feature/subfeature
* release

**master**
* This branch should *ALWAYS* be stable
  * Ensured by only merging releases
  * master branch represents what is deployed in Production
* The idea is anyone who checks out the repository and builds master can run the app
* The only branches that can merge into master are release branches
* Never commit to this branch

**develop**
* This branch should *ALWAYS* be stable
  * Ensured by code reviewing any feature/develop branch for merging here
  * develop branch represents what can be shown to the stakeholders whenever they ask for progress on any ongoing development
* Can pull from master at *any* time (also why master should always be stable)
  * Generally after master is updated, develop pulls master to ensure that active development branches can be updated
* Any new feature development should branch off from develop
* Releases branch off from develop
* Any feature development that wants to get into develop must pass code review

**feature develop**
* A branch designed for features expected for later release
* This should be as stable as possible
  * feature/develop represents a usable prototype of the feature as it is being built
* Developers do not commit on this branch directly

**feature branches**
* An in-progress branch for a feature
* May not always be stable - developers may actively be building on it
* Should only merge to feature/develop when it is in a usable state (not crashing, etc)

**release**
* A branch used for releasing a build for testing or Production
* Always comes from develop branch
* Version number and changelog updates are done here
* Made with the intention of possibly merging to master, so it must be as stable as possible
* Any bugs found in a testing release is done on the release branch for that version
  * Build number is incremented by 1
  * Build is released again after for testing
* Whenever a change occurs in a release branch submit a pull request for merging to master

#### Merging Flow

Merging flows ensure that changes don't occur in unexpected places.

The main develop branch is where work is compiled among team members - while feature development is ongoing, a team member will branch off from develop to create <feature>/develop (eg. approval-tabs/develop), and then branch off from there to create sub branches as needed (eg. approval-tabs/ui, approval-tabs/refactor).

The merging flow for the branches are like so:
```
feature/subfeature -> feature/develop -> CODE REVIEW -> develop -> release -> master -> develop
```
Developers can merge their feature/subfeature branches into their feature/develop freely - they control how their feature/develop branches are built.

However, a pull request must be made when a feature/develop is to be merged into develop, as if the developer is saying, "I'm finished engineering this feature, I want it to be added to our app".

#### Pull Flow

Developers can perform pull actions at any time, provided that they follow the rules indicated above. In summary, the update (pull) flow for branches are like so:
```
develop <- master
feature/develop <- develop
feature/subfeature <- feature/develop
```
Developers might want to pull to their branches whenever something new is added to develop. They must follow the flow from top-to-bottom, depending on where the changes are located.

For instance, if there are two developers working on Feature A and B respectively, and Feature B contains changes that Feature A can use.

When Feature B is merged to develop (feature-b/develop -> CODE REVIEW -> develop), the developer of Feature A can get the Feature B changes by pulling like so:
```
feature-a/develop <- develop
feature-a/active <- feature-a/develop
```
#### Release Flow

The lead developer in-charge of creating a release build follows a release flow like so:
```
develop -> release-<major version>.<minor version>.<patch version>
```
In the release branch, the developer needs to perform the following tasks:
* Update the version number (use Semantic Versioning)
  * X.Y.Z where,
    * X increments with breaking / major changes
    * Y increments with new features
    * Z increments with bug fixes
* Update CHANGELOG.MD with the list of changes
  * Use proper Markdown, follow the existing format
* Commit the above changes in the release branch

Refer to the Deployment section of the document for concrete release steps to the Firebase or for internal testing.

If a release version needs fixes, do it on the release branch, then submit a pull request for merging to master. Merge it only if the issues are confirmed to be fixed in testing / Production.

### Pull Requests and Code Review

#### Developers - Do
* Create pull requests for branches to be merged to develop
* Create pull requests for changes in a release branch to be merged to master
* Ensure that you followed standards and clean code concepts
  * Don't duplicate code, etc.
* Explain any non-standard or inefficient code (technical debt) in your pull request
* Respect code reviewers' time by doing pull requests correctly

#### Developers - Don't
* Do not submit a pull request for code that was not tested
* Do not submit overly large pull requests with many files changed
  * If needed, submit a pull request of your feature branches to your feature/develop so you can have others review early

#### Reviewers - Do
* For code reviewers, checkout the branch for review and run the code
* Ensure that coding standards are followed
* Test for memory leaks, autolayout issues
* Find as many edge cases as possible to help the developer create a stable feature
* Give detailed critique

#### Reviewers - Don't
* Don't disregard timing (Sprint demo, releases, etc.)
  * Don't block pull requests if they work and are stable; there will be time to refactor later
* Don't make feedback personal

# PROGRAMMING

* Team / Programmer Identity
* Project Config
  * File Structure
* Architecture
* Testing
  * Accessibility (Automated QA)
    * General Instructions
  * Unit Testing
* Libraries
  * Rxjava, RxAndroid and RxKotlin (Reactive)
  * Retrofit (Networking)
  * KotlinX (Json Serializer)
  * Epoxy (List Library)
  * DaggerAndroid  (Dependency Injection)
  * Firebase (Push Notifications)
  
## Team / Programmer Identity

The app is deployed to two different destinations, with different implications on how it is downloaded or managed.

### Google Play Store Team: 
#### UnionBank of the Philippines
* Used for deployments to the Google Play
* Testing via App Testing(Firebase Distribution Application) only

## Project Config

The project uses multiple *schemes* with their own individual properties files to allow developers to change settings between schemes.

All properties files can be found in the autobahn-android folder. For example, the QAT build to be deployed via Enterprise distribution contains the following:

client.properties:
```
hostDev = "http://10.19.23.170:8080/"
hostQat = "http://10.19.23.171:8080/"
hostQat2 = "http://10.19.20.81:8080/"
hostStaging = "https://demo-business.unionbankph.com/"
hostPreProduction = "http://10.51.1.116"
hostProduction = "https://business.unionbankph.com/"

clientIdDev = "82d078e8-a694-4169-b4dd-506ec082f15e"
clientIdQat = "622ab32f-53f2-4188-8a1b-8adf42563995"
clientIdQat2 = "622ab32f-53f2-4188-8a1b-8adf42563995"
clientIdStaging = "622ab32f-53f2-4188-8a1b-8adf42563995"
clientIdPreProduction = "622ab32f-53f2-4188-8a1b-8adf42563995"
clientIdProduction = "622ab32f-53f2-4188-8a1b-8adf42563995"

clientSecretDev = "EPeARkGqFvxqtFGcEXHPgq9GzrFhatbXRKt7gWEfbgyMdFWKwR8T8kmWF8cw"
clientSecretQat = "vqYd6wc4WbQQhLMmxcVTx9YZHK2ezWsUgkXbCDEycx92usx7VD9aKwXfxCAv"
clientSecretQat2 = "vqYd6wc4WbQQhLMmxcVTx9YZHK2ezWsUgkXbCDEycx92usx7VD9aKwXfxCAv"
clientSecretStaging = "vqYd6wc4WbQQhLMmxcVTx9YZHK2ezWsUgkXbCDEycx92usx7VD9aKwXfxCAv"
clientSecretPreProduction = "vqYd6wc4WbQQhLMmxcVTx9YZHK2ezWsUgkXbCDEycx92usx7VD9aKwXfxCAv"
clientSecretProduction = "vqYd6wc4WbQQhLMmxcVTx9YZHK2ezWsUgkXbCDEycx92usx7VD9aKwXfxCAv"

clientApiVersion= "v3"
msmeClientApiVersion= "v1"
```

version.properties:
```
devVersionMajor=1
devVersionMinor=33
devVersionPatch=12

qatVersionMajor=1
qatVersionMinor=33
qatVersionPatch=12

qat2VersionMajor=1
qat2VersionMinor=1
qat2VersionPatch=4

stagingVersionMajor=1
stagingVersionMinor=1
stagingVersionPatch=4

preProductionVersionMajor=1
preProductionVersionMinor=1
preProductionVersionPatch=4

productionVersionMajor=1
productionVersionMinor=1
productionVersionPatch=4

demoVersionMajor=1
demoVersionMinor=3
demoVersionPatch=0
```
All this configs manipulated by build.gradle(app).

also in the firebase config we have google-services.json file located in autobahn-android/app. In this file you can manipulate firebase services.

For the variant of apps we have multiple build variants:

Debug: In this build we can trace the logs by the every action in the app.

Release: Hide all the logs so this one is for production use.

List of build variant:
* dev
* qat
* qat2
* staging
* preproduction
* production

* BUILD_NAME
  * Shown in Login for DEBUG builds - appended to the version text
* DISPLAY_NAME
  * Application display name when installed
* CLIENT_ID / CLIENT_SECRET
  * Used to identify the app as a legitimate client
* API_VERSION
  * Determines which API version to use for a specific scheme
  * Do not manage API versions individually per endpoint; just use this
* HOST
  * Server URL

### File Structure

The package folder contains the following:
com.unionbankph.corporate.
* feature
  * File used to determine app feature
* feature/data
  * Contains more on related in data like user persistence, api calls, models, form and etc.
* feature/presentation
  * Contains UI components, View Model, Dependency Injection, Widgets, Customized Libraries, Services, Broadcast Receivers and etc.
* /res
  * Contains resources like drawables, images, strings, colors, themes, styles and dimensions.

## Architecture

We use Rx Java and the Model-View-ViewModel architecture to help make our classes testable.

Our ViewModels conform to ViewModel, which at base requires:
```
class YourViewModel: ViewModel() {
    private val _state = MutableLiveData<State>()
    val state: State<LoginState> get() = _state
    
    init {
        // initialize variables
    }
}
```

The idea here is to think of View Models like engines or computer chips, where different inputs go in and produce different outputs.

Here's a sample View Model for logging in:
```
class LoginViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val authGateway: AuthGateway,
    private val settingsGateway: SettingsGateway
) : BaseViewModel() {

     fun login(loginForm: LoginForm) {
        authGateway.login(loginForm)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _loginState.value = ShowLoginLoading }
            .doFinally { _loginState.value = ShowLoginDismissLoading }
            .subscribe(
                {
                    _loginState.value = ShowLoginSuccess(it)
                }, {
                    Timber.e(it, "Login Failed")
                    _loginState.value = ShowLoginError(it)
                })
            .addTo(disposables)
    }
    
}
```

## Testing

### Accessibility (Automated QA)

Our QA had ability to make a script for automation for android platform.
Make sure the UI had ids so that they can capture every single views of the screen and they can manipulate the action of the android app.

### Unit Testing

We've purposely not written Unit Tests for majority of our modules despite adopting an architecture that should make this easy. It's considered intentional technical debt for the moment.

Critical modules (particularly form validation) has had some Unit Tests written for them, and can be used as a basis for how to write Unit Tests.

## Libraries

General Information
* build.gradle: Manipulate libraries in dependencies section
* dependencies.gradle: Contains versioning of libraries

### Rx (Reactive)
```
rxJava2Version = "2.2.2"       // Base Rx library version
rxKotlinVersion = "2.3.0"      // Base Rx library version
rxAndroidVersion = "2.1.0"     // Base Rx library version
rxSharedPreferenceVersion = "2.0.0"
rxFingerprintVersion = "2.2.1"
rxRelayVersion = "2.1.0"
rxBinding2Version = "2.1.1"

implementation libs.rxRelay
implementation libs.rxBinding2
implementation libs.rxJava
implementation libs.rxJava2Adapter
implementation libs.rxAndroid
implementation libs.rxKotlin
implementation libs.rxSharedPreference
implementation libs.rxFingerprint
implementation libs.rxPermission
```

### Retrofit (Networking)
```
retrofit2Version = "2.4.0"     // Base retrofit networking library version
implementation libs.retrofit
```

### KotlinX (JSON Serializer)

### DaggerAndroid (Dependency Injection)
```
dagger2Version = "2.17"        // Base dagger library version
implementation libs.dagger
implementation libs.daggerSupport
kapt libs.daggerCompiler
kapt libs.daggerProcessor
```

### CI, Firebase
```
apply plugin: "io.fabric"
apply plugin: "com.google.gms.google-services"

implementation libs.fireBaseMessaging
implementation libs.googlePlayCore
implementation(libs.fireBaseCrashlytics) { transitive = true }
```

### Epoxy (List Library)
```
epoxyVersion = "3.8.0"

implementation libs.epoxy
kapt libs.epoxyProcessor
```

### Others
```
/* View */
implementation support.appCompatAndroidX
implementation support.activityAndroidX
implementation support.fragmentAndroidX
implementation support.constraintlayout
implementation support.material
implementation support.recyclerView
implementation support.cardView

/* Various */
implementation libs.circleIndicator
implementation libs.zoomLayout
implementation libs.spotLight
implementation libs.securePreferences
implementation(libs.materialDialog) { transitive = true }
implementation libs.shimmer
implementation libs.epoxy
implementation libs.ahbottomnavigation
implementation libs.shortcutBadger
implementation libs.phoneNumber
implementation libs.countryCodePicker
implementation libs.inputMask
implementation libs.autofitTextView
implementation libs.materialDatePicker
implementation libs.fab
implementation libs.spinKit
implementation libs.circularProgress
implementation libs.aeroGear
implementation libs.zxingCore
implementation libs.cameraView
implementation libs.compressor
implementation libs.uCrop
```
-----
# DEPLOYMENT

* Summary
* Versioning
* Source Control
* Deployment Targets
  * Firebase App Distribution
  * Google Play Store
* Internal Testing
  * Firebase / Crashlytics
* Post-Release

## Summary

When you're ready to submit a new version for testing,  retrieve the next release's version number by getting the number of new features and bug fixes.

By this time it's expected that all changes for this new version have been merged to origin/develop (make sure that it has been code reviewed, and is stable).

Go to the root autobahn-android folder and edit CHANGELOG.MD, and follow the existing Markdown format in writing new changelogs per version. You will be using this new version changelog when informing Autobahn Mobile stakeholders of the new changes.

## Versioning

We use [Semantic Versioning](https://semver.org/).

Essentially the format is XXX.YYY.ZZZ, where:

* X means MAJOR change
  * This refers to breaking/incompatible API changes, or for mobile, major UI/UX changes
* Y means MINOR change
  * Increment this for new features
* Z means PATCH change
  * Increment this for any bugfixes or minor changes

Whenever a number increases, the next numbers resets to zero. For example, in the case of 1.2.3:

* Incrementing MAJOR version to 2 will result in the version 2.0.0
* Incrementing MINOR version to 3 will result in the version 1.3.0
* Incrementing PATCH version to 4 will result in the version 1.2.4
* Incrementing MINOR and PATCH by 1 will result in the version 1.3.1

## Source Control

As noted in the Release Flow section above, in terms of Source Control, all releases branch out from origin/develop.

Steps:

* In origin/develop, branch out to release-<version>-<build number>
* Update CHANGELOG.MD
* Update Info.plist version and build number by tapping on Autobahn project file in XCode then heading into General
* Commit the changes with the message "Info.plist - Updated version to <version>"

## Deployment Targets

After the steps above, it's now time to consider where you want to deploy. We have a number of deployment targets:

For The Portal:
* dev
* qat
* qat2
* staging(internal demo)
* preproduction
* production

For SME Banking: 
* smedev
* smeqat
* smeqat2
* smestaging(internal demo)
* smepreproduction
* smeproduction

So in the based Application(named App) having a condition that can determine If It's The Portal or SME Banking. Let's see the condition below:

```
fun isTableView() = !BuildConfig.FLAVOR.contains("sme")
```


### Google Play Store

Only the PRODUCTION target goes to the Google Play Store.

In your release branch, perform the build signed APK or you can use fastlane. There's a keystore for uploading the app in Google Play Store you can get the keystore from Migs Fermin.

Android Studio will build a signed APK and it will make a app.bundle for you to upload in the Google Play Store.

### Firebase / Crashlytics

The Firebase Organization where builds are uploaded is called "Autobahn". [URL](https://console.firebase.google.com/u/0/project/autobahn-216703/overview)

You can get access from the following:

* Miguel Fermin (White Cloak CTO): miguel.fermin@whitecloak.com
* Herald Santos (Android Dev): herald.santos@whitecloak.com
* Jerome Fami (iOS Dev): jerome.fami@whitecloak.com

You can use Firebase to manage testers.

## Post-Release

After a release, clean up Source Control remotes by deleting unused branches.

Make a pull/merge request for your release branch to master also, then once the release is stable, merge it.

Then pull master to develop. This will allow the next Sprint to start once more from develop.
