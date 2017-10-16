/*
 Copyright 2009-2011 Urban Airship Inc. All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.

 2. Redistributions in binaryform must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided withthe distribution.

 THIS SOFTWARE IS PROVIDED BY THE URBAN AIRSHIP INC``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 EVENT SHALL URBAN AIRSHIP INC OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

//  See GGLInstanceID.h
#define GMP_NO_MODULES true

#import "PushPlugin.h"
@import FirebaseInstanceID;
@import FirebaseMessaging;
@import FirebaseAnalytics;

@implementation PushPlugin : CDVPlugin

@synthesize notificationMessage;
@synthesize isInline;
@synthesize coldstart;

@synthesize callbackId;
@synthesize notificationCallbackId;
@synthesize callback;
@synthesize clearBadge;
@synthesize handlerObj;

@synthesize usesFCM;
@synthesize fcmSandbox;
@synthesize fcmSenderId;
@synthesize fcmRegistrationOptions;
@synthesize fcmRegistrationToken;
@synthesize fcmTopics;

-(void)initRegistration;
{
    NSString * registrationToken = [[FIRInstanceID instanceID] token];

    if (registrationToken != nil) {
        NSLog(@"FCM Registration Token: %@", registrationToken);
        [self setFcmRegistrationToken: registrationToken];

        id topics = [self fcmTopics];
        if (topics != nil) {
            for (NSString *topic in topics) {
                NSLog(@"subscribe to topic: %@", topic);
                id pubSub = [FIRMessaging messaging];
                [pubSub subscribeToTopic:topic];
            }
        }

        [self registerWithToken:registrationToken];
    } else {
        NSLog(@"FCM token is null");
    }

}

//  FCM refresh token
//  Unclear how this is testable under normal circumstances
- (void)onTokenRefresh {
#if !TARGET_IPHONE_SIMULATOR
    // A rotation of the registration tokens is happening, so the app needs to request a new token.
    NSLog(@"The FCM registration token needs to be changed.");
    [[FIRInstanceID instanceID] token];
    [self initRegistration];
#endif
}

// contains error info
- (void)sendDataMessageFailure:(NSNotification *)notification {
    NSLog(@"sendDataMessageFailure");
}
- (void)sendDataMessageSuccess:(NSNotification *)notification {
    NSLog(@"sendDataMessageSuccess");
}

- (void)didSendDataMessageWithID:messageID {
    NSLog(@"didSendDataMessageWithID");
}

- (void)willSendDataMessageWithID:messageID error:error {
    NSLog(@"willSendDataMessageWithID");
}

- (void)didDeleteMessagesOnServer {
    NSLog(@"didDeleteMessagesOnServer");
    // Some messages sent to this device were deleted on the GCM server before reception, likely
    // because the TTL expired. The client should notify the app server of this, so that the app
    // server can resend those messages.
}

- (void)unregister:(CDVInvokedUrlCommand*)command;
{
    NSArray* topics = [command argumentAtIndex:0];

    if (topics != nil) {
        id pubSub = [FIRMessaging messaging];
        for (NSString *topic in topics) {
            NSLog(@"unsubscribe from topic: %@", topic);
            [pubSub unsubscribeFromTopic:topic];
        }
    } else {
        [[UIApplication sharedApplication] unregisterForRemoteNotifications];
        [self successWithMessage:command.callbackId withMsg:@"unregistered"];
    }
}

- (void)subscribe:(CDVInvokedUrlCommand*)command;
{
    NSString* topic = [command argumentAtIndex:0];

    if (topic != nil) {
        NSLog(@"subscribe from topic: %@", topic);
        id pubSub = [FIRMessaging messaging];
        [pubSub subscribeToTopic:topic];
        NSLog(@"Successfully subscribe to topic %@", topic);
        [self successWithMessage:command.callbackId withMsg:[NSString stringWithFormat:@"Successfully subscribe to topic %@", topic]];
    } else {
        NSLog(@"There is no topic to subscribe");
        [self successWithMessage:command.callbackId withMsg:@"There is no topic to subscribe"];
    }
}

- (void)unsubscribe:(CDVInvokedUrlCommand*)command;
{
    NSString* topic = [command argumentAtIndex:0];

    if (topic != nil) {
        NSLog(@"unsubscribe from topic: %@", topic);
        id pubSub = [FIRMessaging messaging];
        [pubSub unsubscribeFromTopic:topic];
        NSLog(@"Successfully unsubscribe from topic %@", topic);
        [self successWithMessage:command.callbackId withMsg:[NSString stringWithFormat:@"Successfully unsubscribe from topic %@", topic]];
    } else {
        NSLog(@"There is no topic to unsubscribe");
        [self successWithMessage:command.callbackId withMsg:@"There is no topic to unsubscribe"];
    }
}

- (void)init:(CDVInvokedUrlCommand*)command;
{
    [[NSNotificationCenter defaultCenter]
     addObserver:self selector:@selector(onTokenRefresh)
     name:kFIRInstanceIDTokenRefreshNotification object:nil];

    [[NSNotificationCenter defaultCenter]
     addObserver:self selector:@selector(sendDataMessageFailure:)
     name:FIRMessagingSendErrorNotification object:nil];

    [[NSNotificationCenter defaultCenter]
     addObserver:self selector:@selector(sendDataMessageSuccess:)
     name:FIRMessagingSendSuccessNotification object:nil];

    [[NSNotificationCenter defaultCenter]
     addObserver:self selector:@selector(didDeleteMessagesOnServer)
     name:FIRMessagingMessagesDeletedNotification object:nil];

    [self.commandDelegate runInBackground:^ {

        NSLog(@"Push Plugin register called");
        self.callbackId = command.callbackId;

        NSMutableDictionary* options = [command.arguments objectAtIndex:0];
        NSMutableDictionary* iosOptions = [options objectForKey:@"ios"];

        NSArray* topics = [iosOptions objectForKey:@"topics"];
        [self setFcmTopics:topics];

        UIUserNotificationType UserNotificationTypes = UIUserNotificationTypeNone;

        id badgeArg = [iosOptions objectForKey:@"badge"];
        id soundArg = [iosOptions objectForKey:@"sound"];
        id alertArg = [iosOptions objectForKey:@"alert"];
        id clearBadgeArg = [iosOptions objectForKey:@"clearBadge"];

        if (([badgeArg isKindOfClass:[NSString class]] && [badgeArg isEqualToString:@"true"]) || [badgeArg boolValue])
        {
            UserNotificationTypes |= UIUserNotificationTypeBadge;
        }

        if (([soundArg isKindOfClass:[NSString class]] && [soundArg isEqualToString:@"true"]) || [soundArg boolValue])
        {
            UserNotificationTypes |= UIUserNotificationTypeSound;
        }

        if (([alertArg isKindOfClass:[NSString class]] && [alertArg isEqualToString:@"true"]) || [alertArg boolValue])
        {
            UserNotificationTypes |= UIUserNotificationTypeAlert;
        }

        UserNotificationTypes |= UIUserNotificationActivationModeBackground;

        if (clearBadgeArg == nil || ([clearBadgeArg isKindOfClass:[NSString class]] && [clearBadgeArg isEqualToString:@"false"]) || ![clearBadgeArg boolValue]) {
            NSLog(@"PushPlugin.register: setting badge to false");
            clearBadge = NO;
        } else {
            NSLog(@"PushPlugin.register: setting badge to true");
            clearBadge = YES;
            [[UIApplication sharedApplication] setApplicationIconBadgeNumber:0];
        }
        NSLog(@"PushPlugin.register: clear badge is set to %d", clearBadge);

        isInline = NO;

        NSLog(@"PushPlugin.register: better button setup");
        // setup action buttons
        NSMutableSet *categories = [[NSMutableSet alloc] init];
        id categoryOptions = [iosOptions objectForKey:@"categories"];
        if (categoryOptions != nil && [categoryOptions isKindOfClass:[NSDictionary class]]) {
            for (id key in categoryOptions) {
                NSLog(@"categories: key %@", key);
                id category = [categoryOptions objectForKey:key];

                id yesButton = [category objectForKey:@"yes"];
                UIMutableUserNotificationAction *yesAction;
                if (yesButton != nil && [yesButton  isKindOfClass:[NSDictionary class]]) {
                    yesAction = [self createAction: yesButton];
                }
                id noButton = [category objectForKey:@"no"];
                UIMutableUserNotificationAction *noAction;
                if (noButton != nil && [noButton  isKindOfClass:[NSDictionary class]]) {
                    noAction = [self createAction: noButton];
                }
                id maybeButton = [category objectForKey:@"maybe"];
                UIMutableUserNotificationAction *maybeAction;
                if (maybeButton != nil && [maybeButton  isKindOfClass:[NSDictionary class]]) {
                    maybeAction = [self createAction: maybeButton];
                }

                // First create the category
                UIMutableUserNotificationCategory *notificationCategory = [[UIMutableUserNotificationCategory alloc] init];

                // Identifier to include in your push payload and local notification
                notificationCategory.identifier = key;

                NSMutableArray *categoryArray = [[NSMutableArray alloc] init];
                NSMutableArray *minimalCategoryArray = [[NSMutableArray alloc] init];
                if (yesButton != nil) {
                    [categoryArray addObject:yesAction];
                    [minimalCategoryArray addObject:yesAction];
                }
                if (noButton != nil) {
                    [categoryArray addObject:noAction];
                    [minimalCategoryArray addObject:noAction];
                }
                if (maybeButton != nil) {
                    [categoryArray addObject:maybeAction];
                }

                // Add the actions to the category and set the action context
                [notificationCategory setActions:categoryArray forContext:UIUserNotificationActionContextDefault];

                // Set the actions to present in a minimal context
                [notificationCategory setActions:minimalCategoryArray forContext:UIUserNotificationActionContextMinimal];

                NSLog(@"Adding category %@", key);
                [categories addObject:notificationCategory];
            }

        }

        if ([[UIApplication sharedApplication]respondsToSelector:@selector(registerUserNotificationSettings:)]) {
            UIUserNotificationSettings *settings = [UIUserNotificationSettings settingsForTypes:UserNotificationTypes categories:categories];
            dispatch_async(dispatch_get_main_queue(), ^{
                [[UIApplication sharedApplication] registerUserNotificationSettings:settings];
                [[UIApplication sharedApplication] registerForRemoteNotifications];
            });
        }

        // Read GoogleService-Info.plist
        NSString *path = [[NSBundle mainBundle] pathForResource:@"GoogleService-Info" ofType:@"plist"];

        // Load the file content and read the data into arrays
        NSDictionary *dict = [[NSDictionary alloc] initWithContentsOfFile:path];
        fcmSenderId = [dict objectForKey:@"GCM_SENDER_ID"];
        BOOL isGcmEnabled = [[dict valueForKey:@"IS_GCM_ENABLED"] boolValue];

        NSLog(@"FCM Sender ID %@", fcmSenderId);

        //  GCM options
        [self setFcmSenderId: fcmSenderId];
        if(isGcmEnabled && [[self fcmSenderId] length] > 0) {
            NSLog(@"Using FCM Notification");
            [self setUsesFCM: YES];
            dispatch_async(dispatch_get_main_queue(), ^{
                if([FIRApp defaultApp] == nil)
                    [FIRApp configure];
                [self initRegistration];
            });
        } else {
            NSLog(@"Using APNS Notification");
            [self setUsesFCM:NO];
        }
        id fcmSandboxArg = [iosOptions objectForKey:@"fcmSandbox"];

        [self setFcmSandbox:@NO];
        if ([self usesFCM] &&
            (([fcmSandboxArg isKindOfClass:[NSString class]] && [fcmSandboxArg isEqualToString:@"true"]) ||
             [fcmSandboxArg boolValue]))
        {
            NSLog(@"Using FCM Sandbox");
            [self setFcmSandbox:@YES];
        }

        if (notificationMessage) {			// if there is a pending startup notification
            dispatch_async(dispatch_get_main_queue(), ^{
                // delay to allow JS event handlers to be setup
                [self performSelector:@selector(notificationReceived) withObject:nil afterDelay: 0.5];
            });
        }
    }];
}

- (UIMutableUserNotificationAction *)createAction:(NSDictionary *)dictionary {

    UIMutableUserNotificationAction *myAction = [[UIMutableUserNotificationAction alloc] init];

    myAction = [[UIMutableUserNotificationAction alloc] init];
    myAction.identifier = [dictionary objectForKey:@"callback"];
    myAction.title = [dictionary objectForKey:@"title"];
    id mode =[dictionary objectForKey:@"foreground"];
    if (mode == nil || ([mode isKindOfClass:[NSString class]] && [mode isEqualToString:@"false"]) || ![mode boolValue]) {
        myAction.activationMode = UIUserNotificationActivationModeBackground;
    } else {
        myAction.activationMode = UIUserNotificationActivationModeForeground;
    }
    id destructive = [dictionary objectForKey:@"destructive"];
    if (destructive == nil || ([destructive isKindOfClass:[NSString class]] && [destructive isEqualToString:@"false"]) || ![destructive boolValue]) {
        myAction.destructive = NO;
    } else {
        myAction.destructive = YES;
    }
    myAction.authenticationRequired = NO;

    return myAction;
}

- (void)didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {
    if (self.callbackId == nil) {
        NSLog(@"Unexpected call to didRegisterForRemoteNotificationsWithDeviceToken, ignoring: %@", deviceToken);
        return;
    }
    NSLog(@"Push Plugin register success: %@", deviceToken);

    NSMutableDictionary *results = [NSMutableDictionary dictionary];
    NSString *token = [[[[deviceToken description] stringByReplacingOccurrencesOfString:@"<"withString:@""]
                        stringByReplacingOccurrencesOfString:@">" withString:@""]
                       stringByReplacingOccurrencesOfString: @" " withString: @""];
    [results setValue:token forKey:@"deviceToken"];

#if !TARGET_IPHONE_SIMULATOR
    // Get Bundle Info for Remote Registration (handy if you have more than one app)
    [results setValue:[[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleDisplayName"] forKey:@"appName"];
    [results setValue:[[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleVersion"] forKey:@"appVersion"];

    // Check what Notifications the user has turned on.  We registered for all three, but they may have manually disabled some or all of them.

    NSUInteger rntypes = [[[UIApplication sharedApplication] currentUserNotificationSettings] types];

    // Set the defaults to disabled unless we find otherwise...
    NSString *pushBadge = @"disabled";
    NSString *pushAlert = @"disabled";
    NSString *pushSound = @"disabled";

    // Check what Registered Types are turned on. This is a bit tricky since if two are enabled, and one is off, it will return a number 2... not telling you which
    // one is actually disabled. So we are literally checking to see if rnTypes matches what is turned on, instead of by number. The "tricky" part is that the
    // single notification types will only match if they are the ONLY one enabled.  Likewise, when we are checking for a pair of notifications, it will only be
    // true if those two notifications are on.  This is why the code is written this way
    if(rntypes & UIUserNotificationTypeBadge){
        pushBadge = @"enabled";
    }
    if(rntypes & UIUserNotificationTypeAlert) {
        pushAlert = @"enabled";
    }
    if(rntypes & UIUserNotificationTypeSound) {
        pushSound = @"enabled";
    }

    [results setValue:pushBadge forKey:@"pushBadge"];
    [results setValue:pushAlert forKey:@"pushAlert"];
    [results setValue:pushSound forKey:@"pushSound"];

    // Get the users Device Model, Display Name, Token & Version Number
    UIDevice *dev = [UIDevice currentDevice];
    [results setValue:dev.name forKey:@"deviceName"];
    [results setValue:dev.model forKey:@"deviceModel"];
    [results setValue:dev.systemVersion forKey:@"deviceSystemVersion"];

    if(![self usesFCM]) {
        [self registerWithToken: token];
    }
#endif
}

- (void)didFailToRegisterForRemoteNotificationsWithError:(NSError *)error
{
    if (self.callbackId == nil) {
        NSLog(@"Unexpected call to didFailToRegisterForRemoteNotificationsWithError, ignoring: %@", error);
        return;
    }
    NSLog(@"Push Plugin register failed");
    [self failWithMessage:self.callbackId withMsg:@"" withError:error];
}

- (void)notificationReceived {
    NSLog(@"Notification received");

    if (notificationMessage && self.callbackId != nil)
    {
        NSMutableDictionary* message = [NSMutableDictionary dictionaryWithCapacity:4];
        NSMutableDictionary* additionalData = [NSMutableDictionary dictionaryWithCapacity:4];


        for (id key in notificationMessage) {
            if ([key isEqualToString:@"aps"]) {
                id aps = [notificationMessage objectForKey:@"aps"];

                for(id key in aps) {
                    NSLog(@"Push Plugin key: %@", key);
                    id value = [aps objectForKey:key];

                    if ([key isEqualToString:@"alert"]) {
                        if ([value isKindOfClass:[NSDictionary class]]) {
                            for (id messageKey in value) {
                                id messageValue = [value objectForKey:messageKey];
                                if ([messageKey isEqualToString:@"body"]) {
                                    [message setObject:messageValue forKey:@"message"];
                                } else if ([messageKey isEqualToString:@"title"]) {
                                    [message setObject:messageValue forKey:@"title"];
                                } else {
                                    [additionalData setObject:messageValue forKey:messageKey];
                                }
                            }
                        }
                        else {
                            [message setObject:value forKey:@"message"];
                        }
                    } else if ([key isEqualToString:@"title"]) {
                        [message setObject:value forKey:@"title"];
                    } else if ([key isEqualToString:@"badge"]) {
                        [message setObject:value forKey:@"count"];
                    } else if ([key isEqualToString:@"sound"]) {
                        [message setObject:value forKey:@"sound"];
                    } else if ([key isEqualToString:@"image"]) {
                        [message setObject:value forKey:@"image"];
                    } else {
                        [additionalData setObject:value forKey:key];
                    }
                }
            } else {
                [additionalData setObject:[notificationMessage objectForKey:key] forKey:key];
            }
        }

        if (isInline) {
            [additionalData setObject:[NSNumber numberWithBool:YES] forKey:@"foreground"];
        } else {
            [additionalData setObject:[NSNumber numberWithBool:NO] forKey:@"foreground"];
        }

        if (coldstart) {
            [additionalData setObject:[NSNumber numberWithBool:YES] forKey:@"coldstart"];
        } else {
            [additionalData setObject:[NSNumber numberWithBool:NO] forKey:@"coldstart"];
        }

        [message setObject:additionalData forKey:@"additionalData"];

        // send notification message
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:message];
        [pluginResult setKeepCallbackAsBool:YES];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];

        self.coldstart = NO;
        self.notificationMessage = nil;
    }
}

- (void)setApplicationIconBadgeNumber:(CDVInvokedUrlCommand *)command
{
    NSMutableDictionary* options = [command.arguments objectAtIndex:0];
    int badge = [[options objectForKey:@"badge"] intValue] ?: 0;

    [[UIApplication sharedApplication] setApplicationIconBadgeNumber:badge];

    NSString* message = [NSString stringWithFormat:@"app badge count set to %d", badge];
    CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:message];
    [self.commandDelegate sendPluginResult:commandResult callbackId:command.callbackId];
}

- (void)getApplicationIconBadgeNumber:(CDVInvokedUrlCommand *)command
{
    NSInteger badge = [UIApplication sharedApplication].applicationIconBadgeNumber;

    CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsInt:(int)badge];
    [self.commandDelegate sendPluginResult:commandResult callbackId:command.callbackId];
}

- (void)clearAllNotifications:(CDVInvokedUrlCommand *)command
{
    [[UIApplication sharedApplication] setApplicationIconBadgeNumber:0];

    NSString* message = [NSString stringWithFormat:@"cleared all notifications"];
    CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:message];
    [self.commandDelegate sendPluginResult:commandResult callbackId:command.callbackId];
}

- (void)hasPermission:(CDVInvokedUrlCommand *)command
{
    BOOL enabled = NO;
    id<UIApplicationDelegate> appDelegate = [UIApplication sharedApplication].delegate;
    if ([appDelegate respondsToSelector:@selector(userHasRemoteNotificationsEnabled)]) {
        enabled = [appDelegate performSelector:@selector(userHasRemoteNotificationsEnabled)];
    }

    NSMutableDictionary* message = [NSMutableDictionary dictionaryWithCapacity:1];
    [message setObject:[NSNumber numberWithBool:enabled] forKey:@"isEnabled"];
    CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:message];
    [self.commandDelegate sendPluginResult:commandResult callbackId:command.callbackId];
}

-(void)successWithMessage:(NSString *)myCallbackId withMsg:(NSString *)message
{
    if (myCallbackId != nil)
    {
        CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:message];
        [self.commandDelegate sendPluginResult:commandResult callbackId:myCallbackId];
    }
}

-(void)registerWithToken:(NSString*)token; {
    // Send result to trigger 'registration' event but keep callback
    NSMutableDictionary* message = [NSMutableDictionary dictionaryWithCapacity:2];
    [message setObject:token forKey:@"registrationId"];
    if ([self usesFCM]) {
      [message setObject:@"FCM" forKey:@"registrationType"];
    } else {
      [message setObject:@"APNS" forKey:@"registrationType"];
    }
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:message];
    [pluginResult setKeepCallbackAsBool:YES];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
}


-(void)failWithMessage:(NSString *)myCallbackId withMsg:(NSString *)message withError:(NSError *)error
{
    NSString        *errorMessage = (error) ? [NSString stringWithFormat:@"%@ - %@", message, [error localizedDescription]] : message;
    CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:errorMessage];

    [self.commandDelegate sendPluginResult:commandResult callbackId:myCallbackId];
}

-(void) finish:(CDVInvokedUrlCommand*)command
{
    NSLog(@"Push Plugin finish called");

    [self.commandDelegate runInBackground:^ {
        NSString* notId = [command.arguments objectAtIndex:0];

        dispatch_async(dispatch_get_main_queue(), ^{
            [NSTimer scheduledTimerWithTimeInterval:0.1
                                             target:self
                                           selector:@selector(stopBackgroundTask:)
                                           userInfo:notId
                                            repeats:NO];
        });

        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

-(void)stopBackgroundTask:(NSTimer*)timer
{
    UIApplication *app = [UIApplication sharedApplication];

    NSLog(@"Push Plugin stopBackgroundTask called");

    if (handlerObj) {
        NSLog(@"Push Plugin handlerObj");
        completionHandler = [handlerObj[[timer userInfo]] copy];
        if (completionHandler) {
            NSLog(@"Push Plugin: stopBackgroundTask (remaining t: %f)", app.backgroundTimeRemaining);
            completionHandler(UIBackgroundFetchResultNewData);
            completionHandler = nil;
        }
    }
}

@end
