#import "MGLMapView.h"
#import "MGLAccountManager_Private.h"
#import "MGLStyle.h"

#import <mbgl/mbgl.hpp>
#import <mbgl/map/camera.hpp>
#import <mbgl/platform/darwin/reachability.h>
#import <mbgl/platform/gl.hpp>
#import <mbgl/storage/default_file_source.hpp>
#import <mbgl/storage/network_status.hpp>
#import <mbgl/storage/sqlite_cache.hpp>
#import <mbgl/util/math.hpp>
#import <mbgl/util/constants.hpp>

#import "NSException+MGLAdditions.h"
#import "NSString+MGLAdditions.h"

class MBGLView;

const NSTimeInterval MGLAnimationDuration = 0.3;

std::chrono::steady_clock::duration MGLDurationInSeconds(float duration) {
    return std::chrono::duration_cast<std::chrono::steady_clock::duration>(std::chrono::duration<float, std::chrono::seconds::period>(duration));
}

mbgl::LatLng MGLLatLngFromLocationCoordinate2D(CLLocationCoordinate2D coordinate) {
    return mbgl::LatLng(coordinate.latitude, coordinate.longitude);
}

CLLocationCoordinate2D MGLLocationCoordinate2DFromLatLng(mbgl::LatLng latLng) {
    return CLLocationCoordinate2DMake(latLng.latitude, latLng.longitude);
}

@interface MGLOpenGLView : NSOpenGLView

@property (nonatomic, weak) MGLMapView *mapView;

- (instancetype)initWithFrame:(NSRect)frameRect mapView:(MGLMapView *)mapView NS_DESIGNATED_INITIALIZER;

@end

@interface MGLMapView ()

- (CVReturn)getFrameForTime:(const CVTimeStamp *)outputTime;

@end

@implementation MGLMapView {
    mbgl::Map *_mbglMap;
    MGLOpenGLView *_glView;
    MBGLView *_mbglView;
    std::shared_ptr<mbgl::SQLiteCache> _mbglFileCache;
    mbgl::DefaultFileSource *_mbglFileSource;
}

- (instancetype)initWithFrame:(NSRect)frameRect {
    if (self = [super initWithFrame:frameRect]) {
        [self commonInit];
        self.styleURL = nil;
    }
    return self;
}

- (instancetype)initWithFrame:(CGRect)frame styleURL:(nullable NSURL *)styleURL {
    if (self = [super initWithFrame:frame]) {
        [self commonInit];
        self.styleURL = styleURL;
    }
    return self;
}

- (instancetype)initWithCoder:(nonnull NSCoder *)decoder {
    if (self = [super initWithCoder:decoder]) {
        [self commonInit];
        self.styleURL = nil;
    }
    return self;
}

- (void)commonInit {
    _glView = [[MGLOpenGLView alloc] initWithFrame:self.bounds mapView:self];
    _glView.autoresizingMask = NSViewWidthSizable | NSViewHeightSizable;
    [self addSubview:_glView];
    
    _mbglView = new MBGLView(self, [NSScreen mainScreen].backingScaleFactor);
    
    NSString *fileCachePath = @"";
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES);
    if (paths.count != 0) {
        NSString *libraryDirectory = paths[0];
        fileCachePath = [libraryDirectory stringByAppendingPathComponent:@"cache.db"];
    }
    _mbglFileCache = mbgl::SharedSQLiteCache::get(fileCachePath.UTF8String);
    _mbglFileSource = new mbgl::DefaultFileSource(_mbglFileCache.get());
    
    _mbglMap = new mbgl::Map(*_mbglView, *_mbglFileSource, mbgl::MapMode::Continuous);
    
    // Observe for changes to the global access token (and find out the current one).
    [[MGLAccountManager sharedManager] addObserver:self
                                        forKeyPath:@"accessToken"
                                           options:(NSKeyValueObservingOptionInitial |
                                                    NSKeyValueObservingOptionNew)
                                           context:NULL];
    
    // Notify map object when network reachability status changes.
    MGLReachability *reachability = [MGLReachability reachabilityForInternetConnection];
    reachability.reachableBlock = ^(MGLReachability *) {
        mbgl::NetworkStatus::Reachable();
    };
    [reachability startNotifier];
    
    self.acceptsTouchEvents = YES;
    _scrollEnabled = YES;
    _zoomEnabled = YES;
    
    mbgl::CameraOptions options;
    options.center = mbgl::LatLng(0, 0);
    options.zoom = _mbglMap->getMinZoom();
    _mbglMap->jumpTo(options);
}

