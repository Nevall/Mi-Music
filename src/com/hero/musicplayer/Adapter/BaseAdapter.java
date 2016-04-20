package com.hero.musicplayer.Adapter;

import android.content.Context;
import android.view.LayoutInflater;

import java.util.ArrayList;
import java.util.List;


public abstract class BaseAdapter<T> extends android.widget.BaseAdapter{

	Context context;

	List<T> data;

	LayoutInflater inflater;

	public BaseAdapter(Context context, List<T> data) {
		setContext(context);
		setData(data);
		setInflater(inflater);
	}

	public Context getContext() {
		return context;
	}


	public void setContext(Context context) {
		if(context == null){
			throw new IllegalArgumentException("参数Context不能为null值！！！");
		}
		this.context = context;
	}


	public List<T> getData() {
		return data;
	}


	public void setData(List<T> data) {
		if(data == null){
			data = new ArrayList<T>();
		}
		this.data = data;
	}
	
	public LayoutInflater getInflater() {
		return inflater;
	}

	
	public void setInflater(LayoutInflater inflater) {
		this.inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return data.size();
	}
	

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

}
