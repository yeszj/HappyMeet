package cn.happy.beautyface.ui.utils

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import cn.happy.beautyface.sensetime.BeautyStats
import cn.happy.beautyface.sensetime.CaptureMode
import cn.happy.beautyface.sensetime.Config
import cn.happy.beautyface.sensetime.ErrorCode
import cn.happy.beautyface.sensetime.IEventCallback
import cn.happy.beautyface.sensetime.STHandlers
import cn.happy.beautyface.sensetime.SenseTimeBeautyAPI
import cn.happy.beautyface.sensetime.createSenseTimeBeautyAPI
import cn.yanhu.baselib.utils.ext.logcom
import com.blankj.utilcode.util.GsonUtils
import io.agora.base.VideoFrame
import io.agora.rtc2.Constants
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.video.IVideoFrameObserver
import io.agora.rtc2.video.VideoCanvas
import java.lang.ref.WeakReference
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.Future

object BeautyManager {

    private var context: Application? = null
    private var rtcEngine: RtcEngine? = null
    private var senseTimeBeautyAPI: SenseTimeBeautyAPI? = null

    private var videoView: WeakReference<View>? = null
    private var renderMode: Int = Constants.RENDER_MODE_HIDDEN

    private val workerExecutor = Executors.newSingleThreadExecutor()
    private val mainExecutor = Handler(Looper.getMainLooper())
    private var createBeautyFuture: Future<*>? = null
    private var destroyBeautyFuture: Future<*>? = null

    // Track initialization status of beauty SDKs
    private var senseTimeInitSuccess = false

    // Beauty type
    var beautyType = BeautyType.SenseTime
        set(value) {
            if (field == value) {
                when (value) {
                    BeautyType.SenseTime -> senseTimeBeautyAPI?.let { return }
//                    BeautyType.FaceUnity -> faceUnityBeautyAPI?.let { return }
//                    BeautyType.ByteDance -> byteDanceBeautyAPI?.let { return }
                    BeautyType.Agora -> return
                }
            }
            val oldType = field
            field = value
            switchBeauty(oldType, value)
        }

    // Beauty switch
    var enable = false
        set(value) {
            field = value
            when (beautyType) {
                BeautyType.SenseTime -> senseTimeBeautyAPI?.enable(value)
//                BeautyType.FaceUnity -> faceUnityBeautyAPI?.enable(value)
//                BeautyType.ByteDance -> byteDanceBeautyAPI?.enable(value)
                BeautyType.Agora -> AgoraBeautySDK.enable(value)
            }
        }

    fun setDefaultConfig(context: Context) {
        when (beautyType) {
            BeautyType.SenseTime -> SenseTimeBeautySDK.setDefaultConfig(context)
//                BeautyType.FaceUnity -> faceUnityBeautyAPI?.enable(value)
//                BeautyType.ByteDance -> byteDanceBeautyAPI?.enable(value)
            BeautyType.Agora -> AgoraBeautySDK.setDefaultConfig(context)
        }
    }

    fun saveBeautyConfig() {
        when (beautyType) {
            BeautyType.SenseTime -> SenseTimeBeautySDK.saveBeautyConfig()
//                BeautyType.FaceUnity -> faceUnityBeautyAPI?.enable(value)
//                BeautyType.ByteDance -> byteDanceBeautyAPI?.enable(value)
            BeautyType.Agora -> AgoraBeautySDK.saveBeautyConfig()
        }
    }


    fun setStickerItem(stickerItem: SenseTimeBeautySDK.StickerItem?) {
        when (beautyType) {
            BeautyType.SenseTime -> SenseTimeBeautySDK.beautyConfig.sticker = stickerItem
            BeautyType.Agora -> {}
        }
    }

    @JvmStatic
    fun initialize(context: Context, rtcEngine: RtcEngine) {
        this.context = context.applicationContext as Application
        this.rtcEngine = rtcEngine
        this.beautyType = BeautyType.SenseTime
        this.enable = true // Disable beauty on low-end devices
      //  rtcEngine.registerVideoFrameObserver(MultiBeautyVideoObserver())
    }

