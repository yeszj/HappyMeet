package cn.yanhu.commonres.bean

import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import cn.zj.netrequest.BR
import java.io.Serializable

/**
 * @author: zhengjun
 * created: 2024/4/1
 * desc:
 */
class RoomDetailInfo : RoomListBean(), Serializable, Observable {
    var uuid: String = ""
    var uid: String = ""
    var agoraToken: String = ""

    @Bindable
    var autoSeat: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.autoSeat)
        }

    var welcomeMsg: String = ""
    var manApplyInfo: ApplyUserInfo = ApplyUserInfo()
    var womanApplyInfo: ApplyUserInfo = ApplyUserInfo()
    var admin: Int = 0
    var roomSeatResList: MutableList<RoomSeatInfo> = mutableListOf()

    class ApplyUserInfo : Serializable {
        var applyNum: Int = 0
        var onlineNum: Int = 1
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