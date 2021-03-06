package com.werds.ishowup.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.werds.ishowup.R;
import com.werds.ishowup.dbcommunication.DatabaseCommunicator;
import com.werds.ishowup.dbcommunication.DatabaseReader;
import com.werds.ishowup.ui.MyRecordFragment.MyAdapter;

public class ListViewFragment extends Fragment implements OnRefreshListener {

	private static final String ATTENDANCE_HISTORY_URL = "http://web.engr.illinois.edu/~ishowup4cs411/cgi-bin/attendancehistory.php";
	public static final String POSITION_KEY = "com.burnside.embeddedfragmenttest.POSITION";
	private SharedPreferences sp;
	private int sectionPosition;

	private PullToRefreshLayout mPullToRefreshLayout;
	private ListView listView;

	
	public static ListViewFragment newInstance(int position) {
		ListViewFragment fragment = new ListViewFragment();
		fragment.setPosition(position);
		return fragment;
	}

	public void setPosition(int position) {
		this.sectionPosition = position;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_test, container,
				false);
		listView = (ListView) rootView.findViewById(R.id.list);
		listView.setAdapter(new SimpleAdapter(getActivity(), getData(sectionPosition),
				R.layout.item, new String[] { "record_date", "record_status" },
				new int[] { R.id.record_date, R.id.record_status }));
		
		mPullToRefreshLayout = (PullToRefreshLayout)rootView.findViewById(R.id.ptr_layout);
		// Now setup the PullToRefreshLayout
		ActionBarPullToRefresh.from(getActivity())
	            // Mark All Children as pullable
	            .allChildrenArePullable()
	            // Set the OnRefreshListener
	            .listener(this)
	            // Finally commit the setup to our PullToRefreshLayout
	            .setup(mPullToRefreshLayout);
		
		
		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

	}

	private List<Map<String, Object>> getData(int position) {

		List<Map<String, Object>> listViewItems = new ArrayList<Map<String, Object>>();

		sp = getActivity().getSharedPreferences("userInfo", 0);
		Set<String> allSections = sp.getStringSet("allSections",
				new LinkedHashSet<String>());
		String[] allSectionsArray = allSections.toArray(new String[0]);
		String netID = sp.getString("NetID", null);

		String currSection = allSectionsArray[position].replace(' ', '_');
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("netid", netID);
		parameters.put("sectionfullname", currSection);
		DatabaseCommunicator historyReader = new DatabaseCommunicator(ATTENDANCE_HISTORY_URL);
		String historyDataRaw = historyReader.execute(parameters);
		//Map<String, Object> currHistory = new LinkedHashMap<String, Object>();

		try {
			JSONObject historyResultJSON = new JSONObject(historyDataRaw);
			String historyStatus = historyResultJSON.getString("Status");
			if (historyStatus.equals("OK")) {
				JSONObject historyDataJSON = historyResultJSON.getJSONObject("Data");
				Iterator<String> it = historyDataJSON.keys();
				while (it.hasNext()) {
					Map<String, Object> currHistory = new LinkedHashMap<String, Object>();
					String currKey = it.next();
					boolean currValue = historyDataJSON.getBoolean(currKey);
					Date currSectionDate = new SimpleDateFormat("MMddyyyy").parse(currKey.substring(1));
					DateFormat format = new SimpleDateFormat("EEE MMM d, yyyy");
					String currSectionDateFormatted = format.format(currSectionDate);
					int currSectionHistoryFormatted = currValue ? R.drawable.check : R.drawable.cross;
					currHistory.put("record_date", currSectionDateFormatted);
					currHistory.put("record_status", currSectionHistoryFormatted);
					listViewItems.add(currHistory);
				}
			}
			if (historyStatus.equals("EMPTY")) {
				Map<String, Object> currHistory = new LinkedHashMap<String, Object>();
				currHistory.put("record_date", "You don't have any attendance records for this section.");
				currHistory.put("record_status", R.drawable.check);
				listViewItems.add(currHistory);
			}
		} catch (Exception e) {
			Map<String, Object> currHistory = new LinkedHashMap<String, Object>();
			currHistory.put("record_date", "Internal Error");
			currHistory.put("record_status", R.drawable.cross);
			listViewItems.add(currHistory);
		}
		//listViewItems.add(currHistory);
		return listViewItems;
	}


	@Override
	public void onRefreshStarted(View view) {
		new AsyncTask<Void, Void, Void>() {
			
			private List<Map<String, Object>> historyData; 
			
			@Override
			protected Void doInBackground(Void... params) {
				
				//historyData = getData(sectionPosition);
				//mPullToRefreshLayout.setRefreshComplete();

				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// Notify PullToRefreshLayout that the refresh has finished
				/*listView.setAdapter(new SimpleAdapter(getActivity(), historyData,
						R.layout.item, new String[] { "record_date", "record_status" },
						new int[] { R.id.record_date, R.id.record_status }));*/
				mPullToRefreshLayout.setRefreshComplete();
			}

			
		}.execute();

		
	}

}
