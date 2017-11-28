package accountlogin.registrationapp;

import android.content.Intent;
import android.graphics.Color;
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
import android.widget.LinearLayout;
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
    LinearLayout linearLayoutOption1, linearLayoutOption2, linearLayoutOption3;
    Spinner aisle_num_spinner, bay_num_spinner, setupOptions,aisle_num_spinner2;
    EditText num_shelves, num_shelves2, num_shelves3;
    Button assign_shelves_btn, absetup_btn, main_menu_btn, assign_shelves_btn2, assign_shelves_btn3;
    ListView mListView;
    CheckBox checkBox;

    //Array Lists used to hold values and/or sort values
    ArrayList<Integer> advBBArr = new ArrayList<>();
    ArrayList<Integer>advAarr = new ArrayList<>();
    ArrayList<Integer> zSortAll = new ArrayList<Integer>();
    ArrayList<String> allSortedBay = new ArrayList<>();

    //ArrayLists used for ListViews
    ArrayList<String> my_arr_list = new ArrayList<>();
    ArrayList<String> my_arr_list1 = new ArrayList<>();
    ArrayList<String> ABSarr = new ArrayList<>();
    ArrayList<String> updateAllShelves = new ArrayList<>();
    ArrayList<String> returnList = new ArrayList<String>();


    //add firebase variables
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private DatabaseReference AisleBayRef;
    private DatabaseReference ShelfRef;
    private String userID;

    String regexStr  = "^[0-9]*$";
    String alphabet = "abcdefghijklmnopqrstuvwxyz";
    String updateAll = "notSelected";

    int findMaxBayNum=0;
    int findMaxAisleNum=0;

    //Intent Data Variables
    String getStoreName = "";
    String employeeID;
    String getUserPermissions="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shelving_setup);

        Intent intent = getIntent();
        getStoreName = intent.getStringExtra("STORE_NAME");
        getUserPermissions = intent.getStringExtra("USER_PERMISSIONS");
        employeeID = intent.getStringExtra("STORE_USER");

         /*-- Main menu & View Layout btn listeners --*/
        mainMenuBtnListener();
        abSetupBtnListener();
        inititializeLayoutVariables();
        setupOptionsVisibility();
        checkboxListener();

        //where btnclickused to go
        assignShelvesOption1();
        assignShelvesOption2();
        assignShelvesOption3();

        //Firebase initialization / Auth Listener
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
        AisleBayRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                advAarr = new ArrayList<>();
                advBBArr = new ArrayList<>();
                for (DataSnapshot childSnapShot : dataSnapshot.getChildren()) {
                    String advAisle = (String) childSnapShot.child("aisle").getValue();
                    String advBay = (String) childSnapShot.child("bays").getValue();
                    if (advAisle != null && advBay != null && advBBArr != null) {
                        advAarr.add(Integer.parseInt(advAisle));
                        advBBArr.add(Integer.parseInt(advBay));
                    }
                }
                generateAisleSpinnner();
                generateBaySpinner();
                findMaxBayNum = generateMaxBay();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

       /*====================Display & Update Listview=================*/
        ShelfRef = mFirebaseDatabase.getReference().child(userID).child("ShelfSetup");
        ShelfRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                my_arr_list = new ArrayList<>();

                ABSarr = new ArrayList<>();

                for (DataSnapshot childSnapShot : dataSnapshot.getChildren()) {
                    String aisleNum = (String) childSnapShot.child("aisle_num").getValue();
                    String bayNum = (String) childSnapShot.child("bay_num").getValue();
                    String shelfNum = (String) childSnapShot.child("num_of_shelves").getValue();
                    String strx = "\t\tAisle: " + aisleNum + "\t\tBay: " + bayNum + "\t\tShelves: " + shelfNum;
                    String strz = aisleNum + "," + bayNum + "," + shelfNum;
                    if (aisleNum != null && bayNum != null && shelfNum != null) {
                        my_arr_list.add(strz);
                        my_arr_list1.add(strx);
                    }
                }

                if (updateAll.equals("selected")){
                    mListView = (ListView) findViewById(R.id.listViewX);
                    ArrayAdapter arrayAdapter = new ArrayAdapter(ShelvingSetup.this, R.layout.custom_listview_layout, updateAllShelves);
                    mListView.setAdapter(arrayAdapter);
                    updateAll = "notSelected";
                } else {
                    if (findMaxBayNum >= 10){ //Then use the sorting method to display listview
                        ABSarr = sortedABSlist();
                        mListView = (ListView) findViewById(R.id.listViewX);
                        ArrayAdapter arrayAdapter = new ArrayAdapter(ShelvingSetup.this, R.layout.custom_listview_layout, ABSarr);
                        mListView.setAdapter(arrayAdapter);
                    } else { //Display listview normally no sorting needed
                        ABSarr = my_arr_list1;
                        mListView = (ListView) findViewById(R.id.listViewX);
                        ArrayAdapter arrayAdapter = new ArrayAdapter(ShelvingSetup.this,R.layout.custom_listview_layout, my_arr_list1);
                        mListView.setAdapter(arrayAdapter);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void updateListView(){
    /*====================Display & Update Listview=================*/
        ShelfRef = mFirebaseDatabase.getReference().child(userID).child("ShelfSetup");
        ShelfRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                my_arr_list = new ArrayList<>();
                my_arr_list1 = new ArrayList<>();
                ABSarr = new ArrayList<>();

                for (DataSnapshot childSnapShot : dataSnapshot.getChildren()) {
                    String aisleNum = (String) childSnapShot.child("aisle_num").getValue();
                    String bayNum = (String) childSnapShot.child("bay_num").getValue();
                    String shelfNum = (String) childSnapShot.child("num_of_shelves").getValue();
                    String strx = "\t\tAisle: " + aisleNum + "\t\tBay: " + bayNum + "\t\tShelves: " + shelfNum;
                    String strz = aisleNum + "," + bayNum + "," + shelfNum;
                    if (aisleNum != null && bayNum != null && shelfNum != null) {
                        my_arr_list.add(strz);
                        my_arr_list1.add(strx);
                    }
                }
                if (findMaxBayNum >= 10){ //Then use the sorting method to display listview
                    ABSarr = sortedABSlist();
                    mListView = (ListView) findViewById(R.id.listViewX);
                    ArrayAdapter arrayAdapter = new ArrayAdapter(ShelvingSetup.this, R.layout.custom_listview_layout, ABSarr);
                    mListView.setAdapter(arrayAdapter);
                } else { //Display listview normally no sorting needed
                    ABSarr = my_arr_list1;
                    mListView = (ListView) findViewById(R.id.listViewX);
                    ArrayAdapter arrayAdapter = new ArrayAdapter(ShelvingSetup.this,R.layout.custom_listview_layout, my_arr_list1);
                    mListView.setAdapter(arrayAdapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public int generateMaxBay(){
        int max=0;
        for (int i = 0; i < advBBArr.size() ; i++) {
            if (max < advBBArr.get(i)) {
                max = advBBArr.get(i);
            }
        }
        return max;
    }
    public ArrayList<String> sortBayList(){
        //Sort both Aisle & Bay ArrayList
        Collections.sort(advBBArr);
        Collections.sort(advAarr);

        String strSort = "";
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
            allSortedBay.add(bayArr[i]);
        }
        return allSortedBay;
    }

    /*== Sort the entries from the database so they are in order when display on listview ==*/
    public ArrayList<String> sortedABSlist () {
        returnList = new ArrayList<String>();
        String a = "";
        String[] myABS = new String[3];
        int cont = 0;
        int sortBay = 1;
        int sortAisle = 1;
        allSortedBay = sortBayList();
        for (int i = 0; i < allSortedBay.size(); i++) {
            if (i != 0 && allSortedBay.get(i).equals("1")) {
                cont++;
            }
            sortBay = Integer.parseInt(allSortedBay.get(i));
            sortAisle = advAarr.get(cont);

            for (int j = 0; j < my_arr_list.size(); j++) {
                a = my_arr_list.get(j);
                myABS = a.split("\\s*,\\s*");
                if (Integer.parseInt(myABS[0]) == sortAisle && Integer.parseInt(myABS[1]) == sortBay) {
                    returnList.add("\t\t\tAisle: " + myABS[0] + "\t\t\tBay: " + myABS[1] + "\t\t\tShelves: " + myABS[2]);
                    my_arr_list.remove(j);
                    break;
                }
            }
        }
        return returnList;
    }

    /*== Genereate Bay Spinner Method ==*/
    public void generateBaySpinner(){
        aisle_num_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ArrayList<String> baySpinnerList = new ArrayList<>();
                int currentAisle = aisle_num_spinner.getSelectedItemPosition()+1;

                //Locate a match for the currentAisle thats selected
                // and generate an arrayList dedicated to that currentAisle's number of bays.
                for (int i = 0; i < advBBArr.size(); i++) {
                    if (currentAisle == i+1){
                        for (int j = 0; j < advBBArr.get(i) ; j++) {
                            baySpinnerList.add(String.valueOf(j+1));
                        }
                    }
                }
                ArrayAdapter advBadp = new ArrayAdapter(ShelvingSetup.this, android.R.layout.simple_spinner_dropdown_item, baySpinnerList);
                bay_num_spinner.setAdapter(advBadp);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }
    /*== Genereate Aisle Spinner Method ==*/
    public void generateAisleSpinnner(){
        Collections.sort(advAarr);
        ArrayAdapter advAadp = new ArrayAdapter(ShelvingSetup.this, android.R.layout.simple_spinner_dropdown_item, advAarr);
        aisle_num_spinner.setAdapter(advAadp);
    }

    public void abSetupBtnListener(){
        absetup_btn = (Button)findViewById(R.id.absetup_btn);
        absetup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShelvingSetup.this, AisleBaySetup.class);
                sendIntentData(intent);
            }
        });
    }
    /*== Individual Shelf Assign Button Listener==*/
    public void assignShelvesOption1(){
        assign_shelves_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String a = "";
                int bayHolder = 0;
                aisle_num_spinner = (Spinner) findViewById(R.id.aisle_num_spinner);
                String aisleSpinTxt = aisle_num_spinner.getSelectedItem().toString();
                bay_num_spinner = (Spinner) findViewById(R.id.bay_num_spinner);
                String baySpinTxt = null;
                if (bay_num_spinner != null && bay_num_spinner.getSelectedItem() != null) {
                    baySpinTxt = (String) bay_num_spinner.getSelectedItem();
                } else {

                }
                String strNumShelves = num_shelves.getText().toString();
                /*== Individual Shelf Assign ==*/
                if (!checkBox.isChecked() || setupOptions.getSelectedItemPosition() == 0) {
                    /* == Check users input is valid first == */
                    if (strNumShelves.trim().matches(regexStr) && baySpinTxt != null && strNumShelves.trim().length() >= 1) {
                        /*== Loop through to find the current aisle & bay thats selected ==*/
                        for (int i = 0; i < advAarr.size(); i++) {
                            String[] alphabetSplitter = alphabet.split("");
                            a = alphabetSplitter[i + 1].toString();
                            bayHolder = advBBArr.get(i);

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
                updateListView();
            }
        });
    }
    /*=== Assign all Bays Same # Shelves Button Listener===*/
    public void assignShelvesOption2(){
        assign_shelves_btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                  /*=== Assign Shelf onClick Listener === */
                String a = "";
                int bayHolder = 0;
                String strNumShelves = num_shelves3.getText().toString();
                /*=== Assign all Bays Same # Shelves ===*/
                if (checkBox.isChecked() && setupOptions.getSelectedItemPosition() == 1) {
                    /* == Check users input is valid first == */
                    if (strNumShelves.trim().matches(regexStr) && strNumShelves.trim().length() >= 1) {
                        updateAllShelves = new ArrayList<String>();
                        updateAll = "selected";
                        /*== Loop through to find the current aisle & bay thats selected ==*/
                        for (int i = 0; i < advAarr.size(); i++) {
                            String[] alphabetSplitter = alphabet.split("");
                            a = alphabetSplitter[i + 1].toString();
                            bayHolder = advBBArr.get(i);
                            for (int j = 1; j <= bayHolder; j++) {
                                /*===== Add Shelf to current aisle & bay selected ==== */
                                myRef.child(userID).child("ShelfSetup").child(a + "AisleID" + j).setValue(String.valueOf(j));
                                myRef.child(userID).child("ShelfSetup").child(a + "AisleID" + j).child("aisle_num").setValue(String.valueOf(i + 1));
                                myRef.child(userID).child("ShelfSetup").child(a + "AisleID" + j).child("bay_num").setValue(String.valueOf(j));
                                myRef.child(userID).child("ShelfSetup").child(a + "AisleID" + j).child("num_of_shelves").setValue(strNumShelves.trim());
                                String insertData = "\t\t\tAisle: " + String.valueOf(i+1) + "\t\t\tBay: " + String.valueOf(j) + "\t\t\tShelves: " + strNumShelves.trim();
                                updateAllShelves.add(insertData);
                            }
                        }
                        mListView = (ListView) findViewById(R.id.listViewX);
                        ArrayAdapter arrayAdapter = new ArrayAdapter(ShelvingSetup.this, R.layout.custom_spinner_layout, updateAllShelves);
                        mListView.setAdapter(arrayAdapter);
                    } else {
                        toastMessage("Select a valid bay & enter a valid integer");
                    }

                }
            }
        });
    }
    /*=== Assign Specific Aisle Same # of Shelves Button Listener ===*/
    public void assignShelvesOption3(){
        assign_shelves_btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String a = "";
                int bayHolder = 0;
                aisle_num_spinner2 = (Spinner) findViewById(R.id.aisle_num_spinner2);
                String aisleSpinTxt = aisle_num_spinner2.getSelectedItem().toString();

                String strNumShelves = num_shelves2.getText().toString();
                /*=== Assign Specific Aisle Same # of Shelves ===*/
                if (checkBox.isChecked() && setupOptions.getSelectedItemPosition() == 2) {
                    /* == Check users input is valid first == */
                    if (strNumShelves.trim().matches(regexStr) && strNumShelves.trim().length() >= 1) {
                        /*== Loop through to find the current aisle & bay thats selected ==*/
                        for (int i = 0; i < advAarr.size(); i++) {
                            String[] alphabetSplitter = alphabet.split("");
                            a = alphabetSplitter[i + 1].toString();
                            bayHolder = advBBArr.get(i);
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
                        updateListView();
                    } else {
                        toastMessage("Please select a valid bay & enter a valid integer");
                    }
                }
            }
        });
    }
    /*== Setup Options Spiiner on Selected Layout Visibility Controller ==*/
    public void setupOptionsVisibility(){
        setupOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
                ((TextView) parent.getChildAt(0)).setTextSize(14);
                if (setupOptions.getSelectedItemPosition() == 1) {
                    linearLayoutOption1.setVisibility(View.INVISIBLE);
                    linearLayoutOption2.setVisibility(View.INVISIBLE);
                    linearLayoutOption3.setVisibility(View.VISIBLE);
                }
                if (setupOptions.getSelectedItemPosition() == 2) {
                    linearLayoutOption1.setVisibility(View.INVISIBLE);
                    linearLayoutOption2.setVisibility(View.VISIBLE);
                    linearLayoutOption3.setVisibility(View.INVISIBLE);
                    ArrayAdapter advAadp = new ArrayAdapter(ShelvingSetup.this, R.layout.custom_spinner_layout, advAarr);
                    aisle_num_spinner2.setAdapter(advAadp);
                    aisle_num_spinner2.setSelection(0);
                }
                if (setupOptions.getSelectedItemPosition() == 0){
                    linearLayoutOption1.setVisibility(View.VISIBLE);
                    linearLayoutOption2.setVisibility(View.INVISIBLE);
                    linearLayoutOption3.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /*=== Checkbox Listener (Set Visible/Invisible Options) ===*/
    public void checkboxListener(){
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
    }

    /*== Initialize Layout Variables && Generate Shelf Setup Spinner Values ==*/
    public void inititializeLayoutVariables(){
        linearLayoutOption1 = (LinearLayout)findViewById(R.id.linearLayoutOption1);
        linearLayoutOption2 = (LinearLayout)findViewById(R.id.linearLayoutOption2);
        linearLayoutOption2.setVisibility(View.INVISIBLE);
        linearLayoutOption3 = (LinearLayout)findViewById(R.id.linearLayoutOption3);
        linearLayoutOption3.setVisibility(View.INVISIBLE);
        //Layout Variables Initialization
        aisle_num_spinner = (Spinner) findViewById(R.id.aisle_num_spinner);
        aisle_num_spinner2 = (Spinner) findViewById(R.id.aisle_num_spinner2);
        bay_num_spinner = (Spinner) findViewById(R.id.bay_num_spinner);
        num_shelves = (EditText) findViewById(R.id.num_shelves);
        num_shelves2 = (EditText) findViewById(R.id.num_shelves2);
        num_shelves3 = (EditText) findViewById(R.id.num_shelves3);
        assign_shelves_btn = (Button) findViewById(R.id.assign_shelves_btn);
        assign_shelves_btn2 = (Button) findViewById(R.id.assign_shelves_btn2);
        assign_shelves_btn3 = (Button) findViewById(R.id.assign_shelves_btn3);
        mListView = (ListView) findViewById(R.id.listViewX);
        checkBox = (CheckBox) findViewById(R.id.checkBox);
        setupOptions = (Spinner) findViewById(R.id.setupOptions);
        setupOptions.setEnabled(false);
        setupOptions.setVisibility(View.INVISIBLE);

        /*=== Quick Shelf Setup Spinner Options ===*/
        ArrayList<String> options = new ArrayList<>();
        options.add("Assign All Shelves Individually");
        options.add("Assign All Bays Same # Shelves");
        options.add("Assign Specific Aisles Same # of Shelves");
        ArrayAdapter optionAdapter = new ArrayAdapter(ShelvingSetup.this, R.layout.custom_spinner_layout, options);
        setupOptions.setAdapter(optionAdapter);
    }
    /*== Main Menu Btn Listener ==*/
    public void mainMenuBtnListener(){
        main_menu_btn = (Button)findViewById(R.id.main_menu_btn);
        main_menu_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShelvingSetup.this, MainMenu.class);
                sendIntentData(intent);
            }
        });
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