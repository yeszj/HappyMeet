package cn.huanyuan.sweetlove.ui.main.tab_blinddate

import androidx.fragment.app.Fragment
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.FrgTabBlinddateBinding
import cn.huanyuan.sweetlove.net.rxApi
import cn.huanyuan.sweetlove.ui.main.MainViewModel
import cn.huanyuan.sweetlove.ui.userinfo.UserSearchActivity
import cn.huanyuan.sweetlove.ui.userinfo.auth.RealNameActivity
import cn.yanhu.agora.manager.LiveRoomManager
import cn.yanhu.baselib.adapter.MyFrgFragmentStateAdapter
import cn.yanhu.baselib.base.BaseFragment
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ViewPager2Helper
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.widget.indicator.CommonIndicatorAdapter
import cn.yanhu.commonres.bean.RoomListBean
import cn.yanhu.commonres.router.RouteIntent
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.blankj.utilcode.util.GsonUtils
import com.pcl.sdklib.bean.CheckBaiduFaceResult
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator

/**
 * @author: zhengjun
 * created: 2024/2/4
 * desc:
 */
class TabBlindDateFrg : BaseFragment<FrgTabBlinddateBinding, MainViewModel>(
    R.layout.frg_tab_blinddate,
    MainViewModel::class.java
) {
    override fun initData() {
        //mBinding.viewPager.desensitization()
        initTabLayout()
        initVpData()
    }

    override fun initListener() {
        super.initListener()
        mBinding.tvSearch.setOnSingleClickListener {
            UserSearchActivity.lunch(mContext)
        }
        mBinding.tvStart.setOnSingleClickListener {
            checkCanCreateRoom()
        }
    }

    var isChecking = false
    private fun checkCanCreateRoom() {
        if (isChecking){
            return
        }
        isChecking = true
        request({ rxApi.checkBaiduFace() },
            object : OnRequestResultListener<CheckBaiduFaceResult> {
                override fun onSuccess(data: BaseBean<CheckBaiduFaceResult>) {
                    isChecking = false
                    val roomConfigInfo = data.data ?: return
                    when (roomConfigInfo.authId) {
                        -1 -> {
                            val params = roomConfigInfo.params
                            val fromJson = GsonUtils.fromJson(params, RoomListBean::class.java)
                            LiveRoomManager.toLiveRoomPage(mContext, fromJson.roomId!!)
                        }

                        0 -> {
                            RouteIntent.lunchToCreateRoom(roomConfigInfo.params)
                        }

                        else -> {
                            DialogUtils.showConfirmDialog(
                                "开播提示",
                                {
                                    if (roomConfigInfo.authId == 1 || roomConfigInfo.authId == 2) {
                                        RealNameActivity.lunch(mContext, roomConfigInfo)
                                    }
                                },
                                {
                                },
                                "根据国家法律法规，需通过活体实名认证才可以开启直播哦",
                                confirm = "去认证"
                            )
                        }
                    }
                }

                override fun onFail(code: Int?, msg: String?) {
                    super.onFail(code, msg)
                    isChecking = false
                }
            })
    }

    private fun initTabLayout() {
        val magicIndicator = mBinding.tabLayout
        val commonNavigator = CommonNavigator(mContext)
        val list = mutableListOf("大厅")
        commonNavigator.adapter = CommonIndicatorAdapter(
            mBinding.viewPager,
            list.toTypedArray(),
            textSize = CommonUtils.getSpByDimen(com.zj.dimens.R.dimen.sp_18).toFloat(),
            paddingLeft = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_8)
        )
        commonNavigator.isAdjustMode = false
        magicIndicator.navigator = commonNavigator
        ViewPager2Helper.bind(magicIndicator, mBinding.viewPager)
    }

    private var frgList: MutableList<Fragment> = mutableListOf()
    private fun initVpData() {
        val fragments = childFragmentManager.fragments
        if (fragments.size>0){
            frgList = fragments
        }else{
            frgList.add(BlindUserOrRoomItemFrg.newsInstance(BlindUserOrRoomItemFrg.TYPE_RECOMMEND))
            //frgList.add(BlindDateUserRoomListFrg())
            // frgList.add(BlindUserOrRoomItemFrg.newsInstance(BlindUserOrRoomItemFrg.TYPE_FRIENDS))
        }
        mBinding.viewPager.adapter = MyFrgFragmentStateAdapter(this@TabBlindDateFrg, frgList)
        mBinding.viewPager.offscreenPageLimit = frgList.size
    }

}