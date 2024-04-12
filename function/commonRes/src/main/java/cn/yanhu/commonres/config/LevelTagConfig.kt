package cn.yanhu.commonres.config

import cn.yanhu.commonres.R

/**
 * @author: zhengjun
 * created: 2024/2/5
 * desc:
 */
object LevelTagConfig {
    fun getLevelTagBg(level:Int):Int{
       return when (level) {
           in 1..9 -> {
               R.drawable.level_tag1
           }
           in 10..19 -> {
               R.drawable.level_tag2
           }
           in 20..29 -> {
               R.drawable.level_tag3
           }
           in 30..39 -> {
               R.drawable.level_tag4
           }
           in 40..59 -> {
               R.drawable.level_tag5
           }
           in 60..79 -> {
               R.drawable.level_tag6
           }
           in 80..99 -> {
               R.drawable.level_tag7
           }
           else -> {
               R.drawable.level_tag6
           }
       }
    }


    fun getUserEnterBg(level:Int):Int{
        return when (level) {
            in 1..10 -> {
                R.drawable.level_bg_enter1
            }
            in 10..19 -> {
                R.drawable.level_bg_enter1
            }
            in 20..29 -> {
                R.drawable.level_bg_enter2
            }
            in 30..39 -> {
                R.drawable.level_bg_enter3
            }
            in 40..59 -> {
                R.drawable.level_bg_enter4
            }
            in 60..79 -> {
                R.drawable.level_bg_enter5
            }
            in 80..99 -> {
                R.drawable.level_bg_enter6
            }
            else -> {
                R.drawable.level_bg_enter7
            }
        }
    }
}