//
//  MeiQia.m
//  UZApp
//
//  Created by Injoy on 16/1/6.
//  Copyright © 2016年 APICloud. All rights reserved.
//

#import "MeiQia.h"

@implementation MeiQia
{
    NSInteger receiveMessageCbId;
    
    NSString *loginCustomizedId;
    NSString *loginMQClientId;
    UIColor *setTitleBarColor;
    UIColor *titleColor;
}

- (id)initWithUZWebView:(UZWebView *)webView_ {
    if (self = [super initWithUZWebView:webView_]) {
        [theApp addAppHandle:self];
    }
    return self;
}

- (void)dispose {
    [theApp removeAppHandle:self];
}

- (void)applicationDidEnterBackground:(UIApplication *)application {
    [MQManager closeMeiqiaService];
}

- (void)applicationWillEnterForeground:(UIApplication *)application {
    [MQManager openMeiqiaService];
}

- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken
{
    [MQManager registerDeviceToken:deviceToken];
}


- (void)initMeiQia:(NSDictionary *)paramDict {
    NSInteger cbId = [paramDict integerValueForKey:@"cbId" defaultValue:-1];
    NSString *appkey = paramDict[@"appkey"];
    if (!appkey) {
        
    }
    
    //初始化SDK
    [MQManager initWithAppkey:appkey completion:^(NSString *clientId, NSError *error) {
        if (error) {
            [self sendResultEventWithCallbackId:cbId dataDict:nil errDict:@{@"info": error.domain} doDelete:YES];
            return;
        }
        //初始化成功，返回clientId
        [self sendResultEventWithCallbackId:cbId dataDict:@{@"clientId": clientId} errDict:nil doDelete:YES];
    }];
}

//设置用户信息
- (void)setClientInfo:(NSDictionary *)paramDict {
    NSDictionary *clientInfo = paramDict[@"clientInfo"];
    if (clientInfo && [clientInfo isKindOfClass:NSDictionary.class]) {
        [MQManager setClientInfo:clientInfo completion:^(BOOL success, NSError *error) {
            
        }];
    }
}

//指定分配客服
- (void)setScheduledAgentOrAgentGroup:(NSDictionary *)paramDict
{
    NSString *agentId = paramDict[@"agentId"];
    NSString *AgentGroup = paramDict[@"agentGroup"];
    NSString *scheduleRule = paramDict[@"scheduleRule"];
    
    //转接逻辑
    MQScheduleRules rules = MQScheduleRulesRedirectEnterprise;
    if (scheduleRule) {
        if ([scheduleRule isEqualToString:@"none"]) {
            rules = MQScheduleRulesRedirectNone;
        }else if ([scheduleRule isEqualToString:@"group"]) {
            rules = MQScheduleRulesRedirectGroup;
        }else if ([scheduleRule isEqualToString:@"enterprise"]) {
            rules = MQScheduleRulesRedirectEnterprise;
        }
    }
    
    //指定分配
    [MQManager setScheduledAgentWithAgentId:agentId agentGroupId:AgentGroup scheduleRule:rules];
}

- (void)setTitleColor:(NSDictionary *)paramDict
{
    NSString *colorStr = paramDict[@"color"];
    titleColor = [UZAppUtils colorFromNSString:colorStr];
}

- (void)setTitleBarColor:(NSDictionary *)paramDict
{
    NSString *colorStr = paramDict[@"color"];
    setTitleBarColor = [UZAppUtils colorFromNSString:colorStr];
}

- (void)show:(NSDictionary *)paramDict {
    //    NSInteger cbId = [paramDict integerValueForKey:@"cbId" defaultValue:-1];
    NSString *type = paramDict[@"type"];
    if (!type) type = @"";
    
    MQChatViewManager *chatViewManager = [MQChatViewManager new];
    [chatViewManager setNavigationBarColor:setTitleBarColor];
    [chatViewManager setNavigationBarTintColor:titleColor];
    [chatViewManager setLoginMQClientId:loginMQClientId];
    [chatViewManager setLoginCustomizedId:loginCustomizedId];
    
    if ([type.lowercaseString isEqualToString:@"present"]) {
        [chatViewManager presentMQChatViewControllerInViewController:self.viewController];
    }else{
        MQChatViewController *viewController = [chatViewManager pushMQChatViewControllerInViewController:self.viewController];
        [self.viewController.navigationController setNavigationBarHidden:NO];
        if (isIOS7) {
            //开启滑动返回
            viewController.navigationController.interactivePopGestureRecognizer.enabled = YES;
        }
    }
}

- (void)dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self name:MQAudioPlayerDidInterruptNotification object:nil];
}

@end