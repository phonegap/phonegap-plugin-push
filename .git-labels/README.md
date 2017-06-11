# :flags: [`git-labelmaker`][github-repos-url]
> Edit GitHub labels from the command line using **[`git-labelmaker`][github-repos-url]**! You can easily add or remove GitHub labels, making it easier for your projects to adhere to a [sane labelling](https://medium.com/@dave_lunny/sane-github-labels-c5d2e6004b63) scheme.

 [![travis-build-badge](https://api.travis-ci.org/himynameisdave/git-labelmaker.svg?branch=master)](https://travis-ci.org/himynameisdave/git-labelmaker) [![Coverage Status](https://coveralls.io/repos/github/himynameisdave/git-labelmaker/badge.svg?branch=master)](https://coveralls.io/github/himynameisdave/git-labelmaker?branch=master) [![Join the chat at https://gitter.im/himynameisdave/git-labelmaker](https://badges.gitter.im/himynameisdave/git-labelmaker.svg)](https://gitter.im/himynameisdave/git-labelmaker?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
## 1. Table of contents
<!-- TOC depthFrom:2 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

- [1. Table of contents](#1-table-of-contents)
- [2. Install](#2-install)
- [3. Usage](#3-usage)
	- [3.1. Standard labels](#31-standard-labels)
		- [3.1.2. Issue Priority](#312-issue-priority)
		- [3.1.3. Issue Status](#313-issue-status)
		- [3.1.4. Issue Type](#314-issue-type)
	- [3.2. Token](#32-token)
	- [3.3. Add Custom Labels](#33-add-custom-labels)
	- [3.4. Add Labels From Package](#34-add-labels-from-package)
	- [3.5. Create Package From Labels](#35-create-package-from-labels)
	- [3.6. Remove Labels](#36-remove-labels)
- [4. Contributing](#4-contributing)

<!-- /TOC -->

## 2. Install

Install [`git-labelmaker`][github-repos-url] globally:

```bash

$ yarn global add git-labelmaker

```

OR

```bash

$ npm i -g git-labelmaker

```

Currently you **must be using version `>= 4.0.0` of node**, because we're using some fancy-shmancy ES6 stuff (Promises are too awesome to not use), and also because the dependant package `git-label` also currently requires `>= 4.0.0`.

Use **npm version `>= 3.9.0`**,

## 3. Usage

Using this bad boy is a breeze. First `cd` into your git repository, run the command and follow the prompts!

```bash

$ git-labelmaker


```

### 3.1. Standard labels

The `.git-labels` directory contains has a `common.json` file in it with the following Issue labels:

1. `CLA: Signed`
2. `CLA: Unsigned`

#### 3.1.2. Issue Priority

3. `Priority: Critical`
4. `Priority: High`
5. `Priority: Medium`
6. `Priority: Low`

#### 3.1.3. Issue Status

7. `Status: Abandoned`
8. `Status: Accepted`
9. `Status: Available`
10. `Status: Blocked`
11. `Status: Completed`
12. `Status: In Progress`
13. `Status: On Hold`
14. `Status: Pending`
15. `Status: Rejected`
16. `Status: Review Needed`
17. `Status: Revision Needed`


#### 3.1.4. Issue Type

18. `Type: Breaking Change`
18. `Type: Build`
19. `Type: Chore`
20. `Type: CI`
21. `Type: Defect`
22. `Type: Docs`
23. `Type: Duplicate`
24. `Type: Feature`
25. `Type: Feedback`
26. `Type: Fix`
27. `Type: Performance`
28. `Type: Question`
29. `Type: Refactor`
30. `Type: Revert`
31. `Type: Spike`
32. `Type: Style`
33. `Type: Test`

### 3.2. Token

![Preview of git-labelmaker](http://i.imgur.com/UYSjdNw.png)

To interact with the GitHub API, you will need your own access token, which you can [generate over here](https://github.com/settings/tokens). Make sure your token has `repo` permissions.

Instead of having to enter your token each time, [`git-labelmaker`][github-repos-url] will remember it and keep it secure for you while you instead only need to remember a password you create. You can make your password whatever you like - passwords are easier to remember than tokens!

Currently, [`git-labelmaker`][github-repos-url] only supports calling the public GitHub API. If you are using GitHub Enterprise and have a custom API path, you might want to check out [`git-label`](https://github.com/jasonbellamy/git-label) instead.

### 3.3. Add Custom Labels

You can add your own labels one at a time. You will be prompted for your new label's text and color. Include the `#` in front of your 3 or 6 digit hex color. Add as many as you like!

### 3.4. Add Labels From Package

If you have a labels package in your current directory that you would like to use for adding labels, just supply the path and name of that file. So like if it's at the root of the current directory, just `common.json`.

It must be a valid, parsable JSON file (although the extension doesn't matter). Check out [these really good ones](https://github.com/jasonbellamy/git-label-packages/tree/master/packages) if you need a template.

### 3.5. Create Package From Labels

Create a git label package from the current labels on a repo, so that you can easily use it again.

### 3.6. Remove Labels

You can also remove labels. Just select the ones you want to ditch and :boom: they're gone.

## 4. Contributing

Feel free to contribute to the project by opening a [Pull Request](https://github.com/himynameisdave/git-labelmaker/compare), filing a [new issue](https://github.com/himynameisdave/git-labelmaker/issues/new), or by barking at me on [the twitters](https://twitter.com/dave_lunny).

**Related Stuff:**
> - [`git-label`](https://github.com/jasonbellamy/git-label) by [**jasonbellamy**](https://github.com/jasonbellamy), which `git-labelmaker` uses to add and remove labels
> - [`git-label-packages`](https://github.com/jasonbellamy/git-label-packages) is a really good set of default packages if you really want to level up your projects
> - [`git-label-faces`](https://github.com/himynameisdave/git-label-faces) is a joke package set that you should totally never use for real
> - [Sane GitHub Labels](https://medium.com/@dave_lunny/sane-github-labels-c5d2e6004b63) - an article I wrote about the importance of a good, rational labelling system in your projects

---

*Created by [Dave Lunny](https://twitter.com/dave_lunny) in the glorious year of 2017.*
*Licensed under MIT :hand:*

[github-repos-url]: https://github.com/himynameisdave/git-labelmaker
