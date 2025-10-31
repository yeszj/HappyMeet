//package cn.yanhu.baselib.pool
//
///**
// * @author: zhengjun
// * created: 2025/10/28
// * desc:
// */
///**
// * 可池化对象接口
// */
//interface Poolable {
//    /**
//     * 重置对象状态
//     */
//    fun reset()
//}
//
//// PoolUtils.kt
//object PoolUtils {
//
//    /**
//     * 安全地使用对象池对象执行操作
//     */
//    inline fun <T : Any, R> usePooledObject(
//        pool: ObjectPool<T>,
//        block: (T) -> R
//    ): R {
//        val obj = pool.obtain()
//        try {
//            return block(obj)
//        } finally {
//            pool.recycle(obj)
//        }
//    }
//
//    /**
//     * 批量处理列表数据，使用对象池
//     */
//    inline fun <T : Any, R> processListWithPool(
//        pool: ObjectPool<T>,
//        sourceList: List<*>,
//        block: (T, Any?) -> R
//    ): List<R> {
//        return sourceList.map { item ->
//            usePooledObject(pool) { pooledObj ->
//                block(pooledObj, item)
//            }
//        }
//    }
//
//    /**
//     * 创建列表并使用对象池填充
//     */
//    inline fun <T : Any> createListWithPool(
//        pool: ObjectPool<T>,
//        size: Int,
//        init: (T) -> Unit
//    ): List<T> {
//        return List(size) {
//            pool.obtain().apply(init)
//        }
//    }
//
//    /**
//     * 回收对象列表
//     */
//    fun <T : Any> recycleList(pool: ObjectPool<T>, list: List<T>) {
//        list.forEach { pool.recycle(it) }
//    }
//}