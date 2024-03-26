package cn.zj.netrequest.download;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.blankj.utilcode.util.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author: zhengjun
 * created: 2023/7/10
 * desc:
 */
/**
 * 文件工具类
 */
public class FileUtil {

    private final String SDCardRoot;


    /**
     * 专为Android4.4设计的从Uri获取文件绝对路径，以前的方法已不好使
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            //一些三方的文件浏览器会进入到这个方法中，例如ES
            //QQ文件管理器不在此列
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {// MediaStore (and general)
            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();


            if (isQQMediaDocument(uri)) {
                String path = uri.getPath();
                File fileDir = Environment.getExternalStorageDirectory();
                File file = new File(fileDir, path.substring("/QQBrowser".length(), path.length()));
                return file.exists() ? file.toString() : null;
            }

            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {// File

            return uri.getPath();
        }
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = MediaStore.MediaColumns.DATA;
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


    /**
     * 使用第三方qq文件管理器打开
     *
     * @param uri
     * @return
     */
    public static boolean isQQMediaDocument(Uri uri) {
        return "com.tencent.mtt.fileprovider".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public FileUtil(Context context) {
        //得到当前外部存储设备的目录
        SDCardRoot = context.getExternalFilesDir(null).getAbsolutePath() + File.separator;
        //File.separator为文件分隔符”/“,方便之后在目录下创建文件
    }

    /**
     * 在SD卡上创建文件
     *
     *
     * @param fileName, dir
     * @return file
     */
    public File createFileInSDCard(String fileName, String dir) throws IOException {
        File file = new File(SDCardRoot + dir + File.separator + fileName);
        file.createNewFile();
        return file;
    }

    /**
     * 在SD卡上创建目录
     *
     *
     * @param dir
     * @return file
     */
    public void createSDDir(String dir) throws IOException {
        File dirFile = new File(SDCardRoot + dir);
        if (!dirFile.exists()) {
            if (dirFile.mkdirs()) {
                Log.d("SUCCESS", "The directory was created successfully");
            }
        } else {
            Log.d("SUCCESS", "Directory already exists");
        }
    }

    /**
     * 判断文件是否存在
     *
     *
     * @param fileName, dir
     * @return Does the file exist
     */
    public boolean isFileExist(String fileName, String dir) throws IOException {
        File file = new File(SDCardRoot + dir + File.separator + fileName);
        return file.exists();
    }

    /**
     * 将一个InoutStream里面的数据写入到SD卡中
     *
     *
     * @param fileName, dir， input
     * @return Write file
     */
    public File write2SDFromInput(String fileName, String dir, InputStream input) {
        File file = null;
        OutputStream output = null;
        try {
            //创建目录
            createSDDir(dir);
            //创建文件
            if (isFileExist(fileName,dir)){
                FileUtils.delete(new File(SDCardRoot + dir + File.separator + fileName));
            }
            file = createFileInSDCard(fileName, dir);
            //写数据流
            output = new FileOutputStream(file);
            byte[] buffer = new byte[4 * 1024];//每次存4K
            int temp;
            //写入数据
            while ((temp = input.read(buffer)) != -1) {
                output.write(buffer, 0, temp);
            }
            output.flush();
        } catch (Exception e) {
            System.out.println("写数据异常：" + e);
        } finally {
            try {
                output.close();
            } catch (Exception e2) {
                System.out.println(e2);
            }
        }
        return file;
    }

}