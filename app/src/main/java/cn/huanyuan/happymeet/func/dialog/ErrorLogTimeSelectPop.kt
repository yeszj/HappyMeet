package cn.huanyuan.happymeet.func.dialog

import android.annotation.SuppressLint
import android.content.Context
import androidx.fragment.app.FragmentActivity
import cn.huanyuan.happymeet.R
import cn.huanyuan.happymeet.bean.TimeInfo
import cn.huanyuan.happymeet.databinding.PopErrorLogTimeSelectBinding
import cn.huanyuan.happymeet.ui.system.adapter.DaySelectAdapter
import cn.huanyuan.happymeet.ui.system.adapter.TimeSelectAdapter
import cn.yanhu.baselib.utils.DateUtils
import com.blankj.utilcode.util.StringUtils
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView
import java.util.Calendar

/**
 * @author: zhengjun
 * created: 2024/3/14
 * desc:
 */
@SuppressLint("ViewConstructor")
class ErrorLogTimeSelectPop(context: Context, private val onSelectTimeListener: OnSelectTimeListener) : BottomPopupView(context) {
    private val timeSelectAdapter by lazy { TimeSelectAdapter() }
    private val daySelectAdapter by lazy { DaySelectAdapter() }
    override fun onCreate() {
        super.onCreate()
        mBinding = PopErrorLogTimeSelectBinding.bind(popupImplView)
        mBinding?.apply {
            initTimeAdapter()
            initDayAdapter()
            addItemClickListener()
            tvSure.setOnClickListener {
                val selectItem = timeSelectAdapter.getSelectItem()
                val hourFormat = selectItem?.startHour.toString() + ":00-" + selectItem?.endHour + ":00"
                onSelectTimeListener.onSelect("$daySelectValue $hourFormat")
                dismiss()
            }
            tvCancel.setOnClickListener { dismiss() }
        }
    }

    private fun PopErrorLogTimeSelectBinding.initDayAdapter() {
        rvDay.adapter = daySelectAdapter
        val todayStr = DateUtils.getTodayStr("MM-dd")
        val yestodyStr = DateUtils.getYestodyStr(-1, "MM-dd")
        val yestodyStr2 = DateUtils.getYestodyStr(-2, "MM-dd")
        val list = mutableListOf<String>()
        list.add("$todayStr\n今天")
        list.add("$yestodyStr\n昨天")
        list.add("$yestodyStr2\n前天")
        daySelectAdapter.submitList(list)
    }

    private fun PopErrorLogTimeSelectBinding.initTimeAdapter() {
        val calendar = Calendar.getInstance()
        val hour: Int = calendar.get(Calendar.HOUR_OF_DAY)
        timeSelectAdapter.setCurrentHour(hour)
        rvTime.adapter = timeSelectAdapter
        val startTimeList =
            StringUtils.getStringArray(cn.yanhu.commonres.R.array.range_start_time)
        val endTimeList =
            StringUtils.getStringArray(cn.yanhu.commonres.R.array.range_end_time)
        val timeList = mutableListOf<TimeInfo>()
        for (i in startTimeList.indices) {
            timeList.add(TimeInfo(startTimeList[i].toInt(), endTimeList[i].toInt()))
        }
        timeSelectAdapter.submitList(timeList)
    }

    private var daySelectValue:String =  DateUtils.getTodayStr()
    private fun addItemClickListener() {
        timeSelectAdapter.setOnItemClickListener { _, _, position ->
            val viewHolder = mBinding!!.rvTime.findViewHolderForAdapterPosition(position) as TimeSelectAdapter.VH?
            if (viewHolder!=null){
                val alpha = viewHolder.binding.tvType.alpha
                if (alpha==1f){
                    timeSelectAdapter.setSelectPosition(
                        position
                    )
                }
            }
        }
        daySelectAdapter.setOnItemClickListener { _, _, position ->
            timeSelectAdapter.setSelectDayItem(position)
            daySelectValue = when (position) {
                0 -> {
                    DateUtils.getTodayStr()
                }
                1 -> {
                    DateUtils.getYestodyStr(-1,"yyyy-MM-dd")
                }
                else -> {
                    DateUtils.getYestodyStr(-2,"yyyy-MM-dd")
                }
            }
            daySelectAdapter.setSelectPosition(
                position
            )
        }
    }


    private var mBinding: PopErrorLogTimeSelectBinding? = null
    override fun getImplLayoutId(): Int {
        return R.layout.pop_error_log_time_select
    }

    interface OnSelectTimeListener{
        fun onSelect(time:String)
    }

    companion object {
        @JvmStatic
        fun showDialog(
            mContext: FragmentActivity,
            onSelectTimeListener: OnSelectTimeListener
        ): ErrorLogTimeSelectPop {
            val pop = ErrorLogTimeSelectPop(mContext,onSelectTimeListener)
            val builder = XPopup.Builder(mContext)
            builder.asCustom(pop).show()
            return pop
        }
    }
}