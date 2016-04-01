package com.hero.musicplayer.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.hero.musicplayer.app.MyMusicApplication;
import com.hero.musicplayer.entity.Music;
import com.hero.musicplayer.entity.MusicData;
import com.hero.musicplayer.util.Consts;

public class MusicService extends Service implements Consts ,
MediaPlayer.OnPreparedListener {
	/**
	 * 应用程序app
	 */
	private MyMusicApplication app;
	/**
	 * 音乐播放工具
	 */
	private MediaPlayer player;
	/**
	 * List<Music>数据源
	 */
	List<Music> musics;
	/**
	 * 当前播放的歌曲索引
	 */
	private int currentIndex = 0;
	/**
	 * 暂停位置
	 */
	private int pausePosition;
	/**
	 * 广播接收者
	 */
	private BroadcastReceiver receiver;
	/**
	 * 是否开始播放
	 */
	private boolean isStartPlay;

	/**
	 * MediaPlayer异步准备监听
	 */
	@Override
	public void onPrepared(MediaPlayer player) {
		//异步准备完后，播放音乐
		//跳到暂停位置
		player.seekTo(pausePosition);
		//播放歌曲
		player.start();
		//清除暂停位置
		pausePosition = 0;
		//将播放状态设为开始播放
		isStartPlay = true;
		//创建Intent对象
		Intent intent = new Intent();
		intent.setAction(INTENT_ACTION_SERVICE_PLAY);
		//将currentIndex、duration封装到Intent对象中
		intent.putExtra("currentIndex", currentIndex);
		intent.putExtra("duration", player.getDuration());
		//发送广播：播放
		sendBroadcast(intent);
		//开启线程
		startThread();
	}

	@Override
	public void onCreate() {
		musics = new ArrayList<Music>();
		musics = ((MyMusicApplication) getApplication()).getMusic();
		//初始化媒体播放工具
		player = new MediaPlayer();
		//创建广播接收者
		receiver = new InnerBrodcastReceiver();
		//创建意图过滤器,过滤Activity发送的广播
		IntentFilter filter = new IntentFilter();
		filter.addAction(INTENT_ACTION_ACT_PLAY_OR_PAUSE);
		filter.addAction(INTENT_ACTION_ACT_PREVIOUS);
		filter.addAction(INTENT_ACTION_ACT_NEXT);
		filter.addAction(INTENT_ACTION_ACT_PLAY_NEW);
		filter.addAction(INTENT_ACTION_ACT_SEEK);
		filter.addAction(UPDATE_THE_DATA_OF_SONGS);
		filter.addAction(CURRENT_FRAGMRNT_IS_LOCATION_FRAGMRNT);
		filter.addAction(CURRENT_FRAGMRNT_IS_ONLINE_FRAGMRNT);
		//注册广播接收者
		LocalBroadcastManager.getInstance(this).registerReceiver(receiver,filter);

		//为播放工具设置监听器
		player.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				//如果播放完，播放下一首
				if(isStartPlay){
					next();
				}
			}
		});
	}

	/**
	 * 广播接收者的子类，用于接收Activity发送的广播
	 */
	private class InnerBrodcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			//接收广播，获取Activity发送的Action
			String action = intent.getAction();
			if (UPDATE_THE_DATA_OF_SONGS.equals(action)) {
				MusicData musicData = (MusicData)intent.getSerializableExtra("musicData");
				if (musicData != null) {
					//更新数据源
					musics = musicData.getSongs();
				}
			}else if(INTENT_ACTION_ACT_PLAY_OR_PAUSE.equals(action)){
				//如果正在播放，则暂停，否则播放；
				if(player.isPlaying()){
					pause();
				}else{
					play();
				}
			}else if(INTENT_ACTION_ACT_PREVIOUS.equals(action)){
				//播放上一首
				previous();
			}else if(INTENT_ACTION_ACT_NEXT.equals(action)){
				//播放下一首
				next();
			}else if(INTENT_ACTION_ACT_PLAY_NEW.equals(action)){
				//播放新的歌曲
				play(intent.getIntExtra("position", 0));
			}else if(INTENT_ACTION_ACT_SEEK.equals(action)){
				//跳到SeekBar停止的位置
				int percent = intent.getIntExtra("seekTo", 0);
				pausePosition = percent*musics.get(currentIndex).getDuration()/100;
				play();
			}
		}
	}

	/**
	 * 判断是否正在播放
	 */
	public static boolean isRunning;
	/**
	 * 更新进度条线程
	 */
	private UpdateProgressBarThread updateProgressBarThread;

	/**
	 * 开始更新进度条线程
	 */
	private void startThread(){
		if(!isRunning){
			//更改线程状态
			isRunning = true;
			updateProgressBarThread = new UpdateProgressBarThread();
			//开启线程
			updateProgressBarThread.start();
		}
	}

	/**
	 * 停止更新进度条线程
	 */
	private void stopThread(){
		//更改线程状态
		isRunning = false;
		//停止线程
		updateProgressBarThread = null;
	}

	/**
	 * 更新进度条子线程
	 */
	public class UpdateProgressBarThread extends Thread{
		@Override
		public void run() {
			//由于在service中并不能设置进度条，故只能每隔一段时间给Activity发送广播通知更新进度条
			Intent intent = new Intent();
			while(isRunning){
				//发送广播给Activity通知更新进度条
				intent.putExtra("currentPosition", player.getCurrentPosition());
				intent.setAction(INTENT_ACTION_SERVICE_UPDATE_PROGRESS);
				sendBroadcast(intent);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			super.run();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//设置为非粘性
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		//取消接收广播
		LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
		//停止线程
		stopThread();
		//判断是否正在播放
		if(player.isPlaying()){
			player.pause();
		}
		//释放资源
		player.release();
		//通知回收资源
		player = null;
	}


	/**
	 * 播放歌曲
	 */
	private void play(){
		try {
			//重置
			player.reset();
			//准备数据源
			player.setDataSource(musics.get(currentIndex).getSrc());
			//采用异步准备及播放
			player.setOnPreparedListener(this);
			player.prepareAsync(); // prepare async to not block main thread
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 播放一首新的歌曲
	 */
	private void play(int position) {
		//将索引切换到所点击的位置
		currentIndex = position;
		//清除暂停位置
		pausePosition = 0;
		//播放
		play();
	}

	/**
	 * 暂停播放
	 */
	private void pause(){
		//判断是否正在播放
		if(player.isPlaying()){
			//记录暂停位置
			pausePosition = player.getCurrentPosition();
			//暂停播放
			player.pause();
			//创建Intent对象
			Intent intent = new Intent();
			intent.setAction(INTENT_ACTION_SERVICE_PAUSE);
			//发送广播：暂停
			sendBroadcast(intent);
			//停止线程
			stopThread();
		}
	}

	/**
	 * 播放上一首
	 */
	private void previous(){
		currentIndex--;
		//判断是否为第一首
		if(currentIndex<0){
			currentIndex = musics.size()-1;
		}
		//清除暂停位置
		pausePosition = 0;
		//播放
		play();
	}


	/**
	 * 播放下一首
	 */
	private void next(){
		currentIndex++;
		//判断是否为最后一首
		if(currentIndex>=musics.size()){
			currentIndex = 0;
		}
		//清除暂停位置
		pausePosition = 0;
		//播放
		play();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


}
