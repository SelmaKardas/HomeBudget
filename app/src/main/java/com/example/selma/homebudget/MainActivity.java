package com.example.selma.homebudget;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private GoogleApiClient googleApiClient;
    private FirebaseFirestore rootRef;
    private CollectionReference userShoppingCategories;
    private CollectionReference userBudgets;
    private String userEmail;
    public BottomNavigationView bottomNavigationView;
    private HashMap<String, String> spinnerMap;
    private ArrayList<String> friendsCategories;


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        bottomNavigationView = (BottomNavigationView)findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                switch (item.getItemId()) {
                    case R.id.navigation_shopping_lists:
                        selectedFragment = ShoppingLists.newInstance();
                        setTitle("Shopping Lists");
                        break;
                    case R.id.navigation_incomes:
                        selectedFragment = Incoms.newInstance();
                        setTitle("Incomes");
                        break;
                    case R.id.navigation_expenses:
                        selectedFragment = Incoms.newInstance();
                        setTitle("Expenses");
                        break;
                    case R.id.navigation_reports:
                        selectedFragment = Reports.newInstance();
                        setTitle("Reports");
                        break;
                }
                loadFragment(selectedFragment);
                return true;

            }
        });
        if (savedInstanceState==null){
        loadFragment(ShoppingLists.newInstance());
        setTitle("Shopping Lists");
        }

        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (googleSignInAccount != null) {
            userEmail = googleSignInAccount.getEmail();
        }
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        mAuth=FirebaseAuth.getInstance();
        mAuthListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()==null){
                   startActivity(new Intent(MainActivity.this,LoginActivity.class));
                   finish();
                }

            }
        };

        rootRef=FirebaseFirestore.getInstance();
        userShoppingCategories=rootRef.collection("categories").document(userEmail).collection("userCategories");
        userBudgets=rootRef.collection("budgets").document(userEmail).collection("userBudgets");
        userShoppingCategories.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    boolean noCategories = task.getResult().isEmpty();
                    if(noCategories){
                        setInitialCategories();
                    }
                }
            }
        });

        userBudgets.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){

                    boolean noCategories = task.getResult().isEmpty();
                    if(noCategories){
                        setInitialBudgets();
                    }
                }
            }
        });
    }

    private void setInitialCategories() {
        String[] values={"Groceries","Fuel","Nursery","Presents","Salary","Clothes"};
        for (int i = 0; i < values.length; i++) {
            String categoryId=userShoppingCategories.document().getId();
            Category category=new Category(categoryId, values[i]);
            userShoppingCategories.document(values[i]).set(category);
        }
    }

    private void setInitialBudgets() {
        String[] values={"Budget 1","Budget 2"};
        for (int i = 0; i < values.length; i++) {
            String budgetId = userBudgets.document().getId();
            Budget budget=new Budget(budgetId, values[i],userEmail);
            userBudgets.document(budgetId).set(budget);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_logout:
                FirebaseAuth.getInstance().signOut();
                if (googleApiClient.isConnected()) {
                    Auth.GoogleSignInApi.signOut(googleApiClient);
                }
                return true;

            case R.id.action_settings:
                return true;

            case R.id.action_share_budget:
                shareBudget();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }


    }

    private void shareBudget() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = (MainActivity.this).getLayoutInflater();
        final View update_layout = getLayoutInflater().inflate(R.layout.alert_dialog_share_budget,null);
        final Spinner spinner = (Spinner) update_layout.findViewById(R.id.spinner_budgets);
        userBudgets.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    spinnerMap = new HashMap<>();
                    ArrayList<String>listBudgets = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String budgetName = document.getData().get("budgetName").toString();
                        String budgetId = document.getData().get("budgetId").toString();
                        spinnerMap.put(budgetName, budgetId);
                        listBudgets.add(budgetName);
                    }
                    Collections.sort(listBudgets, new Comparator<String>() {
                        @Override
                        public int compare(String s1, String s2) {
                            return s1.compareToIgnoreCase(s2);
                        }
                    });
                    ArrayAdapter<String> adapter= new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_spinner_item,listBudgets);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);
                }

            }
        });

        builder.setView(update_layout);
        builder.setPositiveButton("Share", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText friends_email_edit_text=update_layout.findViewById(R.id.friendsEmail);
                String budgetName=spinner.getSelectedItem().toString();
                final String budgetId=spinnerMap.get(budgetName);
                Budget sharedBudget=new Budget(budgetId,budgetName,userEmail);
                final String friendsEmail = friends_email_edit_text.getText().toString().trim();
                rootRef.collection("budgets").document(friendsEmail)
                        .collection("userBudgets").document(budgetId)
                        .set(sharedBudget).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Map<String, Object> users = new HashMap<>();
                        Map<String, Object> map = new HashMap<>();
                        map.put(userEmail, true);
                        map.put(friendsEmail, true);
                        users.put("users", map);
                        rootRef.collection("budgets").document(userEmail)
                                .collection("userBudgets").document(budgetId)
                                .update(users);

                        rootRef.collection("budgets").document(friendsEmail)
                                .collection("userBudgets").document(budgetId)
                                .update(users);

                        rootRef.collection("categories").document(friendsEmail).collection("userCategories").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful()){
                                    friendsCategories = new ArrayList();
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        String category = document.getData().get("categoryName").toString();
                                        friendsCategories.add(category);
                                    }

                                    rootRef.collection("transactions").document(budgetId).collection("budget_transactions").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull final Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()){
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                   Transaction model=document.toObject(Transaction.class);
                                                   String categoryName=model.getCategory().getCategoryName();
                                                   if(!friendsCategories.contains(categoryName)){
                                                        addCategory(categoryName,friendsEmail);

                                                    }

                                                }
                                            }

                                        }
                                    });
                                }
                            }

                        });


                    }
                });

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }


    private void addCategory(String categoryName, String friendsEmail) {
        CollectionReference friendsShoppingCategories=rootRef.collection("categories").document(friendsEmail).collection("userCategories");
        String categoryId = friendsShoppingCategories.document().getId();
        Category category=new Category(categoryId,categoryName);
        friendsShoppingCategories.document(categoryName).set(category);

    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();

        }
    }

private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
        }
}