- (void)dealloc {
    [[MGLAccountManager sharedManager] removeObserver:self forKeyPath:@"accessToken"];
    
    if (_mbglMap) {
        delete _mbglMap;
        _mbglMap = nullptr;
    }
    if (_mbglFileSource) {
        delete _mbglFileSource;
        _mbglFileSource = nullptr;
    }
    if (_mbglView) {
        delete _mbglView;
        _mbglView = nullptr;
    }
    
    if ([[NSOpenGLContext currentContext] isEqual:_glView.openGLContext]) {
        [NSOpenGLContext clearCurrentContext];
    }
}

- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary *)change context:(__unused void *)context {
    // Synchronize mbgl::Map’s access token with the global one in MGLAccountManager.
    if ([keyPath isEqualToString:@"accessToken"] && object == [MGLAccountManager sharedManager]) {
        NSString *accessToken = change[NSKeyValueChangeNewKey];
        if (![accessToken isKindOfClass:[NSNull class]]) {
            _mbglFileSource->setAccessToken((std::string)[accessToken UTF8String]);
        }
    }
}

- (nonnull NSURL *)styleURL {
    NSString *styleURLString = @(_mbglMap->getStyleURL().c_str()).mgl_stringOrNilIfEmpty;
    NSAssert(styleURLString, @"Invalid style URL string %@", styleURLString);
    return styleURLString ? [NSURL URLWithString:styleURLString] : nil;
}

- (void)setStyleURL:(nullable NSURL *)styleURL {
    if (!styleURL) {
        styleURL = [MGLStyle streetsStyleURL];
    }
    
    if (![styleURL scheme]) {
        // Assume a relative path into the application’s resource folder.
        styleURL = [NSURL URLWithString:[@"asset://" stringByAppendingString:[styleURL absoluteString]]];
    }
    
    _mbglMap->setStyleURL([[styleURL absoluteString] UTF8String]);
}

- (void)setFrame:(NSRect)frame {
    super.frame = frame;
    _mbglMap->update(mbgl::Update::Dimensions);
}

- (CVReturn)getFrameForTime:(__unused const CVTimeStamp *)outputTime {
    CGFloat zoomFactor   = _mbglMap->getMaxZoom() - _mbglMap->getMinZoom() + 1;
    CGFloat cpuFactor    = (CGFloat)[NSProcessInfo processInfo].processorCount;
    CGFloat memoryFactor = (CGFloat)[NSProcessInfo processInfo].physicalMemory / 1000 / 1000 / 1000;
    CGFloat sizeFactor   = ((CGFloat)_mbglMap->getWidth()  / mbgl::util::tileSize) * ((CGFloat)_mbglMap->getHeight() / mbgl::util::tileSize);
    
    NSUInteger cacheSize = zoomFactor * cpuFactor * memoryFactor * sizeFactor * 0.5;
    
    _mbglMap->setSourceTileCacheSize(cacheSize);
    
    [self performSelectorOnMainThread:@selector(renderSync) withObject:nil waitUntilDone:YES];
    
    return kCVReturnSuccess;
}

- (void)renderSync {
    _mbglMap->renderSync();
    glFlush();
    
//    [self updateUserLocationAnnotationView];
}

- (void)invalidate {
    MGLAssertIsMainThread();
    
    _glView.needsDisplay = YES;
}

- (void)notifyMapChange:(mbgl::MapChange)change {
    // Ignore map updates when the Map object isn't set.
    if (!_mbglMap) {
        return;
    }
    
    switch (change) {
        case mbgl::MapChangeRegionIsChanging:
        case mbgl::MapChangeRegionDidChange:
        case mbgl::MapChangeRegionDidChangeAnimated:
        {
//            [self updateCompass];
            break;
        }
        case mbgl::MapChangeRegionWillChange:
        case mbgl::MapChangeRegionWillChangeAnimated:
        case mbgl::MapChangeWillStartLoadingMap:
        case mbgl::MapChangeDidFinishLoadingMap:
        case mbgl::MapChangeDidFailLoadingMap:
        case mbgl::MapChangeWillStartRenderingMap:
        case mbgl::MapChangeDidFinishRenderingMap:
        case mbgl::MapChangeDidFinishRenderingMapFullyRendered:
        case mbgl::MapChangeWillStartRenderingFrame:
        case mbgl::MapChangeDidFinishRenderingFrame:
        case mbgl::MapChangeDidFinishRenderingFrameFullyRendered:
        {
            break;
        }
    }
}

