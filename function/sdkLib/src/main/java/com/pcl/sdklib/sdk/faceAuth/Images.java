package com.pcl.sdklib.sdk.faceAuth;

import androidx.camera.core.ImageProxy;

import java.nio.ByteBuffer;

public class Images {
    public static ByteBuffer yuv420ToNv21(final ImageProxy image) {
        final int width = image.getWidth();
        final int height = image.getHeight();

        final ImageProxy.PlaneProxy yPlane = image.getPlanes()[0];
        final ImageProxy.PlaneProxy uPlane = image.getPlanes()[1];
        final ImageProxy.PlaneProxy vPlane = image.getPlanes()[2];

        final ByteBuffer yBuffer = yPlane.getBuffer();
        final ByteBuffer uBuffer = uPlane.getBuffer();
        final ByteBuffer vBuffer = vPlane.getBuffer();

        final int numPixels = (int) (width * height * 3f / 2f);
        final byte[] nv21 = new byte[numPixels];
        int idY = 0;
        int idUV = width * height;
        final int uvWidth = width / 2;
        final int uvHeight = height / 2;

        final int uvRowStride = uPlane.getRowStride();
        final int uvPixelStride = uPlane.getPixelStride();
        final int yRowStride = yPlane.getRowStride();
        final int yPixelStride = yPlane.getPixelStride();

        for (int y = 0; y < height; ++y) {
            int yOffset = y * yRowStride;
            int uvOffset = y * uvRowStride;

            for (int x = 0; x < width; ++x) {
                nv21[idY++] = yBuffer.get(yOffset + x * yPixelStride);

                if (y < uvHeight && x < uvWidth) {
                    int bufferIndex = uvOffset + (x * uvPixelStride);
                    nv21[idUV++] = vBuffer.get(bufferIndex);
                    nv21[idUV++] = uBuffer.get(bufferIndex);
                }
            }
        }

        return ByteBuffer.wrap(nv21);
    }
}
