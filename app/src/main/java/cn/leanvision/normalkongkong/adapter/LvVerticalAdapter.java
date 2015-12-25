package cn.leanvision.normalkongkong.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import cn.leanvision.common.bean.UniteDeviceInfo;
import cn.leanvision.common.util.DeviceTypeUtil;
import cn.leanvision.common.util.StringUtil;
import cn.leanvision.normalkongkong.Constants;
import cn.leanvision.normalkongkong.R;

/********************************
 * Created by lvshicheng on 15/11/3.
 * description
 ********************************/
public class LvVerticalAdapter extends BaseAdapter {

    // Layout inflater
    private Context context;

    private List<UniteDeviceInfo> dataList;

    private HashMap<Integer, View> viewMap;

    private int indexOffset = 0;

    /**
     * Constructor
     */
    public LvVerticalAdapter(Context context) {
        this.context = context;
        viewMap = new HashMap<>();
    }

    public void setDataList(List<UniteDeviceInfo> dataList) {
        indexOffset = 0;
        this.dataList = dataList;
    }

    public List<UniteDeviceInfo> getDataList() {
        return dataList;
    }

    public void addItem(UniteDeviceInfo fpb) {
        dataList.add(0, fpb);
        indexOffset++;
        notifyDataSetChanged();
    }

    public String getLogoPath(String devStatus, HashMap<String, String> logoMap) {
        String key = "off";
        if (DeviceTypeUtil.DEV_STATUS_A002.equals(devStatus)) {
            key = "off";
        } else if (DeviceTypeUtil.DEV_STATUS_A003.equals(devStatus)) {
            key = "onl";
        } else if (DeviceTypeUtil.DEV_STATUS_A004.equals(devStatus)) {
            key = "act";
        }

        String img = logoMap.get(key);
        if (StringUtil.isNullOrEmpty(img)) {
            return null;
        }
        return String.format(Locale.CHINA, "%s/%s", Constants.SERVER_ADDRESS, img);
    }

    @Override
    public int getCount() {
        return dataList == null ? 0 : dataList.size();
    }

    public UniteDeviceInfo getItem(int position) {
        if (dataList == null)
            return null;
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int index, View convertView, ViewGroup parent) {
        View view = viewMap.get(index - indexOffset);
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_vertical_gallery, parent, false);
            viewMap.put(index + indexOffset, view);

//            LogUtil.log(getClass(), String.format("getItem index : %d", index));
            UniteDeviceInfo item = getItem(index);
            HashMap<String, String> logoMap = item.getLogoMap();
            ImageView img = (ImageView) view.findViewById(R.id.iv);
            if (logoMap != null && logoMap.size() > 0) {
                img.setImageResource(R.drawable.mydevice_chazuo_n);

                String logoPath = getLogoPath(DeviceTypeUtil.DEV_STATUS_A003, logoMap);
//                LogUtil.log(getClass(), logoPath);
                Picasso.with(context).load(logoPath).error(R.drawable.mydevice_chazuo_n).into(img);
            } else {
                int imageDefault = DeviceTypeUtil.getDeviceIcon(context, DeviceTypeUtil.DEV_STATUS_A003, item.getDevType(), item.getBigType());
                img.setImageResource(imageDefault);
            }
            view.setTag(index);
        }
        return view;
    }
}
