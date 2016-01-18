# Mapbox GL Native

[![Travis](https://api.travis-ci.org/mapbox/mapbox-gl-native.svg?branch=master)](https://travis-ci.org/mapbox/mapbox-gl-native/builds) [![Bitrise](https://www.bitrise.io/app/7514e4cf3da2cc57.svg?token=OwqZE5rSBR9MVWNr_lf4sA&branch=master)](https://www.bitrise.io/app/7514e4cf3da2cc57)

An OpenGL-based renderer for [Mapbox Vector Tiles](https://www.mapbox.com/blog/vector-tiles), consisting of a C++ library for OS X and Linux and SDK bindings for iOS, Android and Node.js.

**This repository is for development on Mapbox GL Native itself.**

If you want to use products _based on_ Mapbox GL, check out:

- [Mapbox Mobile](http://mapbox.com/mobile) — iOS and Android native development libraries
- [`mapbox-gl-native`](https://www.npmjs.com/package/mapbox-gl-native) - Node.js native module
- [`react-native-mapbox-gl`](https://github.com/mapbox/react-native-mapbox-gl) — React Native library
- [`qmapboxgl`](https://github.com/tmpsantos/qmapboxgl) — Qt library
- [`Mapbox Cordova Plugin`](http://plugins.telerik.com/cordova/plugin/mapbox) - Telerik's Apache Cordova plugin

--

## Targets

* Ubuntu Linux
* OS X 10.9+
* iOS 7.0+
  * iPhone 4S and above (5, 5c, 5s, 6, 6 Plus)
  * iPad 2 and above (3, 4, Mini, Air, Mini 2, Air 2)
  * iPod touch 5th gen and above
* Android 4.0.3+ (API level 15+)

See [`INSTALL.md`](./INSTALL.md) for development setup and install instructions, including dependencies.


# Parts integration

Instructions below outline how to install, edit source, and deploy the changes. This allows for an edited version of the mapbox gl.

## Version

- Pegged to ios-v3.0.1 (8fb141a)

## Installation

To replace the version of mapbox GL being used, first clone the repo:

    git clone https://github.com/winfieldco/mapbox-gl-native
    
In the repo dir start the project:

    make iproj
    
If the project already exists, you can open this xcode project instead to save time:

    mapbox-gl-native/build/ios-all/gyp
    
You may now change the source code and view your changes in the sample project. All testing and changes should be done within this sample project, as building for inclusion in other projects is extremely slow >5 mins per build.

## Deployment

When you are ready to deploy go to the repo directory and build the static libs, note this takes a while:

    make ipackage    
    
In your project's settings.py add the path to your checkout:

    MAPBOX_GL_NATIVE_DIR = '~/Work/sandbox/native/fork/mapbox-gl-native		
## Roadmap

Make it so it can build directly from source to speed up seeing changes. This will be crucial later in development.

## Troubleshooting

### Make fails on missing source files

Run make dist-clean to clear out the cache of build files.

### Linking issues

Do not try to link in the framework version, it has this wierd podfile error... target has transitive dependencies that include static binaries, and will never build. Even if you run directly from cocoapods for 3.0.1, it's plain broken.

That is why the the podspec uses the vendored lib etc.
