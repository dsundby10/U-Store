package accountlogin.registrationapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ShelvingAddEditActivity extends AppCompatActivity {

    private static final String TAG = "ShelvingSetup: ";
    Spinner aisle_num_spinner, bay_num_spinner;
    EditText num_shelves;
    Button assign_shelves_btn, view_layout_btn, main_menu_btn;
    ListView mListView;

    //add firebase variables
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private DatabaseReference AisleBayRef;
    private DatabaseReference AisleBayShelfRef;
    private String userID;

    int aisleSpinInt = 0;
    int baySpinInt = 0;
    int count = 0;
    int arrCounter = 0;
    ArrayList<String> aisle_id_arr;
    ArrayList<String> aisle_num_arr;
    ArrayList<String> bay_num_arr;
    ArrayList<String> num_of_shelves;
    ArrayList<String> my_arr_listview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shelving_add_edit);
        Intent intent = getIntent();
        //Prevents keyboard from auto popping up
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        aisle_num_spinner = (Spinner) findViewById(R.id.aisle_num_spinner);
        bay_num_spinner = (Spinner) findViewById(R.id.bay_num_spinner);
        num_shelves = (EditText) findViewById(R.id.num_shelves);
        assign_shelves_btn = (Button) findViewById(R.id.assign_shelves_btn);
        view_layout_btn = (Button) findViewById(R.id.view_layout_btn);
        main_menu_btn = (Button)findViewById(R.id.view_layout_btn);
        mListView = (ListView) findViewById(R.id.listviewX);

        //Firebase initialization
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {

                }
            }
        };
        AisleBayRef = mFirebaseDatabase.getReference().child(userID).child("BaySetup");
        AisleBayRef.addListenerForSingleValueEvent(new ValueEventListener() {
            ArrayList<String> advAarr = new ArrayList<>();
            ArrayList<String> advBarr = new ArrayList<>();

            String totalNumBays = "";
            String totalNumAisles = "";

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //try array creation here
                for (DataSnapshot childSnapShot : dataSnapshot.getChildren()) {
                    String advAisle = (String) childSnapShot.child("aisle").getValue();
                    String advBay = (String) childSnapShot.child("bays").getValue();

                    advAarr.add(advAisle);
                    advBarr.add(advBay);
                    totalNumBays += advBay + " ";
                    totalNumAisles += advAisle + " ";
                }
                ArrayAdapter advAadp = new ArrayAdapter(ShelvingAddEditActivity.this, android.R.layout.simple_spinner_dropdown_item, advAarr);
                aisle_num_spinner.setAdapter(advAadp);
                aisle_num_spinner.setSelection(0);
                bay_num_spinner.setSelection(0);
                String[] NumBaysList = totalNumBays.split(" ");
                int sumBays = 0;
                for (int i = 0; i < NumBaysList.length; i++) {
                    int sumBayHold = Integer.parseInt(NumBaysList[i]);
                    sumBays += sumBayHold;
                }

                final int totalMaxEntries = sumBays;
                //Set Bay Spinner according to the specific Aisle Num Spinner Selected
                aisle_num_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        int currentAisleSelected = position;
                        currentAisleSelected = aisle_num_spinner.getSelectedItemPosition();
                        int bayHolder = 0;
                        ArrayList<String> currentNumBays = new ArrayList<String>();
                        for (int i = 0; i < advAarr.size(); i++) {
                            if (i == currentAisleSelected) {
                                bayHolder = Integer.parseInt(advBarr.get(i));
                            }
                        }
                        for (int j = 0; j < bayHolder; j++) {
                            currentNumBays.add(String.valueOf(j));
                        }

                        ArrayAdapter advBadp = new ArrayAdapter(ShelvingAddEditActivity.this, android.R.layout.simple_spinner_dropdown_item, currentNumBays);
                        bay_num_spinner.setAdapter(advBadp);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                assign_shelves_btn.setOnClickListener(new View.OnClickListener() {
                    String tv = "";
                    String a = "";
                    String alphabet = "abcdefghijklmnopqrstuvwxyz";
                    int dbCount = 0;
                    String[] abc = new String[totalMaxEntries];
                    int bayHolder = 0;

                    @Override
                    public void onClick(View v) {
                        //only need to create the initial array once during each visit to this page
                       // if (count == 0) {
                            aisle_id_arr = new ArrayList<String>();
                            aisle_num_arr = new ArrayList<String>();
                            bay_num_arr = new ArrayList<String>();
                            num_of_shelves = new ArrayList<String>();
                            my_arr_listview = new ArrayList<String>();
                            dbCount=0;
                            for (int i = 0; i < advAarr.size(); i++) {
                                String[] alphabetSplitter = alphabet.split("");
                                a = alphabetSplitter[i + 1].toString();
                                bayHolder = Integer.parseInt(advBarr.get(i));
                                //Log.i("bayHolder", bayHolder + "");
                                for (int j = 0; j < bayHolder; j++) {
                                    abc[i] = a; //organizes the AisleId's in the database


                                    String aisle_id = myRef.child(userID).child("ShelfSetup").child(a + "AisleID" + j).child(String.valueOf(j)).getKey();
                                    String aisle_num_id = myRef.child(userID).child("ShelfSetup").child(a + "AisleID" + j).child("aisle_num").child(String.valueOf(i)).getKey();
                                    String bay_num_id = myRef.child(userID).child("ShelfSetup").child(a + "AisleID" + j).child("bay_num").child(String.valueOf(j)).getKey();
                                    String shelf_num_id = myRef.child(userID).child("ShelfSetup").child(a + "AisleID" + j).child("num_of_shelves").child(String.valueOf(0)).getKey();


                                    aisle_id_arr.add(aisle_id);
                                    aisle_num_arr.add(aisle_num_id);
                                    bay_num_arr.add(bay_num_id);
                                    num_of_shelves.add(shelf_num_id);


                                    String string_ABS = aisle_id + " " + bay_num_id + " " + shelf_num_id;

                                    my_arr_listview.add(dbCount,string_ABS);
                                    Log.i("ArrCreate: ", my_arr_listview.get(dbCount));

                                    dbCount++;
                                }
                            }
                       // }

                        aisle_num_spinner = (Spinner) findViewById(R.id.aisle_num_spinner);
                        String aisleSpinTxt = aisle_num_spinner.getSelectedItem().toString();
                        aisleSpinInt = Integer.parseInt(aisleSpinTxt);

                        bay_num_spinner = (Spinner) findViewById(R.id.bay_num_spinner);
                        String baySpinTxt = bay_num_spinner.getSelectedItem().toString();
                        baySpinInt = Integer.parseInt(baySpinTxt);

                        String strNumShelves = num_shelves.getText().toString();

                        int placer = 0;

                        //Loop through for however many AisleID: keys were initially added
                        for (int i = 0; i < advAarr.size(); i++) {
                            bayHolder = Integer.parseInt(advBarr.get(i));
                            for (int j = 0; j < bayHolder; j++) {
                                if (advAarr.get(i).equals(aisleSpinTxt) && String.valueOf(j).equals(baySpinTxt)) {
                                   // Log.i("CheckAddShelf", abc[placer]+"AisleID:" + i + " " + aisleSpinTxt + " " + baySpinTxt);
                                    myRef.child(userID).child("ShelfSetup").child(abc[placer] + "AisleID" + j).child("num_of_shelves").setValue(strNumShelves);
                                    toastMessage("Success: " + strNumShelves + " Shelves Added To\n" +"\t\t\t\t" + "Aisle: "  + aisleSpinTxt + " at Bay: " + baySpinTxt);
                                }
                                Log.i("ArrCounter", " " + arrCounter);

                            }
                            placer++;
                        }
                        count++;

                        //Prevents keyboard from auto popping up
                        ShelvingAddEditActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    }

                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        /*====================Display & Update Listview=================*/
        AisleBayShelfRef = mFirebaseDatabase.getReference().child(userID).child("ShelfSetup");
        AisleBayShelfRef.addValueEventListener(new ValueEventListener() {
            ArrayList<String> my_arr_list = new ArrayList<>();
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                my_arr_list=new ArrayList<String>();
                ArrayList<String> shelfHold = new ArrayList<String>();
                for (DataSnapshot childSnapShot : dataSnapshot.getChildren()) {
                    String aisleNum = (String) childSnapShot.child("aisle_num").getValue();
                    String bayNum = (String) childSnapShot.child("bay_num").getValue();
                    String shelfNum = (String) childSnapShot.child("num_of_shelves").getValue();
                    String strHold = aisleNum + " " + bayNum + " " + shelfNum;
                    my_arr_list.add(strHold);
                }
                ArrayAdapter arrayAdapter = new ArrayAdapter(ShelvingAddEditActivity.this,android.R.layout.simple_list_item_1,my_arr_list);
                mListView.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        view_layout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShelvingAddEditActivity.this, StoreLayoutActivity.class);
                startActivity(intent);
            }
        });

        main_menu_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShelvingAddEditActivity.this, MainMenu.class);
                startActivity(intent);
            }
        });

    }
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    private void toastMessage(String message) {
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }
}



