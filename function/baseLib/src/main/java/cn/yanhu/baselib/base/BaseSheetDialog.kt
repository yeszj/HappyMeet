package cn.yanhu.baselib.base

import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.luck.picture.lib.utils.DensityUtil.getNavigationBarHeight


abstract class BaseSheetDialog<B : ViewBinding?> : BottomSheetDialogFragment() {

    var binding: B? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = getViewBinding(inflater, container)
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onHandleOnBackPressed()
                }
            })
        return this.binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.let {
            WindowCompat.setDecorFitsSystemWindows(it, false)

        }
        dialog?.setOnShowListener { _: DialogInterface? ->
            (view.parent as ViewGroup).setBackgroundColor(Color.TRANSPARENT)
            try {
                val bottomSheet = dialog?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
                bottomSheet?.let { sheet ->
                    // 不改变高度，只加底部内边距
                    val navBarHeight = getNavigationBarHeight(requireContext())
                    sheet.setPadding(0, 0, 0, navBarHeight)

                    // 确保内容不被压缩
                    sheet.clipToPadding = false
                    val behavior = BottomSheetBehavior.from(sheet)
                    // 关键：强制触发重新布局
                    sheet.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            sheet.viewTreeObserver.removeOnGlobalLayoutListener(this)
                            // 动画方式：快速展开再收回，触发高度重算
                            behavior.state = BottomSheetBehavior.STATE_EXPANDED
                        }
                    })
                }
            }catch (e: Exception){
                e.printStackTrace()
            }

        }
    }

    protected fun setOnApplyWindowInsets(view: View) {
        dialog?.window?.let {

            ViewCompat.setOnApplyWindowInsetsListener(it.decorView) { v: View?, insets: WindowInsetsCompat ->
                val systemInset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                view.setPadding(0, 0, 0, systemInset.bottom)
                WindowInsetsCompat.CONSUMED
            }
        }
    }

    override fun getTheme(): Int {
        return cn.yanhu.baselib.R.style.Theme_NoWiredStrapInNavigationBar
    }

    override fun onStart() {
        super.onStart()
//        var bottomSheetBehavior = BottomSheetBehavior.from(view?.parent as View) //dialog的高度
//        bottomSheetBehavior.isHideable = false
    }

    override fun dismiss() {
       dismissAllowingStateLoss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    protected abstract fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): B?

    open fun onHandleOnBackPressed() {
        dismiss()
    }

    open fun jumpActivity(cls: Class<*>?) {
        startActivity(Intent(activity, cls))
    }

    open fun jumpActivity(cls: Class<*>?, bundle: Bundle?) {
        val intent = Intent(activity, cls)
        intent.putExtras(bundle!!)
        startActivity(intent)
    }

}