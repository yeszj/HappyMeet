package cn.yanhu.commonres.pop

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatButton
import cn.yanhu.commonres.R
import com.permissionx.guolindev.dialog.RationaleDialog

/**
 * @author: zhengjun
 * created: 2023/12/4
 * desc:
 */
class SystemAlertPermissionDialog(context: Context, private val cancelTxt:String, private val onClickCloseListener: OnClickCloseListener) :
    RationaleDialog(context, R.style.CustomDialog) {

    private var btnCancel: AppCompatButton? = null
    private lateinit var btnNext: AppCompatButton
    private var ivClose: ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_system_alert)
        btnCancel = findViewById(R.id.btn_cancel)
        btnNext = findViewById(R.id.btn_next)
        ivClose = findViewById(R.id.iv_close)
        btnCancel!!.text = cancelTxt
        btnCancel?.setOnClickListener {
            dismiss()
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
        return ivClose
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