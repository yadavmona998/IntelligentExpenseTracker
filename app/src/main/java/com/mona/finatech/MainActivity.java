package com.mona.finatech;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends ActionBarActivity {
    Activity myactivity;
    public static String[] Banklist = { "BOI", "PNB", "SBI","ICICI","HDFC","SBH","CB","BOR","BOB","IDBI","UCO","ORB"};
    public static  String[] months = {"January","February","March","April","May","June","July","August","September","October","November","December"};
    float[] Amount;     //array to store monthly expense
    String TAG="tag";
    int bank_msg_flag=0;
    TextView no_sms_text;
    ListView ls;
    HashMap<String,float []> datamodel;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myactivity=this;
        no_sms_text=(TextView)findViewById(R.id.nosms_text);
        Amount = new float[20];
        Arrays.fill(Amount, 0);
        ls=(ListView)findViewById(R.id.mylistview);
        ls.setVisibility(View.GONE);
        datamodel=new HashMap<>();



        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int cyear = cal.get(Calendar.YEAR);



        Uri message = Uri.parse("content://sms/inbox");
        ContentResolver cr = myactivity.getContentResolver();
        Cursor c = cr.query(message, null, null, null, null);
        myactivity.startManagingCursor(c);
        int totalSMS = c.getCount();
        if(c.moveToFirst())
        {
            for(int i=0;i<totalSMS;i++)
            {
                String sender=c.getString(c.getColumnIndex("address"));
                if(IsFromBank(sender))
                {   bank_msg_flag=1;
                    String msgbody = c.getString(c.getColumnIndexOrThrow("body"));
                    Log.d(TAG,"message "+msgbody);
                        if (msgbody.contains("debited")||msgbody.contains("dr")||msgbody.contains("Debited")||msgbody.contains("DEBITED"))

                        {   Pattern regEx = Pattern.compile("[rR][sS](\\s*.\\s*\\d*)");
                            // Find instance of pattern matches
                            Matcher m = regEx.matcher(msgbody);
                            Log.d(TAG,"debited");
                            if (m.find()) {
                            try {

                                Log.e("amount_value= ", "" + m.group(0));
                                String amount = (m.group(0).replaceAll("inr", ""));
                                amount = amount.replaceAll("rs", "");
                                amount = amount.replaceAll("inr", "");
                                amount = amount.replaceAll(" ", "");
                                amount = amount.replaceAll(",", "");
                                amount=amount.substring(2);
                                float amt = Float.parseFloat(amount);
                                Log.e("matchedValue= ", "" + amount);
                                Log.d(TAG,"found the amount as"+amt);




                                String ms = c.getString(c.getColumnIndexOrThrow("date"));
                                long milliseconds = Long.parseLong(ms);
                                Date date = new Date(milliseconds);

                                cal.setTime(date);
                                int myear = cal.get(Calendar.YEAR);
                                int month = cal.get(Calendar.MONTH);
                                //entering dummy data in hashmap
                                String datamodel_key="";
                                datamodel_key+=myear;
                                if(datamodel.containsKey(datamodel_key))

                                        datamodel.get(datamodel_key)[month]+=amt;
                                else
                                    {

                                        float []Amount_temp;
                                        Amount_temp = new float[20];
                                        Arrays.fill(Amount_temp, 0);
                                        datamodel.put(datamodel_key, Amount_temp);
                                        datamodel.get(datamodel_key)[month]+=amt;
                                    }












                             } catch (Exception e) {
                                e.printStackTrace();
                            }
                            }
                            else {
                            Log.e("No_matchedValue ", "No_matchedValue ");
                             }

                        }




                }

                else
                {//handle case not from bank
                //
                    Log.d(TAG,"not contains any msgs particularly from bank");
                }
                c.moveToNext();
            }
        }
        else{
            Log.d(TAG,"don't contains any msgs");
        }


       if(bank_msg_flag==0)
          no_sms_text.setText("Your mobile hasn't any transaction related messages");
        else
       {    String result="";
           Iterator myiterator=  datamodel.keySet().iterator();
           while(myiterator.hasNext()) {
               String key=(String)myiterator.next();
               float []month_amt=datamodel.get(key);
               for(int i=0;i<12;i++)
               {
                   String s1=months[i]+" : "+String.valueOf(month_amt[i])+" \n";
                   result+=s1;
               }


           }




           no_sms_text.setText(result);
       }

    }



    public static boolean IsFromBank(String inputstr)
    {
        for(int i =0; i < Banklist.length; i++)
        {
            if(inputstr.contains(Banklist[i]))
            {
                return true;
            }
        }
        return false;
    }

}
