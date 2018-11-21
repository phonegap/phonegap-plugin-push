//
//  AppDelegate+notification.h
//  pushtest
//
//  Created by Robert Easterday on 10/26/12.
//
//

#import "AppDelegate.h"
@import UserNotifications;

extern NSString *const pushPluginApplicationDidBecomeActiveNotification;

@interface AppDelegate (notification) <UNUserNotificationCenterDelegate>
- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken;
- (void)application:(UIApplication *)application didFailToRegisterForRemoteNotificationsWithError:(NSError *)error;
- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo fetchCompletionHandler:( void (^)(UIBackgroundFetchResult))completionHandler;
- (void)pushPluginOnApplicationDidBecomeActive:(UIApplication *)application;
- (void)checkUserHasRemoteNotificationsEnabledWithCompletionHandler:(nonnull void (^)(BOOL))completionHandler;
- (id) getCommandInstance:(NSString*)className;

@property (nonatomic, retain) NSDictionary  *launchNotification;
@property (nonatomic, retain) NSNumber  *coldstart;

@end
