package cn.yanhu.commonres.pop

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import cn.yanhu.commonres.R
import cn.yanhu.commonres.manager.AppCacheManager
import com.permissionx.guolindev.dialog.RationaleDialog

/**
 * @author: zhengjun
 * created: 2023/12/4
 * desc:
 */
class SystemAlertPermission2Dialog(context: Context, private val tips:String, private val onClickCloseListener: OnClickCloseListener) :
    RationaleDialog(context, R.style.CustomDialog) {

    private var btnCancel: AppCompatButton? = null
    private lateinit var btnNext: AppCompatButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_system_alert2)
        btnCancel = findViewById(R.id.btn_cancel)
        btnNext = findViewById(R.id.btn_next)
        val tvDesc = findViewById<TextView>(R.id.tv_desc)
        tvDesc.text = tips
        btnCancel?.setOnClickListener {
            dismiss()
            AppCacheManager.alertCheckCount = AppCacheManager.alertCheckCount + 1
            onClickCloseListener.onClose()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if(hasFocus){
            setCanceledOnTouchOutside(true)
        }
    }

    override fun getPositiveButton(): View {
        return btnNext
    }

    override fun getNegativeButton(): View? {
        return null
    }

    override fun getPermissionsToRequest(): MutableList<String> {
        val list = mutableListOf<String>()
        list.add(Manifest.permission.SYSTEM_ALERT_WINDOW)
        return list
    }

    interface OnClickCloseListener{
        fun onClose()
    }
}