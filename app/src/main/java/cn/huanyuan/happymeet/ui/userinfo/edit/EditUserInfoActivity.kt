package cn.huanyuan.happymeet.ui.userinfo.edit

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import android.view.View
import cn.huanyuan.happymeet.R
import cn.huanyuan.happymeet.databinding.ActivityEditUserinfoBinding
import cn.huanyuan.happymeet.ui.userinfo.UserViewModel
import cn.huanyuan.happymeet.ui.userinfo.adapter.EditUserInfoAdapter
import cn.huanyuan.happymeet.ui.userinfo.adapter.EditUserInfoHeadAdapter
import cn.huanyuan.happymeet.ui.userinfo.adapter.EditUserItemAdapter
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.func.photo.ImageSelectUtils
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.baselib.view.TitleBar
import cn.yanhu.commonres.bean.EditIUserItemInfo
import cn.yanhu.commonres.bean.EditUserInfo
import cn.yanhu.commonres.bean.UserInfoItem
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.manager.ImageSelectManager
import cn.yanhu.commonres.manager.RequestCodeManager
import cn.yanhu.commonres.pop.CommonWheelView2Pop
import cn.yanhu.commonres.pop.CommonWheelViewPop
import cn.zj.netrequest.ext.parseState
import cn.zj.netrequest.upload.UploadFileClient
import cn.zj.netrequest.upload.UploadFileProgressListener
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ThreadUtils
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.QuickAdapterHelper
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import java.util.ArrayList

/**
 * @author: zhengjun
 * created: 2024/3/11
 * desc:
 */
