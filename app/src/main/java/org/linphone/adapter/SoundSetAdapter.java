package org.linphone.adapter;

import android.graphics.Color;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import org.linphone.R;
import org.linphone.bean.Sound;

import java.util.List;

/**
 * Created by 62420 on 2019/7/11 15:25.
 */
public class SoundSetAdapter extends BaseQuickAdapter<Sound, BaseViewHolder> {

    private int position = -1;

    public SoundSetAdapter(int layoutResId, List<Sound> mData){
        super(layoutResId,mData);
    }

    @Override
    protected void convert(BaseViewHolder helper, Sound item) {
        helper.setText(R.id.name,item.getName());
        if (position != -1 && position == helper.getAdapterPosition()){
            helper.setBackgroundColor(R.id.ll, Color.parseColor("#e2e2e2"));
        }else {
            helper.setBackgroundColor(R.id.ll, Color.parseColor("#ffffff"));
        }

    }

    public void clickItme(int position){
        this.position = position;
        notifyDataSetChanged();
    }

}
