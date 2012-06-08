/*
 * Copyright (C) 2012 The Serval Project
 *
 * This file is part of the Serval Auto Shutdown Software
 *
 * Serval Auto Shutdown Software is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.servalproject.autoshutdown;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.RootToolsException;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

/**
 * a thread which waits the required amount of time then if necessary shutsdown the phone
 */
public class ShutdownTask implements Runnable {
	
	/*
	 * private class level constants
	 */
	private final int sOneSecond = 10000;
	
	private final int sThirtySeconds = 30000; 
	
	private final String sTag = "ShutdownTask";
	private final boolean V_LOG = true; 
	
	private final String rebootCommand = "reboot -p";
	
	/*
	 * private class level variables
	 */
	private int shutdownDelay;
	private int mediaFile;
	private Context context;
	
	private volatile boolean keepGoing = true;
	
	private MediaPlayer mediaPlayer;
	private volatile boolean mediaPlaying = false;
	
	/**
	 * construct a new ShutdownTask object
	 * 
	 * @param shutdownDelay the delay in milliseconds before the phone will shutdown
	 * @param mediaFile the name of the file to play before shutdown
	 * @param context a context object used to gain access to system resources
	 */
	public ShutdownTask(int shutdownDelay, int mediaFile, Context context) {
		
		// validate the parameters
		if(shutdownDelay <= sThirtySeconds == false) {
			throw new IllegalArgumentException("the specified shutdown delay is invalid");
		}
		
		if(context == null) {
			throw new IllegalArgumentException("the context parameter is required");
		}
		
		// store the values for later
		this.shutdownDelay = shutdownDelay;
		
		this.mediaFile = mediaFile;
		
		this.context = context;
	}
	
	/**
	 * request that this thread stops
	 */
	public void requestStop() {
		keepGoing = false;
		
		if(mediaPlaying) {
			mediaPlayer.stop();
			mediaPlayer.release();
		}
		
		if(V_LOG) {
			Log.v(sTag, "shutdown task requested to stop");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		
		// determine when to stop waiting
		long mShutdownTime = System.currentTimeMillis() + shutdownDelay;
		boolean mShutdownStarted = false;
		
		if(V_LOG) {
			Log.v(sTag, "phone will start shutdown process at: " + mShutdownTime);
		}
		
		// loop until the required time or requested to stop
		while(keepGoing) {
			
			long mCurrentTime = System.currentTimeMillis();
			
			if(mCurrentTime > mShutdownTime) {
				// start the shutdown process
				if(mShutdownStarted == false) {
					startShutdown();
					mShutdownStarted = true;
				} 
			} else {
				
				// sleep for the required amount of time
				try {
					Thread.sleep(sOneSecond);
				} catch (InterruptedException e) {
//					Log.i(sTag, "thread was interrupted", e);
				}
			}
		}
	}
	
	// private method to play the media and then shutdown
	private void startShutdown() {
		
		// check to see if we need to play a media file
		if(mediaFile != -1) {
			
			if(V_LOG) {
				Log.v(sTag, "playing the media file prior to shutdown");
			}
			
			// play the media file
			mediaPlayer = MediaPlayer.create(context, mediaFile);
			
			if(mediaPlayer != null) {
				
				// add a callback so we know when the media finishes
				mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()  {
					/*
					 * (non-Javadoc)
					 * @see android.media.MediaPlayer.OnCompletionListener#onCompletion(android.media.MediaPlayer)
					 */
					@Override
					public void onCompletion(MediaPlayer mp) {
						// do the shutdown
						doShutdown();
					}
				});
				
				mediaPlaying = true;
				mediaPlayer.start();
			}
			
		} else {
			// no media file to play so just shutdown
			doShutdown();
		}
		
	}
	
	// private method to actually do the shutdown
	private void doShutdown() {
		
		if(V_LOG) {
			Log.v(sTag, "doing the shutdown");
		}
		
		// one final check before shutdown
		if(keepGoing) {
			
			// send the shutdown command
			try {
				// execute the command
				RootTools.sendShell(rebootCommand, sThirtySeconds);
			} catch (IOException e) {
				Log.e(sTag, "unable to reboot the phone", e);
			} catch (RootToolsException e) {
				Log.e(sTag, "unable to reboot the phone", e);
			} catch (TimeoutException e) {
				Log.e(sTag, "unable to reboot the phone", e);
			}
		} else {
			if(V_LOG) {
				Log.v(sTag, "shutdown was requested but has been aborted");
			}
		}
	}

}
