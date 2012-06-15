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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * broadcast receiver to receive events from the system
 */
public class SystemEventReceiver extends BroadcastReceiver {
	
	/**
	 * default shutdown delay in milliseconds
	 */
	public final static int DEFAULT_DELAY = 30000;
	
	/**
	 * indicates if the shutdown has been aborted
	 */
	public static boolean ABORT_SHUTDOWN = false;
	
	/*
	 * private class level constants
	 */
	private final String sTag = "SystemEventReceiver";
	private final boolean V_LOG = true;
	
	/*
	 * private class level variables
	 */
	private static ShutdownTask shutdownTask = null;
	private static Thread shutdownTaskThread = null;

	/*
	 * (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		
		// check to see which intent has been received
		if(intent.getAction().equals(context.getString(R.string.system_boot_intent)) == true) {
			// notification that the phone has completed booting
			
			if(V_LOG) {
				Log.v(sTag, "receieved notification that phone has booted");
			}
			
			// get the preferences
			SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			
			int mMediaFile = -1;
			String mPreference;
			int mShutdownDelay;
			
			// check to see if we should be shutting down the phone
			if(mPreferences.getBoolean(context.getString(R.string.system_enable_shutdown_preference), false) == true) {
				
				if(V_LOG) {
					Log.v(sTag, "preference set to shutdown device");
				}
				
				// show the lock screen activity
				Intent mIntent = new Intent(context, org.servalproject.autoshutdown.LockScreenActivity.class);
				mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(mIntent);
				
				// do we need to play a sound
				if(mPreferences.getBoolean("preferences_alert_play_tone", true) == true) {
					// we need to play a sound
					mPreference = mPreferences.getString("preferences_alert_tone", "nasa_countdown.ogg");
					
					if(mPreference.equals("nasa_countdown.ogg") == true) {
						mMediaFile = R.raw.nasa_countdown;
					} else if(mPreference.equals("ekg_flatline.ogg") == true) {
						mMediaFile = R.raw.ekg_flatline;
					} else if(mPreference.equals("steam_train.ogg") == true) {
						mMediaFile = R.raw.steam_train;
					}
				}
				
				// get the shutdown delay
				mPreference = mPreferences.getString("preferences_shutdown_delay", null);
				
				if(mPreference != null) {
					mShutdownDelay = Integer.parseInt(mPreference);
				} else {
					mShutdownDelay = DEFAULT_DELAY;
				}
				
				// start the shutdown task
				shutdownTask = new ShutdownTask(mShutdownDelay, mMediaFile, context);
				shutdownTaskThread = new Thread(shutdownTask);
				shutdownTaskThread.start();
				
				if(V_LOG) {
					Log.v(sTag, "going to shutdown device");
					Log.v(sTag, "delay: " + mShutdownDelay);
					Log.v(sTag, "media: " + mMediaFile);
				}
				
			}
		} else if(intent.getAction().equals(context.getString(R.string.system_user_present_intent)) == true) {
			// notification that the user has unlocked the phone
			
			if(V_LOG) {
				Log.v(sTag, "receieved notifcation that the user is present");
			}
			
			// stop the shutdown task if it is running
			abortShutdown();
			
		} else if(intent.getAction().equals(context.getString(R.string.system_abort_shutdown_intent)) == true) {
			// user has aborted the shutdown
			if(V_LOG) {
				Log.v(sTag, "user has aborted the shutdown");
			}
			
			// stop the shutdown task if it is running
			abortShutdown();
			
		} else {
			Log.w(sTag, "called with an unknown intent: " + intent.getAction());
		}
	}
	
	// private function to abort a shutdown
	private void abortShutdown() {
		
		// set flag to indicate shutdown should be aborted
		ABORT_SHUTDOWN = true;
		
		// stop the shutdown task if it is running
		if(shutdownTask != null) {
			
			if(V_LOG) {
				Log.v(sTag, "shutdown task requested to stop");
			}
			
			shutdownTask.requestStop();
			shutdownTaskThread.interrupt();
		}
		
		// play nice and tidy up
		shutdownTaskThread = null;
		shutdownTask = null;
	}
}
