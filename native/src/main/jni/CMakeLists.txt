cmake_minimum_required(VERSION 3.22.1 FATAL_ERROR)
project(native_jni C CXX ASM)

if (${CMAKE_BUILD_TYPE} STREQUAL "Debug")
    set(LTO_LINK "")
    set(CMAKE_CXX_FLAGS "-std=c++2a -DANDROID -g -DDEBUG_PLATFORM")
    set(CMAKE_C_FLAGS "-w -DANDROID -D_LARGEFILE_SOURCE=1 -g -DDEBUG_PLATFORM")
    set(OPTIMIZE_NORMAL "-O0")
    set(OPTIMIZE_FAST "-O0")
    set(SYM_VISIBILITY "")
else ()
    #set(LTO_LINK "-flto=thin")
    set(LTO_LINK "")
    set(CMAKE_CXX_FLAGS "-std=c++2a -DANDROID -DPROJECT_NATIVE_LOG_DISABLED -ffunction-sections -fdata-sections -fmerge-all-constants ${LTO_LINK}")
    set(CMAKE_C_FLAGS "-w -DANDROID -DPROJECT_NATIVE_LOG_DISABLED -D_LARGEFILE_SOURCE=1 -ffunction-sections -fdata-sections -fmerge-all-constants ${LTO_LINK}")
    set(OPTIMIZE_NORMAL "-Os")
    set(OPTIMIZE_FAST "-O3")
    set(SYM_VISIBILITY "-fvisibility=hidden")
endif ()
set(CMAKE_ASM_FLAGS "${CFLAGS} -x assembler-with-cpp")
set(CMAKE_CXX_STANDARD 20)

add_library(thorvg STATIC
        animation/thorvg/src/renderer/sw_engine/tvgSwFill.cpp
        animation/thorvg/src/renderer/sw_engine/tvgSwImage.cpp
        animation/thorvg/src/renderer/sw_engine/tvgSwMath.cpp
        animation/thorvg/src/renderer/sw_engine/tvgSwMemPool.cpp
        animation/thorvg/src/renderer/sw_engine/tvgSwRaster.cpp
        animation/thorvg/src/renderer/sw_engine/tvgSwRenderer.cpp
        animation/thorvg/src/renderer/sw_engine/tvgSwRle.cpp
        animation/thorvg/src/renderer/sw_engine/tvgSwShape.cpp
        animation/thorvg/src/renderer/sw_engine/tvgSwStroke.cpp
        animation/thorvg/src/renderer/tvgAccessor.cpp
        animation/thorvg/src/renderer/tvgAnimation.cpp
        animation/thorvg/src/renderer/tvgCanvas.cpp
        animation/thorvg/src/renderer/tvgFill.cpp
        animation/thorvg/src/renderer/tvgGlCanvas.cpp
        animation/thorvg/src/renderer/tvgInitializer.cpp
        animation/thorvg/src/renderer/tvgLoader.cpp
        animation/thorvg/src/renderer/tvgPaint.cpp
        animation/thorvg/src/renderer/tvgPicture.cpp
        animation/thorvg/src/renderer/tvgRender.cpp
        animation/thorvg/src/renderer/tvgSaver.cpp
        animation/thorvg/src/renderer/tvgScene.cpp
        animation/thorvg/src/renderer/tvgShape.cpp
        animation/thorvg/src/renderer/tvgSwCanvas.cpp
        animation/thorvg/src/renderer/tvgTaskScheduler.cpp
        animation/thorvg/src/renderer/tvgText.cpp
        animation/thorvg/src/renderer/tvgWgCanvas.cpp
        animation/thorvg/src/loaders/raw/tvgRawLoader.cpp
        animation/thorvg/src/loaders/svg/tvgSvgCssStyle.cpp
        animation/thorvg/src/loaders/svg/tvgSvgLoader.cpp
        animation/thorvg/src/loaders/svg/tvgSvgPath.cpp
        animation/thorvg/src/loaders/svg/tvgSvgSceneBuilder.cpp
        animation/thorvg/src/loaders/svg/tvgSvgUtil.cpp
        animation/thorvg/src/loaders/svg/tvgXmlParser.cpp
        animation/thorvg/src/common/tvgLines.cpp
        animation/thorvg/src/common/tvgCompressor.cpp
        animation/thorvg/src/common/tvgMath.cpp
        animation/thorvg/src/common/tvgStr.cpp)

