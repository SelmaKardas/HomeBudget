package com.example.selma.homebudget;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ShoppingListActivity extends AppCompatActivity {
    private ShoppingList shoppingListModel;
    private CollectionReference shoppingListProductsRef;
    private FirebaseFirestore rootRef;
    private CollectionReference userShoppingCategories;
    private String userEmail;
    private FirestoreRecyclerAdapter<Product, ProductListViewHolder> firestoreRecyclerAdapter;
    private GoogleApiClient googleApiClient;
    private ArrayList<String> listCategories;
    private String shoppingListId;
    private String userName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);
        listCategories=new ArrayList<>();
        shoppingListModel = (ShoppingList) getIntent().getSerializableExtra("shoppingListModel");
        shoppingListId = shoppingListModel.getShoppingListId();
        String shoppingListName = shoppingListModel.getShoppingListName();
        setTitle(shoppingListName);

        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (googleSignInAccount != null) {
            userEmail = googleSignInAccount.getEmail();
            userName = googleSignInAccount.getDisplayName();
        }

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        rootRef=FirebaseFirestore.getInstance();
        userShoppingCategories=rootRef.collection("categories").document(userEmail).collection("userCategories");
        FloatingActionButton fab=findViewById(R.id.fab_products);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userShoppingCategories.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            listCategories=new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String kategorija= document.getData().get("categoryName").toString();
                                listCategories.add(kategorija);
                            }
                            listCategories.add("");
                            listCategories.add("Add new category");
                            final View update_layout = getLayoutInflater().inflate(R.layout.alert_dialog_add_new_product, null);
                            final Spinner spinner_category = (Spinner) update_layout.findViewById(R.id.spinner_categories);
                            ArrayAdapter<String>adapter=new ArrayAdapter<String>(ShoppingListActivity.this,android.R.layout.simple_spinner_item,listCategories);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner_category.setAdapter(adapter);

                            final AlertDialog alertDialog = new AlertDialog.Builder(ShoppingListActivity.this)
                                    .setView(update_layout)
                                    .setTitle("Add new product")
                                    .setPositiveButton("Add", null) //Set to null. We override the onclick
                                    .setNegativeButton("Cancel", null)
                                    .create();

                            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

                                @Override
                                public void onShow(DialogInterface dialog) {
                                    final EditText new_category=update_layout.findViewById(R.id.new_category);
                                    final EditText product_name=update_layout.findViewById(R.id.productName);
                                    spinner_category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                        @Override
                                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                            if(spinner_category.getSelectedItem().toString().equals("Add new category")){
                                                new_category.setVisibility(View.VISIBLE);
                                                new_category.addTextChangedListener(new TextWatcher() {
                                                    @Override
                                                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                                    }

                                                    @Override
                                                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                                                    }

                                                    @Override
                                                    public void afterTextChanged(Editable s) {
                                                        if(new_category.getText().toString().trim().equals("")==false){
                                                            spinner_category.setEnabled(false);
                                                        }
                                                        else{
                                                            spinner_category.setEnabled(true);

                                                        }
                                                    }
                                                });
                                            }
                                            else{
                                                new_category.setVisibility(View.GONE);
                                            }
                                        }

                                        @Override
                                        public void onNothingSelected(AdapterView<?> parent) {

                                        }
                                    });
                                    Button nbutton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                                    nbutton.setTextColor(getResources().getColor(R.color.colorPrimary));
                                    Button pbutton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                                    pbutton.setTextColor(getResources().getColor(R.color.colorPrimary));

                                    pbutton.setOnClickListener(new View.OnClickListener() {

                                        @Override
                                        public void onClick(View view) {
                                            String productName = product_name.getText().toString().trim();
                                            String categoryTitle;
                                            boolean isNewCategory;
                                            if(new_category.getVisibility()==View.GONE) {
                                                categoryTitle=spinner_category.getSelectedItem().toString().trim();
                                                isNewCategory=false;
                                            }
                                            else{
                                                categoryTitle=new_category.getText().toString().trim();
                                                isNewCategory=true;

                                            }

                                            addProduct(productName,categoryTitle,isNewCategory);
                                            product_name.getText().clear();

                                        }
                                    });
                                }
                            });
                            alertDialog.show();

                        }
                    }

                });

            }

        });

        shoppingListProductsRef = rootRef.collection("products").document(shoppingListId).collection("shoppingListProducts");
        final RecyclerView recyclerView = findViewById(R.id.recycler_view_products);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final TextView emptyView = findViewById(R.id.empty_view_products);
        final ProgressBar progressBar = findViewById(R.id.progress_bar_products);
        Query query =shoppingListProductsRef
                .orderBy("izActiveProduct",Query.Direction.DESCENDING)
                .orderBy("category.categoryName",Query.Direction.ASCENDING)
                .orderBy("productName",Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Product> firestoreRecyclerOptions=new FirestoreRecyclerOptions.Builder<Product>()
                .setQuery(query,Product.class)
                .build();

        firestoreRecyclerAdapter=new FirestoreRecyclerAdapter<Product, ProductListViewHolder>(firestoreRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull ProductListViewHolder holder, int position, @NonNull Product model) {
                holder.setProduct(model,userEmail,userName,shoppingListModel);
            }
            @NonNull
            @Override
            public ProductListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_products, parent, false);
                return new ProductListViewHolder(view);
            }

            @Override
            public void onDataChanged() {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);

                }

                if (getItemCount() == 0) {
                    recyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);

                } else {

                    recyclerView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);

                }
            }

            @Override

            public int getItemCount() {
                return super.getItemCount();
            }
        };
        recyclerView.setAdapter(firestoreRecyclerAdapter);

    }

    private void addProduct(String productName, String categoryTitle, boolean isNewCategory) {
        String categoryId;
        if(isNewCategory){
            categoryId=userShoppingCategories.document().getId();
            Category new_category=new Category(categoryId,categoryTitle);
            userShoppingCategories.document(categoryTitle).set(new_category);
        }
        else{
            categoryId=userShoppingCategories.document(categoryTitle).getId();
        }
        String productId = shoppingListProductsRef.document().getId();
        Category category=new Category(categoryId,categoryTitle);
        Product product = new Product(productId, productName,category,true);
        shoppingListProductsRef.document(productId).set(product);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_share) {
            shareShoppingList();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void shareShoppingList() {

        AlertDialog.Builder builder = new AlertDialog.Builder(ShoppingListActivity.this);
        builder.setTitle("Share Shopping List");
        builder.setMessage("Please insert your friend's email");
        final EditText editText = new EditText(ShoppingListActivity.this);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        editText.setHint("Type an email address");
        editText.setHintTextColor(Color.GRAY);
        builder.setView(editText);
        builder.setPositiveButton("Share", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String friendsEmail = editText.getText().toString().trim();
                rootRef.collection("shoppingLists").document(friendsEmail)
                        .collection("userShoppingLists").document(shoppingListId)
                        .set(shoppingListModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Map<String, Object> users = new HashMap<>();
                        Map<String, Object> map = new HashMap<>();
                        map.put(userEmail, true);
                        map.put(friendsEmail, true);
                        users.put("users", map);
                        rootRef.collection("shoppingLists").document(userEmail)
                                .collection("userShoppingLists").document(shoppingListId)
                                .update(users);

                        rootRef.collection("shoppingLists").document(friendsEmail)
                                .collection("userShoppingLists").document(shoppingListId)
                                .update(users);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.shopp_list_menu, menu);

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
        firestoreRecyclerAdapter.startListening();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();

        }
        if (firestoreRecyclerAdapter != null) {
            firestoreRecyclerAdapter.stopListening();

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

}
