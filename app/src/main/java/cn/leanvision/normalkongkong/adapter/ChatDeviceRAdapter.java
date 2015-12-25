package cn.leanvision.normalkongkong.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import cn.leanvision.common.bean.ChatMsgEntity;
import cn.leanvision.common.util.DeviceTypeUtil;
import cn.leanvision.common.util.LogUtil;
import cn.leanvision.common.util.StringUtil;
import cn.leanvision.normalkongkong.Constants;
import cn.leanvision.normalkongkong.R;

/********************************
 * Created by lvshicheng on 15/11/5.
 * description
 ********************************/
public class ChatDeviceRAdapter extends RecyclerView.Adapter<ChatDeviceRAdapter.MViewHolder> {

    private List<ChatMsgEntity> dataList;
    private WeakReference<Context> contextWeakReference;
    private String devStatus;

    public ChatDeviceRAdapter(Context context) {
        contextWeakReference = new WeakReference<>(context);
    }

    @Override
    public MViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = null;
        LayoutInflater mInflater = LayoutInflater.from(contextWeakReference.get());
        if (IMsgViewType.IM_COM_MSG == viewType) {
            convertView = mInflater.inflate(R.layout.chatting_item_msg_text_left, parent, false);
        } else if (IMsgViewType.IM_TO_MSG == viewType) {
            convertView = mInflater.inflate(R.layout.chatting_item_msg_text_right, parent, false);
        } else {
            LogUtil.log(getClass(), "unknown type");
        }
        return new MViewHolder(convertView, viewType);
    }

    @Override
    public void onBindViewHolder(MViewHolder holder, final int position) {
        final ChatMsgEntity chatMsgEntity = dataList.get(position);

        if (IMsgViewType.IM_COM_MSG == holder.viewType) {
            setupDeviceLogo(devStatus, holder.ivHeader);
        }

        initMsgType(chatMsgEntity, holder);
        if (chatMsgEntity.isShowProgressbar()) {
            holder.progressBar.setVisibility(View.VISIBLE);
        } else {
            holder.iv_msg_state_failed.setVisibility(View.GONE);
            holder.progressBar.setVisibility(View.GONE);
        }

        if (chatMsgEntity.isSendCompleted()) {
            holder.iv_msg_state_failed.setVisibility(View.GONE);
        } else {
            holder.progressBar.setVisibility(View.GONE);
            holder.iv_msg_state_failed.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return null == dataList ? 0 : dataList.size();
    }

    public void clear() {
        if (dataList == null)
            return;
        dataList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        ChatMsgEntity entity = dataList.get(position);
        if (entity.isComMsg()) {
            return IMsgViewType.IM_COM_MSG;
        } else {
            return IMsgViewType.IM_TO_MSG;
        }
    }

    /**
     * 初始化消息类型
     */
    private void initMsgType(final ChatMsgEntity chatMsgEntity, MViewHolder holder) {
        holder.tvContent.setVisibility(View.VISIBLE);
        holder.tvSendTime.setText(chatMsgEntity.getDate());
        String chatMsgStr = chatMsgEntity.getText();
        holder.tvContent.setText(chatMsgStr);
        holder.llChatContent.setOnClickListener(null);
    }

    public void setDevStatus(String devStatus) {
        this.devStatus = devStatus;
    }

    public String getDevStatus() {
        if (StringUtil.isNullOrEmpty(devStatus))
            devStatus = DeviceTypeUtil.DEV_STATUS_A002;
        return devStatus;
    }

    public List<ChatMsgEntity> getDataList() {
        if (dataList == null)
            dataList = new ArrayList<>();
        return dataList;
    }

    public void setDataList(List<ChatMsgEntity> dataList) {
        this.dataList = dataList;
    }

    class MViewHolder extends RecyclerView.ViewHolder {

        private final int viewType;
        private TextView tvSendTime;
        private TextView tvContent;
        private LinearLayout llChatContent;
        private ProgressBar progressBar;
        private ImageView iv_msg_state_failed;
        private ImageView ivHeader;

        public MViewHolder(View itemView, int viewType) {
            super(itemView);
            findView();
            this.viewType = viewType;
        }

        private void findView() {
            tvSendTime = (TextView) itemView.findViewById(R.id.tv_sendtime);
            tvContent = (TextView) itemView.findViewById(R.id.tv_chatcontent);
            llChatContent = (LinearLayout) itemView.findViewById(R.id.ll_chatcontent);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
            iv_msg_state_failed = (ImageView) itemView.findViewById(R.id.iv_msg_state_failed);
            ivHeader = (ImageView) itemView.findViewById(R.id.iv_userhead);
        }
    }

    interface IMsgViewType {
        int IM_COM_MSG = 0;
        int IM_TO_MSG = 1;
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
}
