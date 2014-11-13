package app.nrs.specialtopic.tag;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HomeActivity extends Activity implements OnClickListener {

	private static final int NUMBER_OF_FIELDS_IN_A_ROW = 3;
	private Button buttonPlus;
	private Button buttonMinues;
	private TextView textViewIncoming;
	private TextView textViewOutgoing;
    private EditText date = null;
    String username = "";
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    String pastEntries = "";
    List<Entries> pastEntriesList;
    private int tableRowIdCounter;
    Set<String> namesOnTable;
    private int innerCounter;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
        username = getIntent().getStringExtra("username");

		buttonPlus = (Button) findViewById(R.id.buttonPlus);
		buttonMinues = (Button) findViewById(R.id.buttonMinus);
		buttonMinues.setOnClickListener(this);
		buttonPlus.setOnClickListener(this);
        paintTable();
	}

    public void paintTable(){
        try {
            TableLayout tableLayout = (TableLayout) findViewById(R.id.tableLayout);
            tableLayout.removeAllViewsInLayout();
            namesOnTable = new HashSet<String>();
            pastEntriesList = Entries.find(Entries.class, "owner=?", username);
            Entries tempEntry, tempEntry1;
            Iterator<Entries> iterator = pastEntriesList.iterator();
            Iterator<Entries> iterator1;
            double incValue = 0;
            double outValue = 0;

            while(iterator.hasNext()){
                tempEntry = iterator.next();
                iterator1 = pastEntriesList.iterator();
                Boolean positive;//= tempEntry.type.equals("positive");
                double amount = 0;
                if(!(namesOnTable.contains(tempEntry.name))) {
                    namesOnTable.add(tempEntry.name);
                    while (iterator1.hasNext()) {
                        tempEntry1 = iterator1.next();
                        if (tempEntry.name.equals(tempEntry1.name)) {
                            if (tempEntry1.type.equals("positive")) {
                                amount += Double.parseDouble(tempEntry1.amount);
                                incValue += Double.parseDouble(tempEntry1.amount);
                            } else {
                                amount -= Double.parseDouble(tempEntry1.amount);
                                outValue += Double.parseDouble(tempEntry1.amount);
                            }

                        }
                    }
                    if (amount > 0.0) {
                        positive = true;
                        printEntry(tempEntry.name, "" + amount, tempEntry.dueDate, positive, R.id.tableLayout);
                    } else {
                        positive = false;
                        printEntry(tempEntry.name, "" + Math.abs(amount), tempEntry.dueDate, positive, R.id.tableLayout);
                    }

                }
            }// end of while
            textViewIncoming = (TextView) findViewById(R.id.textViewIncoming);
            String text = textViewIncoming.getText().toString();
            String newText = text.substring(0, 15) + ": "
                    + String.valueOf(incValue);
            textViewIncoming.setText(newText);

            textViewOutgoing = (TextView) findViewById(R.id.textViewOutgoing);
            text = textViewOutgoing.getText().toString();
            newText = text.substring(0, 15) + ": "
                    + String.valueOf(outValue);
            textViewOutgoing.setText(newText);
        }
        catch(NullPointerException e){
            Log.d("database_debug", "Empty Database. Starting new.");
            //else start new. :)
        }
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}

    Calendar myCalendar = Calendar.getInstance();

    final DatePickerDialog.OnDateSetListener dateDialog = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(android.widget.DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }

    };



    private void updateLabel() {

        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.ENGLISH);

        date.setText(sdf.format(myCalendar.getTime()));
    }



    @Override
	public void onClick(final View v) {
		// TODO Auto-generated method stub
        pastEntriesList = Entries.find(Entries.class, "owner=?", username);
		if (v.getId() == R.id.buttonPlus) {
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Enter Data");
			final EditText name = new EditText(this);
			name.setHint("Enter The Name");
			final EditText amount = new EditText(this);
			amount.setInputType(InputType.TYPE_CLASS_NUMBER);
			amount.setHint("Enter Amount");
			date = new EditText(this);
            date.setInputType(InputType.TYPE_NULL);

            date.setHint("Enter Date");
			LinearLayout layout = new LinearLayout(getApplicationContext());
			layout.setOrientation(LinearLayout.VERTICAL);
			layout.addView(name);
			layout.addView(amount);
			layout.addView(date);
            date.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    new DatePickerDialog(HomeActivity.this, dateDialog, myCalendar
                            .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                            myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                }
            });
			alert.setView(layout);

			alert.setPositiveButton("Add",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// Positive
							String targetName = name.getText().toString();
							String targetAmount = amount.getText().toString();
							String targetDate = date.getText().toString();

                            //-----------------------------------------------//
                            //                      DB entry
                            Entries entry = new Entries(targetName, (new Date()).toString(), targetDate, targetAmount, "positive", username);
                            Log.d("debug", entry.name);
                            entry.save();
                            //Entry done. :)
                            paintTable();

						}
					});

			alert.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
						}
					});

			alert.show();
		} else if (v.getId() == R.id.buttonMinus) {
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Enter Data");
			final EditText name = new EditText(this);
			name.setHint("Enter The Name");
			final EditText amount = new EditText(this);
			amount.setInputType(InputType.TYPE_CLASS_NUMBER);
			amount.setHint("Enter Amount");
            date = new EditText(this);

			date.setHint("Enter Date");
			LinearLayout layout = new LinearLayout(getApplicationContext());
			layout.setOrientation(LinearLayout.VERTICAL);
			layout.addView(name);
			layout.addView(amount);
			layout.addView(date);
            date.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    new DatePickerDialog(HomeActivity.this, dateDialog, myCalendar
                            .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                            myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                }
            });
			alert.setView(layout);

			alert.setPositiveButton("Add",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// Negative

							String targetName = name.getText().toString();
							String targetAmount = amount.getText().toString();
							String targetDate = date.getText().toString();
//							printEntry(targetName, targetAmount, targetDate,
//                                    false);
                            //-----------------------------------------------//
                            //                      DB entry
                            Entries entry = new Entries(targetName, (new Date()).toString(), targetDate, targetAmount, "negative", username);
                            entry.save();
                            paintTable();
                            //Entry done. :)

						}
					});

			alert.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
						}
					});
            alert.show();

		} else if (v.getId() <= tableRowIdCounter && v.getId() >= 0) {

            TableRow thisRow = (TableRow)findViewById(v.getId());
            String userName = ((TextView)thisRow.getChildAt(0)).getText().toString();
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle("Details");
            final TableLayout tableList = new TableLayout(this);

            LayoutParams tableRowParams = new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            TextView textViewNote = new TextView(HomeActivity.this);
            textViewNote.setText("Long tap on an entry to delete.");

            TableRow tableRowNote = new TableRow(this);

            textViewNote.setLayoutParams(tableRowParams);
            textViewNote.setGravity(Gravity.CENTER_HORIZONTAL);
            tableRowNote.addView(textViewNote);

            //adding a horizontal line
            View horizontalRule = new View(getApplicationContext());
            horizontalRule.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            horizontalRule.setMinimumHeight(2);
            horizontalRule.setBackgroundColor(Color.GRAY);
            tableRowNote.addView(horizontalRule);

            Iterator<Entries> iterator = pastEntriesList.iterator();
            while (iterator.hasNext()) {
                Entries varEntry = iterator.next();
               if(userName.equalsIgnoreCase(varEntry.name)){
                   final TableRow tempTableRow = new TableRow(this);
                   tempTableRow.setId(innerCounter++);
                   tempTableRow.setClickable(true);

                   String[] varValues = {varEntry.name, varEntry.amount, varEntry.dueDate};

                   tableRowParams.weight = 1;
                   for (int i = 0; i < NUMBER_OF_FIELDS_IN_A_ROW; i++) {
                       TextView textview = new TextView(HomeActivity.this);
                       textview.setText(varValues[i]);
                       if (varEntry.type.equalsIgnoreCase("positive")) {
                           textview.setTextColor(Color.BLUE);
                       } else {
                           textview.setTextColor(Color.RED);
                       }
                       textview.setLayoutParams(tableRowParams);
                       textview.setGravity(Gravity.CENTER_HORIZONTAL);
                       textview.setTextSize(18);
                       tempTableRow.addView(textview);
                   }

                   tempTableRow.setOnLongClickListener(new View.OnLongClickListener() {
                       @Override
                       public boolean onLongClick(View v) {
                           String userName = ((TextView)tempTableRow.getChildAt(0)).getText().toString();
                           String userAmt = ((TextView)tempTableRow.getChildAt(1)).getText().toString();
                           String userDate = ((TextView)tempTableRow.getChildAt(2)).getText().toString();

                           Log.d("values name, amt, date", userName + " - " + userAmt + " - " + userDate);

                           List<Entries> removedEntry = Entries.find(Entries.class, "name=? and amount=?", userName, userAmt);
                           Iterator<Entries> itr = removedEntry.iterator();
                           Entries delEntry = (Entries)itr.next();

                           delEntry.delete();
                           ((TableLayout) tempTableRow.getParent()).removeView(tempTableRow);
                           paintTable();
                           return true;
                       }
                   });

                   tableList.addView(tempTableRow, new LinearLayout.LayoutParams(
                           LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
               }
            }

            alert.setView(tableList);

            alert.setPositiveButton("Contact " + userName,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            //fire up contacts intent.
                            Intent callIntent = new Intent(Intent.ACTION_DIAL);
                            startActivity(callIntent);
                        }
                    });

            alert.setNegativeButton("---",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                           //something else
                        }
                    });
            alert.show();
        }

	}

	private void printEntry(String name, String amount, String date,
                            boolean positive, int tableID) {

        if(name == null|| amount == null || date == null){
            Toast.makeText(getApplicationContext(), "Please Enter all details", Toast.LENGTH_SHORT);
        }
        else {
            String[] addData = {name, amount, date};
            TableRow tempTableRow = new TableRow(this);
            tempTableRow.setId(tableRowIdCounter++);
            tempTableRow.setClickable(true);
           // String amt = ""+amount;
            String type;
            if(positive){
                type = "positive";
            }
            else{
                type = "negative";
            }

            // tr1.setOnClickListener(onClickFunction);
            // Maybe Later to access specific entry :: tr1.setId("");
            // tempTableRow.setPadding(0, 5, 0, 5);
            LayoutParams tableRowParams = new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            tableRowParams.weight = 1;

            for (int i = 0; i < NUMBER_OF_FIELDS_IN_A_ROW; i++) {
                TextView textview = new TextView(HomeActivity.this);
                textview.setText(addData[i]);
                if (positive) {
                    textview.setTextColor(Color.BLUE);
                } else {
                    textview.setTextColor(Color.RED);
                }
                textview.setLayoutParams(tableRowParams);
                textview.setGravity(Gravity.CENTER_HORIZONTAL);
                textview.setTextSize(18);
                tempTableRow.addView(textview);
                tempTableRow.setOnClickListener(this);
            }

            TableLayout tableLayout = (TableLayout) findViewById(tableID);
            tableLayout.addView(tempTableRow, new TableLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }
	}
}