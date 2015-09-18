#include <mbgl/renderer/painter.hpp>
#include <mbgl/platform/gl.hpp>
#include <mbgl/map/source.hpp>
#include <mbgl/geometry/sprite_atlas.hpp>
#include <mbgl/shader/pattern_shader.hpp>
#include <mbgl/shader/plain_shader.hpp>
#include <mbgl/util/clip_id.hpp>
#include <mbgl/gl/debugging.hpp>

#include <CoreGraphics/CoreGraphics.h>
#include <CoreFoundation/CoreFoundation.h>

using namespace mbgl;

void Painter::drawClippingMasks(const std::set<Source*>& sources) {
    MBGL_DEBUG_GROUP("clipping masks");
    
    glDisable(GL_DEPTH_TEST);
    //depthMask(false);
    glDepthRange(1.0f, 1.0f);
    glStencilMask(0xFF);
    
    bool ready = false;
    
    for (const auto& source : sources) {
        if (source->viewportTileParsed()) {
            ready = true;
        }
    }
    
    if (ready) {
        useProgram(plainShader->program);
        config.stencilTest = true;
        config.depthTest = true;
        config.depthMask = GL_FALSE;
        config.colorMask = { false, false, false, false };
        config.depthRange = { 1.0f, 1.0f };
        
        coveringPlainArray.bind(*plainShader, tileStencilBuffer, BUFFER_OFFSET_0);
    } else {
        if(gridTextureBinding == 0) {
            CFBundleRef mainBundleRef = CFBundleGetMainBundle();
            CFStringRef pictureStringRef = CFStringCreateWithCString ( kCFAllocatorDefault, "grid-64.png", kCFStringEncodingUTF8);
            CFURLRef urlRef = CFBundleCopyResourceURL(mainBundleRef, pictureStringRef, NULL, NULL);
            CFStringRef stringRef = CFURLCopyPath(urlRef);
            CFStringRef stringRef2 = CFURLCreateStringByReplacingPercentEscapes ( kCFAllocatorDefault, stringRef, CFSTR("") );
            CGDataProviderRef dataProvider = CGDataProviderCreateWithFilename(CFStringGetCStringPtr(stringRef2, kCFStringEncodingUTF8));
            CGImageRef gridCGImage = CGImageCreateWithPNGDataProvider(dataProvider, NULL, NULL, kCGRenderingIntentDefault);
            
            // retrieve pixels
            size_t width = CGImageGetWidth(gridCGImage);
            size_t height = CGImageGetHeight(gridCGImage);
            CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceRGB();
            std::string pixels(width * height * 4, '\0');
            size_t bytesPerPixel = 4;
            size_t bytesPerRow = bytesPerPixel * width;
            size_t bitsPerComponent = 8;
            char *pixelData = const_cast<char *>(pixels.data());
            CGContextRef context = CGBitmapContextCreate(pixelData, width, height, bitsPerComponent, bytesPerRow, colorSpace, kCGImageAlphaPremultipliedLast);
            CGContextDrawImage(context, CGRectMake(0, 0, width, height), gridCGImage);
            CGContextRelease(context);
            CGColorSpaceRelease(colorSpace);
            CFRelease(pictureStringRef);
            CFRelease(stringRef);
            CFRelease(stringRef2);
            CFRelease(urlRef);
            CFRelease(dataProvider);
            
            // add sprite
            /*gridImage = std::make_shared<mbgl::SpriteImage>(width,
                                                            height,
                                                            1.0f,
                                                            std::move(pixels));*/
            MBGL_CHECK_ERROR(glGenTextures(1, &gridTextureBinding));
            MBGL_CHECK_ERROR(glBindTexture(GL_TEXTURE_2D, gridTextureBinding));
#ifndef GL_ES_VERSION_2_0
            MBGL_CHECK_ERROR(glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0));
#endif
            // We are using clamp to edge here since OpenGL ES doesn't allow GL_REPEAT on NPOT textures.
            // We use those when the pixelRatio isn't a power of two, e.g. on iPhone 6 Plus.
            MBGL_CHECK_ERROR(glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE));
            MBGL_CHECK_ERROR(glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE));
            
            MBGL_CHECK_ERROR(glTexImage2D(
                                          GL_TEXTURE_2D, // GLenum target
                                          0, // GLint level
                                          GL_RGBA, // GLint internalformat
                                          (GLsizei)width, // GLsizei width
                                          (GLsizei)height, // GLsizei height
                                          0, // GLint border
                                          GL_RGBA, // GLenum format
                                          GL_UNSIGNED_BYTE, // GLenum type
                                          pixelData // const GLvoid * data
                                          ));

            
        }
        
        // sprite upload
        //_mbglMap->setSprite(cSymbolName, cSpriteImage);
        
        // No viewport tiles are parsed. Draw the grid pattern.
        //const std::shared_ptr<Sprite> &sprite = style.sprite;
        //if (cSpriteImage) {
        {
            //SpriteAtlasElement spriteAtlasElement = spriteAtlas->getImage("matte-256", FALSE);
            //Rect<uint16_t> imagePos = spriteAtlasElement.pos;
            
            /*std::array<float, 2> imageSize = {{
                (float)(imagePos.w * 0.5),
                (float)(imagePos.h * 0.5)
            }
            };*/
            
            useProgram(patternShader->program);

            /*patternShader->setPatternSize(imageSize);
            patternShader->setPatternTopLeft({{
                float(imagePos.x) / spriteAtlas.getWidth(),
                float(imagePos.y) / spriteAtlas.getHeight(),
            }});
            patternShader->setPatternBottomRight({{
                float(imagePos.x + imagePos.w) / spriteAtlas.getWidth(),
                float(imagePos.y + imagePos.h) / spriteAtlas.getHeight(),
            }});
            std::array<float, 4> color = {{ 1, 1, 1, 1 }};
            patternShader->setColor(color);*/
            patternShader->u_pattern_tl_a = {{0.0f, 0.0f}};
            patternShader->u_pattern_br_a = {{1.0f, 1.0f}};
            patternShader->u_opacity = 1.0f;
            
            MBGL_CHECK_ERROR(glBindTexture(GL_TEXTURE_2D, gridTextureBinding));
            
            coveringPatternArray.bind(*patternShader, tileStencilBuffer, BUFFER_OFFSET(0));
        }
    }
    
    for (const auto& source : sources) {
        //Source &source = *pair.second;
        if (source->enabled) {
            source->drawClippingMasks(*this);
        }
    }
    
    glEnable(GL_DEPTH_TEST);
    //depthMask(true);
    glStencilMask(0x0);
}

/*void Painter::drawClippingMasks(const std::set<Source*>& sources) {
    MBGL_DEBUG_GROUP("clipping masks");

    useProgram(plainShader->program);
    config.stencilTest = true;
    config.depthTest = true;
    config.depthMask = GL_FALSE;
    config.colorMask = { false, false, false, false };
    config.depthRange = { 1.0f, 1.0f };

    coveringPlainArray.bind(*plainShader, tileStencilBuffer, BUFFER_OFFSET_0);

    for (const auto& source : sources) {
        source->drawClippingMasks(*this);
    }

    config.depthTest = true;
    config.colorMask = { true, true, true, true };
    config.depthMask = GL_TRUE;
    config.stencilMask = 0x0;
}*/

void Painter::drawClippingMask(const mat4& matrix, const ClipID &clip) {
    plainShader->u_matrix = matrix;

    const GLint ref = (GLint)(clip.reference.to_ulong());
    const GLuint mask = (GLuint)(clip.mask.to_ulong());
    config.stencilFunc = { GL_ALWAYS, ref, mask };
    config.stencilMask = mask;
    MBGL_CHECK_ERROR(glDrawArrays(GL_TRIANGLES, 0, (GLsizei)tileStencilBuffer.index()));
}
