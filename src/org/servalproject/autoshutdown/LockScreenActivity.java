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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;

/**
 * show this activity when the screen is locked
 */
public class LockScreenActivity extends Activity implements OnClickListener {
	
	/*
	 * class level constants
	 */
	private final String sTag = "LockScreenActivity";
	private final boolean V_LOG = true;
	
	private WakeLock wakeLock;
	
	private final int sDelay = 250;
	private Handler statusCheckHandler = new Handler();
	private LockScreenActivity self;
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // check to see if we need to actually instantiate the activity
        if(SystemEventReceiver.ABORT_SHUTDOWN == true) {
        	// shutdown has been aborted, make this screen go away
        	finish();
        }
        
        // keep a reference to this object
        self = this;
        
        if(V_LOG) {
        	Log.v(sTag, "activity has been created");
        }
        
        // set some options so the activity shows over the lockscreen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        setContentView(R.layout.lock_screen);
        
        Button mButton = (Button) findViewById(R.id.lock_screen_ui_btn_abort);
        mButton.setOnClickListener(this);
        
        // wake up the screen if necessary
        // keep the screen awake
        PowerManager mPowerManager = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        wakeLock = mPowerManager.newWakeLock(
        		(PowerManager.SCREEN_BRIGHT_WAKE_LOCK 
        				| PowerManager.FULL_WAKE_LOCK 
        				| PowerManager.ACQUIRE_CAUSES_WAKEUP), 
        		getString(R.string.system_application_name));
        wakeLock.acquire();
        
        statusCheckHandler.postDelayed(checkStatus, sDelay);
       
    }
    
    /*
     * (non-Javadoc)
     * @see android.app.Activity#onStart()
     */
    @Override
    public void onStart() {
    	
    	super.onStart();
    	
    	// check to see if we need to actually instantiate the activity
        if(SystemEventReceiver.ABORT_SHUTDOWN == true) {
        	// shutdown has been aborted, make this screen go away
        	finish();
        }
    }

    /*
     * (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
	@Override
	public void onClick(View v) {
		
		switch(v.getId()) {
		case R.id.lock_screen_ui_btn_abort:
			// abort the shutdown
			Intent mIntent = new Intent();
			mIntent.setAction(getString(R.string.system_abort_shutdown_intent));
			sendBroadcast(mIntent);
			finish();
			break;
		default:
			Log.w(sTag, "an unknown view fired the onClick event");
		}
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	public void onDestroy() {
		// release the wake lock
        wakeLock.release();
        super.onDestroy();
	}
	
	// runnable used to periodically check to see if the shutdown has been aborted
	private Runnable checkStatus = new Runnable() {
		
		public void run() {
			
			if(SystemEventReceiver.ABORT_SHUTDOWN == true) {
	        	// shutdown has been aborted, make this screen go away
	        	self.finish();
	        }
			
			statusCheckHandler.postDelayed(checkStatus, sDelay);
			
		}
	};
}