- (CLLocationCoordinate2D)centerCoordinate {
    return MGLLocationCoordinate2DFromLatLng(_mbglMap->getLatLng());
}

- (void)setCenterCoordinate:(CLLocationCoordinate2D)centerCoordinate {
    _mbglMap->setLatLng(MGLLatLngFromLocationCoordinate2D(centerCoordinate));
}

- (double)zoomLevel {
    return _mbglMap->getZoom();
}

- (void)setZoomLevel:(double)zoomLevel {
    [self setZoomLevel:zoomLevel animated:NO];
}

- (void)setZoomLevel:(double)zoomLevel animated:(BOOL)animated {
    _mbglMap->setZoom(zoomLevel, MGLDurationInSeconds(animated ? MGLAnimationDuration : 0));
}

- (double)maximumZoomLevel {
    return _mbglMap->getMaxZoom();
}

- (double)minimumZoomLevel {
    return _mbglMap->getMinZoom();
}

- (CLLocationDirection)direction {
    return mbgl::util::wrap(_mbglMap->getBearing(), 0., 360.);
}

- (void)setDirection:(CLLocationDirection)direction {
    _mbglMap->setBearing(direction);
}

- (BOOL)acceptsFirstResponder {
    return YES;
}

- (BOOL)acceptsTouchEvents {
    return YES;
}

- (void)mouseDragged:(NSEvent *)event {
    if (!self.scrollEnabled) {
        return;
    }
    
    [[NSCursor closedHandCursor] set];
    
    CGFloat x = event.deltaX;
    CGFloat y = event.deltaY;
    if (x || y) {
        _mbglMap->cancelTransitions();
        _mbglMap->moveBy({ x, y });
    }
}

- (void)mouseUp:(__unused NSEvent *)event {
    [[NSCursor arrowCursor] set];
    
    if (self.zoomEnabled && event.clickCount % 2 == 0) {
        CGPoint zoomInPoint = [self convertPoint:event.locationInWindow toView:nil];
        mbgl::PrecisionPoint center(zoomInPoint.x, self.bounds.size.height - zoomInPoint.y);
        _mbglMap->scaleBy(2, center, MGLDurationInSeconds(MGLAnimationDuration));
    }
}

- (BOOL)wantsScrollEventsForSwipeTrackingOnAxis:(__unused NSEventGestureAxis)axis {
    return YES;
}

- (void)scrollWheel:(NSEvent *)event {
    // https://developer.apple.com/library/mac/releasenotes/AppKit/RN-AppKitOlderNotes/#10_7Dragging
    if (!self.scrollEnabled || event.phase == NSEventPhaseNone) {
        return;
    }
    
    CGFloat x = event.scrollingDeltaX;
    CGFloat y = event.scrollingDeltaY;
    if (x || y) {
        _mbglMap->cancelTransitions();
        _mbglMap->moveBy({ x, y });
    }
}

- (BOOL)showsTileEdges {
    return _mbglMap->getDebug();
}

- (void)setShowsTileEdges:(BOOL)showsTileEdges {
    _mbglMap->setDebug(showsTileEdges);
}

- (BOOL)showsCollisionBoxes {
    return _mbglMap->getCollisionDebug();
}

- (void)setShowsCollisionBoxes:(BOOL)showsCollisionBoxes {
    _mbglMap->setCollisionDebug(showsCollisionBoxes);
}

class MBGLView : public mbgl::View {
public:
    MBGLView(MGLMapView *nativeView_, const float scaleFactor_)
        : nativeView(nativeView_), scaleFactor(scaleFactor_) {}
    virtual ~MBGLView() {}
    
    
    float getPixelRatio() const override {
        return scaleFactor;
    }
    
    std::array<uint16_t, 2> getSize() const override {
        return {{ static_cast<uint16_t>(nativeView.bounds.size.width),
            static_cast<uint16_t>(nativeView.bounds.size.height) }};
    }
    
