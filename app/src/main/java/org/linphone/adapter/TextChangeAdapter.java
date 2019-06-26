package org.linphone.adapter;

import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.linphone.R;

import butterknife.BindView;

public class TextChangeAdapter extends BaseAdapter<String, TextChangeAdapter.TextChangeVH> {


    @Override
    protected TextChangeVH create(View view, int itemViewType) {
        return new TextChangeVH(view);
    }

    private int textSize = 20;

    @Override
    protected void bindView(TextChangeVH textChangeVH, int position, String item) {
        if (position % 2 == 0) {
            textChangeVH.iconOther.setVisibility(View.VISIBLE);
            textChangeVH.me.setVisibility(View.GONE);
            textChangeVH.message.setGravity(Gravity.START);
        } else {
            textChangeVH.iconOther.setVisibility(View.GONE);
            textChangeVH.me.setVisibility(View.VISIBLE);
            textChangeVH.message.setGravity(Gravity.END);
        }
        textChangeVH.message.setText(item);
        textChangeVH.message.setTextSize(textSize);
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
        notifyDataSetChanged();
    }

    @Override
    protected int getDefaultLayout() {
        return R.layout.item_chat_me_simple;
    }

    class TextChangeVH extends BaseAdapter.ViewHolder {
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.message)
        TextView message;
        @BindView(R.id.icon_other)
        ImageView iconOther;
        @BindView(R.id.me)
        ImageView me;

        public TextChangeVH(View view) {
            super(view);
        }
    }
}
