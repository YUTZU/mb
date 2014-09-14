package com.abc.drawer_fragment;

import android.app.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.abc.model.R;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class ClientNote extends Fragment {

	EditText m_titleText;
	EditText m_contentText;
	EditText m_locationText;
	EditText m_remarksText;
	Spinner remindSpinner;
	Button m_datepickerButton = null;
	Button m_timepickerButton = null;
	Button saveButton;
	Calendar c = null;

	private ProgressDialog progressDialog;

	String[] remindTime = new String[] { "10 minutes ago", "15 minutes ago",
			"30 minutes ago", "1 hour ago", "3 hour ago", "12 hour ago",
			"1 day ago" };

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater
				.inflate(R.layout.client_note_layout, container, false);

		remindSpinner = (Spinner) v.findViewById(R.id.remindSpinner);
		m_titleText = (EditText) v.findViewById(R.id.titleText);
		m_contentText = (EditText) v.findViewById(R.id.contextText);
		m_locationText = (EditText) v.findViewById(R.id.locationText);
		m_remarksText = (EditText) v.findViewById(R.id.remarksText);
		m_datepickerButton = (Button) v.findViewById(R.id.datepickerButton);
		m_timepickerButton = (Button) v.findViewById(R.id.timepickerButton);
		saveButton = (Button) v.findViewById(R.id.save);
		// remind Spinner
		ArrayAdapter<String> adapterTime = new ArrayAdapter<String>(
				this.getActivity(), android.R.layout.simple_spinner_item,
				remindTime);
		adapterTime
				.setDropDownViewResource(android.R.layout.simple_spinner_item);
		remindSpinner.setAdapter(adapterTime);

		progressDialog = new ProgressDialog(getActivity());

		if (container == null)
			return null;

		// DatePickerDialog
		m_datepickerButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				onCreateDialog(m_datepickerButton).show();
			}
		});

		// TimePickerDialog
		m_timepickerButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onCreateDialog2(m_timepickerButton).show();
			}
		});

		saveButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String title = m_titleText.getText().toString();
				String content = m_contentText.getText().toString();
				String date = m_datepickerButton.getText().toString();
				String time = m_timepickerButton.getText().toString();
				String location = m_locationText.getText().toString();
				String remarks = m_remarksText.getText().toString();

				progressDialog.setCancelable(false);
				progressDialog.setTitle("Loading...");
				progressDialog.show();

				ParseObject object = new ParseObject("ClientNote");
				object.put("title", title);
				object.put("content", content);
				object.put("date", date);
				object.put("time", time);
				object.put("location", location);
				object.put("remarks", remarks);
				object.setACL(new ParseACL(ParseUser.getCurrentUser()));
				object.saveInBackground(new SaveCallback() {

					@Override
					public void done(ParseException e) {
						progressDialog.dismiss();
						if (e == null) {
							Toast.makeText(getActivity(), "Successful",
									Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(getActivity(), "Error",
									Toast.LENGTH_SHORT).show();
						}

					}
				});
			}
		});
		return v;
	}

	// DatePickerDialog
	protected Dialog onCreateDialog(final Button btn) {
		Dialog dialog = null;
		c = Calendar.getInstance();
		dialog = new DatePickerDialog(getActivity(),
				new DatePickerDialog.OnDateSetListener() {
					public void onDateSet(DatePicker dp, int year, int month,
							int dayOfMonth) {
						String text = String.format("%d/%02d/%02d", year,
								(month + 1), dayOfMonth);
						btn.setText(text);
					}
				}, c.get(Calendar.YEAR), c.get(Calendar.MONTH),
				c.get(Calendar.DAY_OF_MONTH));
		return dialog;
	}

	// TimePickerDialog
	protected Dialog onCreateDialog2(final Button btn) {
		Dialog dialog2 = null;
		c = Calendar.getInstance();
		dialog2 = new TimePickerDialog(getActivity(),
				new TimePickerDialog.OnTimeSetListener() {
					public void onTimeSet(TimePicker view, int hourOfDay,
							int minute) {
						btn.setText(hourOfDay + ":" + minute);
					}
				}, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
		return dialog2;
	}

}