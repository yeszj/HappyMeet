//package cn.yanhu.baselib.pool
//
///**
// * @author: zhengjun
// * created: 2025/10/28
// * desc:
// */
//// GenericObjectPool.kt
//class GenericObjectPool<T>(
//    private val factory: ObjectFactory<T>,
//    maxPoolSize: Int = 20
//) : AbstractObjectPool<T>() {
//
//    init {
//        this.maxPoolSize = maxPoolSize
//    }
//
//    override fun createObject(): T {
//        return factory.create()
//    }
//
//    override fun resetObject(obj: T) {
//        factory.reset(obj)
//    }
//
//    override fun destroyObject(obj: T) {
//        factory.destroy(obj)
//    }
//}
//
//// ObjectFactory.kt
//interface ObjectFactory<T> {
//    fun create(): T
//    fun reset(obj: T)
//    fun destroy(obj: T)
//}