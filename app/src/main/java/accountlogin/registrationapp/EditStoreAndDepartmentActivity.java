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
import java.util.ListIterator;


public class EditStoreAndDepartmentActivity extends AppCompatActivity {
    EditText add_dept_txt, change_dept_txt;
    Spinner remove_dept_spinner, edit_dept_spinner;
    Button add_dept_btn, remove_dept_btn, change_dept_btn, main_menu_btn;

    //Firebase Variables
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private DatabaseReference AisleBayRef;
    private DatabaseReference myDeptRef;
    private String userID;

    String testString;
    String deptNamesString= "";

    ArrayList<String> newDeptArrayList = new ArrayList<>();

    //Intent Data Variables
    private String getStoreName = "";
    private String employeeID;
    private String getUserPermissions="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_store_and_department);
        Intent intent = getIntent();
        /*==Retrieve intent data ==*/
        getStoreName = intent.getStringExtra("STORE_NAME");
        getUserPermissions = intent.getStringExtra("USER_PERMISSIONS");
        employeeID = intent.getStringExtra("STORE_USER");

        //EditText initialization
        add_dept_txt = (EditText) findViewById(R.id.add_dept_txt);
        change_dept_txt = (EditText) findViewById(R.id.change_dept_txt);

        //Button initialization
        add_dept_btn = (Button) findViewById(R.id.add_dept_btn);
        remove_dept_btn = (Button) findViewById(R.id.remove_dept_btn);
        change_dept_btn = (Button) findViewById(R.id.change_dept_btn);
        mainMenuBtnListener();

        //Spinner initialization
        edit_dept_spinner = (Spinner) findViewById(R.id.edit_dept_spinner);
        remove_dept_spinner = (Spinner) findViewById(R.id.remove_dept_spinner);

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

        /*=== Department Name Listener to keep track of current deptListString ==== */
        myDeptRef = mFirebaseDatabase.getReference().child(userID);
        myDeptRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                deptNamesString = "";

                for(DataSnapshot data: dataSnapshot.getChildren()) {
                    if (data.getKey().equals("deptNames")){
                        deptNamesString = data.getValue().toString();
                    }
                }
                /*====Update both spinner values ===== */
                createDeptSpinner();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /*======== Add Dept Btn On Click =========*/
        add_dept_btn.setOnClickListener(new View.OnClickListener() {
            String[] addToDeptArr;
            String deptHolder = "";
            String message1="";
            int count = 0;
            @Override
            public void onClick(View view) {
                deptHolder = "";
                String addDeptString = add_dept_txt.getText().toString();
                String[] addToDeptArr = deptNamesString.split("\\s*,\\s*");
                count = 0;
                //compare the addDeptTxt with each already established Department
                ArrayList<String> deptArrList = new ArrayList<>();

                if (!addDeptString.contains(",")) { // no commas allowed
                    //check for duplicates
                    for (int i = 0; i < addToDeptArr.length; i++) {
                        if (addToDeptArr[i].equals(addDeptString)) {
                            count = 1;
                            message1 = addDeptString + " Already exists at position '" + (i + 1) + "' in your department list, no duplicates allowed!";
                        } else {
                            deptArrList.add(addToDeptArr[i]);
                        }
                    }

                    if (deptNamesString.trim().isEmpty()) {//if its empty
                        deptHolder = addDeptString + ",";
                        myRef.child(userID).child("deptNames").setValue(deptHolder);
                    } else { //if there was a duplicate triggered
                        if (count == 1) {
                            toastMessage(message1);
                        } else { //add to dept list
                        /*=== Gathering current data and adding the new dept to end of deptArrList ===*/
                            for (int i = 0; i < deptArrList.size() + 1; i++) {
                                if (i == deptArrList.size()) {
                                    deptHolder += addDeptString + ",";
                                } else {
                                    deptHolder += deptArrList.get(i) + ",";
                                }
                            }
                            toastMessage(addDeptString + " has been added to your departments.");
                            myRef.child(userID).child("deptNames").setValue(deptHolder);
                        }
                    }
                }else {
                    toastMessage("Your Department name cant contain any commas");
                }
            }


        });
        /*=======Remove Dept Btn On Click ======*/
        remove_dept_btn.setOnClickListener(new View.OnClickListener() {
            String[] deptListArr;
            String strToRemove="";
            String newList="";
            String message1;
            String message2;
            @Override
            public void onClick(View view) {
                String removeSpinnerTxt = remove_dept_spinner.getSelectedItem().toString();
                int count = 0; //control variable
                testString = deptNamesString;
                deptListArr = testString.split("\\s*,\\s*");

                //determine the string to remove and load all the elements into the arrayList.
                ArrayList<String> deptArrList = new ArrayList<>();
                for (int i = 0; i < deptListArr.length; i++) {
                    if (deptListArr[i].equals(removeSpinnerTxt)) {
                        strToRemove = deptListArr[i];
                        message1 = " Department to Remove: " + strToRemove;
                    }
                    deptArrList.add(deptListArr[i]);
                }
                //Iterate through ArrayList and remove the strToRemove that was determined above
                ListIterator<String> itr = deptArrList.listIterator();
                String strElement="";
                int itrCount=0;
                while (itr.hasNext()) {
                    strElement = itr.next();
                    if (strToRemove.equals(strElement)){
                        itr.remove();
                        message2 = message1 + "\nHas been sucessfully removed!";
                        count=1;
                    } else {
                       newList += strElement + ",";
                    }
                }
                //Adding to database
                if (count == 1) {
                    myRef.child(userID).child("deptNames").setValue(newList);
                    toastMessage(message2);
                } else {
                    toastMessage("Something went wrong, please try again... ");
                }
                newList="";
            }
        });

