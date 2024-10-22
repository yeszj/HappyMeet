package cn.yanhu.imchat.manager

import android.view.View
import android.widget.TextView

/**
 * @author: zhengjun
 * created: 2024/8/16
 * desc:
 */
object RelationshipTypeManager {
    const val TYPE_NO_BIND = 0
    const val TYPE_INTIMATE_FRIEND = 1
    const val TYPE_GOOD_FRIEND = 2
    const val TYPE_LOVER = 30
    const val TYPE_DARLING = 4
    @JvmStatic
    fun getRelationshipDesc(type: Int): String {
        return when (type) {
            TYPE_NO_BIND -> {
                "绑关系"
            }

            TYPE_INTIMATE_FRIEND -> {
                "密友"
            }

            TYPE_GOOD_FRIEND -> {
                "好友"
            }

            TYPE_LOVER -> {
                "恋人"
            }
            TYPE_DARLING ->{
                "心肝"
            }

            else -> {
                ""
            }
        }
    }

    @JvmStatic
    fun setRelationTag(tvRelationTag:TextView,relationType:Int){
        tvRelationTag.text = getRelationshipDesc(relationType)
        when (relationType) {
            TYPE_INTIMATE_FRIEND -> {
                tvRelationTag.visibility = View.VISIBLE
                tvRelationTag.setBackgroundResource(cn.yanhu.imchat.R.drawable.tag_intimacy_friend)
            }
            TYPE_GOOD_FRIEND,TYPE_DARLING -> {
                tvRelationTag.visibility = View.VISIBLE
                tvRelationTag.setBackgroundResource(cn.yanhu.imchat.R.drawable.tag_good_friend)
            }
            TYPE_LOVER -> {
                tvRelationTag.visibility = View.VISIBLE
                tvRelationTag.setBackgroundResource(cn.yanhu.imchat.R.drawable.tag_love_friend)
            }

            else -> tvRelationTag.visibility = View.GONE
        }
    }
}