#import <Cocoa/Cocoa.h>
#import <CoreLocation/CoreLocation.h>

NS_ASSUME_NONNULL_BEGIN

@interface MGLMapView : NSView

@property (nonatomic, null_resettable) NSURL *styleURL;
@property (nonatomic) CLLocationCoordinate2D centerCoordinate;
@property (nonatomic) double zoomLevel;
@property (nonatomic, readonly) double maximumZoomLevel;
@property (nonatomic, readonly) double minimumZoomLevel;
@property (nonatomic) CLLocationDirection direction;

- (instancetype)initWithFrame:(CGRect)frame styleURL:(nullable NSURL *)styleURL;

@end

NS_ASSUME_NONNULL_END
