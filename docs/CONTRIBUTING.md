# Contribution Documentation for Developers & Translators

### Table of Contents
- [For coder/developer](#for-coderdeveloper)
- [For translator](#for-translator)
- [Installing UrlChecker in Android Studio](#installing-urlchecker-in-android-studio)
- [App Architecture](#app-architecture)

### For coder/developer:

We don't have a coding style, but try to keep it similar to existing code, with just a couple notes:

- Please write comments. No need to write full paragraphs for each line, but at least a minor comment for functions or non-obvious blocks of code really help.

- Try to make meaningful commits, when possible. On a PR we'll probably check everything together, but later it's nicer to have a detailed git log. For example: don't create a unique commit with everything, but also avoid very small ones. Usually a commit for each functionality/fix/improvement is enough.

You can check existing code to see examples, but be aware that some are not perfect. In doubt, just ask.

### For translator:

You are free to propose one or more translations, or to update any existing one. Be sure of your work before providing it to the developers, who cannot verify it. You can translate it using [Weblate](https://hosted.weblate.org/engage/urlcheck/), as a [pull request](https://github.com/TrianguloY/UrlChecker/pulls)/[issue](https://github.com/TrianguloY/UrlChecker/issues/new) titled `{locale} Translation proposal` or by sending the files by email to [TrianguloY](https://github.com/TrianguloY).

For manual translation, there are two independent things to translate:

- **App strings**: the [`strings.xml`](../app/src/main/res/values/strings.xml) file
  - Translate all texts between the `<string>` and `</string>` tags.
  - Remember to insert a backslash `\` before any apostrophe `'`, or enclose all text in quotes `"`
  - Place the translated file in the corresponding `./app/src/main/res/values-{locale}/` folder

- **Store strings**: the [`title.txt`](../app/src/main/play/listings/en-US/title.txt) [`short-description.txt`](../app/src/main/play/listings/en-US/short-description.txt) and/or [`full-description.txt`](../app/src/main/play/listings/en-US/full-description.txt) files
  - Place the translated files in the corresponding `./app/src/main/play/listings/{locale}/` folder

Note: if you are unsure the locale prefix of a specific language you can find it [here](https://countrycode.org/).



### Installing UrlChecker in Android Studio
This is a small guide for first-time contributors to be able to build and execute this application in Android Studio. 

1. Download Android Studio from [here](https://developer.android.com/studio).
2. Clone this github repository to your local workspace. The git command should look something like this: 
`git clone https://github.com/TrianguloY/UrlChecker.git`
3. Download a device emulator from the Device Manager, so you can see the application running.
4. Follow these commands from the Android Studio terminal to build the application: 

- `./gradlew clean` 
- `./gradlew build`
- `./gradlew installAlpha`
5. If everything went according to plan, you should be able to see the app installed in your emulator and be able to run it smoothly.

### App Architecture

UrlChecker is an Android Application that checks Url links. It is seperated into modules:
 
- Activities
- Dialogs
- Fragments
- Modules
- Services
- Url
- Utilities
- Views
    