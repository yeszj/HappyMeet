package cn.yanhu.commonres.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import cn.yanhu.commonres.bean.BaseUserInfo


/**
 * @author: zhengjun
 * created: 2024/2/29
 * desc:
 */

@Database(entities = [BaseUserInfo::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserInfoDao
}

val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE BaseUserInfo "
                    + " ADD COLUMN onlineStatus INTEGER NOT NULL DEFAULT 0"
        )
    }
}