    std::array<uint16_t, 2> getFramebufferSize() const override {
        NSRect bounds = [nativeView->_glView convertRectToBacking:nativeView->_glView.bounds];
        return {{ static_cast<uint16_t>(bounds.size.width),
            static_cast<uint16_t>(bounds.size.height) }};
    }
    
    void notify() override {}
    
    void notifyMapChange(mbgl::MapChange change) override {
        assert([[NSThread currentThread] isMainThread]);
        [nativeView notifyMapChange:change];
    }
    
    void activate() override {
        [nativeView->_glView.openGLContext makeCurrentContext];
    }
    
    void deactivate() override {
        [NSOpenGLContext clearCurrentContext];
    }
    
    void invalidate() override {
        [nativeView performSelectorOnMainThread:@selector(invalidate)
                                     withObject:nil
                                  waitUntilDone:NO];
    }
    
    void beforeRender() override {}
    
    void afterRender() override {}
    
private:
    __weak MGLMapView *nativeView = nullptr;
    const float scaleFactor;
};

@end

@implementation MGLOpenGLView {
    CVDisplayLinkRef _displayLink;
}

- (instancetype)initWithFrame:(NSRect)frameRect mapView:(MGLMapView *)mapView {
    NSOpenGLPixelFormatAttribute pfas[] = {
        NSOpenGLPFAAccelerated,
        NSOpenGLPFAClosestPolicy,
        NSOpenGLPFAAccumSize, 32,
        NSOpenGLPFAColorSize, 24,
        NSOpenGLPFAAlphaSize, 8,
        NSOpenGLPFADepthSize, 16,
        NSOpenGLPFAStencilSize, 8,
        NULL
    };
    NSOpenGLPixelFormat *format = [[NSOpenGLPixelFormat alloc] initWithAttributes:pfas];
    if (format && (self = [super initWithFrame:frameRect pixelFormat:format])) {
        _mapView = mapView;
        [self setWantsBestResolutionOpenGLSurface:YES];
//        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }
    return self;
}

- (void)dealloc {
    CVDisplayLinkRelease(_displayLink);
}

- (void)prepareOpenGL {
    [super prepareOpenGL];
    
    mbgl::gl::InitializeExtensions([](const char *name) {
        static CFBundleRef framework = CFBundleGetBundleWithIdentifier(CFSTR("com.apple.opengl"));
        if (!framework) {
            throw std::runtime_error("Failed to load OpenGL framework.");
        }
        
        CFStringRef str = CFStringCreateWithCString(kCFAllocatorDefault, name, kCFStringEncodingASCII);
        void *symbol = CFBundleGetFunctionPointerForName(framework, str);
        CFRelease(str);
        
        return reinterpret_cast<mbgl::gl::glProc>(symbol);
    });
    
    // https://developer.apple.com/library/mac/documentation/GraphicsImaging/Conceptual/OpenGL-MacProgGuide/opengl_designstrategies/opengl_designstrategies.html#//apple_ref/doc/uid/TP40001987-CH2-SW12
    GLint swapInt = 1;
    [self.openGLContext setValues:&swapInt forParameter:NSOpenGLCPSwapInterval];
    CVDisplayLinkCreateWithActiveCGDisplays(&_displayLink);
    CVDisplayLinkSetOutputCallback(_displayLink, &MGLDisplayLinkCallback, (__bridge void *)self);
    CGLContextObj cglContext = self.openGLContext.CGLContextObj;
    CGLPixelFormatObj cglPixelFormat = self.pixelFormat.CGLPixelFormatObj;
    CVDisplayLinkSetCurrentCGDisplayFromOpenGLContext(_displayLink, cglContext, cglPixelFormat);
    CVDisplayLinkStart(_displayLink);
}

static CVReturn MGLDisplayLinkCallback(__unused CVDisplayLinkRef displayLink, __unused const CVTimeStamp *now, const CVTimeStamp *outputTime, __unused CVOptionFlags flagsIn, __unused CVOptionFlags *flagsOut, void *displayLinkContext) {
    MGLOpenGLView *glView = (__bridge MGLOpenGLView *)displayLinkContext;
    return [glView.mapView getFrameForTime:outputTime];
}

@end
