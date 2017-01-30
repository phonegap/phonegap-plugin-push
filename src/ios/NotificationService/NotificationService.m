#import "NotificationService.h"

@interface NotificationService ()

@property (nonatomic, strong) void (^contentHandler)(UNNotificationContent *contentToDeliver);
@property (nonatomic, strong) UNMutableNotificationContent *bestAttemptContent;

@end

@implementation NotificationService

- (void)didReceiveNotificationRequest:(UNNotificationRequest *)request withContentHandler:(void (^)(UNNotificationContent * _Nonnull))contentHandler {
    self.contentHandler = contentHandler;
    self.bestAttemptContent = [request.content mutableCopy];

    // Modify the notification content here...
    //self.bestAttemptContent.body = [NSString stringWithFormat:@"%@ [modified]", self.bestAttemptContent.body];

    // check for media attachment, example here uses custom payload keys mediaUrl and mediaType
    NSDictionary *userInfo = request.content.userInfo;
    if (userInfo == nil) {
        [self contentComplete];
        return;
    }

    NSString *mediaUrl = userInfo[@"mediaUrl"];
    NSString *mediaType = userInfo[@"mediaType"];

    if (mediaUrl == nil || mediaType == nil) {
        [self contentComplete];
        return;
    }

    // load the attachment
    [self loadAttachmentForUrlString:mediaUrl
                            withType:mediaType
                   completionHandler:^(UNNotificationAttachment *attachment) {
                       if (attachment) {
                           self.bestAttemptContent.attachments = [NSArray arrayWithObject:attachment];
                       }
                       [self contentComplete];
                   }];

}

- (void)serviceExtensionTimeWillExpire {
    // Called just before the extension will be terminated by the system.
    // Use this as an opportunity to deliver your "best attempt" at modified content, otherwise the original push payload will be used.
    [self contentComplete];
}

- (void)contentComplete {
    self.contentHandler(self.bestAttemptContent);
}

- (NSString *)fileExtensionForMediaType:(NSString *)type {
    NSString *ext = type;

    if ([type isEqualToString:@"image"]) {
        ext = @"jpg";
    }

    if ([type isEqualToString:@"video"]) {
        ext = @"mp4";
    }

    if ([type isEqualToString:@"audio"]) {
        ext = @"mp3";
    }

    return [@"." stringByAppendingString:ext];
}

- (void)loadAttachmentForUrlString:(NSString *)urlString withType:(NSString *)type completionHandler:(void(^)(UNNotificationAttachment *))completionHandler  {

    __block UNNotificationAttachment *attachment = nil;
    NSURL *attachmentURL = [NSURL URLWithString:urlString];
    NSString *fileExt = [self fileExtensionForMediaType:type];

    NSURLSession *session = [NSURLSession sessionWithConfiguration:[NSURLSessionConfiguration defaultSessionConfiguration]];
    [[session downloadTaskWithURL:attachmentURL
                completionHandler:^(NSURL *temporaryFileLocation, NSURLResponse *response, NSError *error) {
                    if (error != nil) {
                        NSLog(@"%@", error.localizedDescription);
                    } else {
                        NSFileManager *fileManager = [NSFileManager defaultManager];
                        NSURL *localURL = [NSURL fileURLWithPath:[temporaryFileLocation.path stringByAppendingString:fileExt]];
                        [fileManager moveItemAtURL:temporaryFileLocation toURL:localURL error:&error];

                        NSError *attachmentError = nil;
                        attachment = [UNNotificationAttachment attachmentWithIdentifier:@"" URL:localURL options:nil error:&attachmentError];
                        if (attachmentError) {
                            NSLog(@"%@", attachmentError.localizedDescription);
                        }
                    }
                    completionHandler(attachment);
                }] resume];
}
@end
