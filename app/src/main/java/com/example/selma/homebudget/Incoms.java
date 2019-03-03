package com.example.selma.homebudget;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;


public class Incoms extends Fragment {
    private DatePickerDialog datePicker;
    private EditText dateEditText;
    private Button saveButton;
    private EditText income;
    private EditText transactionDescr;
    private FirebaseFirestore rootRef;
    private CollectionReference userShoppingCategories;
    private CollectionReference userBudgets;
    private CollectionReference userTransactions;
    private String userEmail;
    private GoogleApiClient googleApiClient;
    private ArrayList<String> listCategories;
    private ArrayList<String> listBudgets;
    private ArrayAdapter<String> adapterBudget;
    private Spinner spinnerBudget;
    private Spinner spinnerCategories;
    private ArrayAdapter<String> adapterCategories;
    private TextView emptyFields;
    private String currentDate;
    private Boolean isIncome;
    private float amount;
    private float saldo;
    private TextView saldo_txv;
    private HashMap<String, String> spinnerMap;
    private String userName;
    private boolean categoriesUploaded;
    private boolean budgetsUploaded;

    public static Incoms newInstance() {
        Incoms fragment = new Incoms();
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View incomesFragmentView= inflater.inflate(R.layout.fragment_incoms, container, false);

        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(getContext());
        if (googleSignInAccount != null) {
            userEmail = googleSignInAccount.getEmail();
            userName = googleSignInAccount.getDisplayName();
        }

        googleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        listBudgets=new ArrayList<>();
        listCategories = new ArrayList<>();
        if(((MainActivity)getActivity()).bottomNavigationView.getSelectedItemId() == R.id.navigation_incomes){
            isIncome=true;

        }else{
            isIncome=false;
        }


        rootRef=FirebaseFirestore.getInstance();
        userShoppingCategories=rootRef.collection("categories").document(userEmail).collection("userCategories");
        userBudgets=rootRef.collection("budgets").document(userEmail).collection("userBudgets");
        userTransactions=rootRef.collection("transactions");
        userShoppingCategories.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull  Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    listCategories = new ArrayList<>();
                    spinnerCategories = incomesFragmentView.findViewById(R.id.spinner_categories);
                    adapterCategories = new ArrayAdapter<String>(getContext(),android.R.layout.simple_spinner_item,listCategories);
                    adapterCategories.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCategories.setAdapter(adapterCategories);

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String kategorija = document.getData().get("categoryName").toString();
                        listCategories.add(kategorija);
                    }
                    listCategories.add("");
                    listCategories.add("Add new category");
                    adapterCategories.notifyDataSetChanged();
                    categoriesUploaded=true;
                    spinnerCategories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
                            String selectedItem = parent.getItemAtPosition(position).toString();
                            if(selectedItem.equals("Add new category"))
                            {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Add new category");
                                final EditText editText = new EditText(getContext());
                                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                                editText.setHint("Type a name");
                                editText.setHintTextColor(Color.GRAY);
                                builder.setView(editText);
                                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String categoryName = editText.getText().toString().trim();
                                        addCategory(categoryName);

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

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                }
            }
        });

        userBudgets.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull final Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    listBudgets = new ArrayList<>();
                    spinnerBudget = incomesFragmentView.findViewById(R.id.spinner_budget);
                    adapterBudget = new ArrayAdapter<String>(getContext(),android.R.layout.simple_spinner_item,listBudgets);
                    adapterBudget.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerBudget.setAdapter(adapterBudget);
                    spinnerMap = new HashMap<>();
                    if(task.getResult().isEmpty()){
                    }
                    else {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String budgetName=document.getData().get("budgetName").toString();
                            String budgetId=document.getData().get("budgetId").toString();
                            spinnerMap.put(budgetName,budgetId);
                            listBudgets.add(budgetName);
                        }
                        Collections.sort(listBudgets, new Comparator<String>() {
                            @Override
                            public int compare(String s1, String s2) {
                                return s1.compareToIgnoreCase(s2);
                            }
                        });
                    }
                    listBudgets.add("");
                    listBudgets.add("Add new budget");
                    adapterBudget.notifyDataSetChanged();
                    budgetsUploaded=true;
                    spinnerBudget.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
                            String selectedItem = parent.getItemAtPosition(position).toString();
                            String budgetId=spinnerMap.get(selectedItem);
                            if(selectedItem.equals("Add new budget"))
                            {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Add new budget");
                                final EditText editText = new EditText(getContext());
                                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                                editText.setHint("Type a name");
                                editText.setHintTextColor(Color.GRAY);
                                builder.setView(editText);
                                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String budgetName = editText.getText().toString().trim();
                                        addBudget(budgetName);

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
                            else{
                                rootRef.collection("transactions").document(budgetId).collection("budget_transactions").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if(task.isSuccessful()){
                                            saldo = 0;
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                String string_amount = document.getData().get("amount").toString();
                                                Float trans_amount=Float.valueOf(string_amount);
                                                saldo = saldo +trans_amount;

                                            }
                                            saldo_txv = incomesFragmentView.findViewById(R.id.text_saldo);
                                            NumberFormat formatter = NumberFormat.getNumberInstance();
                                            formatter.setMinimumFractionDigits(2);
                                            formatter.setMaximumFractionDigits(2);
                                            saldo_txv.setText(formatter.format(saldo));

                                        }

                                    }
                                });

                            }

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                }
            }
        });

        dateEditText=incomesFragmentView.findViewById(R.id.edit_text_date);
        dateEditText.setInputType(InputType.TYPE_NULL);
        Calendar calendar_current=Calendar.getInstance();
        SimpleDateFormat df=new SimpleDateFormat("dd/MM/yyyy");
        currentDate = df.format(calendar_current.getTime());
        dateEditText.setText(currentDate);
        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar=Calendar.getInstance();
                int day=calendar.get(Calendar.DAY_OF_MONTH);
                int month=calendar.get(Calendar.MONTH);
                int year=calendar.get(Calendar.YEAR);

                datePicker=new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        dateEditText.setText(dayOfMonth+"/"+(month+1)+"/"+year);
                    }
                },year,month,day);
                datePicker.show();
            }
        });



        saveButton=incomesFragmentView.findViewById(R.id.button_save);
        income=incomesFragmentView.findViewById(R.id.edit_text_amount);
        transactionDescr=incomesFragmentView.findViewById(R.id.edit_text_transaction_description);
        transactionDescr.addTextChangedListener(watcher);
        income.addTextChangedListener(watcher);
        emptyFields = incomesFragmentView.findViewById(R.id.empty_view);
        saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (budgetsUploaded && categoriesUploaded) {
                        final String budgetName = spinnerBudget.getSelectedItem().toString();
                        final String budgetId=spinnerMap.get(budgetName);
                        final String transactionName=transactionDescr.getText().toString();
                        String categoryName = spinnerCategories.getSelectedItem().toString();
                        if(budgetName.trim().equals("") || categoryName.trim().equals("") || transactionName.trim().equals("") || income.getText().toString().trim().equals("")){
                            emptyFields.setVisibility(View.VISIBLE);
                        }
                        else {
                            if(isIncome){
                                amount = Float.valueOf(income.getText().toString());
                            }
                            else{
                                amount = Float.valueOf(income.getText().toString())*(-1);
                            }
                            DocumentReference docRef = userShoppingCategories.document(categoryName);
                            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    Category category = documentSnapshot.toObject(Category.class);
                                    String date = dateEditText.getText().toString();
                                    SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                                    Date selectedDate;
                                    try {
                                        selectedDate = df.parse(date);
                                        String transactionId = userTransactions.document(budgetId).collection("budget_transactions").document().getId();
                                        Transaction transaction = new Transaction(userEmail, userName, transactionName, transactionId, amount, isIncome, category, selectedDate);
                                        addTransaction(budgetId, transactionId, transaction);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }

                    }
                }
            });




        return incomesFragmentView;

    }
    private final TextWatcher watcher=new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if((transactionDescr.getText().toString().trim().equals(""))||(income.getText().toString().trim().equals(""))||(!categoriesUploaded)||(!budgetsUploaded)){
                saveButton.setEnabled(false);

            }
            else{
                saveButton.setEnabled(true);
            }

        }
    };

    private void addTransaction(final String budgetId, String transactionId, Transaction transaction) {

       userTransactions.document(budgetId).collection("budget_transactions").document(transactionId).set(transaction).addOnSuccessListener(new OnSuccessListener<Void>(){
           @Override
           public void onSuccess(Void aVoid) {
               transactionDescr.getText().clear();
               income.getText().clear();
               emptyFields.setVisibility(View.GONE);
               userTransactions.document(budgetId).collection("budget_transactions").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                   @Override
                   public void onComplete(@NonNull Task<QuerySnapshot> task) {
                       if(task.isSuccessful()){
                           saldo = 0;
                           for (QueryDocumentSnapshot document : task.getResult()) {
                               String string_amount = document.getData().get("amount").toString();
                               Float trans_amount=Float.valueOf(string_amount);
                               saldo = saldo +trans_amount;

                           }
                           NumberFormat formatter = NumberFormat.getNumberInstance();
                           formatter.setMinimumFractionDigits(2);
                           formatter.setMaximumFractionDigits(2);
                           saldo_txv.setText(formatter.format(saldo));

                       }

                   }
               });


           }
       });


    }


        private void addBudget(final String budgetName) {
        final String budgetId = userBudgets.document().getId();
        Budget budget=new Budget(budgetId,budgetName,userEmail);
        userBudgets.document(budgetId).set(budget).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                spinnerMap.put(budgetName,budgetId);
                listBudgets.add(0,budgetName);
                adapterBudget.notifyDataSetChanged();
                spinnerBudget.setSelection(0);

            }
        });
    }

    private void addCategory(final String categoryName) {
        String categoryId = userShoppingCategories.document().getId();
        Category category=new Category(categoryId,categoryName);
        userShoppingCategories.document(categoryName).set(category).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                listCategories.add(0,categoryName);
                adapterCategories.notifyDataSetChanged();
                spinnerCategories.setSelection(0);

            }
        });
    }


}
