package cn.happy.beautyface.ui.utils;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.LogUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import cn.happy.beautyface.bean.FilterItem;


/**
 * Created by softsugar on 16-11-16.
 */

public class FileUtils {



    public static boolean copyFileIfNeed(Context context, String fileName, String className) {
        String path = getFilePath(context, className + File.separator + fileName);
        if (path != null) {
            File file = new File(path);
            if (!file.exists()) {
                //如果模型文件不存在
                try {
                    if (file.exists())
                        file.delete();

                    file.createNewFile();

                    InputStream in = context.getAssets().open(className + File.separator + fileName);
                    if (in == null) {
                        LogUtils.e("copyMode", "the src is not existed");
                        return false;
                    }
                    OutputStream out = new FileOutputStream(file);
                    byte[] buffer = new byte[4096];
                    int n;
                    while ((n = in.read(buffer)) > 0) {
                        out.write(buffer, 0, n);
                    }
                    in.close();
                    out.close();
                } catch (IOException e) {
                    file.delete();
                    return false;
                }
            }
        }
        return true;
    }

    public static String getFilePath(Context context, String fileName) {
        String path = null;
        File dataDir = context.getApplicationContext().getExternalFilesDir(null);
        if (dataDir != null) {
            path = dataDir.getAbsolutePath() + File.separator + fileName;
        }
        return path;
    }

