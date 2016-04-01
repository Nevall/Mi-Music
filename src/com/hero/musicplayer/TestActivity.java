package com.hero.musicplayer;

import java.io.IOException;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;

import com.hero.musicplayer.util.Player;

public class TestActivity extends Activity {

	private Player player;
	private SeekBar progressBar;
	public MediaPlayer mediaPlayer; // 媒体播放器  

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		mediaPlayer = new MediaPlayer();
		progressBar = (SeekBar)findViewById(R.id.progressBar1);
		player = new Player(progressBar); 
		((Button)findViewById(R.id.button1)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new Thread(new Runnable() {  

					@Override  
					public void run() {  
						String url = "http://mr7.doubanio.com/fc9cd578638ff15e1ed52626d1e1c136/0/fm/song/p2598015_128k.mp3";
						try {
							mediaPlayer.reset();
							mediaPlayer.setDataSource(url);
							mediaPlayer.prepare(); // prepare自动播放  
							mediaPlayer.seekTo(120000);
							mediaPlayer.start(); 
//							player.playUrl(url);  
						} catch (Exception e) {
							e.printStackTrace();
						} // 设置数据源  
					}  
				}).start();  

			}
		});;
	}

}
