package accountlogin.registrationapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class AddInventory extends AppCompatActivity {
    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int RESULT_TAKE_IMAGE = 2;
    static int PICTURE_OPTION = 0;

    static int productCounter=0;
    EditText product_name, num_stock, product_id, product_desc;
    Spinner dept_spinner, aisle_spinner, bay_spinner, shelf_spinner;
    Button add_product_btn, take_image_btn, upload_image_btn, main_menu_btn;
    ImageView imageView;

    /*-Aisle Spinner Vars-*/
    String aisleChecker = "";
    ArrayList<String> aisle_spinnerValues = new ArrayList<>();
    ArrayList<String> aisleCheckerList = new ArrayList<>();
    int currentAisleSpinner = 0;

    /*-Bay Spinner Vars-*/
    String bayChecker = "";
    ArrayList<String> bay_spinnerValues = new ArrayList<>();
    ArrayList<String> bayCheckerList = new ArrayList<>();
    ArrayList<String> bayCheckerListz = new ArrayList<String>();
    int currentBaySpinner = 0;

    /*-Shelf Spinner Vars-*/
    ArrayList<String> shelf_spinnerValues = new ArrayList<>();
    ArrayList<String> shelfCheckerList = new ArrayList<>();
    int currentShelfSpinner = 0;

    ArrayList<String> allABS = new ArrayList<>();

    /*-Dept Spinner Vars-*/
    String deptString = "";
    String[] deptArr;
    ArrayList<String> dept_spinnerValues = new ArrayList<>();

    //Place holders for the selected products current spinner values
    String p_curAisle = "";
    String p_curBay="";
    String p_curShelf="";
    String p_curDept="";
    String p_curKey= "";
    String imgPath="";
    String p_curImg="";


    //Firebase Variables
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //Firebase Database References
    private DatabaseReference myRef;
    private DatabaseReference AisleRef;
    private DatabaseReference AisleBayRef;
    private DatabaseReference ShelfRef;
    private String userID;

    //Firebase Image StorageReference
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl("gs://accountloginregistrationapp.appspot.com");
    StorageReference productImg;
    Uri cur_ur;
    Bitmap cur_bitmap;
    int maxAisleCount = 0;

    //Intent Properties to verify user & permissions
    String getUserPermissions;
    String employeeID;
    String getStoreName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_inventory);
        Intent intent = getIntent();

        employeeID = getIntent().getStringExtra("STORE_USER");
        getUserPermissions = getIntent().getStringExtra("USER_PERMISSIONS").trim();
        getStoreName = getIntent().getStringExtra("STORE_NAME");

        //Variable initialization
        imageView = (ImageView) findViewById(R.id.imageView);
        dept_spinner = (Spinner) findViewById(R.id.dept_spinner);
        aisle_spinner = (Spinner) findViewById(R.id.aisle_spinner);
        bay_spinner = (Spinner) findViewById(R.id.bay_spinner);
        shelf_spinner = (Spinner) findViewById(R.id.shelf_spinner);

        product_name = (EditText) findViewById(R.id.product_name);
        num_stock = (EditText) findViewById(R.id.num_stock);
        product_id = (EditText) findViewById(R.id.product_id);
        product_desc = (EditText) findViewById(R.id.product_desc);
        main_menu_btn = (Button)findViewById(R.id.main_menu_btn);
        add_product_btn = (Button) findViewById(R.id.save_changes_btn);
        take_image_btn = (Button) findViewById(R.id.take_image_btn);
        upload_image_btn = (Button) findViewById(R.id.upload_image_btn);

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


        /*======= Pulling Aisle & Dept from DB=======*/
        AisleRef = mFirebaseDatabase.getReference().child(userID);
        AisleRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                deptString = "";

                dept_spinnerValues = new ArrayList<String>();
                for(DataSnapshot data: dataSnapshot.getChildren()) {
                    /*=== If User has an Existing Aisle Entry in the Database ===*/
                    if (data.getKey().equals("aisles") && !data.getValue().toString().trim().equals("")) {
                        aisleChecker = data.getValue().toString();
                        maxAisleCount = Integer.parseInt(aisleChecker);
                    }
                    createSpinner(maxAisleCount);

                    if (data.getKey().equals("deptNames") && !data.getValue().toString().trim().equals("")) {
                      deptString = data.getValue().toString();
                    }
                    createDeptSpinner();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        /*==== Pulling Bay Number out of "BaySetup" in the Database ===*/
        AisleBayRef = mFirebaseDatabase.getReference().child(userID).child("BaySetup");
        AisleBayRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!this.getClass().equals(AddInventory.class)){
                    AisleBayRef.removeEventListener(this);
                }
                bayCheckerList = new ArrayList<>();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    bayCheckerList.add(data.child("bays").getValue().toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


        ShelfRef = mFirebaseDatabase.getReference().child(userID).child("ShelfSetup");
        ShelfRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!this.getClass().equals(AddInventory.class)){
                    ShelfRef.removeEventListener(this);
                }
                aisleCheckerList = new ArrayList<String>();
                bayCheckerListz = new ArrayList<String>();
                shelfCheckerList = new ArrayList<String>();

                allABS = new ArrayList<String>();

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    String a = data.child("aisle_num").getValue().toString();
                    String b = data.child("bay_num").getValue().toString();
                    String s = data.child("num_of_shelves").getValue().toString();
                    aisleCheckerList.add(a);
                    bayCheckerListz.add(b);
                    shelfCheckerList.add(s);
                    //Add corresponding aisle bay shelves together to easily identify the shelf number
                    allABS.add(a + "¿" + b + "¿" + s);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /*===Take Image on click Listener ===*/
        take_image_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, RESULT_TAKE_IMAGE);
            }
        });
        /*===Upload picture on click listener ===*/
        upload_image_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(gallery, RESULT_LOAD_IMAGE);
            }

        });

        /*----------------- Adding Products to Data On Click Listener -----------*/
        add_product_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (product_name.getText().toString().trim().length() == 0){
                    toastMessage("You must assign a name your product!");
                } else {
                String p_id = product_id.getText().toString();
                String p_name = product_name.getText().toString();
                String p_stock = num_stock.getText().toString();
                String p_desc = product_desc.getText().toString();

                String currentAisleSpinner = aisle_spinner.getSelectedItem().toString();
                String currentBaySpinner = bay_spinner.getSelectedItem().toString();
                String currentShelfSpinner = shelf_spinner.getSelectedItem().toString();

                String currentDeptSpinner = dept_spinner.getSelectedItem().toString();

                String ProductKey = myRef.child(userID).child("Products").child("Product" + productCounter).push().getKey();
                myRef.child(userID).child("Products").child(ProductKey).push().getKey();
                myRef.child(userID).child("Products").child(ProductKey).child("P_Name").setValue(p_name);
                myRef.child(userID).child("Products").child(ProductKey).child("P_Stock").setValue(p_stock);
                myRef.child(userID).child("Products").child(ProductKey).child("P_ID").setValue(p_id);
                myRef.child(userID).child("Products").child(ProductKey).child("P_Desc").setValue(p_desc);
                myRef.child(userID).child("Products").child(ProductKey).child("P_Dept").setValue(currentDeptSpinner);
                myRef.child(userID).child("Products").child(ProductKey).child("P_Aisle").setValue(currentAisleSpinner);
                myRef.child(userID).child("Products").child(ProductKey).child("P_Bay").setValue(currentBaySpinner);
                myRef.child(userID).child("Products").child(ProductKey).child("P_Shelf").setValue(currentShelfSpinner);

                imgPath = "n/a";
                    /*--Uploading from Camera--*/
                if (PICTURE_OPTION == 2) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    cur_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                    String imageKey = myRef.child(userID).child("Products").child(ProductKey).child("P_ImagePath").push().getKey();
                    productImg = storageRef.child(userID + ".images/" + p_name + "ú" + imageKey + ".jpg");
                    imgPath = product_name.getText().toString() + "ú" + imageKey + ".jpg";
                    myRef.child(userID).child("Products").child(ProductKey).child("P_ImagePath").setValue(imgPath);

                    byte[] dataz = baos.toByteArray();
                    UploadTask uploadTask = productImg.putBytes(dataz);

                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {

                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        }
                    });
                }

                /*--Uploading image--*/
                if (PICTURE_OPTION == 1) {

                    String imageKey = myRef.child(userID).child("Products").child(ProductKey).child("P_ImagePath").push().getKey();
                    productImg = storageRef.child(userID + ".images/" + p_name + "ú" + imageKey + ".jpg");
                    imgPath = product_name.getText().toString() + "ú" + imageKey + ".jpg";
                    myRef.child(userID).child("Products").child(ProductKey).child("P_ImagePath").setValue(imgPath);

                    UploadTask uploadTask = productImg.putFile(cur_ur);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                     //       Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        }
                    });

                }
                if (PICTURE_OPTION == 0) {
                    myRef.child(userID).child("Products").child(ProductKey).child("P_ImagePath").setValue(imgPath);
                }
                PICTURE_OPTION = 0;
                productCounter++;
                toastMessage(p_name + " has been added to your inventory!");

                    //Restart activity
                    Intent intent = new Intent(AddInventory.this, AddInventory.class);
                    sendIntentData(intent);
                }
            }
        });

        aisle_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                createBaySpinner();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        bay_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                createShelfSpinner();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        main_menu_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddInventory.this, MainMenu.class);
                sendIntentData(intent);
            }
        });

    }
    /*============Create Aisle Spinner==========*/
    public void createSpinner(int numAisles) {
        aisle_spinner = (Spinner) findViewById(R.id.aisle_spinner);
        aisle_spinnerValues = new ArrayList<>();

        for (int i = 1; i <= numAisles; i++) {
            aisle_spinnerValues.add(String.valueOf(i));
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item, aisle_spinnerValues);
        dataAdapter.setDropDownViewResource
                (android.R.layout.simple_spinner_dropdown_item);
        aisle_spinner.setAdapter(dataAdapter);
        aisle_spinner.setSelection(0);

    }
    /*========= Create Bay Spinner==========*/
    public void createBaySpinner(){
        bay_spinner = (Spinner)findViewById(R.id.bay_spinner);
        String crntA = aisle_spinner.getSelectedItem().toString();
        bay_spinnerValues = new ArrayList<>();
        for (int i = 1; i <= Integer.parseInt(aisleChecker); i++) {
            if (String.valueOf(i).equals(crntA)) {
                bayChecker = bayCheckerList.get(i-1);
                for (int j = 1; j <= Integer.parseInt(bayChecker); j++) {
                    bay_spinnerValues.add(String.valueOf(j));
                }
            }
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_spinner_item, bay_spinnerValues);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bay_spinner.setAdapter(dataAdapter);
        bay_spinner.setSelection(0);

    }
    /*=============Create Shelf Spinner==========*/
    public void createShelfSpinner(){
        String currentShelf = "";
        shelf_spinner = (Spinner)findViewById(R.id.shelf_spinner);
        String crntA = aisle_spinner.getSelectedItem().toString();
        String crntB = bay_spinner.getSelectedItem().toString();

        String abshold = "";
        String[] absArr;
        for (int i = 0; i < allABS.size(); i++) {
            abshold = allABS.get(i);
            absArr = abshold.split("¿");
            if (absArr[0].equals(crntA) && absArr[1].equals(crntB)){
                currentShelf = absArr[2];
            }
        }
        shelf_spinnerValues = new ArrayList<>();
        if (currentShelf.equals("0")){
            shelf_spinnerValues.add(String.valueOf(0));
        } else {
            for (int i = 1; i <= Integer.parseInt(currentShelf); i++) {
               shelf_spinnerValues.add(String.valueOf(i));
            }
        }
           ArrayAdapter<String> dataAdapter = new ArrayAdapter<>
                   (this, android.R.layout.simple_spinner_item, shelf_spinnerValues);
           dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
           shelf_spinner.setAdapter(dataAdapter);
    }
    /*===Generate Dept Spinner Values ====*/
    public void createDeptSpinner() {
        dept_spinner = (Spinner)findViewById(R.id.dept_spinner);
        dept_spinnerValues = new ArrayList<>();
        deptArr = deptString.split("\\s*,\\s*");
        /*== Auto Generate a None place holder of None into dept list
             to let user know which products aren't assigned.    ==*/
        for (int i = 0; i < deptArr.length; i++) {
                dept_spinnerValues.add(deptArr[i]);
            }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item,  dept_spinnerValues);
        dataAdapter.setDropDownViewResource
                (android.R.layout.simple_spinner_dropdown_item);
        dept_spinner.setAdapter(dataAdapter);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        /*== Retrieve Uploading Image==*/
        if (requestCode==RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data!=null){
            PICTURE_OPTION=1;
            cur_ur = data.getData();
            Glide.with(AddInventory.this)
                    .load(cur_ur)
                    .override(200,160)
                    .into(imageView);
            //imageView.setImageURI(cur_ur);

        }
        /*=== Retrieve Taking Image ==*/
        if (requestCode==RESULT_TAKE_IMAGE && resultCode == RESULT_OK && data!=null){
            PICTURE_OPTION=2;
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bitmap);
            imageView.setDrawingCacheEnabled(true);
            imageView.buildDrawingCache();
            bitmap = imageView.getDrawingCache();
            cur_bitmap = bitmap;
        }
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
