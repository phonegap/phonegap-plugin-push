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

    if (mediaUrl == nil) {
        [self contentComplete];
        return;
    }

    // load the attachment
    [self loadAttachmentForUrlString:mediaUrl
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

    if ([type isEqualToString:@"image/jpeg"]) {
        ext = @"jpg";
    }

    if ([type isEqualToString:@"image/gif"]) {
        ext = @"gif";
    }

    if ([type isEqualToString:@"image/png"]) {
        ext = @"png";
    }

    if ([type isEqualToString:@"video/mpeg"]) {
        ext = @"mpg";
    }

    if ([type isEqualToString:@"video/avi"]) {
        ext = @"avi";
    }

    if ([type isEqualToString:@"audio/aiff"]) {
        ext = @"aiff";
    }

    if ([type isEqualToString:@"audio/aiff"]) {
        ext = @"aiff";
    }

    if ([type isEqualToString:@"audio/wav"]) {
        ext = @"wav";
    }

    if ([type isEqualToString:@"audio/mpeg3"]) {
        ext = @"mp3";
    }

    return [@"." stringByAppendingString:ext];
}

- (void)loadAttachmentForUrlString:(NSString *)urlString completionHandler:(void(^)(UNNotificationAttachment *))completionHandler  {

    __block UNNotificationAttachment *attachment = nil;
    NSURL *attachmentURL = [NSURL URLWithString:urlString];
    //NSString *fileExt = [self fileExtensionForMediaType:type];

    NSURLSession *session = [NSURLSession sessionWithConfiguration:[NSURLSessionConfiguration defaultSessionConfiguration]];
    [[session downloadTaskWithURL:attachmentURL
                completionHandler:^(NSURL *temporaryFileLocation, NSURLResponse *response, NSError *error) {
                    if (error != nil) {
                        NSLog(@"%@", error.localizedDescription);
                    } else {
                        NSString *fileExt = [self fileExtensionForMediaType:[response MIMEType]];

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
