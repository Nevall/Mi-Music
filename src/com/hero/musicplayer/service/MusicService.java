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
	 * Ӧ�ó���app
	 */
	private MyMusicApplication app;
	/**
	 * ���ֲ��Ź���
	 */
	private MediaPlayer player;
	/**
	 * List<Music>����Դ
	 */
	List<Music> musics;
	/**
	 * ��ǰ���ŵĸ�������
	 */
	private int currentIndex = 0;
	/**
	 * ��ͣλ��
	 */
	private int pausePosition;
	/**
	 * �㲥������
	 */
	private BroadcastReceiver receiver;
	/**
	 * �Ƿ�ʼ����
	 */
	private boolean isStartPlay;

	/**
	 * MediaPlayer�첽׼������
	 */
	@Override
	public void onPrepared(MediaPlayer player) {
		//�첽׼����󣬲�������
		//������ͣλ��
		player.seekTo(pausePosition);
		//���Ÿ���
		player.start();
		//�����ͣλ��
		pausePosition = 0;
		//������״̬��Ϊ��ʼ����
		isStartPlay = true;
		//����Intent����
		Intent intent = new Intent();
		intent.setAction(INTENT_ACTION_SERVICE_PLAY);
		//��currentIndex��duration��װ��Intent������
		intent.putExtra("currentIndex", currentIndex);
		intent.putExtra("duration", player.getDuration());
		//���͹㲥������
		sendBroadcast(intent);
		//�����߳�
		startThread();
	}

	@Override
	public void onCreate() {
		musics = new ArrayList<Music>();
		musics = ((MyMusicApplication) getApplication()).getMusic();
		//��ʼ��ý�岥�Ź���
		player = new MediaPlayer();
		//�����㲥������
		receiver = new InnerBrodcastReceiver();
		//������ͼ������,����Activity���͵Ĺ㲥
		IntentFilter filter = new IntentFilter();
		filter.addAction(INTENT_ACTION_ACT_PLAY_OR_PAUSE);
		filter.addAction(INTENT_ACTION_ACT_PREVIOUS);
		filter.addAction(INTENT_ACTION_ACT_NEXT);
		filter.addAction(INTENT_ACTION_ACT_PLAY_NEW);
		filter.addAction(INTENT_ACTION_ACT_SEEK);
		filter.addAction(UPDATE_THE_DATA_OF_SONGS);
		filter.addAction(CURRENT_FRAGMRNT_IS_LOCATION_FRAGMRNT);
		filter.addAction(CURRENT_FRAGMRNT_IS_ONLINE_FRAGMRNT);
		//ע��㲥������
		LocalBroadcastManager.getInstance(this).registerReceiver(receiver,filter);

		//Ϊ���Ź������ü�����
		player.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				//��������꣬������һ��
				if(isStartPlay){
					next();
				}
			}
		});
	}

	/**
	 * �㲥�����ߵ����࣬���ڽ���Activity���͵Ĺ㲥
	 */
	private class InnerBrodcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			//���չ㲥����ȡActivity���͵�Action
			String action = intent.getAction();
			if (UPDATE_THE_DATA_OF_SONGS.equals(action)) {
				MusicData musicData = (MusicData)intent.getSerializableExtra("musicData");
				if (musicData != null) {
					//��������Դ
					musics = musicData.getSongs();
				}
			}else if(INTENT_ACTION_ACT_PLAY_OR_PAUSE.equals(action)){
				//������ڲ��ţ�����ͣ�����򲥷ţ�
				if(player.isPlaying()){
					pause();
				}else{
					play();
				}
			}else if(INTENT_ACTION_ACT_PREVIOUS.equals(action)){
				//������һ��
				previous();
			}else if(INTENT_ACTION_ACT_NEXT.equals(action)){
				//������һ��
				next();
			}else if(INTENT_ACTION_ACT_PLAY_NEW.equals(action)){
				//�����µĸ���
				play(intent.getIntExtra("position", 0));
			}else if(INTENT_ACTION_ACT_SEEK.equals(action)){
				//����SeekBarֹͣ��λ��
				int percent = intent.getIntExtra("seekTo", 0);
				pausePosition = percent*musics.get(currentIndex).getDuration()/100;
				play();
			}
		}
	}

	/**
	 * �ж��Ƿ����ڲ���
	 */
	public static boolean isRunning;
	/**
	 * ���½������߳�
	 */
	private UpdateProgressBarThread updateProgressBarThread;

	/**
	 * ��ʼ���½������߳�
	 */
	private void startThread(){
		if(!isRunning){
			//�����߳�״̬
			isRunning = true;
			updateProgressBarThread = new UpdateProgressBarThread();
			//�����߳�
			updateProgressBarThread.start();
		}
	}

	/**
	 * ֹͣ���½������߳�
	 */
	private void stopThread(){
		//�����߳�״̬
		isRunning = false;
		//ֹͣ�߳�
		updateProgressBarThread = null;
	}

	/**
	 * ���½��������߳�
	 */
	public class UpdateProgressBarThread extends Thread{
		@Override
		public void run() {
			//������service�в��������ý���������ֻ��ÿ��һ��ʱ���Activity���͹㲥֪ͨ���½�����
			Intent intent = new Intent();
			while(isRunning){
				//���͹㲥��Activity֪ͨ���½�����
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
		//����Ϊ��ճ��
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		//ȡ�����չ㲥
		LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
		//ֹͣ�߳�
		stopThread();
		//�ж��Ƿ����ڲ���
		if(player.isPlaying()){
			player.pause();
		}
		//�ͷ���Դ
		player.release();
		//֪ͨ������Դ
		player = null;
	}


	/**
	 * ���Ÿ���
	 */
	private void play(){
		try {
			//����
			player.reset();
			//׼������Դ
			player.setDataSource(musics.get(currentIndex).getSrc());
			//�����첽׼��������
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
	 * ����һ���µĸ���
	 */
	private void play(int position) {
		//�������л����������λ��
		currentIndex = position;
		//�����ͣλ��
		pausePosition = 0;
		//����
		play();
	}

	/**
	 * ��ͣ����
	 */
	private void pause(){
		//�ж��Ƿ����ڲ���
		if(player.isPlaying()){
			//��¼��ͣλ��
			pausePosition = player.getCurrentPosition();
			//��ͣ����
			player.pause();
			//����Intent����
			Intent intent = new Intent();
			intent.setAction(INTENT_ACTION_SERVICE_PAUSE);
			//���͹㲥����ͣ
			sendBroadcast(intent);
			//ֹͣ�߳�
			stopThread();
		}
	}

	/**
	 * ������һ��
	 */
	private void previous(){
		currentIndex--;
		//�ж��Ƿ�Ϊ��һ��
		if(currentIndex<0){
			currentIndex = musics.size()-1;
		}
		//�����ͣλ��
		pausePosition = 0;
		//����
		play();
	}


	/**
	 * ������һ��
	 */
	private void next(){
		currentIndex++;
		//�ж��Ƿ�Ϊ���һ��
		if(currentIndex>=musics.size()){
			currentIndex = 0;
		}
		//�����ͣλ��
		pausePosition = 0;
		//����
		play();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


}
