package org.linphone.adapter;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import butterknife.ButterKnife;

/**
 * Created by miao on 2018/9/6.
 */
public abstract class BaseAdapter<T, VH extends BaseAdapter.ViewHolder> extends android.widget.BaseAdapter {

    protected Context context;
    protected ArrayList<T> data;

    public BaseAdapter(ArrayList<T> data) {
        this.data = new ArrayList<>(data);
    }

    public BaseAdapter() {
        this.data = new ArrayList<>();
    }

    @Override
    public int getCount() {
        if (data == null) return 0;
        return data.size();
    }

    @Override
    public T getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressWarnings("unchecked")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (context == null) context = parent.getContext();
        VH vh;
        if (convertView == null) {
            int itemViewType = multiItemEnable() ? getAdapterItemViewType(position) : 0;
            convertView = getLayout(parent, itemViewType);
            vh = create(convertView, itemViewType);
            convertView.setTag(vh);
        } else {
            vh = (VH) convertView.getTag();
        }
        bindView(vh, position, getItem(position));
        return convertView;
    }

    protected int getAdapterItemViewType(int position) {
        return 0;
    }

    /**
     * @param view         convertView
     * @param itemViewType adapter 会根据{@link BaseAdapter#multiItemEnable()}自动选择
     * @return 单个或多个viewholder
     */
    protected abstract VH create(View view, int itemViewType);

    /**
     * 根据viewholder 设置数据
     *
     * @param vh       viewholder
     * @param position item position
     * @param item     data
     */
    protected abstract void bindView(VH vh, int position, T item);


    private View getLayout(ViewGroup parent, int typeId) {
        return View.inflate(parent.getContext(), getLayouts()[typeId], null);
    }

    public ArrayList<T> getData() {
        return data;
    }

    public void setData(ArrayList<T> data) {
        if (data == null) return;
        this.data = data;
        notifyDataSetChanged();
    }

    public void addData(ArrayList<T> data) {
        this.data.addAll(data);
        notifyDataSetChanged();
    }

    /**
     * 使用多类型item layoutId请从此传入
     *
     * @return layoutIds
     */
    protected int[] getMultiLayout() {
        return new int[0];
    }

    private int[] getLayouts() {
        return multiItemEnable() ? getMultiLayout() : new int[]{getDefaultLayout()};
    }

    protected abstract int getDefaultLayout();

    public void clear() {
        data.clear();
        notifyDataSetChanged();
    }

    public abstract static class ViewHolder {
        protected View itemView;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
            itemView=view;
        }

    }

    /**
     * 多类型开关
     *
     * @return if true 启用多item 类型
     */
    protected boolean multiItemEnable() {
        return false;
    }

    /**
     * 如果使用多类型item 请重写此方法
     *
     * @return 最大item数量
     */
    protected int getAdapterViewTypeCount() {
        return 1;
    }

    /**
     * 不要重写此方法 此方法已经为子类实现相应功能
     * see{@link BaseAdapter#getAdapterViewTypeCount()}
     *
     * @return item 数量
     */
    @Override
    public int getViewTypeCount() {
        return multiItemEnable() ? getAdapterViewTypeCount() : 1;
    }
}
