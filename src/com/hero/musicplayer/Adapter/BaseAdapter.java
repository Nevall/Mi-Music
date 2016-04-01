package com.hero.musicplayer.Adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;


public abstract class BaseAdapter<T> extends android.widget.BaseAdapter{
	/**
	 * 上下文对象
	 */
	Context context;
	/**
	 * List集合
	 */
	List<T> data;
	/**
	 * 将布局文件加载为View对象工具
	 */
	LayoutInflater inflater;
	
	/**
	 * 构造方法
	 * @param context 上下文对象
	 * @param data 数据源
	 */
	
	public BaseAdapter(Context context, List<T> data) {
		setContext(context);
		setData(data);
		setInflater(inflater);
	}

	/**
	 * 获取上下文对象
	 * @return 上下文对象
	 */
	public Context getContext() {
		return context;
	}

	/**
	 * 设置上下文对象
	 * @param context 上下文对象，不能为null值，否则抛出异常
	 */
	public void setContext(Context context) {
		if(context == null){
			throw new IllegalArgumentException("参数Context不能为null值！！！");
		}
		this.context = context;
	}

	/**
	 * 获取List数据源集合
	 * @return 数据源
	 */
	public List<T> getData() {
		return data;
	}

	/**
	 * 设置List数据源集合
	 * @param data 数据源
	 */
	public void setData(List<T> data) {
		if(data == null){
			data = new ArrayList<T>();
		}
		this.data = data;
	}
	


	/**
	 * 获取LayoutInflater对象
	 * @return LayoutInflater对象
	 */
	public LayoutInflater getInflater() {
		return inflater;
	}

	/**
	 * 设置LayoutInflater对象
	 * @param inflater LayoutInflater对象
	 */
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
