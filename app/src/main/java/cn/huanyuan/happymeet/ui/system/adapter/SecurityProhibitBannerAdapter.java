package cn.huanyuan.happymeet.ui.system.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.youth.banner.adapter.BannerAdapter;

import java.util.List;

import cn.huanyuan.happymeet.R;

/**
 * @author: zhengjun
 * created: 2023/8/17
 * desc:
 */
public class SecurityProhibitBannerAdapter extends BannerAdapter<List<String>, SecurityProhibitBannerAdapter.BannerViewHolder> {

    private final Activity activity;

    public SecurityProhibitBannerAdapter(Activity activity, List<List<String>> datas) {
        super(datas);
        this.activity = activity;
    }

    @Override
    public BannerViewHolder onCreateHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.adapter_security_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindView(BannerViewHolder holder, List<String> data, int position, int size) {
        SecurityProhibitUserAdapter callStaticAdapter = new SecurityProhibitUserAdapter();
        callStaticAdapter.submitList(data);
        holder.rv_static.setAdapter(callStaticAdapter);
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        RecyclerView rv_static;
        public BannerViewHolder(@NonNull View view) {
            super(view);
            this.rv_static = view.findViewById(R.id.rv_static);
        }
    }
}
