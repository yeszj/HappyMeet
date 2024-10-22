package cn.yanhu.agora.ui.beautifyFace

import android.Manifest
import android.annotation.SuppressLint
import android.view.View
import android.widget.SeekBar
import cn.yanhu.agora.R
import cn.yanhu.agora.adapter.BeautyFaceSetAdapter
import cn.yanhu.agora.bean.BeautyBean
import cn.yanhu.agora.databinding.ActivityBeautyFaceSetBinding
import cn.yanhu.agora.manager.AgoraManager
import cn.yanhu.agora.manager.BeautySetManager
import cn.yanhu.agora.ui.liveRoom.LiveRoomViewModel
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.baselib.view.TitleBar
import cn.yanhu.commonres.router.RouterPath
import cn.yanhu.commonres.utils.PermissionXUtils
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ThreadUtils
import com.chad.library.adapter4.BaseQuickAdapter
import com.google.gson.Gson


/**
 * 美颜设置页面
 */
@Route(path = RouterPath.ROUTER_BEAUTIFUL_FACE)
class BeautyFaceSetActivity : BaseActivity<ActivityBeautyFaceSetBinding, LiveRoomViewModel>(
    R.layout.activity_beauty_face_set,
    LiveRoomViewModel::class.java
)  {
    private val beautyFaceSetAdapter by lazy { BeautyFaceSetAdapter() }
    private var beautyAllList: MutableList<BeautyBean> = mutableListOf()
    private var beautyList: MutableList<BeautyBean> = mutableListOf()
    private var selectFacePosition = -1
    private var selectBeautyTypePosition = -1
    private var isBeautyFace = true
    override fun initData() {
        setFullScreenStatusBar()
        mBinding.alertkey = ""
        checkPermission()
        mBinding.rvBeauty.adapter = beautyFaceSetAdapter
        ThreadUtils.executeByIo(object : ThreadUtils.SimpleTask<Boolean>() {
            override fun doInBackground(): Boolean {
                beautyAllList = BeautySetManager.getInstance().beautyList
                return true
            }
            override fun onSuccess(result: Boolean?) {
                beautyList = beautyAllList.subList(0, 5)
                beautyFaceSetAdapter.submitList(beautyList)
            }
        })

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun initListener() {
        super.initListener()
        addAdapterItemListener()
        addOnSeekBarListener()
        mBinding.beautySetRefresh.setOnSingleClickListener {
            showRefreshDialog()
        }
        mBinding.beautySetSkin.setOnSingleClickListener {
            //美颜
            switchToBeautyFace()
        }
        mBinding.beautySetMking.setOnSingleClickListener {
            //美型
            switchToBeautyType()
        }
        initTitleListener()
    }

    private fun initTitleListener() {
        mBinding.titleBar.setTitleButtonOnClickListener(object :
            TitleBar.TitleButtonOnClickListener {
            override fun leftButtonOnClick(v: View?) {
                showExitDialog()
            }

            override fun rightButtonOnClick(v: View?) {
                //保存
                SPUtils.getInstance().put("beautySetList", Gson().toJson(beautyAllList))
                showToast("保存成功")
                finish()
            }
        })
    }

    private fun showExitDialog() {
        DialogUtils.showConfirmDialog("确定退出美颜设置吗？", {
            finish()
        }, cancel = "取消")
    }

    private fun switchToBeautyType() {
        isBeautyFace = false
        mBinding.beautySetMking.setTextColor(CommonUtils.getColor(cn.yanhu.baselib.R.color.femaleColor))
        mBinding.beautySetSkin.setTextColor(CommonUtils.getColor(cn.yanhu.baselib.R.color.white))
        beautyList = beautyAllList.subList(5, beautyAllList.size)
        beautyFaceSetAdapter.submitList(beautyList)
        mBinding.alertkey = ""
        beautyFaceSetAdapter.setSelectPosition(selectBeautyTypePosition)
        if (selectBeautyTypePosition != -1) {
            val beautyBean = beautyList[selectBeautyTypePosition]
            setSeekProgress(beautyBean)
        }
    }

    private fun switchToBeautyFace() {
        isBeautyFace = true
        mBinding.beautySetSkin.setTextColor(CommonUtils.getColor(cn.yanhu.baselib.R.color.femaleColor))
        mBinding.beautySetMking.setTextColor(CommonUtils.getColor(cn.zj.netrequest.R.color.white))
        beautyList = beautyAllList.subList(0, 5)
        beautyFaceSetAdapter.submitList(beautyList)
        mBinding.alertkey = ""
        beautyFaceSetAdapter.setSelectPosition(selectFacePosition)
        if (selectFacePosition != -1) {
            val beautyBean = beautyList[selectFacePosition]
            setSeekProgress(beautyBean)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showRefreshDialog() = DialogUtils.showConfirmDialog("是否恢复所有参数？", {
        for (beautyBean in BeautySetManager.getInstance().beautyList) {
            for (bean in beautyAllList) {
                if (beautyBean.key.equals(bean.key)) {
                    bean.value = beautyBean.value
                }
            }
            if (mBinding.alertkey.equals(beautyBean.key)) {
                mBinding.tvProgressValue.text = beautyBean.value.toString()
                mBinding.seekBar.progress = beautyBean.value
            }
            BeautySetManager.getInstance()
                .setBeautyProperty(beautyBean.key, beautyBean.value)
        }
        beautyFaceSetAdapter.notifyDataSetChanged()
    }, cancel = "取消")

    private fun addOnSeekBarListener() {
        mBinding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                mBinding.tvProgressValue.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                BeautySetManager.getInstance()
                    .setBeautyProperty(mBinding.alertkey, seekBar!!.progress)
                for (beautyBean in beautyList) {
                    if (beautyBean.key.equals(mBinding.alertkey)) {
                        beautyBean.value = seekBar.progress
                    }
                }
            }

        })
    }

    private fun addAdapterItemListener() {
        beautyFaceSetAdapter.setOnItemClickListener(object :
            BaseQuickAdapter.OnItemClickListener<BeautyBean> {
            override fun onClick(
                adapter: BaseQuickAdapter<BeautyBean, *>,
                view: View,
                position: Int
            ) {
                if (isBeautyFace) {
                    selectFacePosition = position
                } else {
                    selectBeautyTypePosition = position
                }
                val item = beautyFaceSetAdapter.getItem(position) ?: return
                beautyFaceSetAdapter.setSelectPosition(position)
                setSeekProgress(item)
            }
        })
    }

    private fun setSeekProgress(item: BeautyBean) {
        mBinding.alertkey = item.key
        mBinding.seekBar.progress = item.value
        mBinding.tvProgressValue.text = item.value.toString()
    }

    private fun checkPermission() {
        val permissions = ArrayList<String>()
        permissions.add(Manifest.permission.CAMERA)
        permissions.add(Manifest.permission.RECORD_AUDIO)
        PermissionXUtils.checkPermission(
            mContext,
            permissions,
            "对爱交友想访问您的以下权限，用于美颜设置",
            "您拒绝授权权限，将无法体验部分功能",
            object : PermissionXUtils.PermissionListener {
                override fun onSuccess() {
                    AgoraManager.getInstence()
                        .init(mContext,  1, mBinding.beautySetSf)
                }

                override fun onFail() {
                }

            })
    }


    override fun back() {
        showExitDialog()
    }


    override fun exactDestroy() {
        super.exactDestroy()
        AgoraManager.getInstence().onDestory()
    }

}