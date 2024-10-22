package cn.yanhu.imchat.view.emojiicon

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import cn.yanhu.imchat.databinding.ViewEmojiMsgBinding
import com.chad.library.adapter4.QuickAdapterHelper
import com.hyphenate.easeui.domain.EaseEmojicon
import com.hyphenate.easeui.domain.EaseEmojicon.Type
import cn.yanhu.imchat.R
import cn.yanhu.imchat.adapter.MessageExpressionAdapter
import cn.yanhu.imchat.db.EmojiUserCacheManager

/**
 * @author: zhengjun
 * created: 2023/4/10
 * desc:
 */
class ChatExtendEmojiView : LinearLayout {
    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }

    private lateinit var mBinding: ViewEmojiMsgBinding
    private val expressionAdapter by lazy {
        MessageExpressionAdapter()
    }
    private lateinit var helper: QuickAdapterHelper
    private fun initView(context: Context) {
        mBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.view_emoji_msg, this, true
        )
        mBinding.apply {
            helper = QuickAdapterHelper.Builder(expressionAdapter)
                .build()
            this.rvExpression.adapter = helper.adapter
            expressionAdapter.setOnItemClickListener { _, _, position ->
                val item = expressionAdapter.getItem(position) ?: return@setOnItemClickListener
                if (!item.isTxtDesc && !TextUtils.isEmpty(item.iconPath)) {
                    if (item.type == Type.NORMAL){
                        EmojiUserCacheManager.saveEmojiInfo(item)
                    }
                    onMessageSendListener?.onSendEmoji(item)
                }
            }
        }
    }

    private var emojiType: Type? = null
    private var hasShowRecent: Boolean = false
    fun setEmojiData(emojiList: MutableList<EaseEmojicon>, type: Type) {
        val layoutManager = mBinding.rvExpression.layoutManager as GridLayoutManager
        var allList: MutableList<EaseEmojicon> = mutableListOf()

        if (type == Type.NORMAL) {
            layoutManager.spanCount = 7
            allList = getRecentUserList()
        } else {
            layoutManager.spanCount = 4
        }
        mBinding.rvExpression.layoutManager = layoutManager
        allList.addAll(emojiList)
        expressionAdapter.submitList(allList)
        emojiType = type
    }

    private fun getRecentUserList(): MutableList<EaseEmojicon> {
        val allList: MutableList<EaseEmojicon> = mutableListOf()
        val recentEmojiList = EmojiUserCacheManager.getRecentEmojiList()?.toMutableList()
        if (!recentEmojiList.isNullOrEmpty()) {
            val emojicon = EaseEmojicon()
            emojicon.emojiText = "最近使用"
            emojicon.emojiType = 3
            allList.add(emojicon)
            if (recentEmojiList.size < 7) {
                for (i in 0..<7 - recentEmojiList.size) {
                    recentEmojiList.add(EaseEmojicon(0, ""))
                }
            }
            allList.addAll(recentEmojiList)
            val emojicon2 = EaseEmojicon()
            emojicon2.emojiText = "所有表情"
            emojicon2.emojiType = 4
            allList.add(emojicon2)
            hasShowRecent = true
        }
        return allList;
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateRecentUserEmoji()
    }

    private fun updateRecentUserEmoji() {
        if (emojiType != null && emojiType==Type.NORMAL) {
            if (hasShowRecent) {
                val recentEmojiList = EmojiUserCacheManager.getRecentEmojiList()?.toMutableList()
                if (!recentEmojiList.isNullOrEmpty()) {
                    for (i in 0..<recentEmojiList.size) {
                        val finalPosition = i + 1
                        val get = recentEmojiList[i]
                        expressionAdapter[finalPosition] = get
                    }
                }
            } else {
                val recentUserList = getRecentUserList()
                expressionAdapter.addAll(0, recentUserList)
            }
        }
    }


    private var onMessageSendListener: OnMessageSendListener? = null
    fun registerOnSendEmojiListener(onSelectListener: OnMessageSendListener) {
        onMessageSendListener = onSelectListener
    }

    interface OnMessageSendListener {
        fun onSendEmoji(dataBean: EaseEmojicon)
    }

}