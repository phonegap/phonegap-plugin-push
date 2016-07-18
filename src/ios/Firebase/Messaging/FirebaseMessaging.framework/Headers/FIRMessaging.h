#import <Foundation/Foundation.h>

/**
 *  The completion handler invoked once the data connection with FIRMessaging is
 *  established.  The data connection is used to send a continous stream of
 *  data and all the FIRMessaging data notifications arrive through this connection.
 *  Once the connection is established we invoke the callback with `nil` error.
 *  Correspondingly if we get an error while trying to establish a connection
 *  we invoke the handler with an appropriate error object and do an
 *  exponential backoff to try and connect again unless successful.
 *
 *  @param error The error object if any describing why the data connection
 *               to FIRMessaging failed.
 */
typedef void(^FIRMessagingConnectCompletion)(NSError * __nullable error);

/**
 *  Notification sent when the upstream message has been delivered
 *  successfully to the server. The notification object will be the messageID
 *  of the successfully delivered message.
 */
FOUNDATION_EXPORT NSString * __nonnull const FIRMessagingSendSuccessNotification;

/**
 *  Notification sent when the upstream message was failed to be sent to the
 *  server.  The notification object will be the messageID of the failed
 *  message. The userInfo dictionary will contain the relevant error
 *  information for the failure.
 */
FOUNDATION_EXPORT NSString * __nonnull const FIRMessagingSendErrorNotification;

/**
 *  Notification sent when the Firebase messaging server deletes pending
 *  messages due to exceeded storage limits. This may occur, for example, when
 *  the device cannot be reached for an extended period of time.
 *
 *  It is recommended to retrieve any missing messages directly from the
 *  server.
 */
FOUNDATION_EXPORT NSString * __nonnull const FIRMessagingMessagesDeletedNotification;

/**
 *  @enum FIRMessagingError
 */
typedef NS_ENUM(NSUInteger, FIRMessagingError) {
  // Unknown error.
  FIRMessagingErrorUnknown = 0,

  // Auth Error -- FIRMessaging couldn't validate request from this client.
  FIRMessagingErrorAuthentication = 1,

  // NoAccess -- InstanceID service cannot be accessed.
  FIRMessagingErrorNoAccess = 2,

  // Timeout -- Request to InstanceID backend timed out.
  FIRMessagingErrorTimeout = 3,

  // Network -- No network available to reach the servers.
  FIRMessagingErrorNetwork = 4,

  // OperationInProgress -- Another similar operation in progress,
  // bailing this one.
  FIRMessagingErrorOperationInProgress = 5,

  // InvalidRequest -- Some parameters of the request were invalid.
  FIRMessagingErrorInvalidRequest = 7,
};

/// Status for the downstream message received by the app.
typedef NS_ENUM(NSInteger, FIRMessagingMessageStatus) {
  FIRMessagingMessageStatusUnknown,
  /// New downstream message received by the app.
  FIRMessagingMessageStatusNew,
};

/// Information about a downstream message received by the app.
@interface FIRMessagingMessageInfo : NSObject

@property(nonatomic, readonly, assign) FIRMessagingMessageStatus status;

@end

/**
 *  Firebase Messaging enables apps to communicate with their app servers
 *  using simple messages.
 *
 *  To send or receive messages, the app must get a
 *  registration token from GGLInstanceID, which authorizes an
 *  app server to send messages to an app instance. Pass your sender ID and
 *  `kGGLInstanceIDScopeFIRMessaging` as parameters to the method.
 *
 *  A sender ID is a project number created when you configure your API project.
 *  It is labeled "Project Number" in the Google Developers Console.
 *
 *  In order to receive FIRMessaging messages, declare application:didReceiveRemoteNotification:
 *
 *  Client apps can send upstream messages back to the app server using the XMPP-based
 *  <a href="http://developers.google.com/cloud-messaging/ccs.html">Cloud Connection Server</a>,
 *
 */
@interface FIRMessaging : NSObject

/**
 *  FIRMessaging
 *
 *  @return An instance of FIRMessaging.
 */
+ (nonnull instancetype)messaging NS_SWIFT_NAME(messaging());

/**
 *  Unavailable. Use +messaging instead.
 */
- (nonnull instancetype)init __attribute__((unavailable("Use +messaging instead.")));

#pragma mark - Connect

/**
 *  Create a FIRMessaging data connection which will be used to send the data notifications
 *  send by your server. It will also be used to send ACKS and other messages based
 *  on the FIRMessaging ACKS and other messages based  on the FIRMessaging protocol.
 *
 *  Use the `disconnect` method to disconnect the connection.
 *
 *  @see FIRMessagingService disconnect
 *
 *  @param handler  The handler to be invoked once the connection is established.
 *                  If the connection fails we invoke the handler with an
 *                  appropriate error code letting you know why it failed. At
 *                  the same time, FIRMessaging performs exponential backoff to retry
 *                  establishing a connection and invoke the handler when successful.
 */
- (void)connectWithCompletion:(nonnull FIRMessagingConnectCompletion)handler;

/**
 *  Disconnect the current FIRMessaging data connection. This stops any attempts to
 *  connect to FIRMessaging. Calling this on an already disconnected client is a no-op.
 *
 *  Call this before `teardown` when your app is going to the background.
 *  Since the FIRMessaging connection won't be allowed to live when in background it is
 *  prudent to close the connection.
 */
- (void)disconnect;

#pragma mark - Topics

/**
 *  Asynchronously subscribes to a topic.
 *
 *  @param topic The name of the topic, for example @"sports".
 */
- (void)subscribeToTopic:(nonnull NSString *)topic;

/**
 *  Asynchronously unsubscribe to a topic.
 *
 *  @param topic The name of the topic, for example @"sports".
 */
- (void)unsubscribeFromTopic:(nonnull NSString *)topic;

#pragma mark - Upstream

/**
 *  Sends an upstream ("device to cloud") message.
 *
 *  The message will be queued if we don't have an active connection.
 *  You can only use the upstream feature if your GCM implementation
 *  uses the XMPP-based Cloud Connection Server.
 *
 *  @param message      Key/Value pairs to be sent. Values must be String, any
 *                      other type will be ignored.
 *  @param to           A string identifying the receiver of the message. For GCM
 *                      project IDs the value is `SENDER_ID@gcm.googleapis.com`.
 *  @param messageID    The ID of the message. This is generated by the application. It
 *                      must be unique for each message generated by this application.
 *                      It allows error callbacks and debugging, to uniquely identify
 *                      each message.
 *  @param ttl          The time to live for the message. In case we aren't able to
 *                      send the message before the TTL expires we will send you a
 *                      callback. If 0, we'll attempt to send immediately and return
 *                      an error if we're not connected.  Otherwise, the message will
 *                      be queued.  As for server-side messages, we don't return an error
 *                      if the message has been dropped because of TTL; this can happen
 *                      on the server side, and it would require extra communication.
 */
- (void)sendMessage:(nonnull NSDictionary *)message
                 to:(nonnull NSString *)receiver
      withMessageID:(nonnull NSString *)messageID
         timeToLive:(int64_t)ttl;

#pragma mark - Analytics

/**
 *  Call this when the app received a downstream message. Used to track message
 *  delivery and analytics for messages. You don't need to call this if you
 *  don't set the `FIRMessagingAutoSetupEnabled` flag in your Info.plist. In the
 *  latter case the library will call this implicitly to track relevant
 *  messages.
 *
 *  @param message The downstream message received by the application.
 *
 *  @return Information about the downstream message.
 */
- (nonnull FIRMessagingMessageInfo *)appDidReceiveMessage:(nonnull NSDictionary *)message;

@end
