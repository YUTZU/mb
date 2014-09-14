package com.abc.drawer_fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.abc.model.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class MessageList extends Fragment {

	public MessageList() {
	}

	ListView listView;
	Button addEvent;

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.message_listview, container, false);
		listView = (ListView) v.findViewById(R.id.listView);
		addEvent = (Button) v.findViewById(R.id.addButton);
		loadDataFromParse();
		addEvent.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				FragmentManager fragmentManager = getFragmentManager();
				fragmentManager.beginTransaction()
						.replace(R.id.content_frame, new Message()).commit();
			}
		});
		return v;
	}

	private void loadDataFromParse() {
		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Message");
		query.orderByDescending("createdAt");
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> objects, ParseException e) {

				final ArrayList<Map<String, String>> data = new ArrayList<Map<String, String>>();
				final ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();

				for (ParseObject ob : objects) {
					Map<String, String> item = new HashMap<String, String>();
					item.put("Message_receiver",
							ob.getString("Message_receiver"));
					item.put("Message_content", ob.getString("Message_content"));
					item.put("id", ob.getObjectId());
					Log.d("Message_receiver", ob.getString("Message_receiver"));
					Log.d("Message_content", ob.getString("Message_content"));
					data.add(item);

					Map<String, String> item2 = new HashMap<String, String>();
					item2.put("Message_receiver",
							ob.getString("Message_receiver"));
					item2.put("Message_content",
							ob.getString("Message_content"));
					item2.put("id", ob.getObjectId());
					list.add(item2);
				}

				try {
					final SimpleAdapter adapter = new SimpleAdapter(
							getActivity(),
							data,
							android.R.layout.simple_list_item_2,
							new String[] { "Message_receiver",
									"Message_content" },
							new int[] { android.R.id.text1, android.R.id.text2 });
					listView.setAdapter(adapter);

					listView.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> parent,
								View view, int position, long id) {
							// TODO Auto-generated method stub
							sendValueToClientNoteView(list, position);
						}

						private void sendValueToClientNoteView(
								ArrayList<Map<String, String>> list,
								int position) {
							// TODO Auto-generated method stub
							Bundle bundle = new Bundle();
							Log.d("list[position]", list.get(position)
									.toString());
							ArrayList arrayList = new ArrayList();
							arrayList.add(list.get(position));
							bundle.putParcelableArrayList("arrayList",
									arrayList);

							Bundle bundle2 = new Bundle();
							bundle2.putBundle("bundle2", bundle);
							MessageView messageView = new MessageView();
							messageView.setArguments(bundle2);
							getActivity().getFragmentManager()
									.beginTransaction()
									.replace(R.id.content_frame, messageView)
									.commit();
						}
					});

					listView.setOnItemLongClickListener(new OnItemLongClickListener() {

						@Override
						public boolean onItemLongClick(AdapterView<?> parent,
								View view, int position, long id) {

							showDeleteDialog(data, position, adapter);
							// Toast.makeText(getActivity(), "deleted",
							// Toast.LENGTH_LONG).show();

							return true;
						}
					});

				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		});

	}

	public void showDeleteDialog(final List<Map<String, String>> data,
			final int index, final SimpleAdapter adapter) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Delete");
		builder.setMessage("Do you want to delete?");
		builder.setPositiveButton("Delete",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String Object_id = data.get(index).get("id");

						Log.d("id", Object_id);
						ParseObject obj = ParseObject.createWithoutData(
								"Message", Object_id);
						obj.deleteEventually();
						data.remove(index);

						adapter.notifyDataSetChanged();
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

}