    @JvmStatic
    fun setupLocalVideo(view: View?, renderMode: Int) {
        mainExecutor.post {
            this.videoView = WeakReference(view)
            this.renderMode = renderMode
            when (beautyType) {
                BeautyType.SenseTime -> {
                    if (senseTimeInitSuccess) {
                        senseTimeBeautyAPI?.setupLocalVideo(view, renderMode)
                    } else {
                        rtcEngine?.setupLocalVideo(
                            VideoCanvas(
                                view,
                                renderMode,
                                0
                            ).apply {
                                mirrorMode = Constants.VIDEO_MIRROR_MODE_AUTO
                            }
                        )
                    }

                }

//                BeautyType.FaceUnity -> {
//                    if (faceUnityInitSuccess) {
//                        faceUnityBeautyAPI?.setupLocalVideo(view, renderMode)
//                    } else {
//                        rtcEngine?.setupLocalVideo(
//                            VideoCanvas(
//                                view,
//                                renderMode,
//                                0
//                            ).apply {
//                                mirrorMode = Constants.VIDEO_MIRROR_MODE_AUTO
//                            }
//                        )
//                    }
//                }
//                BeautyType.ByteDance -> {
//                    if (byteDanceInitSuccess) {
//                        byteDanceBeautyAPI?.setupLocalVideo(view, renderMode)
//                    } else {
//                        rtcEngine?.setupLocalVideo(
//                            VideoCanvas(
//                                view,
//                                renderMode,
//                                0
//                            ).apply {
//                                mirrorMode = Constants.VIDEO_MIRROR_MODE_AUTO
//                            }
//                        )
//                    }
//                }
                BeautyType.Agora -> rtcEngine?.setupLocalVideo(
                    VideoCanvas(
                        view,
                        renderMode,
                        0
                    ).apply {
                        mirrorMode = Constants.VIDEO_MIRROR_MODE_DISABLED
                    }
                )
            }
        }
    }

    fun destroy() {
        rtcEngine?.registerVideoFrameObserver(null)
        mainExecutor.post {
            videoView?.get()?.let {
                rtcEngine?.setupLocalVideo(VideoCanvas(null))
            }
            videoView = null
        }
        context = null
        rtcEngine = null
        destroyBeauty(beautyType)
    }


    private fun switchBeauty(oldType: BeautyType, newType: BeautyType) {
        createBeautyFuture?.cancel(true)
        destroyBeautyFuture?.cancel(true)

        destroyBeautyFuture = destroyBeauty(oldType)
        createBeautyFuture = createBeauty(newType)

    }

