package cn.yanhu.commonres.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cn.yanhu.commonres.bean.BaseUserInfo

/**
 * @author: zhengjun
 * created: 2024/2/29
 * desc:
 */
@Dao
interface UserInfoDao {
    // 增
    @Insert
    fun insert(students: BaseUserInfo)

    // 删
    @Delete
    fun delete(student: BaseUserInfo)

    // 改
    @Update
    fun update(student: BaseUserInfo)

    @Query("SELECT * FROM BaseUserInfo WHERE userId = :userId")
    fun getUserInfo(userId: String): BaseUserInfo

}