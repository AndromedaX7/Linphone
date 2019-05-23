package org.linphone.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.linphone.R;
import org.linphone.activity.PhotoSelector;
import org.linphone.bean.ImageSelectBean;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ImageSelectAdapter extends RecyclerView.Adapter<ImageSelectAdapter.VH> implements View.OnClickListener {

    ArrayList<ImageSelectBean> data = new ArrayList<>();
    private OnItemClickListener listener;

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new VH(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat_photo, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH vh, int i) {
//        if (!TextUtils.isEmpty(data.get(i).getUri()))
        Glide.with(vh.img).load(data.get(i).getUri()).placeholder(R.drawable.photos).into(vh.img);
        vh.select.setChecked(data.get(i).isChecked());
        vh.select.setOnCheckedChangeListener(vh);
        vh.itemView.setTag(i);
        vh.itemView.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(ArrayList<ImageSelectBean> data) {
        if (data != null)
            this.data = data;
        notifyDataSetChanged();
    }

    public ArrayList<ImageSelectBean> getData() {
        ArrayList<ImageSelectBean> data = new ArrayList<>();
        for (int i = 0; i < this.data.size(); i++) {
            if (this.data.get(i).isChecked()) {
                data.add(this.data.get(i));
            }
        }
        for (int i = 0; i < this.data.size(); i++) {
            if (this.data.get(i).isChecked()) {
                this.data.get(i).setChecked(false);
                notifyItemChanged(i);
            }
        }

        return data;
    }

    public void setOnItemClick(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        int tag = (int) v.getTag();
        if (listener != null) listener.onItemClick(tag, data.get(tag));
    }

    class VH extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener {
        @BindView(R.id.img)
        ImageView img;
        @BindView(R.id.select)
        CheckBox select;

        public VH(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            data.get(getLayoutPosition()).setChecked(isChecked);
        }
    }


    public interface OnItemClickListener {
        void onItemClick(int pos, ImageSelectBean item);
    }
}
