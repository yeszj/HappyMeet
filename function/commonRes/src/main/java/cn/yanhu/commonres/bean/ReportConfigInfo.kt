package cn.yanhu.commonres.bean

/**
 * @author: zhengjun
 * created: 2024/10/30
 * desc:
 */
data class ReportConfigInfo (val key:String,val list:MutableList<ConfigInfo>){

    data class ConfigInfo(val id:Int, val desc:String, var select:Boolean)
}