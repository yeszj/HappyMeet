package cn.yanhu.commonres.bean

/**
 * @author: zhengjun
 * created: 2025/4/9
 * desc:
 */
data class SmCheckResult (val recordId:String,val msgType:Int,val canSend:Int,val chatContent:ChatTipContent?){
    companion object{
        const val TYPE_AD = 1 //数美广告+审核消息
    }

    data class ChatTipContent(val content:String,val url: String)
}