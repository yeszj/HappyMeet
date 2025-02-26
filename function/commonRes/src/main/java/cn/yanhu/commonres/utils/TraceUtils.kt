package cn.yanhu.commonres.utils

import cn.yanhu.commonres.api.commonRxApi
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request2
import cn.zj.netrequest.status.BaseBean

object TraceUtils  {
    fun onEventObject(key: String) {
        val map: MutableMap<String, String> = HashMap()
        map["event_key"] = key
        newEventTrace(map)
    }

    fun onEventObject(key: String, map: MutableMap<String, String>) {
        map["event_key"] = key
        newEventTrace(map)
    }

    fun onEventObject(eventKey: String, eventId: String) {
        val map: MutableMap<String, String> = HashMap()
        map["event_key"] = eventKey
        map["event_id"] = eventId
        newEventTrace(map)
    }

    private fun newEventTrace(map: Map<String, String?>) {
        request2({ commonRxApi.newEventTrace(map)},object : OnRequestResultListener<String>{
            override fun onSuccess(data: BaseBean<String>) {

            }

        })
    }
}