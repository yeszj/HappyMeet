package cn.zj.netrequest.status

import cn.zj.netrequest.R
import com.blankj.utilcode.util.StringUtils.getString
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 *自定义异常类
 *@author woochen
 *@time 2019/6/19 11:41
 */
class CustomException : RuntimeException {
    var code: Int? = 0
    var msg: String?

    constructor(code: Int?, message: String?) : super(message) {
        this.msg = message
        this.code = code
    }

    constructor(throwable: Throwable) : super(throwable) {
        var code = ErrorCode.UNKNOW
        var message = throwable.toString()
        if (throwable is UnknownHostException) {
            code = ErrorCode.UNKNOWN_HOST
            message = getString(R.string.net_load_error)
        } else if (throwable is SocketTimeoutException || throwable is ConnectException) {
            code = ErrorCode.TIME_OUT
            message = getString(R.string.tips_timeout)
        }
        this.code = code
        this.msg = message
    }

    override fun toString(): String {
        return "错误码：" + code +
                "," + msg + '\''
    }
}