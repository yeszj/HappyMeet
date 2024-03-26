package cn.yanhu.commonres.manager

/**
 * @author: zhengjun
 * created: 2024/2/5
 * desc:
 */
object SexManager {
    const val SEX_MAN = 1
    const val SEX_WOMAN  = 2
    @JvmStatic
    fun isMan(gender:Int):Boolean{
        return 1==gender
    }
}