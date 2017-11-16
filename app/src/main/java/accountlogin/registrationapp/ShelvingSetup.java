package accountlogin.registrationapp;

import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class ShelvingSetup extends AppCompatActivity {
    private static final String TAG = "ShelvingSetup: ";
    Spinner aisle_num_spinner, bay_num_spinner;
    EditText num_shelves;
    Button assign_shelves_btn;
    ListView mListView;
    Button main_menu_btn, absetup_btn;
    TextView aisleTextView, bayTextView;

    //Added
    CheckBox checkBox;
    Spinner setupOptions;

    //add firebase variables
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private DatabaseReference AisleBayRef;
    private DatabaseReference ShelfRef;
    private String userID;

    ArrayList<String>advBarr = new ArrayList<>();
    ArrayList<Integer>advBBArr = new ArrayList<>();
    ArrayList<Integer>advAarr = new ArrayList<>();


    ArrayList<Integer> crntAisle = new ArrayList<>();
    ArrayList<Integer> crntBay = new ArrayList<>();
    ArrayList<Integer> crntShelf = new ArrayList<>();
    ArrayList<String> ABSarr = new ArrayList<>();
    ArrayList<Integer> zSortAll = new ArrayList<Integer>();
    ArrayList<Integer> allSortedBay = new ArrayList<>();
    ArrayList<String> my_arr_list = new ArrayList<>();


    ArrayList<String> updateAllShelves = new ArrayList<>();

    int userSelectedBay = 0;
    int userSelectedAisle=0;
    String strSort;
    String regexStr  = "^[0-9]*$";
    String alphabet = "abcdefghijklmnopqrstuvwxyz";

    String updateAll = "notSelected";

    int measuredWidth = 0;
    int measuredHeight = 0;

    //Intent Data Variables
    private String getStoreName = "";
    private String employeeID;
    private String getUserPermissions="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shelving_setup);
        setTitle("Part 3: Assign Shelving");
        Intent intent = getIntent();
        getStoreName = intent.getStringExtra("STORE_NAME");
        getUserPermissions = intent.getStringExtra("USER_PERMISSIONS");
        employeeID = intent.getStringExtra("STORE_USER");

        measuredWidth = 0;
        measuredHeight = 0;

        Point size = new Point();
        WindowManager w = getWindowManager();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            w.getDefaultDisplay().getSize(size);
            measuredWidth = size.x;
            measuredHeight = size.y;
            System.out.println(measuredHeight + " <--H-|-W--> " + measuredWidth);
        } else {
            Display d = w.getDefaultDisplay();
            measuredWidth = d.getWidth();
            measuredHeight = d.getHeight();
        }

        //Layout Variables Initialization
        aisle_num_spinner = (Spinner) findViewById(R.id.aisle_num_spinner);
        bay_num_spinner = (Spinner) findViewById(R.id.bay_num_spinner);
        num_shelves = (EditText) findViewById(R.id.num_shelves);
        assign_shelves_btn = (Button) findViewById(R.id.assign_shelves_btn);
        main_menu_btn = (Button) findViewById(R.id.main_menu_btn);
        mListView = (ListView) findViewById(R.id.listViewX);
        absetup_btn = (Button) findViewById(R.id.absetup_btn);


        //Added
        aisleTextView = (TextView) findViewById(R.id.aisleTextView);
        bayTextView = (TextView) findViewById(R.id.bayTextView);
        checkBox = (CheckBox) findViewById(R.id.checkBox);
        setupOptions = (Spinner) findViewById(R.id.setupOptions);
        setupOptions.setEnabled(false);
        setupOptions.setVisibility(View.INVISIBLE);

        aisle_num_spinner.setX(40);
        aisleTextView.setX(30);
        bayTextView.setX(bay_num_spinner.getX());
        num_shelves.setX(bay_num_spinner.getX() + 222);
        assign_shelves_btn.setX((measuredWidth / 2) + (measuredWidth / 4));

        /*=== Quick Shelf Setup Spinner Options ===*/
        ArrayList<String> options = new ArrayList<>();
        options.add("Assign All Shelves Individually");
        options.add("Assign All Bays Same # Shelves");
        options.add("Assign Specific Aisles Same # of Shelves");
        ArrayAdapter optionAdapter = new ArrayAdapter(ShelvingSetup.this, android.R.layout.simple_spinner_dropdown_item, options);
        setupOptions.setAdapter(optionAdapter);

        /*==== setupOptions (Spinner) ====*/
        setupOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                int crntOption = setupOptions.getSelectedItemPosition();
                if (crntOption == 0) {
                    aisle_num_spinner.setX(40);
                    aisleTextView.setX(30);
                    bayTextView.setX(bay_num_spinner.getX());
                    num_shelves.setX(bay_num_spinner.getX() + 222);
                    assign_shelves_btn.setX((measuredWidth / 2) + (measuredWidth / 4));

                    aisleTextView.setVisibility(View.VISIBLE);
                    bayTextView.setVisibility(View.VISIBLE);
                    aisle_num_spinner.setVisibility(View.VISIBLE);
                    bay_num_spinner.setVisibility(View.VISIBLE);
                    aisle_num_spinner.setEnabled(true);
                    bay_num_spinner.setEnabled(true);


                }
                if (crntOption == 1) {

                    float getNewBtnXaxis = (measuredWidth / 2);
                    float getNewShelfXaxis = (measuredWidth / 2) - 250;

                    aisleTextView.setVisibility(View.INVISIBLE);
                    bayTextView.setVisibility(View.INVISIBLE);
                    aisle_num_spinner.setVisibility(View.INVISIBLE);
                    bay_num_spinner.setVisibility(View.INVISIBLE);
                    aisle_num_spinner.setEnabled(false);
                    bay_num_spinner.setEnabled(false);

                    num_shelves.setX(getNewShelfXaxis);
                    assign_shelves_btn.setX(getNewBtnXaxis);

                }
                if (crntOption == 2) {

                    aisleTextView.setVisibility(View.VISIBLE);
                    aisle_num_spinner.setVisibility(View.VISIBLE);
                    aisle_num_spinner.setEnabled(true);

                    bayTextView.setVisibility(View.INVISIBLE);
                    bay_num_spinner.setVisibility(View.INVISIBLE);
                    bay_num_spinner.setEnabled(false);

                    int spacing = 50;
                    aisleTextView.setX((measuredWidth / 5) - (spacing + 10));
                    aisle_num_spinner.setX((measuredWidth / 5) - spacing);
                    num_shelves.setX((measuredWidth / 5) * 2);
                    assign_shelves_btn.setX((measuredWidth / 5) * 3 + spacing);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        /*=== Checkbox Listener (Set Visible/Invisible Options) ===*/
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    setupOptions.setEnabled(true);
                    setupOptions.setVisibility(View.VISIBLE);

                } else {
                    setupOptions.setEnabled(false);
                    setupOptions.setVisibility(View.INVISIBLE);
                    setupOptions.setSelection(0);
                }
            }
        });

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

        /*========== Aisle Bay DB Reference to Generate Spinner Values ====== */
        AisleBayRef = mFirebaseDatabase.getReference().child(userID).child("BaySetup");
        AisleBayRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                advBarr = new ArrayList<>();
                advAarr = new ArrayList<>();
                advBBArr = new ArrayList<>();
                for (DataSnapshot childSnapShot : dataSnapshot.getChildren()) {
                    String advAisle = (String) childSnapShot.child("aisle").getValue();
                    String advBay = (String) childSnapShot.child("bays").getValue();
                    if (advAisle != null && advBay != null && advBBArr != null) {
                        advBarr.add(advBay);
                        advBBArr.add(Integer.parseInt(advBay));
                        advAarr.add(Integer.parseInt(advAisle));
                    }
                }

                Collections.sort(advBBArr);
                Collections.sort(advAarr);
                strSort = "";
                int sortBay = 1;
                zSortAll = new ArrayList<>();
                allSortedBay = new ArrayList<>();
                for (int i = 0; i < advBBArr.size(); i++) {
                    if (advBBArr.get(i) != null) {
                        sortBay = advBBArr.get(i);
                    }
                    for (int j = 1; j <= sortBay; j++) {
                        zSortAll.add(j);
                        if (j == sortBay) {
                            Collections.sort(zSortAll);
                            for (int k = 0; k < zSortAll.size(); k++) {
                                strSort += zSortAll.get(k).toString() + ",";
                            }
                            zSortAll.clear();
                        }
                    }
                }
                String[] bayArr = strSort.split("\\s*,\\s*");
                for (int i = 0; i < bayArr.length; i++) {
                    allSortedBay.add(Integer.parseInt(bayArr[i]));
                }

                ArrayAdapter advAadp = new ArrayAdapter(ShelvingSetup.this, android.R.layout.simple_spinner_dropdown_item, advAarr);
                aisle_num_spinner.setAdapter(advAadp);
                aisle_num_spinner.setSelection(0);

                //Set Bay Spinner according to the specific Aisle Num Spinner Selected
                aisle_num_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        int currentAisleSelected = position;
                        int userBay = userSelectedBay;
                        currentAisleSelected = aisle_num_spinner.getSelectedItemPosition();
                        int bayHolder = 0;

                        for (int i = 0; i < advAarr.size(); i++) {
                            if (i == currentAisleSelected) {
                                bayHolder = Integer.parseInt(advBarr.get(i));
                            }
                        }
                        ArrayList<String> currentNumBays = new ArrayList<String>();
                        for (int j = 1; j <= bayHolder; j++) {
                            currentNumBays.add(String.valueOf(j));
                        }

                        ArrayAdapter advBadp = new ArrayAdapter(ShelvingSetup.this, android.R.layout.simple_spinner_dropdown_item, currentNumBays);
                        bay_num_spinner.setAdapter(advBadp);
                        bay_num_spinner.setSelection(userSelectedBay);
                        userSelectedBay=0;

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                /*=== Assign Shelf onClick Listener === */
                assign_shelves_btn.setOnClickListener(new View.OnClickListener() {
                    String a = "";
                    int bayHolder = 0;

                    @Override
                    public void onClick(View v) {
                        aisle_num_spinner = (Spinner) findViewById(R.id.aisle_num_spinner);
                        String aisleSpinTxt = aisle_num_spinner.getSelectedItem().toString();
                        bay_num_spinner = (Spinner) findViewById(R.id.bay_num_spinner);
                        String baySpinTxt = null;
                        if (bay_num_spinner != null && bay_num_spinner.getSelectedItem() != null) {
                            baySpinTxt = (String) bay_num_spinner.getSelectedItem();
                        } else {

                        }

                        String strNumShelves = num_shelves.getText().toString();

                        /*=== Assign all Bays Same # Shelves ===*/
                        if (checkBox.isChecked() && setupOptions.getSelectedItemPosition() == 1) {
                            /* == Check users input is valid first == */
                            if (strNumShelves.trim().matches(regexStr) && baySpinTxt != null && strNumShelves.trim().length() >= 1) {
                                updateAllShelves = new ArrayList<String>();
                                updateAll = "selected";
                                /*== Loop through to find the current aisle & bay thats selected ==*/
                                for (int i = 0; i < advAarr.size(); i++) {
                                    String[] alphabetSplitter = alphabet.split("");
                                    a = alphabetSplitter[i + 1].toString();
                                    bayHolder = Integer.parseInt(advBarr.get(i));
                                    for (int j = 1; j <= bayHolder; j++) {
                                    /*===== Add Shelf to current aisle & bay selected ==== */
                                        myRef.child(userID).child("ShelfSetup").child(a + "AisleID" + j).setValue(String.valueOf(j));
                                        myRef.child(userID).child("ShelfSetup").child(a + "AisleID" + j).child("aisle_num").setValue(String.valueOf(i + 1));
                                        myRef.child(userID).child("ShelfSetup").child(a + "AisleID" + j).child("bay_num").setValue(String.valueOf(j));
                                        myRef.child(userID).child("ShelfSetup").child(a + "AisleID" + j).child("num_of_shelves").setValue(strNumShelves.trim());
                                        String insertData = "Aisle: " + String.valueOf(i+1) + " Bay: " + String.valueOf(j) + " Shelves: " + strNumShelves.trim();
                                        updateAllShelves.add(insertData);
                                    }
                                }

                            } else {
                                toastMessage("Select a valid bay & enter a valid integer");
                            }

                        }

                        /*=== Assign Specific Aisle Same # of Shelves ===*/
                        if (checkBox.isChecked() && setupOptions.getSelectedItemPosition() == 2) {
                            /* == Check users input is valid first == */
                            if (strNumShelves.trim().matches(regexStr) && baySpinTxt != null && strNumShelves.trim().length() >= 1) {
                                /*== Loop through to find the current aisle & bay thats selected ==*/
                                for (int i = 0; i < advAarr.size(); i++) {
                                    String[] alphabetSplitter = alphabet.split("");
                                    a = alphabetSplitter[i + 1].toString();
                                    bayHolder = Integer.parseInt(advBarr.get(i));
                                    for (int j = 1; j <= bayHolder; j++) {
                                    /*===== Add Shelf to current aisle & bay selected ==== */
                                        if (advAarr.get(i) == Integer.parseInt(aisleSpinTxt)) {
                                            myRef.child(userID).child("ShelfSetup").child(a + "AisleID" + j).setValue(String.valueOf(j));
                                            myRef.child(userID).child("ShelfSetup").child(a + "AisleID" + j).child("aisle_num").setValue(String.valueOf(i + 1));
                                            myRef.child(userID).child("ShelfSetup").child(a + "AisleID" + j).child("bay_num").setValue(String.valueOf(j));
                                            myRef.child(userID).child("ShelfSetup").child(a + "AisleID" + j).child("num_of_shelves").setValue(strNumShelves.trim());
                                        }
                                    }
                                }

                            } else {
                                toastMessage("Please select a valid bay & enter a valid integer");
                            }
                        }

                        if (!checkBox.isChecked() || setupOptions.getSelectedItemPosition() == 0) {
                        /* == Check users input is valid first == */
                            if (strNumShelves.trim().matches(regexStr) && baySpinTxt != null && strNumShelves.trim().length() >= 1) {
                            /*== Loop through to find the current aisle & bay thats selected ==*/
                                for (int i = 0; i < advAarr.size(); i++) {
                                    String[] alphabetSplitter = alphabet.split("");
                                    a = alphabetSplitter[i + 1].toString();
                                    bayHolder = Integer.parseInt(advBarr.get(i));

                                    for (int j = 1; j <= bayHolder; j++) {
                                    /*===== Add Shelf to current aisle & bay selected ==== */
                                        if (advAarr.get(i) == Integer.parseInt(aisleSpinTxt) && String.valueOf(j).equals(baySpinTxt)) {
                                            myRef.child(userID).child("ShelfSetup").child(a + "AisleID" + j).setValue(String.valueOf(j));
                                            myRef.child(userID).child("ShelfSetup").child(a + "AisleID" + j).child("aisle_num").setValue(String.valueOf(i + 1));
                                            myRef.child(userID).child("ShelfSetup").child(a + "AisleID" + j).child("bay_num").setValue(String.valueOf(j));
                                            myRef.child(userID).child("ShelfSetup").child(a + "AisleID" + j).child("num_of_shelves").setValue(strNumShelves.trim());
                                        }
                                    }
                                }
                            } else {
                                toastMessage("Please select a valid bay & enter a valid integer");
                            }
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mListView = (ListView)findViewById(R.id.listViewX);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!checkBox.isChecked() || setupOptions.getSelectedItemPosition() == 0) {
                    userSelectedAisle = crntAisle.get(position) - 1;

                    if (aisle_num_spinner.getSelectedItemPosition() == userSelectedAisle) {
                        bay_num_spinner.setSelection(allSortedBay.get(position) - 1);
                    } else {
                        userSelectedBay = allSortedBay.get(position) - 1;
                        aisle_num_spinner.setSelection(crntAisle.get(position) - 1);
                    }
                }
            }
        });


        /*====================Display & Update Listview=================*/
        ShelfRef = mFirebaseDatabase.getReference().child(userID).child("ShelfSetup");
        ShelfRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                my_arr_list = new ArrayList<String>();
                crntAisle = new ArrayList<>();
                crntBay = new ArrayList<>();
                crntShelf = new ArrayList<>();
                ABSarr = new ArrayList<String>();

                for (DataSnapshot childSnapShot : dataSnapshot.getChildren()) {
                    String aisleNum = (String) childSnapShot.child("aisle_num").getValue();
                    String bayNum = (String) childSnapShot.child("bay_num").getValue();
                    String shelfNum = (String) childSnapShot.child("num_of_shelves").getValue();
                    String strz = aisleNum + "," + bayNum + "," + shelfNum;
                    if (aisleNum != null && bayNum != null && shelfNum != null) {
                        crntAisle.add(Integer.parseInt(aisleNum));
                        crntBay.add(Integer.parseInt(bayNum));
                        crntShelf.add(Integer.parseInt(shelfNum));
                        my_arr_list.add(strz);
                    }
                }
                if (updateAll.equals("selected")){
                    mListView = (ListView) findViewById(R.id.listViewX);
                    ArrayAdapter arrayAdapter = new ArrayAdapter(ShelvingSetup.this, android.R.layout.simple_list_item_1, updateAllShelves);
                    mListView.setAdapter(arrayAdapter);
                    updateAll = "notSelected";
                } else {
                    ABSarr = sortedABSlist();
                    mListView = (ListView) findViewById(R.id.listViewX);
                    ArrayAdapter arrayAdapter = new ArrayAdapter(ShelvingSetup.this, android.R.layout.simple_list_item_1, ABSarr);
                    mListView.setAdapter(arrayAdapter);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        main_menu_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShelvingSetup.this, MainMenu.class);
                sendIntentData(intent);
            }
        });
        absetup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShelvingSetup.this, AisleBaySetup.class);
                sendIntentData(intent);
            }
        });
    }

        public ArrayList<String> sortedABSlist () {
            ArrayList<String> returnList = new ArrayList<String>();
            String a = "";
            String[] myABS = new String[3];
            int cont = 0;
            int sortBay = 1;
            int sortAisle = 1;
            for (int i = 0; i < allSortedBay.size(); i++) {
                if (i != 0 && allSortedBay.get(i) == 1) {
                    cont++;
                }
                sortBay = allSortedBay.get(i);
                sortAisle = advAarr.get(cont);

                for (int j = 0; j < my_arr_list.size(); j++) {
                        a = my_arr_list.get(j);
                        myABS = a.split("\\s*,\\s*");
                    if (Integer.parseInt(myABS[0]) == sortAisle && Integer.parseInt(myABS[1]) == sortBay) {
                        returnList.add("Aisle: " + myABS[0] + " Bay: " + myABS[1] + " Shelves: " + myABS[2]);
                      //  System.out.println("removed: " + my_arr_list.get(j) + " Size Before: " + my_arr_list.size() );
                        my_arr_list.remove(j);
                        break;
                    }
                }
            }

            return returnList;
        }

    public void sendIntentData(Intent intent){
        intent.putExtra("STORE_USER", employeeID);
        intent.putExtra("STORE_NAME", getStoreName);
        intent.putExtra("USER_PERMISSIONS", getUserPermissions);
        startActivity(intent);
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