    public static File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "Camera");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                LogUtils.e("FileUtil", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINESE).format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".png");

        return mediaFile;
    }


    public static void copyStickerFiles(Context context, String index) {
        copyStickerZipFiles(context, index);
        copyStickerIconFiles(context, index);
    }

    public static void copyModelsFiles(Context context, String index) {
        copyStickerZipFiles(context, index);
    }


    public static List<String> copyStickerZipFiles(Context context, String className) {
        String files[] = null;
        ArrayList<String> modelFiles = new ArrayList<String>();

        try {
            files = context.getAssets().list(className);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String folderpath = null;
        File dataDir = context.getExternalFilesDir(null);
        if (dataDir != null) {
            folderpath = dataDir.getAbsolutePath() + File.separator + className;

            File folder = new File(folderpath);

            if (!folder.exists()) {
                folder.mkdir();
            }
        }
        for (int i = 0; i < files.length; i++) {
            String str = files[i];
            if (str.indexOf(".zip") != -1 || str.indexOf(".model") != -1) {
                copyFileIfNeed(context, str, className);
            }
        }

        File file = new File(folderpath);
        File[] subFile = file.listFiles();

        if (subFile == null || subFile.length == 0) {
            return modelFiles;
        }

        for (int i = 0; i < subFile.length; i++) {
            // 判断是否为文件夹
            if (!subFile[i].isDirectory()) {
                String filename = subFile[i].getAbsolutePath();
                String path = subFile[i].getPath();
                // 判断是否为model结尾
                if (filename.trim().toLowerCase().endsWith(".zip") || filename.trim().toLowerCase().endsWith(".model")) {
                    modelFiles.add(filename);
                }
            }
        }

        return modelFiles;
    }

    public static List<String> getStickerZipFilesFromSd(Context context, String className) {
        ArrayList<String> modelFiles = new ArrayList<String>();

        String folderpath = null;
        File dataDir = context.getExternalFilesDir(null);
        if (dataDir != null) {
            folderpath = dataDir.getAbsolutePath() + File.separator + className;

            File folder = new File(folderpath);

            if (!folder.exists()) {
                folder.mkdir();
            }
        }

        File file = new File(folderpath);
        File[] subFile = file.listFiles();

        if (subFile == null || subFile.length == 0) {
            return modelFiles;
        }

        for (int i = 0; i < subFile.length; i++) {
            // 判断是否为文件夹
            if (!subFile[i].isDirectory()) {
                String filename = subFile[i].getAbsolutePath();
                String path = subFile[i].getPath();
                // 判断是否为model结尾
                if (filename.trim().toLowerCase().endsWith(".zip") || filename.trim().toLowerCase().endsWith(".model")) {
                    modelFiles.add(filename);
                }
            }
        }

        return modelFiles;
    }

    public static Map<String, Bitmap> copyStickerIconFiles(Context context, String className) {
        String[] files = null;
        TreeMap<String, Bitmap> iconFiles = new TreeMap<String, Bitmap>();

        try {
            files = context.getAssets().list(className);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String folderpath = null;
        File dataDir = context.getExternalFilesDir(null);
        if (dataDir != null) {
            folderpath = dataDir.getAbsolutePath() + File.separator + className;

            File folder = new File(folderpath);

            if (!folder.exists()) {
                folder.mkdir();
            }
        }
        for (int i = 0; i < files.length; i++) {
            String str = files[i];
            if (str.indexOf(".png") != -1) {
                copyFileIfNeed(context, str, className);
            }
        }

        File file = new File(folderpath);
        File[] subFile = file.listFiles();

        if (subFile == null || subFile.length == 0) {
            return iconFiles;
        }

        for (File value : subFile) {
            // 判断是否为文件夹
            if (!value.isDirectory()) {
                String filename = value.getAbsolutePath();
                String path = value.getPath();
                // 判断是否为png结尾
                if (filename.trim().toLowerCase().endsWith(".png") && filename.indexOf("mode_") == -1) {
                    String name = value.getName();
                    iconFiles.put(getFileNameNoEx(name), BitmapFactory.decodeFile(filename));
                }
            }
        }

        return iconFiles;
    }


    public static Map<String, String> getStickerIconUrlFilesFromSd(Context context, String className) {
        TreeMap<String, String> iconFiles = new TreeMap<String, String>();

        String folderpath = null;
        File dataDir = context.getExternalFilesDir(null);
        if (dataDir != null) {
            folderpath = dataDir.getAbsolutePath() + File.separator + className;

            File folder = new File(folderpath);

            if (!folder.exists()) {
                folder.mkdir();
            }
        }

        File file = new File(folderpath);
        File[] subFile = file.listFiles();

        if (subFile == null || subFile.length == 0) {
            return iconFiles;
        }

        for (int i = 0; i < subFile.length; i++) {
            // 判断是否为文件夹
            if (!subFile[i].isDirectory()) {
                String filename = subFile[i].getAbsolutePath();
                String path = subFile[i].getPath();
                // 判断是否为png结尾
                if (filename.trim().toLowerCase().endsWith(".png") && filename.indexOf("mode_") == -1) {
                    String name = subFile[i].getName();
                    iconFiles.put(getFileNameNoEx(name), filename);
                }
            }
        }

        return iconFiles;
    }

    public static List<String> getStickerNames(Context context, String className) {
        ArrayList<String> modelNames = new ArrayList<String>();
        String folderpath = null;
        File dataDir = context.getExternalFilesDir(null);
        if (dataDir != null) {
            folderpath = dataDir.getAbsolutePath() + File.separator + className;
            File folder = new File(folderpath);

            if (!folder.exists()) {
                folder.mkdir();
            }
        }

        File file = new File(folderpath);
        File[] subFile = file.listFiles();

        if (subFile == null || subFile.length == 0) {
            return modelNames;
        }

        for (int i = 0; i < subFile.length; i++) {
            // 判断是否为文件夹
            if (!subFile[i].isDirectory()) {
                String filename = subFile[i].getAbsolutePath();
                // 判断是否为model结尾
                if (filename.trim().toLowerCase().endsWith(".zip") || filename.trim().toLowerCase().endsWith(".model")) {
                    String name = subFile[i].getName();
                    modelNames.add(getFileNameNoEx(name));
                }
            }
        }

        return modelNames;
    }

    public static ArrayList<FilterItem> getFilterFiles(Context context, String index) {
        ArrayList<FilterItem> filterFiles = new ArrayList<FilterItem>();

        List<String> filterModels = copyFilterModelFiles(context, index);
        Map<String, String> filterIcons2 = copyFilterIconFiles2(context, index);
        List<String> filterNames = getFilterNames(context, index);

        if (filterModels == null || filterModels.size() == 0) {
            return filterFiles;
        }

        for (int i = 0; i < filterModels.size(); i++) {
            if (filterIcons2.get(filterNames.get(i)) != null)
                filterFiles.add(new FilterItem(filterNames.get(i), filterIcons2.get(filterNames.get(i)), filterModels.get(i)));
            else {
                //filterFiles.add(new FilterItem(filterNames.get(i), iconNature, filterModels.get(i)));
            }
        }

        return filterFiles;
    }

    public static List<String> copyFilterModelFiles(Context context, String index) {
        String files[] = null;
        ArrayList<String> modelFiles = new ArrayList<String>();

        try {
            files = context.getAssets().list(index);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String folderpath = null;
        File dataDir = context.getExternalFilesDir(null);
        if (dataDir != null) {
            folderpath = dataDir.getAbsolutePath() + File.separator + index;

            File folder = new File(folderpath);

            if (!folder.exists()) {
                folder.mkdir();
            }
        }
        for (int i = 0; i < files.length; i++) {
            String str = files[i];
            if (str.indexOf(".model") != -1) {
                copyFileIfNeed(context, str, index);
            }
        }

        File file = new File(folderpath);
        File[] subFile = file.listFiles();

        if (subFile == null || subFile.length == 0) {
            return modelFiles;
        }

        for (int i = 0; i < subFile.length; i++) {
            // 判断是否为文件夹
            if (!subFile[i].isDirectory()) {
                String filename = subFile[i].getAbsolutePath();
                String path = subFile[i].getPath();
                // 判断是否为model结尾
                if (filename.trim().toLowerCase().endsWith(".model") && filename.indexOf("filter") != -1) {
                    modelFiles.add(filename);
                }
            }
        }

        return modelFiles;
    }

    public static Map<String, String> copyFilterIconFiles2(Context context, String index) {
        String files[] = null;
        TreeMap<String, String> iconFiles = new TreeMap<String, String>();

        try {
            files = context.getAssets().list(index);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String folderpath = null;
        File dataDir = context.getExternalFilesDir(null);
        if (dataDir != null) {
            folderpath = dataDir.getAbsolutePath() + File.separator + index;

            File folder = new File(folderpath);

            if (!folder.exists()) {
                folder.mkdir();
            }
        }
        for (int i = 0; i < files.length; i++) {
            String str = files[i];
            if (str.indexOf(".png") != -1) {
                copyFileIfNeed(context, str, index);
            }
        }

        File file = new File(folderpath);
        File[] subFile = file.listFiles();

        if (subFile == null || subFile.length == 0) {
            return iconFiles;
        }

        for (int i = 0; i < subFile.length; i++) {
            // 判断是否为文件夹
            if (!subFile[i].isDirectory()) {
                String filename = subFile[i].getAbsolutePath();
                String path = subFile[i].getPath();
                // 判断是否为png结尾
                if (filename.trim().toLowerCase().endsWith(".png") && filename.indexOf("filter") != -1) {
                    String name = extractFileName(subFile[i].getName());
                    if (name.indexOf("filter")!=-1) {
                        name = subFile[i].getName().substring(13);
                    }
                    iconFiles.put(getFileNameNoEx(name), filename);
                }
            }
        }

        return iconFiles;
    }

    public static String extractFileName(String path) {
        if (path == null) {
            return null;
        }

        String[] parts = path.split("/");
        if (parts.length > 0) {
            return parts[parts.length - 1];
        } else {
            return null;
        }
    }

    public static List<String> getFilterNames(Context context, String index) {
        ArrayList<String> modelNames = new ArrayList<String>();
        String folderpath = null;
        File dataDir = context.getExternalFilesDir(null);
        if (dataDir != null) {
            folderpath = dataDir.getAbsolutePath() + File.separator + index;
            File folder = new File(folderpath);

            if (!folder.exists()) {
                folder.mkdir();
            }
        }

        File file = new File(folderpath);
        File[] subFile = file.listFiles();

        if (subFile == null || subFile.length == 0) {
            return modelNames;
        }

        for (int i = 0; i < subFile.length; i++) {
            // 判断是否为文件夹
            if (!subFile[i].isDirectory()) {
                String filename = subFile[i].getAbsolutePath();
                // 判断是否为model结尾
                if (filename.trim().toLowerCase().endsWith(".model") && filename.indexOf("filter") != -1) {
                    String name = extractFileName(subFile[i].getName());
                    if (name.indexOf("filter")!=-1) {
                        name = subFile[i].getName().substring(13);
                    }
                    modelNames.add(getFileNameNoEx(name));
                }
            }
        }

        return modelNames;
    }

    public static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }



    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

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
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            if (isQQMediaDocument(uri)) {
                String path = uri.getPath();
                File fileDir = Environment.getExternalStorageDirectory();
                File file = new File(fileDir, path.substring("/QQBrowser".length()));
                return file.exists() ? file.toString() : null;
            }
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
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
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
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

    public static String getBaseFolder(Context context) {
        String baseFolder = Environment.getExternalStorageDirectory() + "/DCIM/";
        File f = new File(baseFolder);
        if (!f.exists()) {
            boolean b = f.mkdirs();
            if (!b) {
                baseFolder = context.getExternalFilesDir(null).getAbsolutePath() + "/";
            }
        }
        return baseFolder;
    }

    //获取VideoPath
    public static String getPath(Context context, String path, String fileName) {
        String p = getBaseFolder(context) + path;
        File f = new File(p);
        if (!f.exists() && !f.mkdirs()) {
            return getBaseFolder(context) + fileName;
        }
        return p + fileName;
    }

    public static boolean isQQMediaDocument(Uri uri) {
        return "com.tencent.mtt.fileprovider".equals(uri.getAuthority());
    }


    private static final String SEPARATOR = File.separator;

    public static void copyFilesFromAssets(Context context, String assetsPath, String storagePath) {
        String temp = "";

        if (TextUtils.isEmpty(storagePath)) {
            return;
        } else if (storagePath.endsWith(SEPARATOR)) {
            storagePath = storagePath.substring(0, storagePath.length() - 1);
        }

        if (TextUtils.isEmpty(assetsPath) || assetsPath.equals(SEPARATOR)) {
            assetsPath = "";
        } else if (assetsPath.endsWith(SEPARATOR)) {
            assetsPath = assetsPath.substring(0, assetsPath.length() - 1);
        }

        AssetManager assetManager = context.getAssets();
        try {
            File file = new File(storagePath);
            if (!file.exists()) {//如果文件夹不存在，则创建新的文件夹
                file.mkdirs();
            }

            // 获取assets目录下的所有文件及目录名
            String[] fileNames = assetManager.list(assetsPath);
            if (fileNames.length > 0) {//如果是目录 apk
                for (String fileName : fileNames) {
                    if (!TextUtils.isEmpty(assetsPath)) {
                        temp = assetsPath + SEPARATOR + fileName;//补全assets资源路径
                    }

                    String[] childFileNames = assetManager.list(temp);
                    if (!TextUtils.isEmpty(temp) && childFileNames.length > 0) {//判断是文件还是文件夹：如果是文件夹
                        copyFilesFromAssets(context, temp, storagePath + SEPARATOR + fileName);
                    } else {//如果是文件
                        InputStream inputStream = assetManager.open(temp);
                        readInputStream(storagePath + SEPARATOR + fileName, inputStream);
                    }
                }
            } else {//如果是文件 doc_test.txt或者apk/app_test.apk
                InputStream inputStream = assetManager.open(assetsPath);
                if (assetsPath.contains(SEPARATOR)) {//apk/app_test.apk
                    assetsPath = assetsPath.substring(assetsPath.lastIndexOf(SEPARATOR), assetsPath.length());
                }
                readInputStream(storagePath + SEPARATOR + assetsPath, inputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 读取输入流中的数据写入输出流
     *
     * @param storagePath 目标文件路径
     * @param inputStream 输入流
     */
    public static void readInputStream(String storagePath, InputStream inputStream) {
        File file = new File(storagePath);
        try {
            if (!file.exists()) {
                // 1.建立通道对象
                FileOutputStream fos = new FileOutputStream(file);
                // 2.定义存储空间
                byte[] buffer = new byte[inputStream.available()];
                // 3.开始读文件
                int lenght = 0;
                while ((lenght = inputStream.read(buffer)) != -1) {// 循环从输入流读取buffer字节
                    // 将Buffer中的数据写到outputStream对象中
                    fos.write(buffer, 0, lenght);
                }
                fos.flush();// 刷新缓冲区
                // 4.关闭流
                fos.close();
                inputStream.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] readFileFromAssets(Context context, String assetFilePath){
        byte[] buffer = null;
        AssetManager am = context.getAssets();
        try {
            InputStream inputStream = am.open(assetFilePath);
            int length = inputStream.available();
            buffer = new byte[length];
            inputStream.read(buffer);
        }catch (Exception exception){
            exception.printStackTrace();
        }
        return buffer;
    }

    /**
     * 此代码匹配规则假设图片文件是 png 或 jpg，而关联文件是 zip 或 model。如有其他格式，请在代码中添加条件。
     * @param folderPath
     * @return
     */
    public static ArrayList<FilterItem> getFileItems(String folderPath) {
        ArrayList<FilterItem> fileItems = new ArrayList<>();
        File folder = new File(folderPath);

        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                // 存储文件的哈希映射，键是文件名前缀，值是文件路径（按类型区分）
                Map<String, String> imageFiles = new HashMap<>();
                Map<String, String> modelFiles = new HashMap<>();

                for (File file : files) {
                    if (file.isFile()) {
                        String fileName = file.getName();
                        String filePrefix = fileName.substring(0, fileName.lastIndexOf('.'));
                        String fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1);

                        if (fileExtension.equals("png") || fileExtension.equals("jpg")) {
                            imageFiles.put(filePrefix, file.getAbsolutePath());
                        } else if (fileExtension.equals("zip") || fileExtension.equals("model")) {
                            modelFiles.put(filePrefix, file.getAbsolutePath());
                        }
                    }
                }
                SortedMap<String, String> caseInsensitiveMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
                caseInsensitiveMap.putAll(imageFiles);

                // 配对图片和关联文件
                for (String prefix : caseInsensitiveMap.keySet()) {
                    if (modelFiles.containsKey(prefix)) {
                        FilterItem item = new FilterItem(
                                prefix,
                                imageFiles.get(prefix),
                                modelFiles.get(prefix)
                        );
                        fileItems.add(item);
                    }
                }
            }
        }
        return fileItems;
    }

}







