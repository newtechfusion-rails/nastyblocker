package com.blocker.nastyblocker;

import java.lang.reflect.Method;
import java.util.ArrayList;
//import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;
import com.blocker.nastyblocker.R;
public class MainActivity extends ListActivity {

	private static final int PICK_CONTACT_REQUEST = 0;
	CheckBox isEnabled;
	Button contact;
	Button block;
	EditText number;
	DBTools db;
	String name;
	ArrayAdapter<String> adapter;

	AutoAnswerNotifier mNotifier;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);



		try{
			db=new DBTools(this);
			ArrayList<String> numbers=db.getAllNumbers();

			adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, numbers);
			setListAdapter(adapter);
		}
		catch(Exception e){
			e.printStackTrace();
			Log.i("error","mysterious erriorrhnakskjahsjkdhasd");
		}


		initialize();

		mNotifier = new AutoAnswerNotifier(this);
		isEnabled.setChecked(AutoAnswerNotifier.enabled);
		PhoneCallListener phoneCallListener = new PhoneCallListener();
		TelephonyManager telManager = (TelephonyManager) MainActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
		telManager.listen(phoneCallListener, PhoneStateListener.LISTEN_CALL_STATE);
	}

	private void initialize(){
		isEnabled=(CheckBox) findViewById(R.id.blocking);
		contact=(Button) findViewById(R.id.contact);
		number=(EditText) findViewById(R.id.number);
		block=(Button) findViewById(R.id.block);
		setActionListeners();
	}


	private void setActionListeners(){
		setActionListenerBlocking();
		setActionListenerContact();
		setActionListenerBlock();

	}
	private void setActionListenerBlock() {

		block.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				String localNumber=stripToNumbers(number.getText().toString());
				if(localNumber.length()>5){

					try {
						db.insertContact(localNumber,name);
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Exception e) {

						Context context = getApplicationContext();
						CharSequence text = "you already blocked this contact";
						int duration = Toast.LENGTH_SHORT;



						Toast toast = Toast.makeText(context, text, duration);
						toast.setGravity(Gravity.CENTER|Gravity.CENTER, 0, 0);
						toast.show();
						return;

					}
					adapter.add(localNumber);
					adapter.notifyDataSetChanged();
				}
				else
					showMessageNumberToShort();

			}
		});
	}

	private void pickContact() {
		Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
		pickContactIntent.setType(Phone.CONTENT_TYPE); // Show user only contacts w/ phone numbers
		startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
	}
	private void setActionListenerContact(){
		contact.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				pickContact();

			}
		});

	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check which request it is that we're responding to
		if (requestCode == PICK_CONTACT_REQUEST) {
			// Make sure the request was successful
			if (resultCode == RESULT_OK) {
				// Get the URI that points to the selected contact
				Uri contactUri = data.getData();
				// We only need the NUMBER column, because there will be only one row in the result
				String[] projection = {Phone.NUMBER,Phone.DISPLAY_NAME};

				// Perform the query on the contact to get the NUMBER column amd the name
				// We don't need a selection or sort order (there's only one result for the given URI)
				// CAUTION: The query() method should be called from a separate thread to avoid blocking
				// your app's UI thread. (For simplicity of the sample, this code doesn't do that.)
				// Consider using CursorLoader to perform the query.
				Cursor cursor = getContentResolver()
						.query(contactUri, projection, null, null, null);
				cursor.moveToFirst();

				// Retrieve the phone number from the NUMBER column
				int column = cursor.getColumnIndex(Phone.NUMBER);
				int column2=cursor.getColumnIndex(Phone.DISPLAY_NAME);
				name=cursor.getString(column2);
				String number = cursor.getString(column);

				// Do something with the phone number...
				this.number.setText(number);
			}
		}
	}
	private void setActionListenerBlocking(){
		isEnabled.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				mNotifier.updateNotification(isEnabled);

			}
		});
	}
	@Override
	protected void onListItemClick(ListView l,View v,int position, long id){
		super.onListItemClick(l, v, position, id);
		String contact=((TextView)(v)).getText().toString();
		db.deleteContact(contact);
		adapter.remove(contact);
	}
	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}*/







	private class PhoneCallListener extends PhoneStateListener {

		String TAG = "LOGGING PHONE CALL";
		boolean closeCall=false;

		//private boolean phoneCalling = false;

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {

			Context context = getBaseContext();
			boolean blocked=isBlocked(incomingNumber);
			
			if (blocked && TelephonyManager.CALL_STATE_RINGING == state) {

				Log.i(TAG, "RINGING, number: " + incomingNumber);
				//if(incomingNumber is in the database)
				// then block it
				// Call a service, since this could take a few seconds
					try {
						answerPhoneAidl(context);
						Log.i("AutoAnswerIntentService","tried answering with answerPhoneAidl");
					}
					catch (Exception e) {
						e.printStackTrace();
						Log.i("AutoAnswer","Error trying to answer using telephony service.  Falling back to headset.");
						answerPhoneHeadsethook(context);
						Log.i("AutoAnswerIntentService","tried answering with answerPhoneHeadsethook");
					}
					closeCall=true;
			}

			


			if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
				// active
				//Log.i("hohohohohohohohohohohohohoh", "answered, number: " + incomingNumber);
				Log.i("closeCall",closeCall+"");
					TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
					@SuppressWarnings("rawtypes")
					Class clazz;
					try {
						clazz = Class.forName(telephonyManager.getClass().getName());

						@SuppressWarnings("unchecked")
						Method method = clazz.getDeclaredMethod("getITelephony");
						method.setAccessible(true);
						ITelephony telephonyService = (ITelephony) method.invoke(telephonyManager);
						if(closeCall)
						telephonyService.endCall();
						closeCall=false;
						
					}
					catch(Exception e){
						Log.i("error in the answering method itself", "answered, number: " + incomingNumber);
						e.printStackTrace();
					}
				}
			}


		}

	private void answerPhoneHeadsethook(Context context) {
		// Simulate a press of the headset button to pick up the call
		Intent buttonDown = new Intent(Intent.ACTION_MEDIA_BUTTON);		
		buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
		context.sendOrderedBroadcast(buttonDown, "android.permission.CALL_PRIVILEGED");

		// froyo and beyond trigger on buttonUp instead of buttonDown
		Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);		
		buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
		context.sendOrderedBroadcast(buttonUp, "android.permission.CALL_PRIVILEGED");
	}

	@SuppressWarnings("unchecked")
	private void answerPhoneAidl(Context context) throws Exception {
		// Set up communication with the telephony service (thanks to Tedd's Droid Tools!)
		TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		@SuppressWarnings("rawtypes")
		Class c = Class.forName(tm.getClass().getName());
		Method m = c.getDeclaredMethod("getITelephony");
		m.setAccessible(true);
		ITelephony telephonyService;
		telephonyService = (ITelephony)m.invoke(tm);

		// Silence the ringer and answer the call!
		telephonyService.silenceRinger();
		telephonyService.answerRingingCall();
	}

	private boolean isBlocked(String Number){

		return db.getAllNumbers().contains(Number);

	}
	private void showMessageNumberToShort(){
		Context context = getApplicationContext();
		CharSequence text = "The number must be at least 6 digits";
		int duration = Toast.LENGTH_SHORT;



		Toast toast = Toast.makeText(context, text, duration);
		toast.setGravity(Gravity.CENTER|Gravity.CENTER, 0, 0);
		toast.show();
		return;
	}
	private String stripToNumbers(String str){
		str = str.replaceAll("[^\\d]", "");
		return str;
	}

}
