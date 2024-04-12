package cn.yanhu.imchat.custom.chat

import android.content.Context
import android.net.Uri
import android.view.View
import cn.yanhu.baselib.func.photo.ImageSelectUtils
import cn.yanhu.commonres.manager.ImageSelectManager
import com.blankj.utilcode.util.UriUtils
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.netease.yunxin.kit.chatkit.ui.IChatInputMenu
import com.netease.yunxin.kit.chatkit.ui.`fun`.page.fragment.FunChatTeamFragment
import com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants
import com.netease.yunxin.kit.common.ui.action.ActionItem
import java.util.ArrayList

/**
 * @author: zhengjun
 * created: 2024/3/27
 * desc:
 */
class CustomFunChatTeamFragment: FunChatTeamFragment() {

    override fun initView() {
        super.initView()
        viewInitListener?.initFinish()
    }

    override fun initViewModel() {
        super.initViewModel()
        registerChatInputMenu()
    }

    /**
     * 监听聊天输入框下方按钮的事件
     */
    private fun registerChatInputMenu() {
        this.chatConfig.chatInputMenu = object : IChatInputMenu {
            override fun customizeInputMore(actionItemList: MutableList<ActionItem>): MutableList<ActionItem> {
                for (i in actionItemList.count() - 1 downTo 0) {
                    val action = actionItemList[i].action
                    if (ActionConstants.ACTION_TYPE_LOCATION == action || ActionConstants.ACTION_TYPE_FILE == action) {
                        actionItemList.removeAt(i)
                    }
                }
                return actionItemList
            }

            override fun onInputClick(context: Context?, view: View?, action: String?): Boolean {
                //处理自带菜单点击事件
                if (action == ActionConstants.ACTION_TYPE_ALBUM) {
                    //点击相册
                    selectImage(ImageSelectUtils.TYPE_ALL)
                    return true
                } else if (action == ActionConstants.ACTION_TYPE_CAMERA) {
                    //点击拍摄
                    selectImage(ImageSelectUtils.TYPE_CAMERA)
                    return true
                }
                return super.onInputClick(context, view, action)
            }

            override fun onCustomInputClick(
                context: Context,
                view: View,
                action: String
            ): Boolean {
                //处理自定义菜单点击事件
                return super.onCustomInputClick(context, view, action)
            }
        }
    }


    private fun selectImage(type:Int) {
        ImageSelectManager.selectPic(
            requireActivity(),
            false,
            type = type,
            call = object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: ArrayList<LocalMedia>?) {
                    val uriList: MutableList<Uri> = mutableListOf()
                    result?.forEach {
                        val uri = UriUtils.res2Uri(it.availablePath)
                        uriList.add(uri)
                    }
                    this@CustomFunChatTeamFragment.onPickMedia(uriList)
                }

                override fun onCancel() {
                }
            })
    }

    private var viewInitListener: OnViewInitListener?=null
    fun setInitListener(initListener: OnViewInitListener){
        viewInitListener = initListener
    }

    interface OnViewInitListener{
        fun initFinish()
    }

}