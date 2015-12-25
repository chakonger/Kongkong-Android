package cn.leanvision.normalkongkong.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.leanvision.common.bean.DeviceInfo;
import cn.leanvision.normalkongkong.Constants;
import cn.leanvision.normalkongkong.R;

/********************************
 * Created by lvshicheng on 15/12/21.
 * description
 ********************************/
public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.LvViewHolder> {

    private Context context;
    private List<DeviceInfo> dataList;
    private View.OnClickListener onItemClickListener;

    public DeviceListAdapter(Context context) {
        this.context = context;
    }

    public void setDataList(List<DeviceInfo> dataList) {
        this.dataList = dataList;
    }

    @Override
    public LvViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_device, parent, false);
        return new LvViewHolder(view, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(LvViewHolder holder, int position) {
        DeviceInfo deviceInfo = dataList.get(position);
        holder.tvDevice.setText(deviceInfo.getDevName());
        setupDeviceLogo(deviceInfo.getDevStatus(), holder.ivLogo);
        holder.itemView.setTag(position);
    }

    private void setupDeviceLogo(String devStatus, ImageView iv) {
        if (Constants.DEV_TYPE_OFFLINE.equals(devStatus)) {
            iv.setImageResource(R.drawable.mydevice_chazuo_off);
        } else if (Constants.DEV_TYPE_ONLINE.equals(devStatus)) {
            iv.setImageResource(R.drawable.mydevice_chazuo_n);
        } else if (Constants.DEV_TYPE_WORK.equals(devStatus)) {
            iv.setImageResource(R.drawable.mydevice_chazuo);
        } else {
            iv.setImageResource(R.drawable.mydevice_chazuo_n);
        }
    }

    @Override
    public int getItemCount() {
        return null == dataList ? 0 : dataList.size();
    }

    public DeviceInfo getItem(int position) {
        return dataList.get(position);
    }

    class LvViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivLogo;
        private TextView tvDevice;

        public LvViewHolder(View itemView) {
            super(itemView);
            initView();
        }

        public LvViewHolder(View itemView, View.OnClickListener listener) {
            super(itemView);
            initView();

            itemView.setOnClickListener(listener);
        }

        private void initView() {
            ivLogo = (ImageView) itemView.findViewById(R.id.iv_logo);
            tvDevice = (TextView) itemView.findViewById(R.id.tv_device);
        }
    }

    public void setItemClickListener(View.OnClickListener clickListener) {
        this.onItemClickListener = clickListener;
    }
}
