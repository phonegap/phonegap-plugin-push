# Change Log

## 1.0.0

**Breaking:**

* breaking(android): bump fcm@18.+ [#19](https://github.com/havesource/cordova-plugin-push/pull/19)
* breaking(android): drop phonegap-plugin-multidex dependency [#21](https://github.com/havesource/cordova-plugin-push/pull/21)
* breaking(android): move clearAllNotifications to destroy from pause [#13](https://github.com/havesource/cordova-plugin-push/pull/13)

**Feature:**

* feat(android): notification data pass [#31](https://github.com/havesource/cordova-plugin-push/pull/31)
* feat(ios): support critical alert notifications [#12](https://github.com/havesource/cordova-plugin-push/pull/12)
* feat(ios): increase firebase framework to 6.32.2 [#42](https://github.com/havesource/cordova-plugin-push/pull/42)
* feat: remove cordova-support-google-services dependency [#8](https://github.com/havesource/cordova-plugin-push/pull/8)

**Fix:**

* fix(android): missing channel description crash [#53](https://github.com/havesource/cordova-plugin-push/pull/53)
* fix(android): Use app name for default channel [#49](https://github.com/havesource/cordova-plugin-push/pull/49)
* fix(android): enable lights when lightColor is set [#15](https://github.com/havesource/cordova-plugin-push/pull/15)
* fix(browser): corrected path to manifest file. [#32](https://github.com/havesource/cordova-plugin-push/pull/32)

**Chore:**

* chore(android): set requirement >= 8.0.0 [#52](https://github.com/havesource/cordova-plugin-push/pull/52)
* chore(android): cleanup & format [#26](https://github.com/havesource/cordova-plugin-push/pull/26)
* chore(android): bump com.android.support:support-v13:28.0.0 [#20](https://github.com/havesource/cordova-plugin-push/pull/20)
* chore(ios): use latest firebase library [#24](https://github.com/havesource/cordova-plugin-push/pull/24)
* chore(npm): rebuilt package-lock.json [#55](https://github.com/havesource/cordova-plugin-push/pull/55)
* chore(npm): properly configure for scope package [#33](https://github.com/havesource/cordova-plugin-push/pull/33)
* chore(type-definition): Update PushNotificationStatic [#14](https://github.com/havesource/cordova-plugin-push/pull/14)
* chore(github-pages): remove config [#4](https://github.com/havesource/cordova-plugin-push/pull/4)
* chore: update ticket management [#27](https://github.com/havesource/cordova-plugin-push/pull/27)
* chore: add missing build of push.js [#22](https://github.com/havesource/cordova-plugin-push/pull/22)
* chore: match plugin.xml version w/ package.json [#10](https://github.com/havesource/cordova-plugin-push/pull/10)
* chore: update xml namespace [#9](https://github.com/havesource/cordova-plugin-push/pull/9)
* chore: update version requirements [#7](https://github.com/havesource/cordova-plugin-push/pull/7)
* chore: update npm & git ignore list [#6](https://github.com/havesource/cordova-plugin-push/pull/6)
* chore: update plugin package [#1](https://github.com/havesource/cordova-plugin-push/pull/1)
* chore: remove unused dependencies [#2](https://github.com/havesource/cordova-plugin-push/pull/2)

**Refactor & Style:**

* refactor(eslint): update dependencies w/ fixes [#3](https://github.com/havesource/cordova-plugin-push/pull/3)
* style(md): format with md all in one (vscode) [#11](https://github.com/havesource/cordova-plugin-push/pull/11)

**CI & Docs:**

* ci(gh-actions): add workflow [#23](https://github.com/havesource/cordova-plugin-push/pull/23)
* ci: update travis configs [#5](https://github.com/havesource/cordova-plugin-push/pull/5)
* doc(android): enable & set notification light with lightColor [#54](https://github.com/havesource/cordova-plugin-push/pull/54)
* doc: cleanup docs [#51](https://github.com/havesource/cordova-plugin-push/pull/51)
* doc: update various markdown docs [#28](https://github.com/havesource/cordova-plugin-push/pull/28)

## Previous Change Log

Since this repo is a fork from the original Adobe PhoneGap push plugin, you can continue to read the previous changelog here:

https://github.com/havesource/cordova-plugin-push/blob/phonegap-2.3.0/CHANGELOG.md
