package com.liuxin.ilistviewdemo;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.liuxin.ilistviewdemo.adapter.TestAdapter;
import com.liuxin.ilistviewdemo.ilistview.IListView;
import com.liuxin.ilistviewdemo.ilistview.footer.LoadMoreFooterView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements IListView.onLoadMoreListener,IListView.onRefreshListener {

    private IListView listView;
    private LoadMoreFooterView loadMoreFooterView;
    private TestAdapter adapter;
    private List<String> list=new ArrayList<>();
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==0x123){
                loadMoreFooterView.setStatus(LoadMoreFooterView.Status.GONE);
                for(int i=0;i<10;i++){
                    list.add("item"+i);
                }
                adapter.notifyDataSetChanged();
            }else if(msg.what==0x124){
                listView.setRefreshing(false);
                list.clear();
                for(int i=0;i<10;i++){
                    list.add("item"+i);
                }
                adapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }


    protected void initView() {
        listView= (IListView) findViewById(R.id.listView);
        listView.setOnLoadMoreListener(this);
        listView.setOnRefreshListener(this);
        loadMoreFooterView= (LoadMoreFooterView) listView.getLoadMoreView();
        adapter=new TestAdapter(this,list);
        listView.setAdapter(adapter);
        listView.setRefreshing(true);
    }






    @Override
    public void onRefresh() {
        Message message=new Message();
        message.what=0x124;
        handler.sendMessageDelayed(message,2000);

    }

    @Override
    public void onLoadMore() {
        loadMoreFooterView.setStatus(LoadMoreFooterView.Status.LOADING);
        Message message=new Message();
        message.what=0x123;
        handler.sendMessageDelayed(message,2000);
    }
}
