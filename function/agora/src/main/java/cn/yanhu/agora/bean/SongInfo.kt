package cn.yanhu.agora.bean

import cn.yanhu.commonres.bean.BaseUserInfo

/**
 * @author: zhengjun
 * created: 2025/1/15
 * desc:
 */
data class SongInfo(val userId:String,val portrait:String,val nickName:String,val songName:String,val clickInfo:BaseUserInfo)