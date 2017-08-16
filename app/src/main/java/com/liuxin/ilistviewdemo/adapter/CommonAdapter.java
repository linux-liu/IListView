package com.liuxin.ilistviewdemo.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

public abstract class CommonAdapter<T> extends BaseAdapter {

    private List<T> datas;
    private int layoutId;
    protected Context context;

    public CommonAdapter(Context context, List<T> datas) {
        this.datas = datas;
        this.context = context;
    }

    @Override
    public int getViewTypeCount() {
        return super.getViewTypeCount();
    }

    @Override
    public int getCount() {
        if (null == datas || datas.isEmpty()) {
            return 0;
        }
        return datas.size();
    }

    @Override
    public T getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (getViewTypeCount()== 1){
            viewHolder = ViewHolder.get(context, convertView, getLayoutId(),parent);
        }else{
            viewHolder = ViewHolder.get(context, convertView, getLayoutId(position),parent);
        }
        bindView(viewHolder,position,getItem(position));
        return viewHolder.getConvertView();
    }
    public void setDatas(List<T> datas){
        this.datas = datas;
    }

    public List<T> getDatas(){
        return datas;
    }

    /**
     * 由子类重写返回布局ID
     * @return
     */
    public abstract int getLayoutId();

    public int getLayoutId(int position){
        return 0;
    }

    public abstract void bindView(ViewHolder viewHolder,int position,T item);
}
