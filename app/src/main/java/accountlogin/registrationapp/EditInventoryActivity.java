package accountlogin.registrationapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

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
import java.io.File;
import java.util.ArrayList;

public class EditInventoryActivity extends AppCompatActivity {
    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int RESULT_TAKE_IMAGE = 2;
    static int PICTURE_OPTION = 0;

    //Layout Variables
    ImageView imageView;
    Spinner product_spinner;
    Spinner dept_spinner, aisle_spinner, bay_spinner, shelf_spinner;
    EditText product_name, product_id, num_stock, product_desc;
    Button take_image_btn, upload_image_btn, delete_image_btn, undo_changes_btn, save_changes_btn;

    /*-Aisle Spinner Vars-*/
    String aisleChecker = "";
    ArrayList<String> aisle_spinnerValues = new ArrayList<>();

    /*-Bay Spinner Vars-*/
    String bayChecker = "";
    ArrayList<String> bay_spinnerValues = new ArrayList<>();
    ArrayList<String> bayCheckerList = new ArrayList<>();

    /*-Shelf Spinner Vars-*/
    ArrayList<String> shelf_spinnerValues = new ArrayList<>();
    ArrayList<String> shelfCheckerList = new ArrayList<>();

    /*-Dept Spinner Vars-*/
    String deptString = "";
    String[] deptArr;
    ArrayList<String> dept_spinnerValues = new ArrayList<>();

    int currentAisleSpinner = 0;
    int currentBaySpinner = 0;
    int currentShelfSpinner = 0;

    //Firebase Variables
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //Database References
    private DatabaseReference myRef;
    private DatabaseReference AisleRef;
    private DatabaseReference AisleBayRef;
    private DatabaseReference ShelfRef;
    private DatabaseReference ProductRef;
    private String userID;

    //Firebase Image StorageReference
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl("gs://accountloginregistrationapp.appspot.com");
    StorageReference productImg;
    String imagePath = "";
    Uri cur_ur;
    Bitmap cur_bitmap;
    String UriTricker = "";
    ArrayList<String> productChecker = new ArrayList<>();
    String productString = "";

    //Place holders for the selected products current spinner values
    String p_curAisle = "";
    String p_curBay="";
    String p_curShelf="";
    String p_curDept="";
    String p_curKey= "";
    String imgPath="";
    String p_curImg="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_inventory);
        Intent intent = getIntent();

        imageView = (ImageView)findViewById(R.id.imageView);

        //Variable initialization
        product_spinner = (Spinner)findViewById(R.id.product_spinner);
        dept_spinner = (Spinner)findViewById(R.id.dept_spinner);
        aisle_spinner = (Spinner)findViewById(R.id.aisle_spinner);
        bay_spinner = (Spinner)findViewById(R.id.bay_spinner);
        shelf_spinner = (Spinner)findViewById(R.id.shelf_spinner);

        product_name = (EditText)findViewById(R.id.product_name);
        num_stock = (EditText)findViewById(R.id.num_stock);
        product_id = (EditText)findViewById(R.id.product_id);
        product_desc = (EditText)findViewById(R.id.product_desc);

        save_changes_btn = (Button)findViewById(R.id.save_changes_btn);
        take_image_btn = (Button)findViewById(R.id.take_image_btn);
        upload_image_btn = (Button)findViewById(R.id.upload_image_btn);
        delete_image_btn = (Button)findViewById(R.id.delete_image_btn);
        undo_changes_btn = (Button)findViewById(R.id.undo_changes_btn);



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
        /*===== Adding the new changes to the database under the current product key =====*/
        save_changes_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                myRef.child(userID).child("Products").child(p_curKey).child("P_ID").setValue(product_id.getText().toString());
                myRef.child(userID).child("Products").child(p_curKey).child("P_Aisle").setValue(String.valueOf(aisle_spinner.getSelectedItem()));
                myRef.child(userID).child("Products").child(p_curKey).child("P_Bay").setValue(String.valueOf(bay_spinner.getSelectedItem()));
                myRef.child(userID).child("Products").child(p_curKey).child("P_Shelf").setValue(String.valueOf(shelf_spinner.getSelectedItem()));
                myRef.child(userID).child("Products").child(p_curKey).child("P_Dept").setValue(String.valueOf(dept_spinner.getSelectedItem()));
                myRef.child(userID).child("Products").child(p_curKey).child("P_Desc").setValue(product_desc.getText().toString());
                myRef.child(userID).child("Products").child(p_curKey).child("P_Name").setValue(product_name.getText().toString());
                myRef.child(userID).child("Products").child(p_curKey).child("P_Stock").setValue(num_stock.getText().toString());
                imgPath="n/a";