    private fun createBeauty(type: BeautyType) =
        workerExecutor.submit {
            val ctx = context ?: return@submit
            val rtc = rtcEngine ?: return@submit

            val setupLocalVideoCountDownLatch = CountDownLatch(1)
            when (type) {
                BeautyType.SenseTime -> {
                    senseTimeInitSuccess =
                        SenseTimeBeautySDK.initBeautySDK(ctx,false)
                    if (senseTimeInitSuccess) {
                        val senseTimeBeautyAPI = createSenseTimeBeautyAPI()
                        senseTimeBeautyAPI.initialize(
                            Config(
                                ctx,
                                rtc,
                                STHandlers(
                                    SenseTimeBeautySDK.mobileEffectNative,
                                    SenseTimeBeautySDK.humanActionNative
                                ),
                                eventCallback = object : IEventCallback {
                                    override fun onBeautyStats(stats: BeautyStats) {
                                        logcom("costTime", "costTime: ${GsonUtils.toJson(stats)}")
                                    }

                                },
                                statsEnable = true,
                                captureMode = CaptureMode.Agora
                            )
                        )
                        senseTimeBeautyAPI.enable(enable)
                        SenseTimeBeautySDK.setBeautyAPI(senseTimeBeautyAPI)
                        this.senseTimeBeautyAPI = senseTimeBeautyAPI
                        mainExecutor.post {
                            videoView?.get()?.let {
                                senseTimeBeautyAPI.setupLocalVideo(it, renderMode)
                            }
                            setupLocalVideoCountDownLatch.countDown()
                        }

                    } else {
                        mainExecutor.post {
                            videoView?.get()?.let {
                                rtc.setupLocalVideo(
                                    VideoCanvas(
                                        it,
                                        renderMode,
                                        0
                                    ).apply {
                                        mirrorMode = Constants.VIDEO_MIRROR_MODE_AUTO
                                    }
                                )
                            }
                            setupLocalVideoCountDownLatch.countDown()
                        }
                    }
                }

//                BeautyType.FaceUnity -> {
//                    faceUnityInitSuccess = FaceUnityBeautySDK.initBeauty(ctx, BuildConfig.BEAUTY_RESOURCE.isEmpty())
//                    if (faceUnityInitSuccess) {
//                        val faceUnityBeautyAPI = createFaceUnityBeautyAPI()
//                        faceUnityBeautyAPI.initialize(
//                            io.agora.beautyapi.faceunity.Config(
//                                ctx,
//                                rtc,
//                                FURenderKit.getInstance(),
//                                captureMode = io.agora.beautyapi.faceunity.CaptureMode.Custom
//                            )
//                        )
//                        faceUnityBeautyAPI.enable(enable)
//                        FaceUnityBeautySDK.setBeautyAPI(faceUnityBeautyAPI)
//                        this.faceUnityBeautyAPI = faceUnityBeautyAPI
//                        mainExecutor.post {
//                            videoView?.get()?.let {
//                                faceUnityBeautyAPI.setupLocalVideo(it, renderMode)
//                            }
//                            setupLocalVideoCountDownLatch.countDown()
//                        }
//                    } else {
//                        mainExecutor.post {
//                            Toast.makeText(
//                                ctx,
//                                R.string.show_beauty_license_faceunity,
//                                Toast.LENGTH_LONG
//                            ).show()
//                            videoView?.get()?.let {
//                                rtc.setupLocalVideo(
//                                    VideoCanvas(
//                                        it,
//                                        renderMode,
//                                        0
//                                    ).apply {
//                                        mirrorMode = Constants.VIDEO_MIRROR_MODE_AUTO
//                                    }
//                                )
//                            }
//                            setupLocalVideoCountDownLatch.countDown()
//                        }
//                    }
//                }

//                BeautyType.ByteDance -> {
//                    byteDanceInitSuccess = ByteDanceBeautySDK.initBeautySDK(ctx, BuildConfig.BEAUTY_RESOURCE.isEmpty())
//                    if (byteDanceInitSuccess) {
//                        val byteDanceBeautyAPI = createByteDanceBeautyAPI()
//                        byteDanceBeautyAPI.initialize(
//                            io.agora.beautyapi.bytedance.Config(
//                                ctx,
//                                rtc,
//                                ByteDanceBeautySDK.renderManager,
//                                EventCallback(
//                                    onEffectInitialized = {
//                                        ByteDanceBeautySDK.initEffect(ctx)
//                                    },
//                                    onEffectDestroyed = {
//                                        ByteDanceBeautySDK.unInitEffect()
//                                    }
//                                ),
//                                captureMode = io.agora.beautyapi.bytedance.CaptureMode.Custom
//                            )
//                        )
//                        byteDanceBeautyAPI.enable(enable)
//                        ByteDanceBeautySDK.setBeautyAPI(byteDanceBeautyAPI)
//                        this.byteDanceBeautyAPI = byteDanceBeautyAPI
//                        mainExecutor.post {
//                            videoView?.get()?.let {
//                                byteDanceBeautyAPI.setupLocalVideo(it, renderMode)
//                            }
//                            setupLocalVideoCountDownLatch.countDown()
//                        }
//
//                    } else {
//                        mainExecutor.post {
//                            Toast.makeText(
//                                ctx,
//                                R.string.show_beauty_license_bytedance,
//                                Toast.LENGTH_LONG
//                            ).show()
//                            videoView?.get()?.let {
//                                rtc.setupLocalVideo(
//                                    VideoCanvas(
//                                        it,
//                                        renderMode,
//                                        0
//                                    ).apply {
//                                        mirrorMode = Constants.VIDEO_MIRROR_MODE_AUTO
//                                    }
//                                )
//                            }
//                            setupLocalVideoCountDownLatch.countDown()
//                        }
//                    }
//                }

                BeautyType.Agora -> {
                    AgoraBeautySDK.initBeautySDK(ctx, rtc)
                    AgoraBeautySDK.enable(enable)
                    mainExecutor.postDelayed({
                        videoView?.get()?.let {
                            rtc.setupLocalVideo(
                                VideoCanvas(
                                    it,
                                    renderMode,
                                    0
                                ).apply {
                                    mirrorMode = Constants.VIDEO_MIRROR_MODE_DISABLED
                                }
                            )
                        }
                        setupLocalVideoCountDownLatch.countDown()
                    }, 140)
                }
            }
            setupLocalVideoCountDownLatch.await()
        }


