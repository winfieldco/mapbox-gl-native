#import <Cocoa/Cocoa.h>
#import <CoreLocation/CoreLocation.h>

NS_ASSUME_NONNULL_BEGIN

@interface MGLMapView : NSView

- (instancetype)initWithFrame:(CGRect)frame styleURL:(nullable NSURL *)styleURL;

@property (nonatomic, null_resettable) NSURL *styleURL;

@property (nonatomic) CLLocationCoordinate2D centerCoordinate;

@property (nonatomic) double zoomLevel;
@property (nonatomic, readonly) double maximumZoomLevel;
@property (nonatomic, readonly) double minimumZoomLevel;

- (void)setZoomLevel:(double)zoomLevel animated:(BOOL)animated;

@property (nonatomic) CLLocationDirection direction;

@property (nonatomic, getter=isScrollEnabled) BOOL scrollEnabled;
@property (nonatomic, getter=isZoomEnabled) BOOL zoomEnabled;

@property (nonatomic) BOOL showsTileEdges;
@property (nonatomic) BOOL showsCollisionBoxes;

@end

NS_ASSUME_NONNULL_END
