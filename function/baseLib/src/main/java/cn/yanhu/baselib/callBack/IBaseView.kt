package cn.yanhu.baselib.callBack

import cn.yanhu.baselib.R
import com.blankj.utilcode.util.StringUtils.getString

/**
 * Created by admin on 2018/1/18.
 */

interface IBaseView {
    fun showLoading(showContent: Boolean,content:String = getString(R.string.loading))
    fun showContent()
    fun showError(content:String = getString(R.string.net_load_error))
    fun showEmpty()
}
