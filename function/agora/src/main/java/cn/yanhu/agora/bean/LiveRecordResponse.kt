package cn.yanhu.agora.bean

import cn.yanhu.commonres.bean.FilterInfo

/**
 * @author: zhengjun
 * created: 2024/12/23
 * desc:
 */
data class LiveRecordResponse (val filterList:MutableList<FilterInfo>,val dataList:MutableList<RecordInfo>)
{
    data class RecordInfo(val date:String,val kbCount:Int,val roseNum:String,val title:String,val time:String,val roomId:String,val list:MutableList<RecordInfo>)

}