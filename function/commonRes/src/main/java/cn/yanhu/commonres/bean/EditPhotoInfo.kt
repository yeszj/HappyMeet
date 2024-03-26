package cn.yanhu.commonres.bean

import cn.yanhu.commonres.utils.VideoUtils

/**
 * @author: zhengjun
 * created: 2023/6/14
 * desc:
 */
data class EditPhotoInfo (var url:String, var progress:Int = 100, val isVideo:Boolean = false){
    fun isVideoUrl(): Boolean {
       return VideoUtils.isVideo(url)
    }

    fun isUploadFinish():Boolean{
        return progress>=100
    }

    fun isNetUrl():Boolean{
        if (url.contains("http") || url.contains("https")){
            return true
        }
        return false
    }

}