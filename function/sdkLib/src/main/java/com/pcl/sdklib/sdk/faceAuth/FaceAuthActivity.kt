package com.pcl.sdklib.sdk.faceAuth

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.util.Size
import android.view.KeyEvent
import android.view.WindowManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.camera.view.TransformExperimental
import androidx.camera.view.transform.CoordinateTransform
import androidx.camera.view.transform.ImageProxyTransformFactory
import androidx.core.content.ContextCompat
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.ext.logcom
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.yanhu.commonres.utils.PermissionXUtils
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.status.BaseBean
import com.blankj.utilcode.util.AppUtils
import com.pcl.sdklib.R
import com.pcl.sdklib.bean.FaceAuthInfo
import com.pcl.sdklib.databinding.ActivityFaceAuthBinding
import com.sensetime.senseid.Area2d
import com.sensetime.senseid.ErrorCode
import com.sensetime.senseid.Frame
import com.sensetime.senseid.LabelId
import com.sensetime.senseid.Message
import com.sensetime.senseid.Orientation
import com.sensetime.senseid.VideoFormat
import java.nio.ByteBuffer

/**
 * @author: zhengjun
 * created: 2025/2/20
 * desc:
 */
@Suppress("DEPRECATION")
class FaceAuthActivity : BaseActivity<ActivityFaceAuthBinding, FaceAuthViewModel>(
    R.layout.activity_face_auth,
    FaceAuthViewModel::class.java
) {
    private var completed:Boolean =false
    private lateinit var  faceAuthInfo:FaceAuthInfo
    private var source:Int = 1
    private var isConsumeGold:Boolean = false
    companion object{
        fun lunch(context:Context,faceAuthInfo:FaceAuthInfo,source:Int = 1,isConsumeGold:Boolean = false){
            val intent = Intent(context,FaceAuthActivity::class.java)
            intent.putExtra(IntentKeyConfig.DATA,faceAuthInfo)
            intent.putExtra(IntentKeyConfig.SOURCE,source)
            intent.putExtra("isConsumeGold",isConsumeGold)
            context.startActivity(intent)
        }
    }
    override fun initData() {
        setFullScreenStatusBar()
        setStatusBarStyle(false)
        source = intent.getIntExtra(IntentKeyConfig.SOURCE,1)
        isConsumeGold = intent.getBooleanExtra("isConsumeGold",false)
        faceAuthInfo = intent.getSerializableExtra(IntentKeyConfig.DATA) as FaceAuthInfo
        logcom("3.sessionId="+faceAuthInfo.sessionId)
        mViewModel.initialize(LabelId.FACE, faceAuthInfo.bizToken, AppCacheManager.userId, faceAuthInfo.sessionId)
        val permissions = ArrayList<String>()
        permissions.add(Manifest.permission.CAMERA)
        PermissionXUtils.checkPermission(this,
            permissions,
            "${AppUtils.getAppName()}想访问您的摄像头权限，用于人脸认证",
            "您拒绝授权摄像头权限，无法使用人脸认证相关的功能或服务",
            object : PermissionXUtils.PermissionListener {
                override fun onSuccess() {
                    startCamera()
                }
                override fun onFail() {
                    finish()
                }
            })
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.initialResult.observe(this) {
            if (it != 0) {
                showToast("人脸初始化失败")
                finish()
            }
        }
        mViewModel.detectMessage.observe(this) { messagePair ->
            val code: Int = messagePair.first
            val message: Message = messagePair.second
            Log.d("IDaaS", "onUpdate: {message:$message}")
            if (code == ErrorCode.OK) {
                when (message.actionType) {
                    0 -> if (message.color >= 0) {
                        mBinding.tips.text = "请保持不动"
                        val colorValue = 0xFF shl 24 or message.color
                        mBinding.root.setBackgroundColor(colorValue)
                        mBinding.overlay.maskColor = colorValue
                    } else {
                        mBinding.tips.text = "请面向屏幕"
                    }

                    1 -> mBinding.tips.text = "请连续眨眼"
                    2 -> mBinding.tips.text = "请缓慢张嘴"
                    3 -> mBinding.tips.text = "请缓慢摇头"
                    4 -> mBinding.tips.text = "请上下点头"
                    else -> {}
                }
            } else {
                when (code) {
                    ErrorCode.MULTIPLE_FACES -> mBinding.tips.text = "请保持检测框内只有一个人脸"
                    ErrorCode.FACE_NOT_IN_ROI -> mBinding.tips.text = "请保持人脸在检测框内"
                    ErrorCode.FACE_TOO_SMALL -> mBinding.tips.text = "请靠近一点"
                    ErrorCode.FACE_LOW_QUALITY -> mBinding.tips.text = "请保持不动"
                    ErrorCode.FACE_OCCLUSION -> mBinding.tips.text = "请正视屏幕，勿遮挡面部"
                    ErrorCode.FACE_NOT_FORWARD -> mBinding.tips.text = "请正视屏幕"
                    else -> mBinding.tips.text = "请面向屏幕"
                }
            }
        }

        mViewModel.completeMessage.observe(this) { messagePair ->
            val code: Int = messagePair.first
            var message: String = messagePair.second
            Log.d("IDaaS", "onComplete: {code:$code message:$message}")
            completed = true
            if (code == 0) {
                //检测识别结果
                checkFaceResult()
            } else {
                when (code) {
                    ErrorCode.DETECT_TIMEOUT -> {
                        message = "检测超时"
                    }
                    ErrorCode.UNSAFE_ENVIRONMENT -> {
                        message = "不安全环境"
                    }
                    ErrorCode.CONNECTION_FAIL -> {
                        message = "连接失败，请尝试更换下网络或设备"
                    }
                    ErrorCode.CONNECTION_AUTH_INVALID -> {
                        message = "连接鉴权失败"
                    }
                    ErrorCode.DETECT_EXCEPTION -> {
                        message = "检测发生异常"
                    }
                    ErrorCode.DETECT_UNSUPPORTED -> {
                        message = "检测失败"
                    }
                    ErrorCode.NO_FACE -> {
                        message = "没检测到人脸"
                    }
                }
                authFail(message)
            }
        }
    }

    private fun checkFaceResult() {
        mViewModel.checkFaceResult(faceAuthInfo.sessionId,source,isConsumeGold,object : OnRequestResultListener<String>{
            override fun onSuccess(data: BaseBean<String>) {
                LiveDataEventManager.sendLiveDataMessage(LiveDataEventManager.FACE_RESULT, true)
                finish()
            }
            override fun onFail(code: Int?, msg: String?) {
                super.onFail(code, msg)
                FaceAuthResultActivity.lunch(mContext,msg)
                finish()
            }
        })
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK) {
            authFail("已取消")
            true
        } else {
            super.onKeyDown(keyCode, event)
        }
    }


    @SuppressLint("UnsafeOptInUsageError")
    private fun startCamera() {
        val future = ProcessCameraProvider.getInstance(mContext)
        future.addListener({
            try {
                val rotation: Int = mBinding.viewFinder.display.rotation
                val cameraProvider = future.get()
                // Preview
                val preview =
                    Preview.Builder().build()
                preview.targetRotation = rotation
                preview.setSurfaceProvider(mBinding.viewFinder.surfaceProvider)
                cameraProvider.unbindAll()
                val analyzerBuilder = ImageAnalysis.Builder()
                analyzerBuilder.setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                analyzerBuilder.setTargetRotation(rotation)
                analyzerBuilder.setTargetResolution(Size(480, 640))
                val imageAnalysis = analyzerBuilder.build()
                imageAnalysis.setAnalyzer(
                    ContextCompat.getMainExecutor(mContext),
                    BeltIdsAnalyzer(this, mBinding.viewFinder, mBinding.overlay)
                )
                cameraProvider.bindToLifecycle(
                    mContext, CameraSelector.DEFAULT_FRONT_CAMERA,
                    imageAnalysis, preview
                )
            } catch (e: Exception) {
                e.printStackTrace()
                authFail("摄像头异常")
            }
        }, ContextCompat.getMainExecutor(mContext))
    }

    private fun authFail(msg:String) {
        showToast(msg)
        LiveDataEventManager.sendLiveDataMessage(LiveDataEventManager.FACE_RESULT, false)
        finish()
    }


    override fun onResume() {
        super.onResume()
        val attributes: WindowManager.LayoutParams =
            mContext.window.attributes
        attributes.screenBrightness = 1f
        mContext.window.attributes = attributes
    }

    override fun exactDestroy() {
        super.exactDestroy()
        mViewModel.release()
    }


    @TransformExperimental private class BeltIdsAnalyzer(
        detectFragment: FaceAuthActivity,
        previewView: PreviewView,
        overlay: Overlay
    ) :
        ImageAnalysis.Analyzer {
        private val detectFragment: FaceAuthActivity
        private val previewView: PreviewView
        private val overlay: Overlay
        private val imageIndex = 0
        private val imageProxyTransformFactory = ImageProxyTransformFactory()
        init {
            this.detectFragment = detectFragment
            this.previewView = previewView
            this.overlay = overlay
        }

        override fun analyze(image: ImageProxy) {
            if (detectFragment.completed) {
                image.close()
                return
            }
            val nv21: ByteBuffer = Images.yuv420ToNv21(image)
            val source = previewView.outputTransform
            val target = imageProxyTransformFactory.getOutputTransform(image)
            val coordinateTransform = CoordinateTransform(source!!, target)
            val targetRestrictArea = overlay.targetRestrictArea
            coordinateTransform.mapRect(targetRestrictArea)
            val frame = Frame.Builder(
                VideoFormat.NV21, nv21.array(), image.width,
                image.height
            )
            when (image.imageInfo.rotationDegrees) {
                90 -> frame.setOrientation(Orientation.CLOCKWISE_270)
                180 -> frame.setOrientation(Orientation.CLOCKWISE_180)
                270 -> frame.setOrientation(Orientation.CLOCKWISE_90)
                else -> {}
            }
            frame.setIsMirror(true)
            frame.setTargetRestrictArea(
                Area2d(
                    targetRestrictArea.left.toInt(),
                    targetRestrictArea.top.toInt(),
                    targetRestrictArea.width().toInt(),
                    targetRestrictArea.height().toInt()
                )
            )
            detectFragment.mViewModel.input(frame.build())
            image.close()
        }
    }
}