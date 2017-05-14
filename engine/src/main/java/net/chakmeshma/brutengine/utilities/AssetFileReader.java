package net.chakmeshma.brutengine.utilities;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;


public final class AssetFileReader {
    public static String getAssetFileAsString(Context context, String fileName) throws IOException {
        InputStream inputStream;

        inputStream = context.getAssets().open(fileName);

        int fileLength = inputStream.available();

        byte[] bytes = new byte[fileLength];

        inputStream.read(bytes, 0, fileLength);

        String result = new String(bytes);

        return result;
    }
}
