package cn.yanhu.imchat.ui.chatSetting

import android.content.Context
import android.content.Intent
import android.view.View
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.bean.ChatPriceItemInfo
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.pop.CommonWheelViewPop
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.imchat.ImChatViewModel
import cn.yanhu.imchat.R
import cn.yanhu.imchat.adapter.ChatPriceSetAdapter
import cn.yanhu.imchat.databinding.ActivityUserChatSettingBinding
import cn.yanhu.imchat.manager.ImUserManager
import cn.zj.netrequest.ext.parseState
import com.chad.library.adapter4.BaseQuickAdapter
import com.hyphenate.EMValueCallBack
import com.hyphenate.chat.EMClient


/**
 * @author: zhengjun
 * created: 2024/3/27
 * desc:
 */
@Suppress("DEPRECATION")
class UserChatSettingActivity : BaseActivity<ActivityUserChatSettingBinding, ImChatViewModel>(
    R.layout.activity_user_chat_setting,
    ImChatViewModel::class.java
) {
    private val priceSetAdapter by lazy { ChatPriceSetAdapter() }
    private var chatUserId:String = ""
    override fun initData() {
        setFullScreenStatusBar()
        setStatusBarStyle(false)
        val userInfo = intent.getSerializableExtra(IntentKeyConfig.DATA) as UserDetailInfo?
        userInfo?.apply {
            chatUserId = this.userId
            setUserInfo()
            setUseBlack()
            setStickTop()
        }
        initPriceSetAdapter()
        requestData()
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getPriceConfig(chatUserId.toInt())
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.priceConfigObservable.observe(this) { it ->
            parseState(it, {
                priceSetAdapter.submitList(it)
            })
        }
    }

    private fun initPriceSetAdapter(){
        mBinding.recyclerView.adapter = priceSetAdapter
        priceSetAdapter.setOnItemClickListener(object :
            BaseQuickAdapter.OnItemClickListener<ChatPriceItemInfo> {
            override fun onClick(
                adapter: BaseQuickAdapter<ChatPriceItemInfo, *>,
                view: View,
                position: Int
            ) {
                val item = priceSetAdapter.getItem(position) ?: return
                showSelectPricePop(item, position)
            }
        })
    }

    private fun showSelectPricePop(item: ChatPriceItemInfo, position: Int) {
        val list = mutableListOf<String>()
        var selectValue = ""
        item.list.forEach {
            list.add(it.desc)
            if (it.selected) {
                selectValue = it.desc
            }
        }
        CommonWheelViewPop.showDialog(
            mContext,
            list,
            selectValue,
            object : CommonWheelViewPop.OnSelectWheelListener {
                override fun onSelectValue(value: String) {
                    var selectId:Int = 0
                    item.list.forEach {
                        if (it.desc == selectValue) {
                            it.selected = false
                        } else if (it.desc == value) {
                            it.selected = true
                            selectId = it.id
                        }
                    }
                    mViewModel.setPrice(chatUserId.toInt(),item.type,selectId)
                    priceSetAdapter.notifyItemChanged(position)
                }
            })
    }

    override fun initListener() {
        super.initListener()
        mBinding.bgUser.setOnSingleClickListener {
            RouteIntent.lunchPersonHomePage(chatUserId)
        }
        mBinding.tvReport.setOnSingleClickListener {
            RouteIntent.lunchReportPage(chatUserId)
        }
    }

    private fun UserDetailInfo.setUserInfo() {
        GlideUtils.load(mContext, this.portrait, mBinding.ivAvatar)
        mBinding.tvNickName.text = this.nickName
    }

    private fun UserDetailInfo.setUseBlack() {
        val userId = this.userId
        EMClient.getInstance().contactManager().asyncGetBlackListFromServer(object :
            EMValueCallBack<List<String>> {
            override fun onSuccess(value: List<String>?) {
               val inBlack = value!=null && value.contains(userId)
                mBinding.toggleBlack.isChecked = inBlack
                mBinding.toggleBlack.setOnCheckedChangeListener { _, isChecked ->
                    ImUserManager.setUserBlack(isChecked, userId)
                }
            }
            override fun onError(error: Int, errorMsg: String?) {
            }
        })

    }


    private fun UserDetailInfo.setStickTop() {
        val stickTop = ImUserManager.isStickTop(this.userId)
        mBinding.toggleStickTop.isChecked = stickTop
        mBinding.toggleStickTop.setOnCheckedChangeListener { _, isChecked ->
            ImUserManager.setStickTop(
                isChecked,
                this@setStickTop.userId
            )
        }
    }

    companion object{
       fun lunch(context: Context,userInfo:UserDetailInfo?){
            val intent = Intent(context,UserChatSettingActivity::class.java)
           intent.putExtra(IntentKeyConfig.DATA,userInfo)
           context.startActivity(intent)
        }
    }
}