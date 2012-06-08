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

import com.stericson.RootTools.RootTools;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * main activity for the Serval Auto Shutdown application
 */
public class MainActivity extends Activity implements OnClickListener {
	
	/*
	 * class level constants
	 */
	private final String sTag = "MainActivity";
	
	private final int sCheckForRootDialog = 1;
	private final int sRootDeniedDialog = 2;
	private final int sIsEnabledDialog = 3;
	private final int sIsDisabledDialog = 4;
	
	/*
	 * private class level variables
	 */
	private boolean isEnabled = false;
	
	private Button enableButton = null;
    
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // configure the buttons
        Button mButton = (Button) findViewById(R.id.main_ui_btn_settings);
        mButton.setOnClickListener(this);
        
        enableButton = (Button) findViewById(R.id.main_ui_btn_enable);
        enableButton.setOnClickListener(this);
        
        // update the button label if necessary
        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        
        if(mPreferences.getBoolean(getString(R.string.system_enable_shutdown_preference), false) == true) {
        	enableButton.setText(getString(R.string.main_ui_btn_disable));
        	isEnabled = true;
        }
        
        mButton = (Button) findViewById(R.id.main_ui_btn_about);
        mButton.setOnClickListener(this);
        
    }

    /*
     * (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
	@Override
	public void onClick(View v) {
		
		Intent mIntent;
		
		// work out which button was touched
		switch(v.getId()){
		case R.id.main_ui_btn_settings:
			// settings button
			mIntent = new Intent(this, org.servalproject.autoshutdown.SettingsActivity.class);
			startActivity(mIntent);
			break;
		case R.id.main_ui_btn_enable:
			// enable button touched
			// check to see if su is available
			
			if(isEnabled == false) {
				showDialog(sCheckForRootDialog);
			} else {
				// disable the autoshutdown
				setPreference(false);
				isEnabled = false;
				enableButton.setText(getString(R.string.main_ui_btn_enable));
				showDialog(sIsDisabledDialog);
			}
			
			break;
		case R.id.main_ui_btn_about:
			// about button touched
			mIntent = new Intent(this, org.servalproject.autoshutdown.AboutActivity.class);
			startActivity(mIntent);
			break;
		default:
			Log.w(sTag, "an unknown view fired the onClick event");
		}
	}
	
	/*
	 * callback method used to construct the required dialog
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {

		AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
		Dialog mDialog = null;

		switch(id) {
		case sCheckForRootDialog:
			// show an alert dialog
			mBuilder = new AlertDialog.Builder(this);
			mBuilder.setMessage(R.string.main_ui_dialog_check_for_root)
			.setCancelable(false)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					// check for root and enable Auto Shutdown
					enableAutoShutdown();
				}
			})
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			mDialog = mBuilder.create();
			break;
		case sRootDeniedDialog:
			// show an alert dialog
			mBuilder = new AlertDialog.Builder(this);
			mBuilder.setMessage(R.string.main_ui_dialog_root_denied)
			.setCancelable(false)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			mDialog = mBuilder.create();
			break;
		case sIsEnabledDialog:
			// show an alert dialog
			mBuilder = new AlertDialog.Builder(this);
			mBuilder.setMessage(R.string.main_ui_dialog_enabled)
			.setCancelable(false)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			mDialog = mBuilder.create();
			break;
		case sIsDisabledDialog:
			mBuilder = new AlertDialog.Builder(this);
			mBuilder.setMessage(R.string.main_ui_dialog_disabled)
			.setCancelable(false)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			mDialog = mBuilder.create();
			break;
		default:
			Log.w(sTag, "unknown dialog requested");
			mDialog = null;
		}
		
		return mDialog;
	}
	
	/*
	 * private function to check for root and enable auto shutdown
	 */
	private void enableAutoShutdown() {
		
		// check for root
		if(RootTools.isAccessGiven() == false) {
			setPreference(false);
			showDialog(sRootDeniedDialog);
		} else {
			// set the preference to enable autoshutdown
			setPreference(true);
			showDialog(sIsEnabledDialog);
			enableButton.setText(getString(R.string.main_ui_btn_disable));
			isEnabled = true;
		}
	}
	
	/*
	 * private function to edit a preference
	 */
	private void setPreference(boolean value) {
		
		SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
		SharedPreferences.Editor mEditor = mPreferences.edit();
		
		mEditor.putBoolean(getString(R.string.system_enable_shutdown_preference), value);
		
		// TODO once on API 9 or above use apply not commit
		mEditor.commit();
	}
}