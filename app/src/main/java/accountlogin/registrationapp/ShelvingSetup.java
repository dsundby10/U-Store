package accountlogin.registrationapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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
import java.util.List;

public class ShelvingSetup extends AppCompatActivity {
    private static final String TAG = "ShelvingSetup: ";
    Spinner aisle_num_spinner, bay_num_spinner;
    EditText num_shelves;
    Button assign_shelves_btn;
    ListView mListView;

    //add firebase variables
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private String userID;

    //List<String> aisleSpinnerValues = new ArrayList<>();
    //List<String> baySpinnerValues = new ArrayList<>();

    static List<String>array = new ArrayList<>();

    List<String> allDataArr = new ArrayList<>();

    int aisleSpinInt=0;
    int baySpinInt=0;
    static int z=0;
    static int x=0;
    int count = 0;
    int maxEntries=0;
    int aisle =0;
    int bay=0;
    String tvSV_List="";
    //String[] aisle_id_arr;
    //String[] aisle_num_arr;
    //String[] bay_num_arr;

    ArrayList<String> aisle_id_arr;
    ArrayList<String> aisle_num_arr;
    ArrayList<String> bay_num_arr;
    ArrayList<String> num_of_shelves;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shelving_setup);
        Intent intent = getIntent();
        //tvSV_shelf = (TextView)findViewById(R.id.tvSV_shelf);
        aisle_num_spinner = (Spinner)findViewById(R.id.aisle_num_spinner);
        bay_num_spinner = (Spinner)findViewById(R.id.bay_num_spinner);
        num_shelves = (EditText)findViewById(R.id.num_shelves);
        assign_shelves_btn = (Button)findViewById(R.id.assign_shelves_btn);

        mListView = (ListView)findViewById(R.id.listviewX);

        //Firebase initialization
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        final FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {

                }
            }
        };

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                showData(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        assign_shelves_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tv = "";
                Log.i("MaxEntries: ", String.valueOf(maxEntries));

                int dbCount = 0;
                //AutoGenerate Database
                if (count==0){
                    aisle_id_arr = new ArrayList<String>();
                    aisle_num_arr = new ArrayList<String>();
                    bay_num_arr = new ArrayList<String>();
                    num_of_shelves = new ArrayList<String>();
                    for (int i = 0; i < aisle; i++){
                        for (int j = 0; j < bay; j++){
                            //Database Structure Attempt
                            myRef.child(userID).child("ShelfSetup").child("AisleID:" + dbCount).setValue(String.valueOf(dbCount));
                            myRef.child(userID).child("ShelfSetup").child("AisleID:" + dbCount).child("aisle_num").setValue(i);
                            myRef.child(userID).child("ShelfSetup").child("AisleID:" + dbCount).child("bay_num").setValue(j);
                            myRef.child(userID).child("ShelfSetup").child("AisleID:" + dbCount).child("num_of_shelves").setValue(String.valueOf(0));

                            String aisle_id = myRef.child(userID).child("ShelfSetup").child("AisleID:" + dbCount).child(String.valueOf(dbCount)).getKey();
                            String aisle_num_id = myRef.child(userID).child("ShelfSetup").child("AisleID:" + dbCount).child("aisle_num").child(String.valueOf(i)).getKey();
                            String bay_num_id = myRef.child(userID).child("ShelfSetup").child("AisleID:" + dbCount).child("bay_num").child(String.valueOf(j)).getKey();
                            String shelf_num_id = myRef.child(userID).child("ShelfSetup").child("AisleID:" + dbCount).child("num_of_shelves").child(String.valueOf(0)).getKey();

                            aisle_id_arr.add(aisle_id);
                            aisle_num_arr.add(aisle_num_id);
                            bay_num_arr.add(bay_num_id);

                            num_of_shelves.add(shelf_num_id);
                            Log.i("max Current DB count ", String.valueOf(aisle_id) + " " + String.valueOf(aisle_num_id) + " " + String.valueOf(bay_num_id) + " " +  String.valueOf(shelf_num_id));
                            dbCount++;

                        }
                    }
                }
                aisle_num_spinner = (Spinner)findViewById(R.id.aisle_num_spinner);
                String aisleSpinTxt = aisle_num_spinner.getSelectedItem().toString();
                aisleSpinInt = Integer.parseInt(aisleSpinTxt);

                bay_num_spinner = (Spinner)findViewById(R.id.bay_num_spinner);
                String baySpinTxt = bay_num_spinner.getSelectedItem().toString();
                baySpinInt = Integer.parseInt(baySpinTxt);

                String strNumShelves = num_shelves.getText().toString();

                //Users selections Spinner Slections & Num of Shelves
                String strArr = aisleSpinTxt + ", " + baySpinTxt + ", " + strNumShelves + ", ";

                //Loop through for however many AisleID: keys were initially added
                for (int i=0; i<aisle_id_arr.size(); i++) {
                    if (aisle_num_arr.get(i).toString().equals(aisleSpinTxt) && bay_num_arr.get(i).equals(baySpinTxt)){
                        Log.i("maxLogging", "It WORKS: AisleID:" + i + " " + aisleSpinTxt + " " + baySpinTxt);
                        myRef.child(userID).child("ShelfSetup").child("AisleID:"+i).child("num_of_shelves").setValue(strNumShelves);
                    }
                }

                allDataArr.add(count,strArr);

                //display data onto the textview
                tv = "Assignment #" + count + " | Aisle: " + aisleSpinTxt + " Bay#: " +baySpinTxt+ " Shelves:  " + strNumShelves + "\n";
                String temp = tv;
                tvSV_List+=temp;
                ArrayAdapter arrayAdapter = new ArrayAdapter(ShelvingSetup.this,android.R.layout.simple_expandable_list_item_1,allDataArr);
                mListView.setAdapter(arrayAdapter);
                count++;
            }
        });


    }

    private void showData(DataSnapshot dataSnapshot) {
        String str = "";
        String str1="";

        zAllUserData zInfo = new zAllUserData();
        for(DataSnapshot ds : dataSnapshot.getChildren()) {
            zInfo.setNumAisles(ds.child("aisles").getValue(String.class));
            zInfo.setNumBays(ds.child("genBays").getValue(String.class));
            //zInfo.setDeptNames(ds.child(userID).child("aisles").getValue(String.class));
            //zInfo.setNumDepartments(ds.child(userID).child("genBays").getValue(String.class));

            array = new ArrayList<>();
            array.add(zInfo.getNumAisles());
            array.add(zInfo.getNumBays());
            aisle = Integer.parseInt(zInfo.getNumAisles());
            bay  = Integer.parseInt(zInfo.getNumBays());

            ArrayList<String> arrAisle = new ArrayList<>();
            z = Integer.parseInt(array.get(0));
            for (int i=0; i<z; i++) {
                str = str.valueOf(i);
                arrAisle.add(i,str);
            }
           ArrayList<String>arrBay = new ArrayList<>();
            x= Integer.parseInt(array.get(1));
            for (int i=0; i<x; i++) {
                str1 = str1.valueOf(i);
                arrBay.add(i,str1);
            }
            maxEntries = z*x;

            ArrayAdapter arrayAdapter = new ArrayAdapter(ShelvingSetup.this,android.R.layout.simple_spinner_dropdown_item, arrAisle);
            aisle_num_spinner.setAdapter(arrayAdapter);
            ArrayAdapter arrayAdapter1 = new ArrayAdapter(ShelvingSetup.this,android.R.layout.simple_spinner_dropdown_item, arrBay);
            bay_num_spinner.setAdapter(arrayAdapter1);
        }

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
 /*ListIterator<String> itr = allDataArr.listIterator();
                String strElement = "";
                //int itrCount = 0;
                int xCount = 1;
                while (itr.hasNext()) {
                    strElement = itr.next();
                    String[] xx = strElement.split("\\s*,\\s*");
                    String p1 = xx[0];
                    String p2 = xx[1];
                    String p3 = xx[2];
                    if (aisleSpinTxt.equals(p1) && baySpinTxt.equals(p2) && itrCount!= 0 && itrCount!=count) {
                        stopper = itrCount;
                        toastMessage("Current Aisle: " + aisleSpinTxt + " Current Bay " + baySpinTxt +
                                "\nIs equal to Aisle " + p1 + " and Current Bay " + p2 +
                                "\nAt Iteration Number: " + itrCount);
                    }

                    itrCount++;
                }
                if(stopper!=0) {
                    myRef.child(userID).child("aisle_id:"+ stopper).setValue(String.valueOf(count));
                    myRef.child(userID).child("aisle_id:"+ stopper).child("aisle_num").setValue(aisleSpinTxt);
                    myRef.child(userID).child("aisle_id:"+ stopper).child("at_bay_num").setValue(baySpinTxt);
                    myRef.child(userID).child("aisle_id:"+ stopper).child("assign_num_shelves").setValue(strNumShelves);
                    //count--;
                    toastMessage("Stopper! " + stopper + " Count is now " + count);
                } else {

                    //Store data into database
                    myRef.child(userID).child("aisle_id:"+ count).setValue(String.valueOf(count));
                    myRef.child(userID).child("aisle_id:"+ count).child("aisle_num").setValue(aisleSpinTxt);
                    myRef.child(userID).child("aisle_id:"+ count).child("at_bay_num").setValue(baySpinTxt);
                    myRef.child(userID).child("aisle_id:"+ count).child("assign_num_shelves").setValue(strNumShelves);
                    toastMessage("No problems here: stopper is : " + stopper + " and count is at " + count );
                    count++;
                }*/