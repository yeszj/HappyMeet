package cn.yanhu.imchat.ui.chat

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.imchat.api.imChatRxApi
import cn.yanhu.imchat.custom.chat.CustomEaseChatLayout
import cn.yanhu.imchat.custom.chat.CustomEaseChatMessageListLayout
import cn.yanhu.imchat.view.emojiicon.CustomEaseEmojiIconMenu
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMMessage
import com.hyphenate.easeui.R
import com.hyphenate.easeui.constants.EaseConstant
import com.hyphenate.easeui.domain.EaseEmojiconGroupEntity
import com.hyphenate.easeui.manager.EaseDingMessageHelper
import com.hyphenate.easeui.modules.chat.EaseChatFragment
import com.hyphenate.easeui.modules.chat.interfaces.OnAddMsgAttrsBeforeSendEvent
import com.hyphenate.easeui.modules.chat.interfaces.OnChatFinishListener
import com.hyphenate.easeui.modules.chat.interfaces.OnChatLayoutListener
import com.hyphenate.easeui.modules.chat.interfaces.OnChatRecordTouchListener
import com.hyphenate.easeui.modules.chat.interfaces.OnMenuChangeListener
import com.hyphenate.easeui.modules.chat.interfaces.OnTranslateMessageListener
import com.hyphenate.easeui.modules.menu.EaseChatFinishReason
import com.hyphenate.easeui.modules.menu.EasePopupWindowHelper
import com.hyphenate.easeui.modules.menu.MenuItemBean
import com.hyphenate.easeui.ui.EaseBaiduMapActivity
import com.hyphenate.easeui.ui.base.EaseBaseFragment
import com.hyphenate.easeui.utils.EaseCommonUtils
import com.hyphenate.easeui.utils.EaseCompat
import com.hyphenate.easeui.utils.EaseFileUtils
import com.hyphenate.util.EMLog
import com.hyphenate.util.ImageUtils
import com.hyphenate.util.PathUtil
import com.hyphenate.util.VersionUtils
import java.io.File
import java.io.IOException

