package com.pcl.sdklib.bean

import java.io.Serializable

/**
 * @author: zhengjun
 * created: 2024/10/11
 * desc:
 */
data class CheckFaceAuthResult (val authId:Int, val params:String,val isChangeDevice:Boolean = false):Serializable