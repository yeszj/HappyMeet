package cn.yanhu.commonres.bean

import android.text.TextUtils
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import cn.zj.netrequest.BR
import com.smart.adapter.interf.SmartFragmentTypeExEntity
import java.io.Serializable

/**
 * @author: zhengjun
 * created: 2024/2/18
 * desc:
 */
open class RoomListBean : SmartFragmentTypeExEntity(), Serializable, Observable {
    var id = 0
    var coverUrl: String? = null
    var roomName: String = ""

    @Bindable
    var roomType = 0 //1-视频相亲-大厅 2-专属交友 0-用户 3-7人交友 4-7人天使 5-9人房
        set(value) {
            field = value
            notifyPropertyChanged(BR._all)
        }
    var isFullSeat = false
    var ownerInfo: UserDetailInfo? = null
    var seatInfo: BaseUserInfo? = null
    var roomPortraitList: List<String> = mutableListOf()
    var roomId: String? = ""
    val banners: MutableList<BannerBean> = mutableListOf()

    fun getRoomTypeName():String{
        return when (roomType) {
            0 -> {
                "对方在线"
            }
            TYPE_PRIVATE -> {
                "专属交友"
            }
            TYPE_SEVEN_FRIEND -> {
                "七人交友"
            }
            TYPE_SEVEN_ANGLE -> {
                "七人天使"
            }
            TYPE_NINE_FRIEND -> {
                "九人交友"
            }
            TYPE_NINE_ANGLE -> {
                "九人天使"
            }
            else -> {
                "视频相亲"
            }
        }
    }

    fun getBaseInfo(): String {
        return if (seatInfo != null) {
            "${seatInfo!!.age}岁 " + if (TextUtils.isEmpty(seatInfo!!.province)) "" else seatInfo!!.province
        } else {
            "${ownerInfo!!.age}岁 " + if (TextUtils.isEmpty(ownerInfo!!.province)) "" else ownerInfo!!.province
        }
    }

    override fun getFragmentType(): Int {
        return when (roomType) {
            TYPE_PUBLIC, TYPE_PRIVATE -> {
                FRG_THREE_ROOM
            }

            TYPE_SEVEN_FRIEND, TYPE_SEVEN_ANGLE -> {
                FRG_SEVEN_ROOM
            }

            TYPE_NINE_FRIEND,TYPE_NINE_ANGLE -> {
                FRG_NINE_ROOM
            }

            else -> FRG_OTHER_ROOM
        }
    }

    fun isSevenRoom():Boolean{
        return roomType == TYPE_SEVEN_FRIEND
    }

    fun isAngleRoom():Boolean{
        return roomType == TYPE_SEVEN_ANGLE || roomType == TYPE_NINE_ANGLE
    }

    fun isNineRoom():Boolean{
        return roomType == TYPE_NINE_FRIEND
    }

    fun isPrivateRoom(): Boolean {
        return roomType == TYPE_PRIVATE
    }


    companion object {
        const val FRG_THREE_ROOM = 1
        const val FRG_SEVEN_ROOM = 2
        const val FRG_NINE_ROOM = 3
        const val FRG_OTHER_ROOM = 10

        const val TYPE_PUBLIC = 1
        const val TYPE_PRIVATE= 2
        const val TYPE_SEVEN_FRIEND = 3
        const val TYPE_SEVEN_ANGLE = 4
        const val TYPE_NINE_FRIEND = 5
        const val TYPE_NINE_ANGLE = 6
    }

    @Transient
    private var mCallbacks: PropertyChangeRegistry? = null
    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        synchronized(this) {
            if (mCallbacks == null) {
                mCallbacks = PropertyChangeRegistry()
            }
        }
        mCallbacks?.add(callback)
    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        synchronized(this) {
            if (mCallbacks == null) {
                return
            }
        }
        mCallbacks?.remove(callback)
    }

    /**
     * Notifies listeners that all properties of this instance have changed.
     */
    open fun notifyChange() {
        synchronized(this) {
            if (mCallbacks == null) {
                return
            }
        }
        mCallbacks?.notifyCallbacks(this, 0, null)
    }

    /**
     * Notifies listeners that a specific property has changed. The getter for the property
     * that changes should be marked with [Bindable] to generate a field in
     * `BR` to be used as `fieldId`.
     *
     * @param fieldId The generated BR id for the Bindable field.
     */
    open fun notifyPropertyChanged(fieldId: Int) {
        synchronized(this) {
            if (mCallbacks == null) {
                return
            }
        }
        mCallbacks?.notifyCallbacks(this, fieldId, null)
    }
}