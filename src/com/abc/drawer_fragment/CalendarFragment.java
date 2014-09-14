package com.abc.drawer_fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.CalendarContract;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.abc.model.R;
import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class CalendarFragment extends Fragment {

	private static final String tag = "MyCalendarActivity";
	private TextView currentMonth;
	private Button selectedDayMonthYearButton;
	private Button addEvent;
	private Button importCalendar;
	private ImageView prevMonth;
	private ImageView nextMonth;
	private GridView calendarView;
	private GridCellAdapter adapter;
	private Calendar _calendar;
	private int month, year;
	protected List<ParseObject> clientNotes;
	private ListView clientNoteText;
	private static final String dateTemplate = "MMMM yyyy";
	private ProgressDialog progressDialog;
	private List<Map<String, String>> calendarEvents;

	public CalendarFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.calendar_layout, container, false);

		Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(),
				"fonts/Quicksand-Regular.ttf");// font

		_calendar = Calendar.getInstance(Locale.getDefault());
		month = _calendar.get(Calendar.MONTH) + 1;
		year = _calendar.get(Calendar.YEAR);
		Log.d(tag, "Calendar Instance:= " + "Month: " + month + " " + "Year: "
				+ year);

		selectedDayMonthYearButton = (Button) v
				.findViewById(R.id.selectedDayMonthYear);
		addEvent = (Button) v.findViewById(R.id.addEvent);
		importCalendar = (Button) v.findViewById(R.id.importCalendar);

		selectedDayMonthYearButton.setText("Selected: ");
		selectedDayMonthYearButton.setTypeface(typeface);

		prevMonth = (ImageView) v.findViewById(R.id.prevMonth);
		prevMonth.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (month <= 1) {
					month = 12;
					year--;
				} else {
					month--;
				}
				Log.d(tag, "Setting Prev Month in GridCellAdapter: "
						+ "Month: " + month + " Year: " + year);
				setGridCellAdapterToDate(month, year);
			}

		});

		currentMonth = (TextView) v.findViewById(R.id.currentMonth);
		currentMonth.setText(DateFormat.format(dateTemplate,
				_calendar.getTime()));
		currentMonth.setTypeface(typeface);
		nextMonth = (ImageView) v.findViewById(R.id.nextMonth);
		nextMonth.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (month > 11) {
					month = 1;
					year++;
				} else {
					month++;
				}
				Log.d(tag, "Setting Next Month in GridCellAdapter: "
						+ "Month: " + month + " Year: " + year);
				setGridCellAdapterToDate(month, year);
			}

		});

		calendarView = (GridView) v.findViewById(R.id.calendar);
		clientNoteText = (ListView) v.findViewById(R.id.clientNoteText);
		progressDialog = new ProgressDialog(getActivity());

		// Initialised
		adapter = new GridCellAdapter(getActivity().getApplicationContext(),
				R.id.calendar_day_gridcell, month, year);
		adapter.notifyDataSetChanged();
		calendarView.setAdapter(adapter);

		loadClientNoteFromParse();

		addEvent.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// Create new fragment and transaction
				FragmentTransaction transaction = getFragmentManager()
						.beginTransaction();

				// Replace whatever is in the fragment_container view with this
				// fragment,
				// and add the transaction to the back stack

				ClientNote clientNote = new ClientNote();// press addEvent
															// Button will

				transaction.replace(R.id.content_frame, clientNote);
				transaction.addToBackStack(null);

				// Commit the transaction
				transaction.commit();
			}
		});
		importCalendar.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.d("importCalendar", "Onclick");
				getActivity().openContextMenu(importCalendar);
			}
		});

		registerForContextMenu(importCalendar);
		return v;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.import_calendar:
			Log.d("contextItem", "contexItem");
			readCalendarEvent();
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		getActivity().getMenuInflater().inflate(R.menu.calendar_menu, menu);
		Log.d("menu", "run menu");
	}

	/**
	 * read google user
	 */
	private void readCalendarEvent() {

		Cursor cur_event = null;

		// read google event
		String[] projection = new String[] { BaseColumns._ID,
				CalendarContract.Events.TITLE, CalendarContract.Events.DTSTART };
		cur_event = getActivity().getContentResolver().query(
				CalendarContract.Events.CONTENT_URI, projection, null, null,
				null);
		cur_event.moveToFirst();
		String allTitle = "";
		calendarEvents = new ArrayList<Map<String, String>>();
		while (cur_event.moveToNext()) {
			String title = cur_event.getString(1);
			long datetime = Long.parseLong(cur_event.getString(2));
			String dateString = (String) DateFormat.format("yyyy/MM/dd",
					datetime);
			allTitle += title + "," + dateString + ";";

			Map<String, String> item = new HashMap<String, String>();
			item.put("title", title);
			item.put("date", dateString);
			calendarEvents.add(item);

			Log.d("debug", item.toString());
		}
		// Toast.makeText(getActivity(), allTitle, Toast.LENGTH_LONG).show();
	}

	private void loadClientNoteFromParse() {
		progressDialog.setTitle("Loading...");
		progressDialog.setCancelable(false);
		progressDialog.show();

		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(
				"ClientNote"); // get Parse table:ClientNote
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> objects,
					com.parse.ParseException e) {
				if (e == null) { // put resule into a variable:clientNotes
					clientNotes = objects;
				}
				progressDialog.dismiss();
				if (adapter != null) { // when Parse changed it will notify
										// adapter
					adapter.notifyDataSetChanged();
				}
			}
		});
	}

	private void setGridCellAdapterToDate(int month, int year) {
		adapter = new GridCellAdapter(getActivity().getApplicationContext(),
				R.id.calendar_day_gridcell, month, year);
		_calendar.set(year, month - 1, _calendar.get(Calendar.DAY_OF_MONTH));
		currentMonth.setText(DateFormat.format(dateTemplate,
				_calendar.getTime()));
		adapter.notifyDataSetChanged();
		calendarView.setAdapter(adapter);
	}

	@Override
	public void onDestroy() {
		Log.d(tag, "Destroying View ...");
		super.onDestroy();
	}

	// Inner Class
	public class GridCellAdapter extends BaseAdapter implements OnClickListener {
		private static final String tag = "GridCellAdapter";
		private final Context _context;

		private final List<String> list;
		private static final int DAY_OFFSET = 1;
		private final String[] weekdays = new String[] { "Sun", "Mon", "Tue",
				"Wed", "Thu", "Fri", "Sat" };
		private final String[] months = { "January", "February", "March",
				"April", "May", "June", "July", "August", "September",
				"October", "November", "December" };
		private final int[] daysOfMonth = { 31, 28, 31, 30, 31, 30, 31, 31, 30,
				31, 30, 31 };
		private int daysInMonth;
		private int currentDayOfMonth;
		private int currentWeekDay;
		private Button gridcell;
		private TextView num_events_per_day;
		private final HashMap<String, Integer> eventsPerMonthMap;

		// Days in Current Month
		public GridCellAdapter(Context context, int textViewResourceId,
				int month, int year) {
			super();
			this._context = context;
			this.list = new ArrayList<String>();
			Log.d(tag, "==> Passed in Date FOR Month: " + month + " "
					+ "Year: " + year);
			Calendar calendar = Calendar.getInstance();
			setCurrentDayOfMonth(calendar.get(Calendar.DAY_OF_MONTH));
			setCurrentWeekDay(calendar.get(Calendar.DAY_OF_WEEK));
			Log.d(tag, "New Calendar:= " + calendar.getTime().toString());
			Log.d(tag, "CurrentDayOfWeek :" + getCurrentWeekDay());
			Log.d(tag, "CurrentDayOfMonth :" + getCurrentDayOfMonth());

			// Print Month
			printMonth(month, year);

			// Find Number of Events
			eventsPerMonthMap = findNumberOfEventsPerMonth(year, month);

		}

		private int indexOfMonth(String monthStr) {
			for (int i = 0; i < months.length; i++) {
				if (months[i].equals(monthStr)) {
					return i;
				}
			}
			return -1;
		}

		private String getMonthAsString(int i) {
			return months[i];
		}

		private String getWeekDayAsString(int i) {
			return weekdays[i];
		}

		private int getNumberOfDaysOfMonth(int i) {
			return daysOfMonth[i];
		}

		public String getItem(int position) {
			return list.get(position);
		}

		@Override
		public int getCount() {
			return list.size();
		}

		/**
		 * Prints Month
		 * 
		 * @param mm
		 * @param yy
		 */
		private void printMonth(int mm, int yy) {
			Log.d(tag, "==> printMonth: mm: " + mm + " " + "yy: " + yy);
			int trailingSpaces = 0;
			int daysInPrevMonth = 0;
			int prevMonth = 0;
			int prevYear = 0;
			int nextMonth = 0;
			int nextYear = 0;

			int currentMonth = mm - 1;
			String currentMonthName = getMonthAsString(currentMonth);
			daysInMonth = getNumberOfDaysOfMonth(currentMonth);

			Log.d(tag, "Current Month: " + " " + currentMonthName + " having "
					+ daysInMonth + " days.");

			GregorianCalendar cal = new GregorianCalendar(yy, currentMonth, 1);
			Log.d(tag, "Gregorian Calendar:= " + cal.getTime().toString());

			if (currentMonth == 11) {
				prevMonth = currentMonth - 1;
				daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth);
				nextMonth = 0;
				prevYear = yy;
				nextYear = yy + 1;
				Log.d(tag, "*->PrevYear: " + prevYear + " PrevMonth:"
						+ prevMonth + " NextMonth: " + nextMonth
						+ " NextYear: " + nextYear);
			} else if (currentMonth == 0) {
				prevMonth = 11;
				prevYear = yy - 1;
				nextYear = yy;
				daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth);
				nextMonth = 1;
				Log.d(tag, "**--> PrevYear: " + prevYear + " PrevMonth:"
						+ prevMonth + " NextMonth: " + nextMonth
						+ " NextYear: " + nextYear);
			} else {
				prevMonth = currentMonth - 1;
				nextMonth = currentMonth + 1;
				nextYear = yy;
				prevYear = yy;
				daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth);
				Log.d(tag, "***---> PrevYear: " + prevYear + " PrevMonth:"
						+ prevMonth + " NextMonth: " + nextMonth
						+ " NextYear: " + nextYear);
			}

			int currentWeekDay = cal.get(Calendar.DAY_OF_WEEK) - 1;
			trailingSpaces = currentWeekDay;

			Log.d(tag, "Week Day:" + currentWeekDay + " is "
					+ getWeekDayAsString(currentWeekDay));
			Log.d(tag, "No. Trailing space to Add: " + trailingSpaces);
			Log.d(tag, "No. of Days in Previous Month: " + daysInPrevMonth);

			if (cal.isLeapYear(cal.get(Calendar.YEAR)))
				if (mm == 2)
					++daysInMonth;
				else if (mm == 3)
					++daysInPrevMonth;

			// Trailing Month days
			for (int i = 0; i < trailingSpaces; i++) {
				Log.d(tag,
						"PREV MONTH:= "
								+ prevMonth
								+ " => "
								+ getMonthAsString(prevMonth)
								+ " "
								+ String.valueOf((daysInPrevMonth
										- trailingSpaces + DAY_OFFSET)
										+ i));
				list.add(String
						.valueOf((daysInPrevMonth - trailingSpaces + DAY_OFFSET)
								+ i)
						+ "-GREY"
						+ "-"
						+ getMonthAsString(prevMonth)
						+ "-"
						+ prevYear);
			}

			// Current Month Days
			for (int i = 1; i <= daysInMonth; i++) {
				Log.d(currentMonthName, String.valueOf(i) + " "
						+ getMonthAsString(currentMonth) + " " + yy);
				if (i == getCurrentDayOfMonth()) {

					list.add(String.valueOf(i) + "-BLUE" + "-"
							+ getMonthAsString(currentMonth) + "-" + yy);
				} else {
					list.add(String.valueOf(i) + "-WHITE" + "-"
							+ getMonthAsString(currentMonth) + "-" + yy);
				}
			}

			// Leading Month days
			for (int i = 0; i < list.size() % 7; i++) {
				Log.d(tag, "NEXT MONTH:= " + getMonthAsString(nextMonth));
				list.add(String.valueOf(i + 1) + "-GREY" + "-"
						+ getMonthAsString(nextMonth) + "-" + nextYear);
			}
		}

		/**
		 * NOTE: YOU NEED TO IMPLEMENT THIS PART Given the YEAR, MONTH, retrieve
		 * ALL entries from a SQLite database for that month. Iterate over the
		 * List of All entries, and get the dateCreated, which is converted into
		 * day.
		 * 
		 * @param year
		 * @param month
		 * @return
		 */
		private HashMap<String, Integer> findNumberOfEventsPerMonth(int year,
				int month) {
			HashMap<String, Integer> map = new HashMap<String, Integer>();

			return map;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Typeface typeface = Typeface.createFromAsset(getActivity()
					.getAssets(), "fonts/Quicksand-Regular.ttf");// font

			View row = convertView;
			if (row == null) {
				LayoutInflater inflater = (LayoutInflater) _context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				row = inflater.inflate(R.layout.screen_gridcell, parent, false);
			}

			// Get a reference to the Day gridcell
			gridcell = (Button) row.findViewById(R.id.calendar_day_gridcell);
			gridcell.setOnClickListener(this);

			// ACCOUNT FOR SPACING

			Log.d(tag, "Current Day: " + getCurrentDayOfMonth());
			String[] day_color = list.get(position).split("-");
			String theday = day_color[0];
			String themonth = day_color[2];
			String theyear = day_color[3];
			Log.d("day_color", list.get(position));

			if ((!eventsPerMonthMap.isEmpty()) && (eventsPerMonthMap != null)) {
				if (eventsPerMonthMap.containsKey(theday)) {
					num_events_per_day = (TextView) row
							.findViewById(R.id.num_events_per_day);
					num_events_per_day.setTypeface(typeface);
					Integer numEvents = (Integer) eventsPerMonthMap.get(theday);
					num_events_per_day.setText(numEvents.toString());
				}
			}

			String formatDate = String.format("%d/%02d/%02d",
					Integer.parseInt(theyear), indexOfMonth(themonth) + 1,
					Integer.parseInt(theday));

			// Set the Day GridCell
			gridcell.setText(theday);
			gridcell.setTag(formatDate);

			if (day_color[1].equals("GREY")) {
				gridcell.setTextColor(getResources()
						.getColor(R.color.lightgray));
			}
			if (day_color[1].equals("WHITE")) {
				gridcell.setTextColor(getResources().getColor(
						R.color.lightgray02));
			}
			if (clientNotes != null) { // if Parse has data it will change the
										// color:blue
				for (ParseObject clientNote : clientNotes) {
					if (clientNote.getString("date").equals(formatDate)) {
						gridcell.setTextColor(getResources().getColor(
								R.color.blue));
					}
				}
			}
			if (day_color[1].equals("BLUE")) {
				gridcell.setTextColor(getResources().getColor(R.color.orrange));
			}

			return row;
		}

		@Override
		public void onClick(View view) {
			Typeface typeface = Typeface.createFromAsset(getActivity()
					.getAssets(), "fonts/Quicksand-Regular.ttf");// font

			String selectedDate = (String) view.getTag();
			selectedDayMonthYearButton.setText("Selected: " + selectedDate);
			selectedDayMonthYearButton.setTypeface(typeface);
			Log.d("Selected date", selectedDate);

			final List<Map<String, String>> data = new ArrayList<Map<String, String>>();
			final List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

			if (calendarEvents != null) {
				for (Map<String, String> event : calendarEvents) {
					if (event.get("date").equals(selectedDate)) {
						data.add(event);
					}
				}
			}

			for (ParseObject clientNote : clientNotes) {
				if (clientNote.getString("date").equals(selectedDate)) {

					Map<String, String> item = new HashMap<String, String>();
					HashMap<String, String> item2 = new HashMap<String, String>();

					Log.d(tag, "title:" + clientNote.getString("title"));

					item.put("date", clientNote.getString("date"));
					item.put("title", clientNote.getString("title"));
					item.put("id", clientNote.getObjectId());
					data.add(item);

					item2.put("client", clientNote.getString("client"));
					Log.d(tag, "client:" + clientNote.getString("client"));
					item2.put("purpose", clientNote.getString("purpose"));
					item2.put("time", clientNote.getString("time"));
					item2.put("content", clientNote.getString("content"));
					item2.put("location", clientNote.getString("location"));
					item2.put("remarks", clientNote.getString("remarks"));
					list.add(item2);
				}
			}
			Log.d("data", data.toString());
			Log.d("item2", list.toString());

			final SimpleAdapter simpleAdapter = new SimpleAdapter(
					getActivity(), data, R.layout.calendar_listview,
					new String[] { "title", "date" }, new int[] {
							R.id.note_title, R.id.note_date });

			clientNoteText.setAdapter(simpleAdapter);
			clientNoteText.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {

					showDialogAlert(data, list, position);

				}
			});
			clientNoteText
					.setOnItemLongClickListener(new OnItemLongClickListener() {

						@Override
						public boolean onItemLongClick(AdapterView<?> parent,
								View view, int position, long id) {
							showDeleteDialog(data, list, position,
									simpleAdapter);
							return true;
						}
					});

		}

		public int getCurrentDayOfMonth() {
			return currentDayOfMonth;
		}

		private void setCurrentDayOfMonth(int currentDayOfMonth) {
			this.currentDayOfMonth = currentDayOfMonth;
		}

		public void setCurrentWeekDay(int currentWeekDay) {
			this.currentWeekDay = currentWeekDay;
		}

		public int getCurrentWeekDay() {
			return currentWeekDay;
		}

		public void showDialogAlert(List<Map<String, String>> data,
				List<HashMap<String, String>> list, int position) {

			try {
				View v = crateDiaglogView(data, list, position);
				AlertDialog.Builder builder = new AlertDialog.Builder(
						getActivity());
				builder.setView(v);
				builder.setTitle(data.get(position).get("title").toString());
				builder.show();
			} catch (Exception e) {
				// google calendar's event does not support showing dialog
			}
		}

		public void showDeleteDialog(final List<Map<String, String>> data,
				List<HashMap<String, String>> list, final int index,
				final SimpleAdapter simpleAdapter) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle("Delete");
			builder.setPositiveButton("Delete",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							String id = data.get(index).get("id");
							ParseObject obj = ParseObject.createWithoutData(
									"ClientNote", id);
							obj.deleteEventually();
							data.remove(index);
							simpleAdapter.notifyDataSetChanged();
						}
					});
			builder.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

						}
					});
			builder.show();
		}

		public View crateDiaglogView(List<Map<String, String>> data,
				List<HashMap<String, String>> list, int position) {

			Typeface typeface = Typeface.createFromAsset(getActivity()
					.getAssets(), "fonts/Quicksand-Regular.ttf");// font

			LayoutInflater factory = LayoutInflater.from(getActivity());
			View dialogView = factory
					.inflate(R.layout.calendar_show_note, null);

			// date
			TextView getDate = (TextView) dialogView.findViewById(R.id.c_date);
			getDate.setTypeface(typeface, typeface.BOLD);
			TextView getShowDate = (TextView) dialogView
					.findViewById(R.id.show_date);
			getShowDate.setTypeface(typeface);
			// time
			TextView getTime = (TextView) dialogView.findViewById(R.id.c_time);
			getTime.setTypeface(typeface, typeface.BOLD);
			TextView getShowTime = (TextView) dialogView
					.findViewById(R.id.show_time);
			getShowTime.setTypeface(typeface);
			// client
			TextView getClient = (TextView) dialogView
					.findViewById(R.id.c_client);
			getClient.setTypeface(typeface, typeface.BOLD);
			TextView getShowClient = (TextView) dialogView
					.findViewById(R.id.show_client);
			getShowClient.setTypeface(typeface);
			// purpose
			TextView getPurpose = (TextView) dialogView
					.findViewById(R.id.c_purpose);
			getPurpose.setTypeface(typeface, typeface.BOLD);
			TextView getShowPurpose = (TextView) dialogView
					.findViewById(R.id.show_purpose);
			getShowPurpose.setTypeface(typeface);
			// content
			TextView getContent = (TextView) dialogView
					.findViewById(R.id.c_content);
			getContent.setTypeface(typeface, typeface.BOLD);
			TextView getShowContent = (TextView) dialogView
					.findViewById(R.id.show_content);
			getShowContent.setTypeface(typeface);
			// location
			TextView getLocation = (TextView) dialogView
					.findViewById(R.id.c_location);
			getLocation.setTypeface(typeface, typeface.BOLD);
			TextView getShowLocation = (TextView) dialogView
					.findViewById(R.id.show_location);
			getShowLocation.setTypeface(typeface);
			// remarks
			TextView getRemarks = (TextView) dialogView
					.findViewById(R.id.c_remarks);
			getRemarks.setTypeface(typeface, typeface.BOLD);
			TextView getShowRemarks = (TextView) dialogView
					.findViewById(R.id.show_remarks);
			getShowRemarks.setTypeface(typeface);

			String s = " ";
			String date = null;
			String client = null;
			String time = null;
			String location = null;
			String content = null;
			String remarks = null;
			String purpose = null;

			// if data is null it will crash

			date = data.get(position).get("date").toString();
			if (date != null) {
				getShowDate.setText(date);
			} else {
				getShowDate.setText(s);
			}

			if (list.get(position).get("client") != null) {
				client = list.get(position).get("client").toString();
				getShowClient.setText(client);

			} else {
				getShowClient.setText(s);
			}

			time = list.get(position).get("time").toString();
			if (time != null) {
				getShowTime.setText(time);

			} else {
				getShowTime.setText(s);
			}

			location = list.get(position).get("location").toString();
			if (location != null) {
				getShowLocation.setText(location);
			} else {
				getShowLocation.setText(s);
			}

			content = list.get(position).get("content").toString();
			if (content != null) {
				getShowContent.setText(content);
			} else {
				getShowContent.setText(s);
			}

			remarks = list.get(position).get("remarks").toString();
			if (remarks != null) {
				getShowRemarks.setText(remarks);
			} else {
				getShowRemarks.setText(s);
			}

			if (list.get(position).get("purpose") != null) {
				purpose = list.get(position).get("purpose").toString();
				getShowPurpose.setText(purpose);

			} else {
				getShowPurpose.setText(s);
			}

			return dialogView;

		}
	}
}