class EditUserInfoActivity : BaseActivity<ActivityEditUserinfoBinding, UserViewModel>(
    R.layout.activity_edit_userinfo,
    UserViewModel::class.java
) {
    private lateinit var helper: QuickAdapterHelper
    private val editBasicUserInfoAdapter by lazy { EditUserInfoAdapter() }
    private val detailUserInfoAdapter by lazy { EditUserInfoAdapter(false) }
    private val friendInfoAdapter by lazy { EditUserInfoAdapter(false) }
    private val editUserInfoHeadAdapter by lazy { EditUserInfoHeadAdapter(this) }
    private var editInfo: EditUserInfo? = null
     var hasChange:Boolean = false
    override fun initData() {
        setStatusBarStyle(false)
        initBasicAdapter()
        addHeadAdapter()
        requestData()
    }

    override fun initListener() {
        super.initListener()
        mBinding.titleBar.setTitleButtonOnClickListener(object : TitleBar.TitleButtonOnClickListener{
            override fun leftButtonOnClick(v: View?) {
                if (hasChange){
                    DialogUtils.showConfirmDialog("您的个人资料已修改，退出前要保存吗？",{
                        saveUserInfo(true)
                    },{
                       finish()
                    }, cancel = "不保存", confirm = "保存")
                }else{
                    finish()
                }
            }
            override fun rightButtonOnClick(v: View?) {
                saveUserInfo()
            }

        })
    }


    private fun saveUserInfo(isFinish:Boolean = false){
        if (isFinish){
            finish()
        }
    }

    private fun initBasicAdapter() {
        helper = QuickAdapterHelper.Builder(editBasicUserInfoAdapter)
            .build()
        mBinding.rvUserInfo.itemAnimator?.changeDuration = 0
        mBinding.rvUserInfo.adapter = helper.adapter
        editBasicUserInfoAdapter.addOnItemChildClickListener(
            R.id.iv_more
        ) { _, _, position ->
            editBasicUserInfoAdapter.notifyItemChanged(position, true)
            addDetailUserAdapter()
            addFriendInfoAdapter()
        }
        editBasicUserInfoAdapter.registerOnEdiItemClickListener(object :
            EditUserInfoAdapter.OnEditItemClickListener {
            override fun onItemClick(
                adapter: BaseQuickAdapter<UserInfoItem, *>,
                view: View,
                position: Int
            ) {
                selectBasicUserInfo(adapter, position)
            }

        })
    }

    private fun addFriendInfoAdapter() {
        helper.addAfterAdapter(friendInfoAdapter)
        friendInfoAdapter.registerOnEdiItemClickListener(object :
            EditUserInfoAdapter.OnEditItemClickListener {
            override fun onItemClick(
                adapter: BaseQuickAdapter<UserInfoItem, *>,
                view: View,
                position: Int
            ) {
                selectBasicUserInfo(adapter, position)
            }

        })
    }

    private fun addDetailUserAdapter() {
        helper.addAfterAdapter(detailUserInfoAdapter)
        detailUserInfoAdapter.registerOnEdiItemClickListener(object :
            EditUserInfoAdapter.OnEditItemClickListener {
            override fun onItemClick(
                adapter: BaseQuickAdapter<UserInfoItem, *>,
                view: View,
                position: Int
            ) {
                selectBasicUserInfo(adapter, position)
            }

        })
    }

    private var editPosition: Int = 0
    private var editAdapter: EditUserItemAdapter? = null

    /**
     * 选择基本信息
     */
    private fun selectBasicUserInfo(
        adapter: BaseQuickAdapter<UserInfoItem, *>,
        position: Int
    ) {
        val item = adapter.getItem(position) ?: return
        editAdapter = adapter as EditUserItemAdapter?
        editPosition = position
        when (item.type) {
            UserParamType.TYPE_NICKNAME.type -> {
                EditInputActivity.lunch(mContext, EditInputActivity.TYPE_INPUT_NAME, item.value)
            }

            UserParamType.TYPE_AGE.type -> {
                val list = mutableListOf<String>()
                for (i in 18..70) {
                    list.add(i.toString())
                }
                showSelectItemInfoPop(item, list,if (TextUtils.isEmpty(item.value)) "20" else item.value)
            }

            UserParamType.TYPE_HEIGHT.type -> {
                val list = mutableListOf<String>()
                for (i in 144..200) {
                    list.add(i.toString() + "cm")
                }
                showSelectItemInfoPop(item, list,if (TextUtils.isEmpty(item.value)) "155cm" else item.value)
            }

            UserParamType.TYPE_WEIGHT.type -> {
                val list = mutableListOf<String>()
                for (i in 30..200) {
                    list.add(i.toString() + "kg")
                }
                showSelectItemInfoPop(item, list,if (TextUtils.isEmpty(item.value)) "50kg" else item.value)
            }

            UserParamType.TYPE_EDUCATION.type -> {
                val stringArray =
                    StringUtils.getStringArray(cn.yanhu.commonres.R.array.education)
                showSelectItemInfoPop(item, stringArray.toMutableList())
            }

            UserParamType.TYPE_MARRIAGE.type -> {
                val stringArray =
                    StringUtils.getStringArray(cn.yanhu.commonres.R.array.marriage)
                showSelectItemInfoPop(item, stringArray.toMutableList(),if (TextUtils.isEmpty(item.value)) "未婚" else item.value)
            }

            UserParamType.TYPE_ADDRESS.type -> {
                val stringArray =
                    StringUtils.getStringArray(cn.yanhu.commonres.R.array.address_list)
                showSelectItemInfoPop(item, stringArray.toMutableList())
            }

            UserParamType.TYPE_WORK.type -> {
                val stringArray =
                    StringUtils.getStringArray(cn.yanhu.commonres.R.array.work)
                showSelectItemInfoPop(item, stringArray.toMutableList())

            }

            UserParamType.TYPE_MONTY_INCOME.type -> {
                val stringArray =
                    StringUtils.getStringArray(cn.yanhu.commonres.R.array.income)
                showSelectItemInfoPop(item, stringArray.toMutableList())
            }

            UserParamType.TYPE_HOMETOWN.type -> {
                val stringArray =
                    StringUtils.getStringArray(cn.yanhu.commonres.R.array.address_list)
                showSelectItemInfoPop(item, stringArray.toMutableList())
            }

            UserParamType.TYPE_LIVE_ROOM.type -> {
                val stringArray =
                    StringUtils.getStringArray(cn.yanhu.commonres.R.array.live_room)
                showSelectItemInfoPop(item, stringArray.toMutableList())
            }

            UserParamType.TYPE_CAR.type -> {
                val stringArray =
                    StringUtils.getStringArray(cn.yanhu.commonres.R.array.car)
                showSelectItemInfoPop(item, stringArray.toMutableList())
            }

            UserParamType.TYPE_PERSONALITY.type -> {

            }

            UserParamType.TYPE_INTEREST.type -> {

            }

            UserParamType.TYPE_LOVE_TARGET.type -> {
                val stringArray =
                    StringUtils.getStringArray(cn.yanhu.commonres.R.array.love_target)
                showSelectItemInfoPop(item, stringArray.toMutableList())
            }

            UserParamType.TYPE_FRIEND_AGE.type -> {
                showSelectRangeAge(item)
            }

            UserParamType.TYPE_FRIEND_ADDRESS.type -> {
                val stringArray =
                    StringUtils.getStringArray(cn.yanhu.commonres.R.array.address_list).toMutableList()
                stringArray.add(0,"不限")
                showSelectItemInfoPop(item, stringArray)
            }

            UserParamType.TYPE_FRIEND_HEIGHT.type -> {
                showSelectHeightRange(item)
            }

            UserParamType.TYPE_FRIEND_MIN_EDUCATION.type -> {
                val stringArray =
                    StringUtils.getStringArray(cn.yanhu.commonres.R.array.education).toMutableList()
                stringArray.add(0,"不限")
                showSelectItemInfoPop(item, stringArray)
            }

            UserParamType.TYPE_FRIEND_MIN_INCOME.type -> {
                val stringArray =
                    StringUtils.getStringArray(cn.yanhu.commonres.R.array.income).toMutableList()
                stringArray.add(0,"不限")
                showSelectItemInfoPop(item, stringArray)
            }
        }
    }

    private fun showSelectHeightRange(item: UserInfoItem) {
        val list = mutableListOf<String>()
        for (i in 144..200) {
            list.add(i.toString())
        }
        list.add(0,"不限")
        val heightRange = item.value
        val value = if (!TextUtils.isEmpty(heightRange)) {
            if (heightRange.contains("cm以下")){
                val lists = mutableListOf<String>()
                lists.add("不限")
                lists.add(heightRange.replace("cm以下",""))
                lists
            }else if (heightRange.contains("cm以上")){
                val lists = mutableListOf<String>()
                lists.add(heightRange.replace("cm以上",""))
                lists.add("不限")
                lists
            }else{
                val replace = heightRange.replace("cm", "")
                replace.split("-")
            }
        } else {
            mutableListOf()
        }
        CommonWheelView2Pop.showDialog(
            mContext,
            list,
            list,
            if (value.size == 2) value[0].trim() else "",
            if (value.size == 2) value[1].trim() else "",
            object : CommonWheelView2Pop.OnSelectWheelListener {
                override fun onSelectValue(value: String, value2: String) {
                    val selectAge = if (value=="不限"){
                        if(value2 == "不限"){
                            "不限"
                        }else{
                            value2+"cm以下"
                        }
                    }else{
                        if(value2 == "不限"){
                            value+"cm以上"
                        }else{
                            if (CommonUtils.compareString(value, value2)) {
                                "$value2 - $value"+ "cm"
                            } else {
                                "$value - $value2"+ "cm"
                            }
                        }
                    }
                    item.value = selectAge
                    updateItemInfo()
                }
            })
    }

    private fun showSelectRangeAge(item: UserInfoItem) {
        val list = mutableListOf<String>()
        for (i in 18..70) {
            list.add(i.toString())
        }
        val aageRange = item.value
        val value = if (!TextUtils.isEmpty(aageRange)) {
            val replace = aageRange.replace("岁", "")
            replace.split("-")
        } else {
            mutableListOf()
        }
        CommonWheelView2Pop.showDialog(
            mContext,
            list,
            list,
            if (value.size == 2) value[0].trim() else "",
            if (value.size == 2) value[1].trim() else "",
            object : CommonWheelView2Pop.OnSelectWheelListener {
                override fun onSelectValue(value: String, value2: String) {
                    val selectAge = if (CommonUtils.compareString(value, value2)) {
                        "$value2 - $value"
                    } else {
                        "$value - $value2"
                    }
                    item.value = selectAge + "岁"
                    updateItemInfo()
                }
            })
    }

    private fun showSelectItemInfoPop(item: UserInfoItem, list: MutableList<String>,selectValue:String?=null) {
        CommonWheelViewPop.showDialog(
            mContext,
            list,
            if(TextUtils.isEmpty(selectValue)) item.value else selectValue!!,
            object : CommonWheelViewPop.OnSelectWheelListener {
                override fun onSelectValue(value: String) {
                    item.value = value
                    updateItemInfo()
                }
            })
    }

    private fun addHeadAdapter() {
        helper.addBeforeAdapter(editUserInfoHeadAdapter)
        editUserInfoHeadAdapter.addOnItemChildClickListener(
            R.id.iv_user_portrait
        ) { _, _, _ ->
            ImageSelectManager.selectPic(
                mContext,
                type = ImageSelectUtils.TYPE_IMAGE,
                call = object : OnResultCallbackListener<LocalMedia> {
                    override fun onResult(result: ArrayList<LocalMedia>?) {
                        result?.apply {
                            val availablePath = result[0].availablePath
                            uploadAvatar(availablePath)
                        }
                    }

                    override fun onCancel() {
                    }
                })
        }
        editUserInfoHeadAdapter.addOnItemChildClickListener(
            R.id.viewBg
        ) { _, _, _ ->
            EditInputActivity.lunch(
                mContext,
                EditInputActivity.TYPE_INPUT_DESC,
                editInfo?.description
            )
        }
    }

    /**
     * 上传头像
     */
    private fun uploadAvatar(path: String) {
        hasChange = true
        val portraitEditInfo = editInfo!!.getPortraitEditInfo()
        portraitEditInfo.url = path
        UploadFileClient.uploadFile(
            path,
            object : UploadFileProgressListener {
                override fun onProgress(hasWrittenLen: Long, totalLen: Long) {
                    ThreadUtils.getMainHandler().post {
                        var progress = (hasWrittenLen * 100 / totalLen).toInt()
                        if (progress == 100) {
                            progress = 99
                        }
                        portraitEditInfo.progress = progress
                        editUserInfoHeadAdapter.notifyItemChanged(
                            0,
                            EditUserInfoHeadAdapter.TYPE_UPDATE_AVATAR
                        )
                    }
                }

                override fun onUploadSuccess(url: String) {
                    ThreadUtils.getMainHandler().post {
                        portraitEditInfo.progress = 100
                        editUserInfoHeadAdapter.notifyItemChanged(
                            0,
                            EditUserInfoHeadAdapter.TYPE_UPDATE_AVATAR
                        )
                        ThreadUtils.getMainHandler().postDelayed({
                            editInfo?.portrait = url
                            portraitEditInfo.url = url
                        }, 150)
                    }
                }

                override fun onUploadFail(msg: String) {
                    showToast(msg)
                }
            })
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getEditInfo()
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.editInfoObservable.observe(this) { it ->
            parseState(it, {
                editInfo = it

                editUserInfoHeadAdapter.item = it

                val info = EditIUserItemInfo("基本信息", it.basicInfo)
                editBasicUserInfoAdapter.item = info

                val info2 = EditIUserItemInfo("详细信息", it.detailInfo)
                detailUserInfoAdapter.item = info2

                val info3 = EditIUserItemInfo("征友条件", it.friendInfo)
                friendInfoAdapter.item = info3

            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == RequestCodeManager.REQUEST_CODE_INPUT_CONTENT) {
                data?.apply {
                    val type = getIntExtra(IntentKeyConfig.TYPE, -1)
                    val content = getStringExtra(IntentKeyConfig.DATA).toString()
                    when (type) {
                        EditInputActivity.TYPE_INPUT_NAME -> {
                            //更新昵称
                            editAdapter?.getItem(editPosition)?.value = content
                            updateItemInfo()
                        }

                        EditInputActivity.TYPE_INPUT_DESC -> {
                            //更新交友心声内容
                            editUserInfoHeadAdapter.item?.description = content
                            editUserInfoHeadAdapter.notifyItemChanged(
                                0,
                                EditUserInfoHeadAdapter.TYPE_UPDATE_DESC
                            )
                            hasChange = true
                        }
                    }
                }
            }
        }
    }

    private fun updateItemInfo() {
        hasChange = true
        editAdapter?.notifyItemChanged(editPosition, true)
    }
}