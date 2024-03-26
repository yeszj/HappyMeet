package cn.yanhu.commonres.bean

/**
 * @author: zhengjun
 * created: 2024/2/6
 * desc:
 */
data class MineMenuBean(val id: Int, val image: String, val title: String, val url: String,var type:String){
    companion object{
        const val TYPE_DIVIDER = "dividerLine"
    }
}