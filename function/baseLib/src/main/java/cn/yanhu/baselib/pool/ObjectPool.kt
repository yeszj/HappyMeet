//package cn.yanhu.baselib.pool
//
//import java.util.Stack
//
///**
// * @author: zhengjun
// * created: 2025/10/28
// * desc:
// */
//// ObjectPool.kt
//interface ObjectPool<T> {
//    /**
//     * 从对象池获取对象
//     */
//    fun obtain(): T
//
//    /**
//     * 将对象回收到对象池
//     */
//    fun recycle(obj: T)
//
//    /**
//     * 清空对象池
//     */
//    fun clear()
//
//    /**
//     * 获取对象池当前大小
//     */
//    fun size(): Int
//
//    /**
//     * 获取对象池最大容量
//     */
//    fun getMaxPoolSize(): Int
//}
//
//// AbstractObjectPool.kt
//abstract class AbstractObjectPool<T> : ObjectPool<T> {
//    protected val availableObjects = Stack<T>()
//    protected var createdCount = 0
//    protected var maxPoolSize = 20
//
//    override fun obtain(): T {
//        return synchronized(this) {
//            if (availableObjects.isNotEmpty()) {
//                availableObjects.pop()
//            } else {
//                createdCount++
//                createObject()
//            }
//        }
//    }
//
//    override fun recycle(obj: T) {
//        synchronized(this) {
//            if (availableObjects.size < maxPoolSize) {
//                resetObject(obj)
//                availableObjects.push(obj)
//            } else {
//                // 池已满，直接销毁对象
//                destroyObject(obj)
//                createdCount--
//            }
//        }
//    }
//
//    override fun clear() {
//        synchronized(this) {
//            availableObjects.forEach { destroyObject(it) }
//            availableObjects.clear()
//            createdCount = 0
//        }
//    }
//
//    override fun size(): Int {
//        return synchronized(this) { availableObjects.size }
//    }
//
//    override fun getMaxPoolSize(): Int = maxPoolSize
//
//    fun setMaxPoolSize(size: Int) {
//        synchronized(this) {
//            maxPoolSize = size
//            // 如果当前大小超过新的最大大小，移除多余对象
//            while (availableObjects.size > maxPoolSize) {
//                destroyObject(availableObjects.pop())
//                createdCount--
//            }
//        }
//    }
//
//    /**
//     * 创建新对象
//     */
//    protected abstract fun createObject(): T
//
//    /**
//     * 重置对象状态以便复用
//     */
//    protected abstract fun resetObject(obj: T)
//
//    /**
//     * 销毁对象（当对象池已满时）
//     */
//    protected abstract fun destroyObject(obj: T)
//
//    /**
//     * 获取已创建的对象总数
//     */
//    fun getCreatedCount(): Int = synchronized(this) { createdCount }
//}