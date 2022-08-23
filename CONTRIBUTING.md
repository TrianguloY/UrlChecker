### For coder/developer:

I don't have a coding style, but try to keep it similar to existing code, with just a couple notes:

- Please write comments. No need to write full paragraphs for each line, but at least a minor comment for functions or non-obvious blocks of code really help.

- Try to make meaningful commits, when possible. On a PR I'll probably check everything together, but later it's nicer to have a detailed git log. For example: don't create a unique commit with everything, but also avoid very small ones. Usually a commit for each functionality/fix/improvement is enough.

You can check existing code to see examples, but be aware that some are not perfect. In doubt, just ask.


### For translator: 

you are free to propose one or more translation(s). Be sure of your work before providing it to the developer, who cannot verify it.

- It is necessary to translate the `strings.xml` file present in the `app/src/main/res/values/` directory

>- Translate all texts between the `<string>` and `</string>` tags.
>- Remember to insert a backslash `\` before any apostrophe `'` Or enclose all text in quotes `"`

Optionally you can:
- Translate the `full-description.txt` and `short-description.txt` files _(presentation for application stores)_
present in the directory `app/src/main/play/listings/en-US/`
