//
//  AppDelegate.m
//  osxapp
//
//  Created by Minh Nguyen on 2015-11-21.
//  Copyright © 2015 Mapbox. All rights reserved.
//

#import "AppDelegate.h"

#import <mbgl/osx/MGLAccountManager.h>
#import <mbgl/osx/MGLMapView.h>
#import <mbgl/osx/MGLStyle.h>

@interface AppDelegate ()

@property (weak) IBOutlet NSWindow *window;
@property (strong) IBOutlet MGLMapView *mapView;

@end

@implementation AppDelegate

- (void)applicationDidFinishLaunching:(NSNotification *)aNotification {
    // Set access token, unless MGLAccountManager already read it in from Info.plist.
    if (![MGLAccountManager accessToken]) {
        NSString *accessToken = [[NSProcessInfo processInfo] environment][@"MAPBOX_ACCESS_TOKEN"];
        if (accessToken) {
            // Store to preferences so that we can launch the app later on without having to specify
            // token.
            [[NSUserDefaults standardUserDefaults] setObject:accessToken forKey:@"access_token"];
        } else {
            // Try to retrieve from preferences, maybe we've stored them there previously and can reuse
            // the token.
            accessToken = [[NSUserDefaults standardUserDefaults] objectForKey:@"access_token"];
        }
        if (!accessToken) {
            [NSAlert alertWithMessageText:@"Access token required" defaultButton:nil alternateButton:nil otherButton:nil informativeTextWithFormat:@"To load Mapbox-hosted tiles and styles, set the MAPBOX_ACCESS_TOKEN environment variable."];
        }
        
        [MGLAccountManager setAccessToken:accessToken];
    }
    
    self.mapView = [[MGLMapView alloc] initWithFrame:self.window.contentView.bounds];
    self.mapView.autoresizingMask = NSViewWidthSizable | NSViewHeightSizable;
    [self.window.contentView addSubview:self.mapView];
}

- (void)applicationWillTerminate:(NSNotification *)aNotification {
}

- (IBAction)setStyle:(id)sender {
    NSMenuItem *menuItem = sender;
    NSURL *styleURL;
    switch (menuItem.tag) {
        case 1:
            styleURL = [MGLStyle streetsStyleURL];
            break;
        case 2:
            styleURL = [MGLStyle emeraldStyleURL];
            break;
        case 3:
            styleURL = [MGLStyle lightStyleURL];
            break;
        case 4:
            styleURL = [MGLStyle darkStyleURL];
            break;
        case 5:
            styleURL = [MGLStyle satelliteStyleURL];
            break;
        case 6:
            styleURL = [MGLStyle hybridStyleURL];
            break;
        default:
            NSAssert(NO, @"Cannot set style from control with tag %li", (long)menuItem.tag);
            break;
    }
    self.mapView.styleURL = styleURL;
}

- (IBAction)zoomIn:(id)sender {
    self.mapView.zoomLevel++;
}

- (IBAction)zoomOut:(id)sender {
    self.mapView.zoomLevel--;
}

- (BOOL)validateMenuItem:(NSMenuItem *)menuItem {
    if (menuItem.action == @selector(setStyle:)) {
        NSURL *styleURL = self.mapView.styleURL;
        NSCellStateValue state;
        switch (menuItem.tag) {
            case 1:
                state = [styleURL isEqual:[MGLStyle streetsStyleURL]];
                break;
            case 2:
                state = [styleURL isEqual:[MGLStyle emeraldStyleURL]];
                break;
            case 3:
                state = [styleURL isEqual:[MGLStyle lightStyleURL]];
                break;
            case 4:
                state = [styleURL isEqual:[MGLStyle darkStyleURL]];
                break;
            case 5:
                state = [styleURL isEqual:[MGLStyle satelliteStyleURL]];
                break;
            case 6:
                state = [styleURL isEqual:[MGLStyle hybridStyleURL]];
                break;
            default:
                return NO;
        }
        menuItem.state = state;
        return YES;
    }
    if (menuItem.action == @selector(zoomIn:)) {
        return self.mapView.zoomLevel < self.mapView.maximumZoomLevel;
    }
    if (menuItem.action == @selector(zoomOut:)) {
        return self.mapView.zoomLevel > self.mapView.minimumZoomLevel;
    }
    return [super validateMenuItem:menuItem];
}

@end
