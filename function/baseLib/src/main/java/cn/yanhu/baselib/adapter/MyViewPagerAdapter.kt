package cn.yanhu.baselib.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import java.util.ArrayList

/**
 * @author zhengjun
 * @desc
 * @create at 2021/6/17 19:45
 */
class MyViewPagerAdapter(fm: FragmentManager?, list: List<Fragment>) : FragmentPagerAdapter(
    fm!!
) {
    private var listfragment: List<Fragment> = ArrayList() //创建一个List<Fragment>
    override fun getItem(arg0: Int): Fragment {
        return listfragment[arg0] //返回第几个fragment
    }

    override fun getCount(): Int {
        return listfragment.size //总共有多少个fragment
    }


    init {
        listfragment = list
    }
}