open class CustomEaseChatFragment : EaseBaseFragment(), OnChatLayoutListener, OnMenuChangeListener,
    OnAddMsgAttrsBeforeSendEvent, OnChatRecordTouchListener, OnTranslateMessageListener,
    OnChatFinishListener {
    lateinit var chatLayout: CustomEaseChatLayout
     var conversationId: String = ""
    var chatType = 0
    private var historyMsgId: String? = null
    var isRoam = false
    private var isMessageInit = false
    private var listener: OnChatLayoutListener? = null
    private var cameraFile: File? = null
    private var isAutoShowGift = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initArguments()
        return inflater.inflate(R.layout.ease_fragment_chat_list, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initListener()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initData()
    }


    private fun initArguments() {
        val bundle = arguments
        if (bundle != null) {
            conversationId = bundle.getString(EaseConstant.EXTRA_CONVERSATION_ID).toString()
            chatType = bundle.getInt(EaseConstant.EXTRA_CHAT_TYPE, EaseConstant.CHATTYPE_SINGLE)
            historyMsgId = bundle.getString(EaseConstant.HISTORY_MSG_ID)
            isRoam = bundle.getBoolean(EaseConstant.EXTRA_IS_ROAM, true)
            isAutoShowGift = bundle.getBoolean(IntentKeyConfig.IS_SHOW, false)
        }
    }

    open fun initView() {
        chatLayout = findViewById(R.id.layout_chat)
        chatLayout.chatMessageListLayout
            .setItemShowType(CustomEaseChatMessageListLayout.ShowType.NORMAL)
        val customEaseEmojiIconMenu = CustomEaseEmojiIconMenu(mContext)
        chatLayout.chatInputMenu.setCustomEmojiconMenu(customEaseEmojiIconMenu)
        getExpression()
    }

    private fun getExpression() {
        request(
            { imChatRxApi.getChatEmoji() },
            object : OnRequestResultListener<MutableList<EaseEmojiconGroupEntity>> {
                override fun onSuccess(data: BaseBean<MutableList<EaseEmojiconGroupEntity>>) {
                    val emojiconMenu =
                        chatLayout.chatInputMenu.emojiconMenu as CustomEaseEmojiIconMenu
                    emojiconMenu.init(data.data)
                }

            },
            false
        )
    }


    fun initListener() {
        chatLayout.setOnChatLayoutListener(this)
        chatLayout.setOnPopupWindowItemClickListener(this)
        chatLayout.setOnAddMsgAttrsBeforeSendEvent(this)
        chatLayout.setOnChatRecordTouchListener(this)
        chatLayout.setOnTranslateListener(this)
        chatLayout.setOnChatFinishListener(this)
    }

    fun initData() {
        if (!TextUtils.isEmpty(historyMsgId)) {
            chatLayout.init(
                CustomEaseChatMessageListLayout.LoadDataType.HISTORY,
                conversationId,
                chatType
            )
            chatLayout.loadData(historyMsgId)
        } else {
            if (isRoam) {
                chatLayout.init(
                    CustomEaseChatMessageListLayout.LoadDataType.ROAM,
                    conversationId,
                    chatType
                )
            } else {
                chatLayout.init(conversationId, chatType)
            }
            chatLayout.loadDefaultData()
        }
        isMessageInit = true
    }

    override fun onResume() {
        super.onResume()
        if (isMessageInit && chatLayout != null) {
            EMLog.e("EMUnRead", "onResume中调用refreshMessages方法，conversationId=$conversationId")
            chatLayout.chatMessageListLayout.refreshMessages()
        }
    }

    fun setOnChatLayoutListener(listener: OnChatLayoutListener?) {
        this.listener = listener
    }

    override fun onBubbleClick(message: EMMessage): Boolean {
        return if (listener != null) {
            listener!!.onBubbleClick(message)
        } else false
    }

    override fun onBubbleLongClick(v: View, message: EMMessage): Boolean {
        return if (listener != null) {
            listener!!.onBubbleLongClick(v, message)
        } else false
    }

    override fun onUserAvatarClick(username: String) {
        if (listener != null) {
            listener!!.onUserAvatarClick(username)
        }
    }

    override fun onUserAvatarLongClick(username: String) {
        if (listener != null) {
            listener!!.onUserAvatarLongClick(username)
        }
    }

    override fun onChatExtendMenuItemClick(view: View, itemId: Int) {
        if (itemId == R.id.extend_item_take_picture) {
            selectPicFromCamera()
        } else if (itemId == R.id.extend_item_picture) {
            selectPicFromLocal()
        } else if (itemId == R.id.extend_item_location) {
            startMapLocation(REQUEST_CODE_MAP)
        } else if (itemId == R.id.extend_item_video) {
            selectVideoFromLocal()
        } else if (itemId == R.id.extend_item_file) {
            selectFileFromLocal()
        }
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    override fun onChatSuccess(message: EMMessage) {
        // you can do something after sending a successful message
        val messageAdapter = chatLayout.chatMessageListLayout.messageAdapter
        val position =
            if (chatLayout.chatMessageListLayout.layoutManager.reverseLayout) 0 else chatLayout.chatMessageListLayout.messageAdapter.itemCount - 1
        val item = messageAdapter.getItem(position)
        item.setStatus(EMMessage.Status.SUCCESS)
        chatLayout.chatMessageListLayout.messageAdapter.notifyItemChanged(position)
        chatLayout.chatMessageListLayout.checkMessageReward(messageAdapter.data, true)
    }

    override fun onChatError(code: Int, errorMsg: String) {
        if (listener != null) {
            listener!!.onChatError(code, errorMsg)
        }
    }

    override fun onQuoteClick(message: EMMessage): Boolean {
        return false
    }

    override fun onQuoteLongClick(v: View, message: EMMessage): Boolean {
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            chatLayout.chatInputMenu.hideExtendContainer()
            if (requestCode == REQUEST_CODE_CAMERA) { // capture new image
                onActivityResultForCamera(data)
            } else if (requestCode == REQUEST_CODE_LOCAL) { // send local image
                onActivityResultForLocalPhotos(data)
            } else if (requestCode == REQUEST_CODE_MAP) { // location
                onActivityResultForMapLocation(data)
            } else if (requestCode == REQUEST_CODE_DING_MSG) { // To send the ding-type msg.
                onActivityResultForDingMsg(data)
            } else if (requestCode == REQUEST_CODE_SELECT_FILE) {
                onActivityResultForLocalFiles(data)
            } else if (requestCode == REQUEST_CODE_SELECT_VIDEO) {
                onActivityResultForLocalVideos(data)
            }
        }
    }

    private fun onActivityResultForLocalVideos(data: Intent?) {
        if (data != null) {
            val uri = data.data
            val mediaPlayer = MediaPlayer()
            try {
                mediaPlayer.setDataSource(mContext, uri!!)
                mediaPlayer.prepare()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val duration = mediaPlayer.duration
            EMLog.d(TAG, "path = " + uri!!.path + ",duration=" + duration)
            EaseFileUtils.saveUriPermission(mContext, uri, data)
            chatLayout.sendVideoMessage(uri, duration)
        }
    }

    /**
     * select picture from camera
     */
    protected fun selectPicFromCamera() {
        if (!checkSdCardExist()) {
            return
        }
        cameraFile = File(
            PathUtil.getInstance().imagePath,
            EMClient.getInstance().currentUser + System.currentTimeMillis() + ".jpg"
        )
        cameraFile!!.parentFile.mkdirs()
        startActivityForResult(
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(
                MediaStore.EXTRA_OUTPUT, EaseCompat.getUriForFile(
                    context, cameraFile!!
                )
            ), REQUEST_CODE_CAMERA
        )
    }

    /**
     * select local image
     */
    protected fun selectPicFromLocal() {
        EaseCompat.openImage(this, REQUEST_CODE_LOCAL)
    }

    /**
     * 启动定位
     *
     * @param requestCode
     */
    protected fun startMapLocation(requestCode: Int) {
        EaseBaiduMapActivity.actionStartForResult(this, requestCode)
    }

    /**
     * select local video
     */
    protected fun selectVideoFromLocal() {
        val intent = Intent()
        if (VersionUtils.isTargetQ(activity)) {
            intent.action = Intent.ACTION_OPEN_DOCUMENT
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                intent.action = Intent.ACTION_GET_CONTENT
            } else {
                intent.action = Intent.ACTION_OPEN_DOCUMENT
            }
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "video/*"
        startActivityForResult(intent, REQUEST_CODE_SELECT_VIDEO)
    }

    /**
     * select local file
     */
    protected fun selectFileFromLocal() {
        val intent = Intent()
        if (VersionUtils.isTargetQ(activity)) {
            intent.action = Intent.ACTION_OPEN_DOCUMENT
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                intent.action = Intent.ACTION_GET_CONTENT
            } else {
                intent.action = Intent.ACTION_OPEN_DOCUMENT
            }
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        startActivityForResult(intent, REQUEST_CODE_SELECT_FILE)
    }

    /**
     * 相机返回处理结果
     *
     * @param data
     */
    protected fun onActivityResultForCamera(data: Intent?) {
        if (cameraFile != null && cameraFile!!.exists()) {
            //检查图片是否被旋转并调整回来
            val restoreImageUri = ImageUtils.checkDegreeAndRestoreImage(
                mContext, Uri.parse(
                    cameraFile!!.absolutePath
                )
            )
            chatLayout.sendImageMessage(restoreImageUri)
        }
    }

    /**
     * 选择本地图片处理结果
     *
     * @param data
     */
    protected fun onActivityResultForLocalPhotos(data: Intent?) {
        if (data != null) {
            val selectedImage = data.data
            if (selectedImage != null) {
                val filePath = EaseFileUtils.getFilePath(mContext, selectedImage)
                if (!TextUtils.isEmpty(filePath) && File(filePath).exists()) {
                    chatLayout.sendImageMessage(Uri.parse(filePath))
                } else {
                    EaseFileUtils.saveUriPermission(mContext, selectedImage, data)
                    chatLayout.sendImageMessage(selectedImage)
                }
            }
        }
    }

    /**
     * 地图定位结果处理
     *
     * @param data
     */
    protected fun onActivityResultForMapLocation(data: Intent?) {
        if (data != null) {
            val latitude = data.getDoubleExtra("latitude", 0.0)
            val longitude = data.getDoubleExtra("longitude", 0.0)
            val locationAddress = data.getStringExtra("address")
            val buildingName = data.getStringExtra("buildingName")
            if (locationAddress != null && locationAddress != "") {
                chatLayout.sendLocationMessage(latitude, longitude, locationAddress, buildingName)
            } else {
                if (listener != null) {
                    listener!!.onChatError(-1, resources.getString(R.string.unable_to_get_loaction))
                }
            }
        }
    }

    protected fun onActivityResultForDingMsg(data: Intent?) {
        if (data != null) {
            val msgContent = data.getStringExtra("msg")
            EMLog.i(TAG, "To send the ding-type msg, content: $msgContent")
            // Send the ding-type msg.
            val dingMsg = EaseDingMessageHelper.get().createDingMessage(conversationId, msgContent)
            chatLayout.sendMessage(dingMsg)
        }
    }

    /**
     * 本地文件选择结果处理
     *
     * @param data
     */
    protected fun onActivityResultForLocalFiles(data: Intent?) {
        if (data != null) {
            val uri = data.data
            if (uri != null) {
                val filePath = EaseFileUtils.getFilePath(mContext, uri)
                if (!TextUtils.isEmpty(filePath) && File(filePath).exists()) {
                    chatLayout.sendFileMessage(Uri.parse(filePath))
                } else {
                    EaseFileUtils.saveUriPermission(mContext, uri, data)
                    chatLayout.sendFileMessage(uri)
                }
            }
        }
    }

    /**
     * 检查sd卡是否挂载
     *
     * @return
     */
    protected fun checkSdCardExist(): Boolean {
        return EaseCommonUtils.isSdcardExist()
    }

    override fun onPreMenu(helper: EasePopupWindowHelper, message: EMMessage, v: View) {}
    override fun onMenuItemClick(item: MenuItemBean, message: EMMessage): Boolean {
        return false
    }

    override fun addMsgAttrsBeforeSend(message: EMMessage) {}

    /**
     * Set whether can touch voice button
     *
     * @param v
     * @param event
     * @return
     */
    override fun onRecordTouch(v: View, event: MotionEvent): Boolean {
        return true
    }

    override fun translateMessageSuccess(message: EMMessage) {}
    override fun translateMessageFail(message: EMMessage, code: Int, error: String) {}
    override fun onChatFinish(reason: EaseChatFinishReason, id: String) {
        if (mContext != null) {
            mContext.finish()
        }
    }

    override fun onPause() {
        super.onPause()
        if (mContext.isFinishing) {
            chatLayout.removeListener()
        }
    }

    companion object {
        protected const val REQUEST_CODE_MAP = 1
        protected const val REQUEST_CODE_CAMERA = 2
        protected const val REQUEST_CODE_LOCAL = 3
        protected const val REQUEST_CODE_DING_MSG = 4
        protected const val REQUEST_CODE_SELECT_VIDEO = 11
        protected const val REQUEST_CODE_SELECT_FILE = 12
        private val TAG = EaseChatFragment::class.java.simpleName
    }
}