target_compile_options(thorvg PRIVATE
        -DTVG_STATIC=1 -fno-exceptions -ffast-math ${OPTIMIZE_NORMAL} -fno-rtti -fno-unwind-tables -fno-asynchronous-unwind-tables -Woverloaded-virtual -Wno-unused-parameter ${SYM_VISIBILITY})
target_include_directories(thorvg PRIVATE
        animation/thorvg/inc
        animation/thorvg/src/renderer
        animation/thorvg/src/renderer/sw_engine
        animation/thorvg/src/loaders/raw
        animation/thorvg/src/loaders/svg
        animation/thorvg/src/common)

add_library(rlottie STATIC
        animation/rlottie/src/lottie/lottieanimation.cpp
        animation/rlottie/src/lottie/lottieitem.cpp
        animation/rlottie/src/lottie/lottiekeypath.cpp
        animation/rlottie/src/lottie/lottieloader.cpp
        animation/rlottie/src/lottie/lottiemodel.cpp
        animation/rlottie/src/lottie/lottieparser.cpp
        animation/rlottie/src/lottie/lottieitem_capi.cpp
        animation/rlottie/src/vector/freetype/v_ft_math.cpp
        animation/rlottie/src/vector/freetype/v_ft_raster.cpp
        animation/rlottie/src/vector/freetype/v_ft_stroker.cpp
        animation/rlottie/src/vector/stb/stb_image.cpp
        animation/rlottie/src/vector/vbezier.cpp
        animation/rlottie/src/vector/vbitmap.cpp
        animation/rlottie/src/vector/vbrush.cpp
        animation/rlottie/src/vector/varenaalloc.cpp
        animation/rlottie/src/vector/vdasher.cpp
        animation/rlottie/src/vector/vdebug.cpp
        animation/rlottie/src/vector/vdrawable.cpp
        animation/rlottie/src/vector/vdrawhelper.cpp
        animation/rlottie/src/vector/vdrawhelper_sse2.cpp
        animation/rlottie/src/vector/vdrawhelper_neon.cpp
        animation/rlottie/src/vector/vdrawhelper_common.cpp
        animation/rlottie/src/vector/velapsedtimer.cpp
        animation/rlottie/src/vector/vimageloader.cpp
        animation/rlottie/src/vector/vinterpolator.cpp
        animation/rlottie/src/vector/vmatrix.cpp
        animation/rlottie/src/vector/vpainter.cpp
        animation/rlottie/src/vector/vpath.cpp
        animation/rlottie/src/vector/vpathmesure.cpp
        animation/rlottie/src/vector/vraster.cpp
        animation/rlottie/src/vector/vrect.cpp
        animation/rlottie/src/vector/vrle.cpp)
target_compile_options(rlottie PRIVATE
        -fno-exceptions -ffast-math ${OPTIMIZE_NORMAL} -fno-rtti -fno-unwind-tables -fno-asynchronous-unwind-tables -Woverloaded-virtual -Wno-unused-parameter ${SYM_VISIBILITY})
target_include_directories(rlottie PRIVATE
        animation/rlottie/inc
        animation/rlottie/src/vector
        animation/rlottie/src/vector/pixman
        animation/rlottie/src/vector/freetype
        animation/rlottie/src/vector/stb)

