### For coder/developer:

We don't have a coding style, but try to keep it similar to existing code, with just a couple notes:

- Please write comments. No need to write full paragraphs for each line, but at least a minor comment for functions or non-obvious blocks of code really help.

- Try to make meaningful commits, when possible. On a PR we'll probably check everything together, but later it's nicer to have a detailed git log. For example: don't create a unique commit with everything, but also avoid very small ones. Usually a commit for each functionality/fix/improvement is enough.

You can check existing code to see examples, but be aware that some are not perfect. In doubt, just ask.


### For translator: 

You are free to propose one or more translations, or to update any existing one. Be sure of your work before providing it to the developers, who cannot verify it. There are two independent things to translate:

- **App strings**: the [`strings.xml`](./app/src/main/res/values/strings.xml) file
  - Translate all texts between the `<string>` and `</string>` tags.
  - Remember to insert a backslash `\` before any apostrophe `'`, or enclose all text in quotes `"`
  - Place the translated file in the corresponding `./app/src/main/res/values-{locale}/` folder

- **Store strings**: the [`title.txt`](./app/src/main/play/listings/en-US/title.txt) [`short-description.txt`](./app/src/main/play/listings/en-US/short-description.txt) and/or [`full-description.txt`](./app/src/main/play/listings/en-US/full-description.txt) files
  - Place the translated files in the corresponding `./app/src/main/play/listings/{locale}/` folder
  
Note: if you are unsure the locale prefix of a specific language you can find it [here](https://countrycode.org/).  
Optionally you can also send the files by email to [TrianguloY](https://github.com/TrianguloY) or by opening an [issue](https://github.com/TrianguloY/UrlChecker/issues/new) titled `{locale} Translation proposal`.
