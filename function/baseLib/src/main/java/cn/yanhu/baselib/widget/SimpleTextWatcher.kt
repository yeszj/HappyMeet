package cn.yanhu.baselib.widget

import android.text.Editable
import android.text.TextWatcher

/**
 * @author: zhengjun
 * created: 2024/2/28
 * desc:
 */
open class SimpleTextWatcher:TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    override fun afterTextChanged(s: Editable?) {
    }
}