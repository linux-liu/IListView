package com.liuxin.ilistviewdemo.adapter;

import android.content.Context;
import android.widget.TextView;


import com.liuxin.ilistviewdemo.R;

import java.util.List;

/**
 * Created by liuxin on 2017/8/16.
 */

public class TestAdapter extends CommonAdapter<String> {


    public TestAdapter(Context context, List<String> datas) {
        super(context, datas);
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_simple_text;
    }

    @Override
    public void bindView(ViewHolder viewHolder, int position, String item) {
        TextView text_content=viewHolder.findViewById(R.id.text_content);

        text_content.setText(item);
    }
}
