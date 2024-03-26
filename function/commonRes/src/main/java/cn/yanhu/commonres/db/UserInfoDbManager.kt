package cn.yanhu.commonres.db

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import cn.yanhu.commonres.bean.BaseUserInfo
import cn.yanhu.commonres.manager.AppCacheManager
import com.blankj.utilcode.util.ThreadUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @author: zhengjun
 * created: 2024/2/29
 * desc:
 */
object UserInfoDbManager {
    private lateinit var db: AppDatabase

    fun initDb(context: Context) {
        db = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java, "HappyMeet.db"
        ).addMigrations(MIGRATION_1_2).build()
    }

    fun saveUserInfo(context: FragmentActivity, userInfo: BaseUserInfo) {
        val userDao = db.userDao()
        context.lifecycleScope.launch(Dispatchers.IO) {
            userDao.insert(userInfo)
        }
    }

    fun getUserInfo(context: FragmentActivity,onGetUserInfoListener: OnGetUserInfoListener) {
        val userDao = db.userDao()
        context.lifecycleScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                userDao.getUserInfo(AppCacheManager.userId)
            }.onSuccess {
                ThreadUtils.runOnUiThread {
                    onGetUserInfoListener.onUserInfo(it)
                }
            }.onFailure {
                ThreadUtils.runOnUiThread {
                    it.printStackTrace()
                    onGetUserInfoListener.onUserInfo(null)
                }
            }
        }
    }

    interface OnGetUserInfoListener{
        fun onUserInfo(userInfo: BaseUserInfo?)
    }
}