        /************Change Dept Btn On Click *****************/
        change_dept_btn.setOnClickListener(new View.OnClickListener() {
            String strToChange;
            String newList="";
            String message1; //failed to add to database
            String message2; //updated new department to database
            @Override
            public void onClick(View view) {
                String editSpinnerTxt = edit_dept_spinner.getSelectedItem().toString(); //Users selection to edit
                String changeDeptTxt = change_dept_txt.getText().toString(); //Users desired new dept name
                int editCount = 0; //control variable
                String[] deptListArr = deptNamesString.split("\\s*,\\s*");

                ArrayList<String> deptArrList = new ArrayList<>();
                if (!changeDeptTxt.contains(",")){ //no commas allowed

                for (int i = 0; i < deptListArr.length; i++) {
                    //checking for any matches between the user textfield and the original dept list
                    if(changeDeptTxt.equals(deptListArr[i].toString())) {
                        editCount = 1;
                        message1 = changeDeptTxt + " " + " in row '" + (i + 1) + "' "
                                + "\nAlready exists in your list of departments!";
                    }

                    if (editCount!= 1 && !deptListArr[i].equals(editSpinnerTxt)) {
                        deptArrList.add(deptListArr[i]);
                    } else { //str to change identified!
                        strToChange = deptListArr[i];
                        deptArrList.add(deptListArr[i]);
                    }

                }
                /*=== Change the selected dept name to the new dept Name ===*/
                ListIterator<String> itr = deptArrList.listIterator();
                String strElement="";
                while (itr.hasNext()) {
                    strElement = itr.next();
                     if (editCount!= 1 && strToChange.equals(strElement)) {
                        itr.set(changeDeptTxt);
                        message2 = strToChange + " Sucessfully changed to " + changeDeptTxt;
                        //add the changed dept txt in the same position as the old dept txt to the new department list
                        newList += changeDeptTxt + ", ";
                     } else { //adding all the other elements as usual
                        newList += strElement + ",";
                     }
                }
                //Do nothing
                if (editCount == 1) {
                    toastMessage(message1);
                } else {//Adding newList to database
                    toastMessage(message2);
                    myRef.child(userID).child("deptNames").setValue(newList);
                }
                newList= "";
            } else {
                    toastMessage("Department name cant contain any commas.");
                }
            }

        });
    }

    public void createDeptSpinner(){
        //Spinner initialization
        edit_dept_spinner = (Spinner) findViewById(R.id.edit_dept_spinner);
        remove_dept_spinner = (Spinner) findViewById(R.id.remove_dept_spinner);
        String stringHolder = "";
        stringHolder = deptNamesString;

        String[] deptlistarr = stringHolder.split("\\s*,\\s*");
        newDeptArrayList = new ArrayList<>();

        for (int i = 0; i < deptlistarr.length; i++) {
            newDeptArrayList.add(deptlistarr[i]);
        }
        //Load deptArrList into the dept to remove spinner
        ArrayAdapter arrayAdapter = new ArrayAdapter(EditStoreAndDepartmentActivity.this, android.R.layout.simple_spinner_dropdown_item, newDeptArrayList);
        remove_dept_spinner.setAdapter(arrayAdapter);
        //Load deptArrList into the dept to edit spinner
        ArrayAdapter arrayAdapter1 = new ArrayAdapter(EditStoreAndDepartmentActivity.this, android.R.layout.simple_spinner_dropdown_item, newDeptArrayList);
        edit_dept_spinner.setAdapter(arrayAdapter1);

    }
    public void mainMenuBtnListener(){
        main_menu_btn = (Button)findViewById(R.id.main_menu_btn);
        main_menu_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditStoreAndDepartmentActivity.this, MainMenu.class);
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



