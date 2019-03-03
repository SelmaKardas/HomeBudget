package com.example.selma.homebudget;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.util.Date;


public class ShoppingLists extends Fragment {

    private CollectionReference userShoppingListsRef;
    private FirebaseFirestore rootRef;
    private String userEmail;
    private FirestoreRecyclerAdapter<ShoppingList, ShoppingListViewHolder> firestoreRecyclerAdapter;
    private GoogleApiClient googleApiClient;
    private Date date;

    public static ShoppingLists newInstance() {
        ShoppingLists fragment = new ShoppingLists();
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View shoppingListViewFragment= inflater.inflate(R.layout.fragment_shopping_lists, container, false);


        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(getContext());
        if (googleSignInAccount != null) {
            userEmail = googleSignInAccount.getEmail();
        }

        googleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();
        rootRef = FirebaseFirestore.getInstance();
        FloatingActionButton fab=shoppingListViewFragment.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Create new shopping list");
                final EditText editText = new EditText(getContext());
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                editText.setHint("Type a name");
                editText.setHintTextColor(Color.GRAY);
                builder.setView(editText);
                builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String shoppingListName = editText.getText().toString().trim();
                        addList(shoppingListName);
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
                Button nbutton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                nbutton.setTextColor(getResources().getColor(R.color.colorPrimary));
                Button pbutton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                pbutton.setTextColor(getResources().getColor(R.color.colorPrimary));
            }

        });

        userShoppingListsRef = rootRef.collection("shoppingLists").document(userEmail).collection("userShoppingLists");
       final RecyclerView recyclerView = shoppingListViewFragment.findViewById(R.id.recycler_view);
       recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
       final TextView emptyView = shoppingListViewFragment.findViewById(R.id.empty_view);
       final ProgressBar progressBar = shoppingListViewFragment.findViewById(R.id.progress_bar);
       Query query = userShoppingListsRef.orderBy("shoppingListName",Query.Direction.ASCENDING);

       FirestoreRecyclerOptions<ShoppingList>firestoreRecyclerOptions=new FirestoreRecyclerOptions.Builder<ShoppingList>()
                .setQuery(query,ShoppingList.class)
                .build();

       firestoreRecyclerAdapter=new FirestoreRecyclerAdapter<ShoppingList, ShoppingListViewHolder>(firestoreRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull ShoppingListViewHolder holder, int position, @NonNull ShoppingList model) {
                holder.setShoppingList(getContext(),model);
            }
            @NonNull
            @Override
            public ShoppingListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shopping_lists, parent, false);
                return new ShoppingListViewHolder(view);
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
        return shoppingListViewFragment;
    }

    private void addList(String shoppingListName) {
        String shoppingListId = userShoppingListsRef.document().getId();
        ShoppingList shoppList=new ShoppingList(shoppingListId,shoppingListName,userEmail);
        userShoppingListsRef.document(shoppingListId).set(shoppList);

    }

    @Override
    public void onResume() {
        super.onResume();
        firestoreRecyclerAdapter.startListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        googleApiClient.connect();
        firestoreRecyclerAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        if (firestoreRecyclerAdapter != null) {
            firestoreRecyclerAdapter.stopListening();

        }

    }

}
