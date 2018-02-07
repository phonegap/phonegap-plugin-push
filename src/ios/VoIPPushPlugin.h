#import <Cordova/CDV.h>
#import <PushKit/PushKit.h>

@interface VoIPPushPlugin : CDVPlugin <PKPushRegistryDelegate>

@property (nonatomic, copy) NSString *callbackId;
- (void)init:(CDVInvokedUrlCommand*)command;
- (void)pushRegistry:(PKPushRegistry *)registry didUpdatePushCredentials:(PKPushCredentials *)credentials forType:(NSString *)type;
- (void)pushRegistry:(PKPushRegistry *)registry didReceiveIncomingPushWithPayload:(PKPushPayload *)payload forType:(NSString *)type;

@end
