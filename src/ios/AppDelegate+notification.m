//
//  AppDelegate+notification.m
//  pushtest
//
//  Created by Robert Easterday on 10/26/12.
//
//

#import "AppDelegate+notification.h"
#import "PushPlugin.h"
#import <objc/runtime.h>

static char launchNotificationKey;
static char coldstartKey;

@implementation AppDelegate (notification)

- (id) getCommandInstance:(NSString*)className
{
    return [self.viewController getCommandInstance:className];
}

// its dangerous to override a method from within a category.
// Instead we will use method swizzling. we set this up in the load call.
+ (void)load
{
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        Class class = [self class];

        SEL originalSelector = @selector(init);
        SEL swizzledSelector = @selector(pushPluginSwizzledInit);

        Method original = class_getInstanceMethod(class, originalSelector);
        Method swizzled = class_getInstanceMethod(class, swizzledSelector);

        BOOL didAddMethod =
        class_addMethod(class,
                        originalSelector,
                        method_getImplementation(swizzled),
                        method_getTypeEncoding(swizzled));

        if (didAddMethod) {
            class_replaceMethod(class,
                                swizzledSelector,
                                method_getImplementation(original),
                                method_getTypeEncoding(original));
        } else {
            method_exchangeImplementations(original, swizzled);
        }
    });
}

- (AppDelegate *)pushPluginSwizzledInit
{
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(createNotificationChecker:)
                                                 name:UIApplicationDidFinishLaunchingNotification
                                               object:nil];
    [[NSNotificationCenter defaultCenter]addObserver:self
                                            selector:@selector(pushPluginOnApplicationDidBecomeActive:)
                                                name:UIApplicationDidBecomeActiveNotification
                                              object:nil];

    // This actually calls the original init method over in AppDelegate. Equivilent to calling super
    // on an overrided method, this is not recursive, although it appears that way. neat huh?
    return [self pushPluginSwizzledInit];
}

// This code will be called immediately after application:didFinishLaunchingWithOptions:. We need
// to process notifications in cold-start situations
- (void)createNotificationChecker:(NSNotification *)notification
{
    NSLog(@"createNotificationChecker");
    if (notification)
    {
        NSDictionary *launchOptions = [notification userInfo];
        if (launchOptions) {
            NSLog(@"coldstart");
            self.launchNotification = [launchOptions objectForKey: @"UIApplicationLaunchOptionsRemoteNotificationKey"];
            self.coldstart = [NSNumber numberWithBool:YES];
        } else {
            NSLog(@"not coldstart");
            self.coldstart = [NSNumber numberWithBool:NO];
        }
    }
}

- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {
    PushPlugin *pushHandler = [self getCommandInstance:@"PushNotification"];
    [pushHandler didRegisterForRemoteNotificationsWithDeviceToken:deviceToken];
}

- (void)application:(UIApplication *)application didFailToRegisterForRemoteNotificationsWithError:(NSError *)error {
    PushPlugin *pushHandler = [self getCommandInstance:@"PushNotification"];
    [pushHandler didFailToRegisterForRemoteNotificationsWithError:error];
}

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler {
    NSLog(@"didReceiveNotification with fetchCompletionHandler");

    // app is in the foreground so call notification callback
    if (application.applicationState == UIApplicationStateActive) {
        NSLog(@"app active");
        PushPlugin *pushHandler = [self getCommandInstance:@"PushNotification"];
        pushHandler.notificationMessage = userInfo;
        pushHandler.isInline = YES;
        [pushHandler notificationReceived];

        completionHandler(UIBackgroundFetchResultNewData);
    }
    // app is in background or in stand by
    else {
        NSLog(@"app in-active");

        // do some convoluted logic to find out if this should be a silent push.
        long silent = 0;
        id aps = [userInfo objectForKey:@"aps"];
        id contentAvailable = [aps objectForKey:@"content-available"];
        if ([contentAvailable isKindOfClass:[NSString class]] && [contentAvailable isEqualToString:@"1"]) {
            silent = 1;
        } else if ([contentAvailable isKindOfClass:[NSNumber class]]) {
            silent = [contentAvailable integerValue];
        }

        if (silent == 1) {
            NSLog(@"this should be a silent push");
            void (^safeHandler)(UIBackgroundFetchResult) = ^(UIBackgroundFetchResult result){
                dispatch_async(dispatch_get_main_queue(), ^{
                    completionHandler(result);
                });
            };

            PushPlugin *pushHandler = [self getCommandInstance:@"PushNotification"];

            if (pushHandler.handlerObj == nil) {
                pushHandler.handlerObj = [NSMutableDictionary dictionaryWithCapacity:2];
            }

            id notId = [userInfo objectForKey:@"notId"];
            if (notId != nil) {
                NSLog(@"Push Plugin notId %@", notId);
                [pushHandler.handlerObj setObject:safeHandler forKey:notId];
            } else {
                NSLog(@"Push Plugin notId handler");
                [pushHandler.handlerObj setObject:safeHandler forKey:@"handler"];
            }

            pushHandler.notificationMessage = userInfo;
            pushHandler.isInline = NO;
            [pushHandler notificationReceived];
        } else {
            NSLog(@"just put it in the shade");
            //save it for later
            self.launchNotification = userInfo;

            completionHandler(UIBackgroundFetchResultNewData);
        }
    }
}

