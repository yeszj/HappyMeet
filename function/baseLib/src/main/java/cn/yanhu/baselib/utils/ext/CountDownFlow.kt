package cn.yanhu.baselib.utils.ext

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch


/**
 * @author: witness
 * created: 2022/4/26
 * desc:
 */
/**
 * 倒计时的实现
 */
@ExperimentalCoroutinesApi
fun FragmentActivity.countDown(
    time: Int = 60,
    delayTime: Long = 1000,
    start: (scope: CoroutineScope) -> Unit,
    end: () -> Unit,
    next: (time: Int) -> Unit,
    cancel: () -> Unit,
) {
    lifecycleScope.launch {
        // 在这个范围内启动的协程会在Lifecycle被销毁的时候自动取消
        flow {
            (time downTo 0).forEach {
                if (it != time) {
                    delay(delayTime)
                }
                emit(it)
            }
        }.onStart {
            // 倒计时开始 ，在这里可以让Button 禁止点击状态
            start(this@launch)
        }.onCompletion {
            // 倒计时结束 ，在这里可以让Button 恢复点击状态
            if (it is CancellationException) {
                cancel()
            } else {
                end()
            }
        }.catch {
            //错误

        }.collect {
            // 在这里 更新值来显示到UI
            next(it)
        }

    }
}