package com.keetab;

import com.keetab.model.ViewerSettings;

import android.support.v4.app.DialogFragment;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;

/**
 * This dialog displays the viewer settings to the user.
 * The model is represented by the class {@link ViewerSettings}
 *
 */
@SuppressLint("ValidFragment")
public class ViewerSettingsDialog extends DialogFragment {
	
	/**
	 * Interface to notify the listener when a viewer settings have been changed.
	 */
	public interface OnViewerSettingsChange {
		public void onViewerSettingsChange(ViewerSettings settings);
	}

	protected static final String TAG = "ViewerSettingsDialog";
	
	private OnViewerSettingsChange mListener;

	private ViewerSettings mOriginalSettings;
	
	public ViewerSettingsDialog(OnViewerSettingsChange listener, ViewerSettings originalSettings) {
		mListener = listener;
		mOriginalSettings = originalSettings;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		getDialog().setTitle(R.string.settings);
		View dialogView = inflater.inflate(R.layout.viewer_settings, container);
		Log.i(TAG, "mOriginalSettings: "+mOriginalSettings);
		
		
		final SeekBar fontSizeBar = (SeekBar) dialogView.findViewById(R.id.fontSize);
		fontSizeBar.setProgress(mOriginalSettings.getFontSize() - 50);
		
		final RadioGroup columns = (RadioGroup)dialogView.findViewById(R.id.columns);
		if (mOriginalSettings.isSyntheticSpread()) {
		    columns.check(R.id.columns_double);
		} else {
		    columns.check(R.id.columns_single);
		}
		
		Button ok = (Button) dialogView.findViewById(R.id.ok);
		ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mListener != null) {
					int fontSize = fontSizeBar.getProgress() + 50;
					int columnGap = 20;
					boolean spread = columns.getCheckedRadioButtonId() == R.id.columns_double;
					ViewerSettings settings = new ViewerSettings(spread, fontSize, columnGap);
					mListener.onViewerSettingsChange(settings);
				}
				dismiss();
			}
		});
		
		Button cancel = (Button) dialogView.findViewById(R.id.cancel);
		cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		return dialogView;
	}

}