                if (PICTURE_OPTION == 2) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    cur_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    StorageReference productImg = storageRef.child(userID + ".images/" + product_name.getText().toString() + ".jpg");
                    byte[] dataz = baos.toByteArray();
                    UploadTask uploadTask = productImg.putBytes(dataz);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                            cur_ur = taskSnapshot.getDownloadUrl();
                            imgPath = product_name.getText().toString() + ".jpg";
                            Log.i("P.Option=2: ", cur_ur.getPath());
                            myRef.child(userID).child("Products").child(p_curKey).child("P_ImagePath").setValue(imgPath);
                        }
                    });
                }
                if (PICTURE_OPTION == 1) {
                    //Storing images in folders based on the userID (or we can do StoreName)
                    productImg = storageRef.child(userID + ".images/" + product_name.getText().toString() + ".jpg");
                    productImg.putFile(cur_ur);
                    imgPath = product_name.getText().toString() + ".jpg";
                    myRef.child(userID).child("Products").child(p_curKey).child("P_ImagePath").setValue(imgPath);
                    Log.i("P.Option=1: ", cur_ur.getPath());
                }

            }
        });
        take_image_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, RESULT_TAKE_IMAGE);
            }
        });
        upload_image_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(gallery,RESULT_LOAD_IMAGE);

            }

        });

          /*==== Product listener - pulling all the stores product info and forming it into an arrayList ====*/
        ProductRef = mFirebaseDatabase.getReference().child(userID).child("Products");
        ProductRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                productChecker = new ArrayList<String>();
                productString = "";
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    String p_key = data.getKey();
                    String p_aisle = data.child("P_Aisle").getValue().toString();
                    String p_bay = data.child("P_Bay").getValue().toString();
                    String p_shelf = data.child("P_Shelf").getValue().toString();
                    String p_dept = data.child("P_Dept").getValue().toString();

                    String p_name = data.child("P_Name").getValue().toString();
                    String p_id = data.child("P_ID").getValue().toString();
                    String p_desc = data.child("P_Desc").getValue().toString();
                    String p_stock = data.child("P_Stock").getValue().toString();
                    String p_ImagePath = data.child("P_ImagePath").getValue().toString();

                    /*==Probably should auto assign null values on AddInventory..==*/
                    if (p_ImagePath.isEmpty()){
                        p_ImagePath = "n/a";
                    }
                    if (p_id.isEmpty()){
                        p_id="N/A";
                    }
                    if (p_desc.isEmpty()){
                        p_desc="N/A";
                    }
                    if (p_stock.isEmpty()){
                        p_stock="N/A";
                    }
                    if (p_aisle.isEmpty()){
                        p_aisle="¿";
                    }
                    if (p_bay.isEmpty()){
                        p_bay="¿";
                    }
                    if (p_shelf.isEmpty()){
                        p_shelf="¿";
                    }
                    if (p_dept.isEmpty()){
                        p_dept="None";
                    }
                    productString = p_name + "¿" + p_aisle + "¿" + p_bay + "¿" + p_shelf + "¿" + p_dept + "¿" + p_id + "¿" + p_desc + "¿" + p_stock + "¿" + p_key + "¿"+ p_ImagePath;
                    productChecker.add(productString);
                }
                createProductSpinner();

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        /*====== On Product Selected ====*/
        product_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] myProdArr;
                String currentProductSpinner = product_spinner.getSelectedItem().toString();

                for (int i = 0; i < productChecker.size() ; i++) {
                    String strHold = productChecker.get(i);
                    myProdArr = strHold.split("¿");
                    //Getting the current products variables & assigning them to their corresponding position
                    if (currentProductSpinner.equals(myProdArr[0])){
                        p_curImg = myProdArr[9];
                        p_curKey = myProdArr[8]; //current product key
                        product_name.setText(myProdArr[0]);
                        p_curAisle = myProdArr[1];
                        p_curBay = myProdArr[2];
                        p_curShelf = myProdArr[3];
                        /*Handle the empty fields -- to avoid error on display results based on whats in the myProdArr*/
                        if (myProdArr[4].equals("None")){
                            p_curDept = "None";
                        } else {
                            p_curDept = myProdArr[4];
                        }

                        if (myProdArr[5].equals("N/A")){
                            product_id.setText("");
                        } else {
                            product_id.setText(myProdArr[5]);
                        }
                        if (myProdArr[6].equals("N/A")){
                            product_desc.setText("");
                        } else {
                            product_desc.setText(myProdArr[6]);
                        }

                        if (myProdArr[7].equals("N/A")){
                            num_stock.setText("");
                        }else {
                            num_stock.setText(myProdArr[7]);
                        }
                        if (!myProdArr[9].equals("n/a")) {
                           // productImg = storageRef.child(userID + ".images/" + myProdArr[0] + ".jpg");
                            Uri downloadURI = storageRef.child(userID+".images/"+myProdArr[0]+".jpg").getDownloadUrl().getResult();
                            imageView.setImageURI(downloadURI);


                        }
                    }
                }

                /*==Generate Aisle/Bay/Shelf/Dept Spinners====*/
                createSpinner();
                createBaySpinner();
                createShelfSpinner();
                createDeptSpinner();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        aisle_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                createShelfSpinner();
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
                    }
                    if (data.getKey().equals("deptNames") && !data.getValue().toString().trim().equals("")) {
                        deptString = data.getValue().toString();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        /*===== ShelfSetup DB reference to generate spinner ===*/
        ShelfRef = mFirebaseDatabase.getReference().child(userID).child("ShelfSetup");
        ShelfRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                shelfCheckerList = new ArrayList<String>();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    shelfCheckerList.add(data.child("num_of_shelves").getValue().toString());
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
                bayCheckerList = new ArrayList<>();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    bayCheckerList.add(data.child("bays").getValue().toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    /*========Create Product Spinner ==========*/
    public void createProductSpinner(){
        product_spinner = (Spinner)findViewById(R.id.product_spinner);
        String productHold = "";
        String[] productHoldArr;
        ArrayList<String> productSpin = new ArrayList<>();
        for (int i = 0; i <productChecker.size(); i++) {
            productHold = productChecker.get(i);
            productHoldArr = productHold.split("¿");
            productSpin.add(i,productHoldArr[0]);
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item,  productSpin);
        dataAdapter.setDropDownViewResource
                (android.R.layout.simple_spinner_dropdown_item);
        product_spinner.setAdapter(dataAdapter);
    }


    /*============Create Aisle Spinner==========*/
    public void createSpinner() {
        aisle_spinner = (Spinner) findViewById(R.id.aisle_spinner);
        int cur_selection = 0;
        aisle_spinnerValues = new ArrayList<>(); //ensures spinner values wont duplicate
        if (Integer.parseInt(aisleChecker) < 0) {
            Log.i("Checking Aisle in DB: ", "There's no value!");
        } else { //Generate Spinner
            for (int i = 0; i < Integer.parseInt(aisleChecker); i++) {
                /*--Get Current Product Aisle Position--*/
                if (i == Integer.parseInt(p_curAisle)){
                    cur_selection = i;
                }
                    aisle_spinnerValues.add(String.valueOf(i));
            }
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>
                    (this, android.R.layout.simple_spinner_item, aisle_spinnerValues);
            dataAdapter.setDropDownViewResource
                    (android.R.layout.simple_spinner_dropdown_item);
            aisle_spinner.setAdapter(dataAdapter);
            aisle_spinner.setSelection(cur_selection);

        }
    }
    /*========= Create Bay Spinner==========*/
    public void createBaySpinner(){
        int cur_selection = 0;
        bay_spinner = (Spinner)findViewById(R.id.bay_spinner);
        int currentAisleSpinner = aisle_spinner.getSelectedItemPosition();
        bay_spinnerValues = new ArrayList<>();
        for (int i = 0; i < Integer.parseInt(aisleChecker); i++) {
            if (i == currentAisleSpinner) {
                bayChecker = bayCheckerList.get(i);
                for (int j = 0; j < Integer.parseInt(bayChecker); j++) {
                    /*--Get Current Product Bay Position--*/
                    if (j == Integer.parseInt(p_curBay)){
                        cur_selection=j;
                    }
                    bay_spinnerValues.add(String.valueOf(j));
                }
            }
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_spinner_item, bay_spinnerValues);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bay_spinner.setAdapter(dataAdapter);
        bay_spinner.setSelection(cur_selection);
    }
    /*=============Create Shelf Spinner==========*/
    public void createShelfSpinner() {
        int cur_selection = 0;
        String currentShelf = "";
        shelf_spinner = (Spinner) findViewById(R.id.shelf_spinner);
        currentAisleSpinner = aisle_spinner.getSelectedItemPosition();
        currentBaySpinner = bay_spinner.getSelectedItemPosition();

        shelf_spinnerValues = new ArrayList<>();
        for (int i = 0; i <= currentAisleSpinner; i++) {
            for (int j = 0; j <= currentBaySpinner; j++) {
                if (currentAisleSpinner == i && currentBaySpinner == j) {
                    currentShelf = shelfCheckerList.get(j);
                }
            }
        }
        if (Integer.parseInt(currentShelf) == 0) {
            shelf_spinnerValues.add(String.valueOf(0));
        } else {
            for (int i = 0; i < Integer.parseInt(currentShelf); i++) {
                /*--Get Current Product Shelf Position--*/
                if (i == Integer.parseInt(p_curShelf)){
                    cur_selection = i;
                }
                shelf_spinnerValues.add(String.valueOf(i));
            }
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_spinner_item, shelf_spinnerValues);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shelf_spinner.setAdapter(dataAdapter);
        shelf_spinner.setSelection(cur_selection);
    }
    /*===Generate Dept Spinner Values ====*/
    public void createDeptSpinner() {
        dept_spinner = (Spinner)findViewById(R.id.dept_spinner);
        dept_spinnerValues = new ArrayList<>();
        deptArr = deptString.split("\\s*,\\s*");
        int cur_selection = deptArr.length;
        /*== Auto Generate a None place holder of None into dept list
             to let user know which products aren't assigned.    ==*/
        for (int i = 0; i <= deptArr.length; i++) {
            if (i==deptArr.length){
                dept_spinnerValues.add("None");
            } else {
                dept_spinnerValues.add(deptArr[i]);
                if (deptArr[i].equals(p_curDept)){
                    cur_selection=i;
                }
            }
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item,  dept_spinnerValues);
        dataAdapter.setDropDownViewResource
                (android.R.layout.simple_spinner_dropdown_item);
        dept_spinner.setAdapter(dataAdapter);
        dept_spinner.setSelection(cur_selection);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode==RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data!=null){
            PICTURE_OPTION=1;
            Uri selectedImage = data.getData();
            imagePath = selectedImage.getPath();
            cur_ur = selectedImage;
            imageView.setImageURI(cur_ur);
        }
        if (requestCode==RESULT_TAKE_IMAGE && resultCode == RESULT_OK && data!=null){
            PICTURE_OPTION=2;
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bitmap);
            imageView.setDrawingCacheEnabled(true);
            imageView.buildDrawingCache();

            bitmap = imageView.getDrawingCache();
            imageView.setImageBitmap(bitmap);

            cur_bitmap = bitmap;

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
}
