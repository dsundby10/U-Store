package accountlogin.registrationapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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

public class ShelvingSetup extends AppCompatActivity {
    private static final String TAG = "ShelvingSetup: ";
    Spinner aisle_num_spinner, bay_num_spinner;
    EditText num_shelves;
    Button assign_shelves_btn;
    ListView mListView;
    Button main_menu_btn, absetup_btn;

    //add firebase variables
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private DatabaseReference AisleBayRef;
    private DatabaseReference AisleBayShelfRef;
    private String userID;

    String regexStr  = "^[0-9]*$";
    String alphabet = "abcdefghijklmnopqrstuvwxyz";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shelving_setup);
        setTitle("Part 3: Assign Shelving");
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_MODE_CHANGED);
        Intent intent = getIntent();
        //Prevents keyboard from auto popping up
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        aisle_num_spinner = (Spinner)findViewById(R.id.aisle_num_spinner);
        bay_num_spinner = (Spinner)findViewById(R.id.bay_num_spinner);
        num_shelves = (EditText)findViewById(R.id.num_shelves);
        assign_shelves_btn = (Button)findViewById(R.id.assign_shelves_btn);
        main_menu_btn = (Button)findViewById(R.id.view_layout_btn);
        mListView = (ListView)findViewById(R.id.listviewX);
        absetup_btn = (Button)findViewById(R.id.absetup_btn);

        //Firebase initialization
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

        AisleBayShelfRef = mFirebaseDatabase.getReference().child(userID).child("ShelfSetup");

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                }
            }
        };

        /*========== Aisle Bay DB Reference to Generate Spinner Values ====== */
        AisleBayRef = mFirebaseDatabase.getReference().child(userID).child("BaySetup");
        AisleBayRef.addListenerForSingleValueEvent(new ValueEventListener() {
            ArrayList<String>advAarr = new ArrayList<>();
            ArrayList<String>advBarr = new ArrayList<>();

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapShot : dataSnapshot.getChildren()){
                    String advAisle = (String) childSnapShot.child("aisle").getValue();
                    String advBay = (String)childSnapShot.child("bays").getValue();

                    advAarr.add(advAisle);
                    advBarr.add(advBay);

                }
                    ArrayAdapter advAadp = new ArrayAdapter(ShelvingSetup.this,android.R.layout.simple_spinner_dropdown_item, advAarr);
                        aisle_num_spinner.setAdapter(advAadp);
                        aisle_num_spinner.setSelection(0);
                        bay_num_spinner.setSelection(0);

                    //Set Bay Spinner according to the specific Aisle Num Spinner Selected
                    aisle_num_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        int currentAisleSelected = position;
                        currentAisleSelected = aisle_num_spinner.getSelectedItemPosition();
                        int bayHolder = 0;
                        ArrayList<String> currentNumBays = new ArrayList<String>();
                        for (int i=0; i < advAarr.size(); i++) {
                            if (i == currentAisleSelected) {
                                bayHolder = Integer.parseInt(advBarr.get(i));
                            }
                        }
                            for (int j = 0; j < bayHolder; j++) {
                                currentNumBays.add(String.valueOf(j));
                            }

                        ArrayAdapter advBadp = new ArrayAdapter(ShelvingSetup.this,android.R.layout.simple_spinner_dropdown_item, currentNumBays);
                        bay_num_spinner.setAdapter(advBadp);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                assign_shelves_btn.setOnClickListener(new View.OnClickListener() {
                    String a = "";
                    int bayHolder=0;

                    @Override
                    public void onClick(View v) {
                        aisle_num_spinner = (Spinner)findViewById(R.id.aisle_num_spinner);
                        String aisleSpinTxt = aisle_num_spinner.getSelectedItem().toString();

                        bay_num_spinner = (Spinner)findViewById(R.id.bay_num_spinner);
                        String baySpinTxt = bay_num_spinner.getSelectedItem().toString();


                        String strNumShelves = num_shelves.getText().toString();

                        /* == Check users input is valid first == */
                        if (strNumShelves.trim().matches(regexStr) && !strNumShelves.trim().isEmpty()) {
                            /*== Loop through to find the current aisle & bay thats selected ==*/
                            for (int i = 0; i < advAarr.size(); i++) {
                                String[] alphabetSplitter = alphabet.split("");
                                a = alphabetSplitter[i+1].toString();
                                bayHolder = Integer.parseInt(advBarr.get(i));
                                for (int j = 0; j < bayHolder; j++) {
                                    /*===== Add Shelf to current aisle & bay selected ==== */
                                    if (advAarr.get(i).equals(aisleSpinTxt) && String.valueOf(j).equals(baySpinTxt)) {
                                        myRef.child(userID).child("ShelfSetup").child(a+"AisleID" + j).setValue(String.valueOf(j));
                                        myRef.child(userID).child("ShelfSetup").child(a+"AisleID" + j).child("aisle_num").setValue(String.valueOf(i));
                                        myRef.child(userID).child("ShelfSetup").child(a+"AisleID" + j).child("bay_num").setValue(String.valueOf(j));
                                        myRef.child(userID).child("ShelfSetup").child(a+ "AisleID" + j).child("num_of_shelves").setValue(strNumShelves);
                                    }
                                }
                            }
                        } else {
                            toastMessage("Your entry must be a valid integer..");
                        }
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
                    String strHold = "\t\t\t\tAisle: " + aisleNum + "\t\t\t\tBay: " + bayNum + "\t\t\t\tShelves: " + shelfNum;

                    my_arr_list.add(strHold);

                }
                ArrayAdapter arrayAdapter = new ArrayAdapter(ShelvingSetup.this,android.R.layout.simple_list_item_1,my_arr_list);
                mListView.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        main_menu_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShelvingSetup.this, MainMenu.class);
                startActivity(intent);
            }
        });
            absetup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShelvingSetup.this, AisleBaySetup.class);
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
