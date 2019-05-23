package org.linphone.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.linphone.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ImageDetailsActivity extends AppCompatActivity {

    @BindView(R.id.mImg)
    ImageView mImg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_details);
        ButterKnife.bind(this);
        Glide.with(mImg).load(getIntent().getStringExtra("imgUrl")).into(mImg);
        ;
    }

    @OnClick(R.id.mImg)
    public void onViewClicked() {
        finish();
    }
}