add_library(libyuv STATIC
        animation/libyuv/source/compare.cc
        animation/libyuv/source/compare_common.cc
        animation/libyuv/source/compare_gcc.cc
        animation/libyuv/source/compare_msa.cc
        animation/libyuv/source/compare_neon64.cc
        animation/libyuv/source/compare_neon.cc
        animation/libyuv/source/convert_argb.cc
        animation/libyuv/source/convert.cc
        animation/libyuv/source/convert_from_argb.cc
        animation/libyuv/source/convert_from.cc
        animation/libyuv/source/convert_jpeg.cc
        animation/libyuv/source/convert_to_argb.cc
        animation/libyuv/source/convert_to_i420.cc
        animation/libyuv/source/cpu_id.cc
        animation/libyuv/source/mjpeg_decoder.cc
        animation/libyuv/source/mjpeg_validate.cc
        animation/libyuv/source/planar_functions.cc
        animation/libyuv/source/rotate_any.cc
        animation/libyuv/source/rotate_argb.cc
        animation/libyuv/source/rotate.cc
        animation/libyuv/source/rotate_common.cc
        animation/libyuv/source/rotate_gcc.cc
        animation/libyuv/source/rotate_lsx.cc
        animation/libyuv/source/rotate_msa.cc
        animation/libyuv/source/rotate_neon64.cc
        animation/libyuv/source/rotate_neon.cc
        animation/libyuv/source/row_any.cc
        animation/libyuv/source/row_common.cc
        animation/libyuv/source/row_gcc.cc
        animation/libyuv/source/row_lasx.cc
        animation/libyuv/source/row_lsx.cc
        animation/libyuv/source/row_msa.cc
        animation/libyuv/source/row_neon64.cc
        animation/libyuv/source/row_neon.cc
        animation/libyuv/source/row_rvv.cc
        animation/libyuv/source/scale_any.cc
        animation/libyuv/source/scale_argb.cc
        animation/libyuv/source/scale.cc
        animation/libyuv/source/scale_common.cc
        animation/libyuv/source/scale_gcc.cc
        animation/libyuv/source/scale_lsx.cc
        animation/libyuv/source/scale_msa.cc
        animation/libyuv/source/scale_neon64.cc
        animation/libyuv/source/scale_neon.cc
        animation/libyuv/source/scale_rgb.cc
        animation/libyuv/source/scale_rvv.cc
        animation/libyuv/source/scale_uv.cc
        animation/libyuv/source/video_common.cc)
target_compile_options(libyuv PRIVATE
        -ffast-math ${OPTIMIZE_NORMAL} -funroll-loops -fno-strict-aliasing -fno-math-errno ${SYM_VISIBILITY})
target_include_directories(libyuv PRIVATE
        animation/libyuv/include)

add_library(common STATIC
        compress/lz4/lz4.c
        compress/zlib/adler32.c
        compress/zlib/compress.c
        compress/zlib/crc32.c
        compress/zlib/deflate.c
        compress/zlib/gzclose.c
        compress/zlib/gzlib.c
        compress/zlib/gzread.c
        compress/zlib/gzwrite.c
        compress/zlib/infback.c
        compress/zlib/inffast.c
        compress/zlib/inflate.c
        compress/zlib/inftrees.c
        compress/zlib/trees.c
        compress/zlib/uncompr.c
        compress/zlib/zutil.c
)

target_include_directories(common
        PRIVATE
        ./
        compress/lz4
        compress/zlib)

target_compile_options(common PRIVATE
        ${OPTIMIZE_FAST} -DHAVE_ZLIB=1 -DTAGLIB_STATIC=1 -funroll-loops -fno-strict-aliasing -fno-math-errno -Wno-unused-parameter -Wno-deprecated-declarations -Wno-unused-variable ${SYM_VISIBILITY})

add_library(native_jni
        SHARED
        jni_call.cpp
        animation/lottie_jni.cpp
        animation/thorvg_jni.cpp
        animation/image_processing_util_jni.cpp
        parcel_jni.cpp
        string_exist_jni.cpp)

target_include_directories(native_jni
        PRIVATE
        ./
        animation/rlottie/inc
        animation/thorvg/inc
        animation/libyuv/include
        compress/lz4
        compress/zlib)

target_compile_options(native_jni PRIVATE
        ${OPTIMIZE_NORMAL} -funroll-loops -fno-strict-aliasing -fno-math-errno -Wno-unused-parameter -Wno-deprecated-declarations -Wno-unused-variable)

target_link_libraries(native_jni
        PRIVATE android
        PRIVATE jnigraphics
        PRIVATE log
        PRIVATE common ${LTO_LINK}
        PRIVATE rlottie ${LTO_LINK}
        PRIVATE thorvg ${LTO_LINK}
        PRIVATE libyuv ${LTO_LINK})

target_link_options(native_jni PRIVATE "-Wl,-z,max-page-size=16384")
