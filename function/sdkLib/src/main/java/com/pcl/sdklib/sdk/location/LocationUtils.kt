package com.pcl.sdklib.sdk.location

import android.Manifest
import android.app.Activity
import android.content.Context
import android.location.LocationManager
import android.os.Looper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.config.EventBusKeyConfig
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.yanhu.commonres.utils.PermissionXUtils
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request2
import cn.zj.netrequest.status.BaseBean
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.github.gzuliyujiang.oaid.DeviceID
import com.pcl.sdklib.api.sdkRxApi
import com.tencent.map.geolocation.TencentLocation
import com.tencent.map.geolocation.TencentLocationListener
import com.tencent.map.geolocation.TencentLocationManager
import com.tencent.map.geolocation.TencentLocationRequest

/**
 * @author: zhengjun
 * created: 2022/9/13
 * desc:
 */
object LocationUtils {

    fun getTencentLocation(
        activity: FragmentActivity,
        locationResultListener: OnLocationResultListener?
    ) {
        if (LocationCacheManager.mapLocation !=null){
            locationResultListener?.onLocationResult(LocationCacheManager.mapLocation)
            return
        }
        //判断是否授权必要权限
        val permissions = ArrayList<String>()
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS)
        PermissionXUtils.checkPermission(
            activity,
            permissions,
            "为了基于地理位置向您推荐用户或动态，请授予我们获取位置的权限",
            "您拒绝授权权限，将无法体验部分功能",
            object : PermissionXUtils.PermissionListener {
                override fun onSuccess() {
                    getLocation(activity, locationResultListener)
                }

                override fun onFail() {
                    locationResultListener?.onLocationResult(null)
                }
            })
    }
    fun getTencentLocation(
        activity: Fragment,
        locationResultListener: OnLocationResultListener?
    ) {
        if (LocationCacheManager.mapLocation !=null){
            locationResultListener?.onLocationResult(LocationCacheManager.mapLocation)
            return
        }
        //判断是否授权必要权限
        val permissions = ArrayList<String>()
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS)
        PermissionXUtils.checkPermission(
            activity,
            permissions,
            "为了基于地理位置向您推荐用户或动态，请授予我们获取位置的权限",
            "您拒绝授权权限，将无法体验部分功能",
            object : PermissionXUtils.PermissionListener {
                override fun onSuccess() {
                    getLocation(activity.requireActivity(), locationResultListener)
                }

                override fun onFail() {
                    locationResultListener?.onLocationResult(null)
                }
            })
    }
    private fun getLocation(context: Activity, locationResultListener: OnLocationResultListener?) {

        if (isLocServiceEnable(context)) {
            //定位服务已开启
            context.runOnUiThread {
                TencentLocationManager.setUserAgreePrivacy(true)
                val mLocationManager =
                    TencentLocationManager.getInstance(context.applicationContext)
                mLocationManager.setDeviceID(context, DeviceID.getClientId())
                val request = TencentLocationRequest.create()
                request.requestLevel = TencentLocationRequest.REQUEST_LEVEL_ADMIN_AREA
                request.isAllowGPS = true
                request.isIndoorLocationMode = true
                // 设置定位模式
                val locMode =
                    TencentLocationRequest.HIGH_ACCURACY_MODE //高精度定位模式，将同时使用网络定位和卫星定位，优先返回精度高的定位
                request.locMode = locMode
                mLocationManager.requestSingleFreshLocation(request, object : TencentLocationListener {
                    override fun onLocationChanged(
                        aMapLocation: TencentLocation,
                        error: Int,
                        reason: String
                    ) {
                        //将经纬度保存到本地
                        if (error == TencentLocation.ERROR_OK) {
                            // 定位成功
                            LogUtils.aTag("locationResult", GsonUtils.toJson(aMapLocation))
                            AppCacheManager.province = aMapLocation.province
                            LocationCacheManager.mapLocation = aMapLocation
                            saveAddress(aMapLocation)
                            locationResultListener?.onLocationResult(aMapLocation)
                        } else {
                            // 定位失败
                            locationResultListener?.onLocationResult(null)
                            LogUtils.aTag("locationResult", "error=" + error + "reason=" + reason)
                        }
                        mLocationManager.removeUpdates(this)
                    }

                    override fun onStatusUpdate(name: String, status: Int, desc: String) {
                        if (status == 5) {
                            mLocationManager.removeUpdates(this)
                            locationResultListener?.onLocationResult(null)
                        }
                        LogUtils.aTag("locationResult", "status=" + status + "desc=" + desc)
                    }
                }, Looper.getMainLooper())
            }
        } else {
            //定位服务未开启 无法获取定位信息
            if (locationResultListener != null) {
                showToast("获取定位信息失败,请开启手机定位服务")
                locationResultListener.onLocationResult(null)
            }
        }
    }

    private fun saveAddress(aMapLocation: TencentLocation) {
        request2(
            { sdkRxApi.updatePersonalPageSingle(8, aMapLocation.province) },
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    LiveDataEventManager.sendLiveDataMessage(EventBusKeyConfig.REFRESH_USER_INFO,true)
                }
            }
        )
    }

    /**
     * 手机是否开启位置服务，如果没有开启那么所有app将不能使用定位功能
     */
    private fun isLocServiceEnable(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun hasLocationPermission(context: Context):Boolean{
        val permissions = ArrayList<String>()
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS)
        return isLocServiceEnable(context) && PermissionXUtils.hasPermission(permissions)
    }

    interface OnLocationResultListener {
        fun onLocationResult(aMapLocation: TencentLocation?)
    }
}