package com.liuxin.ilistviewdemo.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ViewHolder {

    private SparseArray<View> views;
    private View convertView;

    public ViewHolder(Context context,int layoutId,ViewGroup parent){
        this.views = new SparseArray<View>();
        this.convertView = LayoutInflater.from(context).inflate(layoutId,parent,false);
        this.convertView.setTag(this);
    }

    public static ViewHolder get(Context context, View convertView, int layoutId, ViewGroup parent){
        if (convertView == null) {
            return new ViewHolder(context,layoutId,parent);
        }
        return (ViewHolder) convertView.getTag();
    }

    @SuppressWarnings("unchecked")
    public <T extends View> T findViewById(int viewId){
        View view = views.get(viewId);
        if (null == view) {
            view = convertView.findViewById(viewId);
            views.put(viewId, view);
        }
        return (T) view;
    }

    public View getConvertView(){
        return convertView;
    }
}
