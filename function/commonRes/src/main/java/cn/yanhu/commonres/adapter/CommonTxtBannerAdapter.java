package cn.yanhu.commonres.adapter;


import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.youth.banner.adapter.BannerAdapter;

import java.util.List;

import cn.yanhu.commonres.R;

/**
 * @author: zhengjun
 * created: 2023/8/17
 * desc:
 */
public class CommonTxtBannerAdapter extends BannerAdapter<String, CommonTxtBannerAdapter.BannerViewHolder> {

    private final Activity activity;

    public CommonTxtBannerAdapter(Activity activity, List<String> datas) {
        super(datas);
        this.activity = activity;
    }

    @Override
    public BannerViewHolder onCreateHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.adapter_online_banner_item, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindView(BannerViewHolder holder, String data, int position, int size) {
        holder.tv_desc.setText(data);
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        AppCompatTextView tv_desc;

        public BannerViewHolder(@NonNull View view) {
            super(view);
            this.tv_desc = view.findViewById(R.id.tv_desc);
        }
    }
}
