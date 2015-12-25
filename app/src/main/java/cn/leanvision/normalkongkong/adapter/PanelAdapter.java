package cn.leanvision.normalkongkong.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;

import cn.leanvision.common.bean.ControllerPanelBean;
import cn.leanvision.common.util.DensityUtil;
import cn.leanvision.common.util.StringUtil;
import cn.leanvision.normalkongkong.Constants;
import cn.leanvision.normalkongkong.R;
import cn.leanvision.normalkongkong.framework.sharepreferences.SharedPrefHelper;

/********************************
 * Created by lvshicheng on 15/11/2.
 * description
 ********************************/
public class PanelAdapter extends RecyclerView.Adapter<PanelAdapter.MViewHolder> implements View.OnClickListener, View.OnTouchListener {

    private WeakReference<Context> contextWeakReference;
    private OnRecyclerItemClickListener itemClickListener;
    private OnRecyclerItemTouchListener itemTouchListener;
    private List<ControllerPanelBean.ItemPanel> dataList;
    private String imgBaseUrl;
    private int dip_50;

    public PanelAdapter(Context context) {
        contextWeakReference = new WeakReference<>(context);
        dip_50 = DensityUtil.dip2px(context, 50);
    }

    public Context getContext() {
        return contextWeakReference.get();
    }

    public void setDataList(List<ControllerPanelBean.ItemPanel> dataList) {
        this.dataList = dataList;
    }

    public ControllerPanelBean.ItemPanel getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public MViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_panel, parent, false);
        MViewHolder mViewHolder = new MViewHolder(view);
        view.setOnClickListener(this);
        view.setOnTouchListener(this);
        return mViewHolder;
    }

    @Override
    public void onBindViewHolder(MViewHolder holder, int position) {
        // 给相应的view设置值
        Context context = contextWeakReference.get();
        ControllerPanelBean.ItemPanel itemPanel = dataList.get(position);
        if (StringUtil.isNotNull(imgBaseUrl)) {
            Picasso.with(context).load(String.format(Locale.CANADA, "%s%s", imgBaseUrl, itemPanel.getImg())).placeholder(R.drawable.new_cold).into(holder.imageView);
            if (StringUtil.isNotNull(itemPanel.getBkg()))
                Picasso.with(context).load(String.format(Locale.CANADA, "%s%s", imgBaseUrl, itemPanel.getBkg())).resize(dip_50, dip_50).into(holder.bgImageView);
        }
        holder.textView.setText(itemPanel.getText());
        if (StringUtil.isNullOrEmpty(itemPanel.getText())) {
            holder.imageView.setVisibility(View.INVISIBLE);
        } else {
            holder.imageView.setVisibility(View.VISIBLE);
        }
        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    @Override
    public void onClick(View v) {
        if (itemClickListener != null) {
            itemClickListener.onItemClicked(v, (Integer) v.getTag());
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (itemTouchListener != null)
            itemTouchListener.onItemTouched(v, (Integer) v.getTag(), event);
        return false;
    }

    class MViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageView;
        public TextView textView;
        public ImageView bgImageView;

        public MViewHolder(View itemView) {
            super(itemView);

            imageView = (ImageView) itemView.findViewById(R.id.item_iv);
            textView = (TextView) itemView.findViewById(R.id.item_tv);
            bgImageView = (ImageView) itemView.findViewById(R.id.iv_bg);
        }
    }

    public void setImgBaseUrl(String imgBaseUrl) {
        if (imgBaseUrl.startsWith("http:"))
            this.imgBaseUrl = imgBaseUrl;
        else {
            SharedPrefHelper sharedp = SharedPrefHelper.getInstance();
            String serverAddress = Constants.SERVER_ADDRESS;
            this.imgBaseUrl = String.format(Locale.CHINA, "%s/%s", serverAddress, imgBaseUrl);
        }
    }

    public void setOnRecyclerItemClickListener(OnRecyclerItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setOnRecyclerItemTouchListener(OnRecyclerItemTouchListener itemTouchListener) {
        this.itemTouchListener = itemTouchListener;
    }

    public interface OnRecyclerItemClickListener {
        void onItemClicked(View v, int position);
    }

    public interface OnRecyclerItemTouchListener {
        void onItemTouched(View v, int position, MotionEvent event);
    }
}