    private fun destroyBeauty(type: BeautyType) =
        workerExecutor.submit {
            val setupLocalVideoCountDownLatch = CountDownLatch(1)

            mainExecutor.post {
                videoView?.get()?.let {
                    rtcEngine?.setupLocalVideo(VideoCanvas(null))
                }
                setupLocalVideoCountDownLatch.countDown()
            }

            setupLocalVideoCountDownLatch.await()

            when (type) {
                BeautyType.SenseTime ->
                    senseTimeBeautyAPI?.let {
                        it.release()
                        senseTimeBeautyAPI = null
                        SenseTimeBeautySDK.unInitBeautySDK()
                        senseTimeInitSuccess = false
                    }

//
//                BeautyType.FaceUnity ->
//                    faceUnityBeautyAPI?.let {
//                        it.release()
//                        faceUnityBeautyAPI = null
//                        FaceUnityBeautySDK.unInitBeauty()
//                        faceUnityInitSuccess = false
//                    }
//
//                BeautyType.ByteDance ->
//                    byteDanceBeautyAPI?.let {
//                        it.release()
//                        byteDanceBeautyAPI = null
//                        byteDanceInitSuccess = false
//                    }

                BeautyType.Agora ->
                    AgoraBeautySDK.unInitBeautySDK()
            }
        }


    enum class BeautyType {
        SenseTime,

        //        FaceUnity,
//        ByteDance,
        Agora
    }

    class MultiBeautyVideoObserver : IVideoFrameObserver {
        private var isFront = true
        override fun onCaptureVideoFrame(type: Int, videoFrame: VideoFrame?): Boolean {
            if (destroyBeautyFuture?.isDone != true) {
                return false
            }
            if (createBeautyFuture?.isDone != true) {
                return false
            }
            val frame = videoFrame ?: return false
            isFront = frame.sourceType == VideoFrame.SourceType.kFrontCamera

            return when (beautyType) {
                BeautyType.SenseTime -> {
                    when (senseTimeBeautyAPI?.onFrame(frame)) {
                        ErrorCode.ERROR_FRAME_SKIPPED.value -> false
                        else -> true
                    }
                }

                BeautyType.Agora -> true
            }
        }

        override fun onPreEncodeVideoFrame(type: Int, videoFrame: VideoFrame?) = true

        override fun onMediaPlayerVideoFrame(videoFrame: VideoFrame?, mediaPlayerId: Int) = true

        override fun onRenderVideoFrame(
            channelId: String?,
            uid: Int,
            videoFrame: VideoFrame?
        ) = true

        override fun getVideoFrameProcessMode() = IVideoFrameObserver.PROCESS_MODE_READ_WRITE

        override fun getVideoFormatPreference() = IVideoFrameObserver.VIDEO_PIXEL_DEFAULT

        override fun getRotationApplied() = false

        override fun getMirrorApplied(): Boolean {
            return when (beautyType) {
                BeautyType.SenseTime -> senseTimeBeautyAPI?.getMirrorApplied() ?: false
                BeautyType.Agora -> isFront
            }
        }

        override fun getObservedFramePosition() = IVideoFrameObserver.POSITION_POST_CAPTURER

    }
}