# Contributing

We love pull requests from everyone.

## How to Contribute

There are multiple ways to contribute:

* Submit Issue Tickets (Bugs Reports)
* Improve Documentation
* Contribute Code

## Setup for Contributing Codde 

Before we can start submitting code contributions, we setup locally a workspace where we can work on the code.

1. [Fork](https://help.github.com/articles/fork-a-repo/) this repo by following the GitHub's "[Fork a repo](https://help.github.com/articles/fork-a-repo/)" quickstart guide.
2. [Clone](https://help.github.com/articles/cloning-a-repository/) the newly forked repo locally.

    ```bash
    git clone git@github.com:your-username/phonegap-plugin-push.git
    ```

## Make First Codde Contribution

1. Create a new branch where you will make your feature or bugfix changes, with a link to the original repo:

    ```bash
    git checkout -b my-awesome-new-feature
    git push --set-upstream origin my-awesome-new-feature
    git remote add upstream https://github.com/havesource/cordova-plugin-push.git
    ```

2. Install project dependencies

    ```bash
    npm install
    ```

3. Make sure that the tests are pass before and after making any changes:

    ```bash
    npm test
    ```

4. After making changes and tests are passing, commit the changes:

    ```bash
    git commit -m "feat(android): my new cool feature"
    ```

    &#10071; Make sure to follow the [Commit Message and PR Title Semantics](#Commit-Message-and-PR-Title-Semantics)

5. Push your commits to your fork:

    ```bash
    git push origin
    ```

6. [Submit a pull request](https://help.github.com/articles/creating-a-pull-request/).

## Commit Message and PR Title Semantics

When writting a commit message or title to a PR, please make sure to follow the convention described below to help use understand what is being reviewed. The semantics will be validated automatticly by a semantic checker.

### `Types`

There are various `types` of commits that can be choosed from. The following `types`, described below, should be used when writting a commit message to help us understand the type of changes we are reviewing.

* `ci:` - When change are made to the CI configuration files.
* `chore:` - When changes do not modify source or test files. E.g. Wpdating a dependency that does not require code changes.
* `docs:` - When making documentation only changes.
* `feat:` - When adding a new features. E.g. adding a new parameter to perform a new task.
* `fix:` - When making a bug fix.
* `refactor:` - When making code change that does not fix or add new features.
* `perf:` - When making code changes to improve performance.
* `revert:` - When reverting a previous commit.
* `style:` - When making formatting changes that does not affect the code. E.g. commas, semi-colons, whitespaces, indentioned, etc.
* `test:` - Adding missing or correcting existing tests.


### `Scope`

`Scope` can also be applied to the commit messages to provide more insight to where the changes are made. For example: if a change is being made only to the `android` source code, you can use `android` as the scope.

Example: "**feat(android): added support for abc**"

## Update with Upstream

Periodically your cloned repo's branch and PR may become out-of-dated with upstream's master. You should make sure your branch is up-to-date with the original repo:

```bash
git fetch upstream
git merge upstream/master
```

## After Submitting a PR

At this point you're waiting on us. We do our best to keep on top of all the pull requests. We may suggest some changes, improvements or alternatives.

Some things that will increase the chance that your pull request is accepted:

* Write tests
* Write a good commit message
* Make sure the PR merges cleanly with the latest master.
* Describe your feature/bugfix and why it's needed/important in the pull request description.
* Link your PR with the associated issue ticket is present

## Editor Config

The project uses [.editorconfig](https://editorconfig.org/) to define the coding style of each file. We recommend that you install the Editor Config extension for your preferred IDE. Consistency is key.

## ESLint

The project uses [.eslint](https://eslint.org/) to define the JavaScript coding conventions. Most editors now have a ESLint add-on to provide on-save or on-edit linting.

We look forward to your contributions!
