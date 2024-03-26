package cn.yanhu.commonres.utils

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentActivity
import cn.yanhu.baselib.callBack.OnSavePictureListener
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.DisplayUtils
import cn.yanhu.baselib.utils.ext.showToast
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.ThreadUtils
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream

/**
 * @author zhengjun
 * @desc
 * @create at 2021/6/23 16:26
 */
object BitmapUtils {
    fun view2Bitmap(shareView: View): Bitmap {
        val bitmap = Bitmap.createBitmap(
            shareView.measuredWidth, shareView.measuredHeight,
            Bitmap.Config.RGB_565
        )
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        shareView.draw(canvas)
        return bitmap
    }

    fun view2Bitmap(context: Context?, shareView: View, width: Int, height: Int): Bitmap {
        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(
            DisplayUtils.dp2px(context, width.toFloat()),
            View.MeasureSpec.EXACTLY
        )
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(
            DisplayUtils.dp2px(context, height.toFloat()),
            View.MeasureSpec.EXACTLY
        )
        shareView.measure(widthMeasureSpec, heightMeasureSpec)
        shareView.layout(0, 0, shareView.measuredWidth, shareView.measuredHeight)
        val bitmap = Bitmap.createBitmap(
            shareView.measuredWidth, shareView.measuredHeight,
            Bitmap.Config.RGB_565
        )
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        shareView.draw(canvas)
        return bitmap
    }

    fun savePicture(
        mActivity: FragmentActivity,
        finalBitmap: Bitmap,
        loadListener: OnSavePictureListener?
    ) {
        val permissions = ArrayList<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        PermissionXUtils.checkPermission(mActivity,permissions,"${AppUtils.getAppName()}想访问您的以下权限，用于图片保存", "您拒绝授权权限，将无法体验部分功能",object :PermissionXUtils.PermissionListener{
            override fun onSuccess() {
                DialogUtils.showLoading("正在保存...")
                ThreadUtils.executeByIo(object : ThreadUtils.SimpleTask<Boolean>() {
                    override fun onSuccess(result: Boolean) {
                        if (result){
                            showToast("保存成功")
                            loadListener?.onSuccess()
                        }else{
                            loadListener?.onFinish()
                        }
                        DialogUtils.dismissLoading()
                    }
                    override fun doInBackground(): Boolean {
                        return try {
                            savePicture(mActivity, finalBitmap, System.currentTimeMillis().toString())
                        }catch (e:Exception){
                            false
                        }
                    }
                })

            }
            override fun onFail() {
            }
        })
    }

    /*
     * 保存文件，文件名为当前日期
     */
    fun savePicture(context: Context, bitmap: Bitmap, bitName: String): Boolean {
        val fileName: String
        val file: File
        val brand = Build.BRAND
        fileName = if (brand == "xiaomi") { // 小米手机brand.equals("xiaomi")
            Environment.getExternalStorageDirectory().path + "/DCIM/Camera/" + bitName
        } else if (brand.equals("Huawei", ignoreCase = true)) {
            Environment.getExternalStorageDirectory().path + "/DCIM/Camera/" + bitName
        } else { // Meizu 、Oppo
            Environment.getExternalStorageDirectory().path + "/DCIM/" + bitName
        }
        file = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return saveSignImage(context, bitName, bitmap)
        } else {
            Log.v("saveBitmap brand", "" + brand)
            File(fileName)
        }
        if (file.exists()) {
            file.delete()
        }
        val out: FileOutputStream
        try {
            out = FileOutputStream(file)
            // 格式为 JPEG，照相机拍出的图片为JPEG格式的，PNG格式的不能显示在相册中
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)) {
                out.flush()
                out.close()
                // 插入图库
                MediaStore.Images.Media.insertImage(
                    context.contentResolver,
                    file.absolutePath,
                    bitName,
                    null
                )
            }
        } catch (e: FileNotFoundException) {
            Log.e("FileNotFoundException", "FileNotFoundException:" + e.message)
            e.printStackTrace()
            return false
        } catch (e: Exception) {
            Log.e("IOException", "IOException:" + e.message)
            e.printStackTrace()
            return false
        }
        // 发送广播，通知刷新图库的显示
        context.sendBroadcast(
            Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(
                    "file://$fileName"
                )
            )
        )
        return true
    }

    /**
     * android Q
     * 将文件保存到公共的媒体文件夹
     * 这里的filepath不是绝对路径，而是某个媒体文件夹下的子路径，和沙盒子文件夹类似
     * 这里的filename单纯的指文件名，不包含路径
     */
    private fun saveSignImage(context: Context, fileName: String, bitmap: Bitmap): Boolean {
        try {
            //设置保存参数到ContentValues中
            val contentValues = ContentValues()
            //设置文件名
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            //兼容Android Q和以下版本
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                //android Q中不再使用DATA字段，而用RELATIVE_PATH代替
                //RELATIVE_PATH是相对路径不是绝对路径
                //DCIM是系统文件夹，关于系统文件夹可以到系统自带的文件管理器中查看，不可以写没存在的名字
                contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/")
                //contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Music/signImage");
            } else {
                contentValues.put(
                    MediaStore.Images.Media.DATA, Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES
                    ).path
                )
            }
            //设置文件类型
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/JPEG")
            //执行insert操作，向系统文件夹中添加文件
            //EXTERNAL_CONTENT_URI代表外部存储器，该值不变
            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            if (uri != null) {
                //若生成了uri，则表示该文件添加成功
                //使用流将内容写入该uri中即可
                val outputStream = context.contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                    outputStream.flush()
                    outputStream.close()
                }
                Log.d("path", uri.path!!)
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}