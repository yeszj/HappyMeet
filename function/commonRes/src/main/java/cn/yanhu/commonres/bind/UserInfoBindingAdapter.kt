package cn.yanhu.commonres.bind

import androidx.databinding.BindingAdapter
import cn.yanhu.commonres.bean.BaseUserInfo
import cn.yanhu.commonres.view.UserTagView
import cn.yanhu.commonres.view.UserAvatarView
import cn.yanhu.commonres.view.UserLevelView

/**
 * @author: witness
 * created: 2022/4/26
 * desc:
 */

@BindingAdapter(value = ["avatarUrl", "avatarFrameUrl", "avatarUserId"], requireAll = false)
fun setUserAvatar(
    userAvatarView: UserAvatarView,
    avatarUrl: String?,
    avatarFrameUrl: String?,
    avatarUserId: String?
) {
    val user = BaseUserInfo()
    user.userId = avatarUserId
    user.portrait = avatarUrl
    user.avatarFrame = avatarFrameUrl
    userAvatarView.setUserAvatar(user)
}

@BindingAdapter(value = ["tagValue", "tagColorId","tagTextColorId"], requireAll = false)
fun setTagValue(userAgeView: UserTagView, tagValue: String?, tagColorId: Int,tagTextColorId:Int) {
    userAgeView.setTagValue(tagValue, tagColorId,tagTextColorId)
}

@BindingAdapter(value = ["userLevel"], requireAll = false)
fun setUserLevel(useLevelView: UserLevelView, userLevel: Int) {
    useLevelView.setUserLevel(userLevel)
}

@BindingAdapter(value = ["userAge", "userGender"], requireAll = false)
fun setUserAge(userAgeView: UserTagView, userAge: Int, userGender: Int) {
    userAgeView.setUserAge(userAge, userGender)
}

@BindingAdapter(value = ["userProvince"], requireAll = false)
fun setUserLocation(userAgeView: UserTagView, userProvince: String?) {
        userAgeView.setUserLocation(userProvince)
}

@BindingAdapter(value = ["onlineCount"], requireAll = false)
fun setOnLineCount(userAgeView: UserTagView, onlineCount: Int) {
    userAgeView.setOnLineCount(onlineCount)
}

@BindingAdapter(value = ["groupUserTotalCount"], requireAll = false)
fun setGroupUserTotalCount(userAgeView: UserTagView, groupUserTotalCount: Int) {
    userAgeView.setGroupUserTotalCount(groupUserTotalCount)
}

@BindingAdapter(value = ["groupUserCount"], requireAll = false)
fun setGroupUserCount(userAgeView: UserTagView, groupUserCount: String) {
    userAgeView.setGroupUserCount(groupUserCount)
}
