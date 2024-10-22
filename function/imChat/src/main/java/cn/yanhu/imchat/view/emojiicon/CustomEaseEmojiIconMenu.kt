package cn.yanhu.imchat.view.emojiicon

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.hyphenate.easeui.domain.EaseEmojicon
import com.hyphenate.easeui.domain.EaseEmojiconGroupEntity
import com.hyphenate.easeui.modules.chat.interfaces.EaseEmojiconMenuListener
import com.hyphenate.easeui.modules.chat.interfaces.IChatEmojiconMenu
import cn.yanhu.imchat.R
/**
 * @author: zhengjun
 * created: 2024/5/22
 * desc:
 */
class CustomEaseEmojiIconMenu @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), IChatEmojiconMenu {
    private var emojiconColumns = 0
    private var bigEmojiconColumns = 0
    private val tabBar: CustomEaseEmojiconScrollTabBar
    private val pagerView: CustomEaseEmojiconPagerView
    private val emojiconGroupList: MutableList<EaseEmojiconGroupEntity> = ArrayList()
    private var listener: EaseEmojiconMenuListener? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.ease_custom_widget_emojiicon, this)
        pagerView =
            findViewById<View>(com.hyphenate.easeui.R.id.pager_view) as CustomEaseEmojiconPagerView
        tabBar = findViewById<View>(com.hyphenate.easeui.R.id.tab_bar) as CustomEaseEmojiconScrollTabBar
        initAttrs(context, attrs)
    }

    @SuppressLint("CustomViewStyleable")
    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        val ta =
            context.obtainStyledAttributes(attrs, com.hyphenate.easeui.R.styleable.EaseEmojiconMenu)
        emojiconColumns = ta.getInt(
            com.hyphenate.easeui.R.styleable.EaseEmojiconMenu_emojiconColumns,
            defaultColumns
        )
        bigEmojiconColumns = ta.getInt(
            com.hyphenate.easeui.R.styleable.EaseEmojiconMenu_bigEmojiconRows,
            defaultBigColumns
        )
        ta.recycle()
    }

    @JvmOverloads
    fun init(groupEntities: MutableList<EaseEmojiconGroupEntity>? = null) {
        if (groupEntities != null && groupEntities.size > 0) {
            for (groupEntity in groupEntities) {
                emojiconGroupList.add(groupEntity)
                tabBar.addTab(groupEntity.iconPath)
            }
        }
        pagerView.setPagerViewListener(EmojiconPagerViewListener())
        pagerView.init(emojiconGroupList)
        tabBar.setTabBarItemClickListener { position -> pagerView.setGroupPostion(position) }
    }

    /**
     * add emojicon group
     * @param groupEntity
     */
    override fun addEmojiconGroup(groupEntity: EaseEmojiconGroupEntity) {
        emojiconGroupList.add(groupEntity)
        pagerView.addEmojiconGroup(groupEntity, true)
        tabBar.addTab(groupEntity.iconPath)
    }

    /**
     * add emojicon group list
     * @param groupEntitieList
     */
    override fun addEmojiconGroup(groupEntitieList: List<EaseEmojiconGroupEntity>) {
        for (i in groupEntitieList.indices) {
            val groupEntity = groupEntitieList[i]
            emojiconGroupList.add(groupEntity)
            pagerView.addEmojiconGroup(
                groupEntity,
                i == groupEntitieList.size - 1
            )
            tabBar.addTab(groupEntity.iconPath)
        }
    }

    /**
     * remove emojicon group
     * @param position
     */
    override fun removeEmojiconGroup(position: Int) {
        emojiconGroupList.removeAt(position)
        pagerView.removeEmojiconGroup(position)
        tabBar.removeTab(position)
    }

    override fun setTabBarVisibility(isVisible: Boolean) {
        if (!isVisible) {
            tabBar.visibility = GONE
        } else {
            tabBar.visibility = VISIBLE
        }
    }

    override fun setEmojiconMenuListener(listener: EaseEmojiconMenuListener) {
        this.listener = listener
    }

    private inner class EmojiconPagerViewListener : CustomEaseEmojiconPagerView.EaseEmojiconPagerViewListener {
        override fun onPagerViewInited() {
            tabBar.selectedTo(0)
        }

        override fun onGroupPositionChanged(groupPosition: Int) {
            tabBar.selectedTo(groupPosition)
        }

        override fun onGroupInnerPagePostionChanged(oldPosition: Int, newPosition: Int) {}
        override fun onGroupPagePostionChangedTo(position: Int) {}
        override fun onGroupMaxPageSizeChanged(maxCount: Int) {}
        override fun onDeleteImageClicked() {
            if (listener != null) {
                listener!!.onDeleteImageClicked()
            }
        }

        override fun onExpressionClicked(emojicon: EaseEmojicon) {
            if (listener != null) {
                listener!!.onExpressionClicked(emojicon)
            }
        }
    }

    companion object {
        private const val defaultColumns = 7
        private const val defaultBigColumns = 4
    }
}