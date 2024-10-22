package cn.yanhu.imchat.listener

interface CallBackListener {
    fun onSuccess()
    fun onError(code: Int, errorMsg: String){}
}