- (BOOL)userHasRemoteNotificationsEnabled {
    UIApplication *application = [UIApplication sharedApplication];
    if ([[UIApplication sharedApplication] respondsToSelector:@selector(registerUserNotificationSettings:)]) {
        return application.currentUserNotificationSettings.types != UIUserNotificationTypeNone;
    } else {
#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wdeprecated-declarations"
        return application.enabledRemoteNotificationTypes != UIRemoteNotificationTypeNone;
#pragma GCC diagnostic pop
    }
}

- (void)pushPluginOnApplicationDidBecomeActive:(NSNotification *)notification {

    NSLog(@"active");

    UIApplication *application = notification.object;

    PushPlugin *pushHandler = [self getCommandInstance:@"PushNotification"];
    if (pushHandler.clearBadge) {
        NSLog(@"PushPlugin clearing badge");
        //zero badge
        application.applicationIconBadgeNumber = 0;
    } else {
        NSLog(@"PushPlugin skip clear badge");
    }

    if (self.launchNotification) {
        pushHandler.isInline = NO;
        pushHandler.coldstart = [self.coldstart boolValue];
        pushHandler.notificationMessage = self.launchNotification;
        self.launchNotification = nil;
        self.coldstart = [NSNumber numberWithBool:NO];
        [pushHandler performSelectorOnMainThread:@selector(notificationReceived) withObject:pushHandler waitUntilDone:NO];
    }
}


- (void)application:(UIApplication *) application handleActionWithIdentifier: (NSString *) identifier
forRemoteNotification: (NSDictionary *) notification completionHandler: (void (^)()) completionHandler {

    NSLog(@"Push Plugin handleActionWithIdentifier %@", identifier);
    NSMutableDictionary *userInfo = [notification mutableCopy];
    [userInfo setObject:identifier forKey:@"actionCallback"];
    NSLog(@"Push Plugin userInfo %@", userInfo);

    if (application.applicationState == UIApplicationStateActive) {
        PushPlugin *pushHandler = [self getCommandInstance:@"PushNotification"];
        pushHandler.notificationMessage = userInfo;
        pushHandler.isInline = NO;
        [pushHandler notificationReceived];
    } else {
        void (^safeHandler)() = ^(void){
            dispatch_async(dispatch_get_main_queue(), ^{
                completionHandler();
            });
        };

        PushPlugin *pushHandler = [self getCommandInstance:@"PushNotification"];

        if (pushHandler.handlerObj == nil) {
            pushHandler.handlerObj = [NSMutableDictionary dictionaryWithCapacity:2];
        }

        id notId = [userInfo objectForKey:@"notId"];
        if (notId != nil) {
            NSLog(@"Push Plugin notId %@", notId);
            [pushHandler.handlerObj setObject:safeHandler forKey:notId];
        } else {
            NSLog(@"Push Plugin notId handler");
            [pushHandler.handlerObj setObject:safeHandler forKey:@"handler"];
        }

        pushHandler.notificationMessage = userInfo;
        pushHandler.isInline = NO;

        [pushHandler performSelectorOnMainThread:@selector(notificationReceived) withObject:pushHandler waitUntilDone:NO];
    }
}

// The accessors use an Associative Reference since you can't define a iVar in a category
// http://developer.apple.com/library/ios/#documentation/cocoa/conceptual/objectivec/Chapters/ocAssociativeReferences.html
- (NSMutableArray *)launchNotification
{
    return objc_getAssociatedObject(self, &launchNotificationKey);
}

- (void)setLaunchNotification:(NSDictionary *)aDictionary
{
    objc_setAssociatedObject(self, &launchNotificationKey, aDictionary, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

- (NSNumber *)coldstart
{
    return objc_getAssociatedObject(self, &coldstartKey);
}

- (void)setColdstart:(NSNumber *)aNumber
{
    objc_setAssociatedObject(self, &coldstartKey, aNumber, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

- (void)dealloc
{
    self.launchNotification = nil; // clear the association and release the object
    self.coldstart = nil;
}

@end
