package cn.yanhu.commonres.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * @author: zhengjun
 * created: 2023/6/5
 * desc:
 */
class LoadingAdapter(private val mContext: Context, private val mLayoutId: Int,private val itemCount:Int = 10) :
    RecyclerView.Adapter<LoadingAdapter.LoadingDefaultHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoadingDefaultHolder {
        return LoadingDefaultHolder(LayoutInflater.from(mContext).inflate(mLayoutId, parent, false))
    }

    override fun onBindViewHolder(holder: LoadingDefaultHolder, position: Int) {}
    override fun getItemCount(): Int {
        return itemCount
    }

    class LoadingDefaultHolder(itemView: View?) : RecyclerView.ViewHolder(
        itemView!!
    )
}