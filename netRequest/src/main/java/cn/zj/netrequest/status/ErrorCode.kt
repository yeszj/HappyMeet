package cn.zj.netrequest.status


object ErrorCode {

     //custom client code
    const val UNKNOW = 80000         //未知异常
    const val UNKNOWN_HOST = 80001 //无法解析主机
    const val TIME_OUT = 80002  //连接超时

    //server code
    const val SUCCESS = 200 //请求成功
    const val TOKEN_INVALID = 301 //token失效
    const val NO_MORE = 20003 //m没有更多数据
    const val TOKEN_FAIL = 104
    const val UNLOGIN = 999
    const val DATA_EMPTY = 80003//数据data为null

    const val DATA_ERROR = 80004 //data 位boolean类型时 返回位false

    const val ACCOUNT_BLOCK = 302 //帐号被封


    const val BALANCE = 60014 //余额不足

    const val LEVEL_DYNAMIC = 80001 //等级不够 不能发表动态


    const val VERSION_UPDATE = 60025 //版本更新

}