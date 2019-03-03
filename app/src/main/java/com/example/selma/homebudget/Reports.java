package com.example.selma.homebudget;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
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


public class Reports extends Fragment {
    private DatePickerDialog datePicker;
    private FirebaseFirestore rootRef;
    private CollectionReference userShoppingCategories;
    private CollectionReference userBudgets;
    private CollectionReference budgetTransactions;
    private String userEmail;
    private GoogleApiClient googleApiClient;
    private Spinner spinnerBudget;
    private Spinner spinnerCategories;
    private FirestoreRecyclerAdapter<Transaction, TransactionViewHolder> firestoreRecyclerAdapter;
    private boolean categoriesUploaded;
    private boolean budgetsUploaded;
    private Query query;
    private Query query_new;
    private boolean isGeneratedOnce;
    private FirestoreRecyclerOptions<Transaction> firestoreRecyclerOptions;
    private Date date_start;
    private Date date_stop;
    private HashMap<String, String> spinnerMap;
    private Button generateReport;
    private String userName;

    public static Reports newInstance() {
        Reports fragment = new Reports();
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
        if (firestoreRecyclerAdapter != null) {
            firestoreRecyclerAdapter.stopListening();

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View reportsFragmentView = inflater.inflate(R.layout.fragment_reports, container, false);

        categoriesUploaded=false;
        budgetsUploaded=false;
        isGeneratedOnce=false;


        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(getContext());
        if (googleSignInAccount != null) {
            userEmail = googleSignInAccount.getEmail();
            userName = googleSignInAccount.getDisplayName();
        }

        googleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();


        rootRef = FirebaseFirestore.getInstance();
        userShoppingCategories=rootRef.collection("categories").document(userEmail).collection("userCategories");
        userBudgets=rootRef.collection("budgets").document(userEmail).collection("userBudgets");
        generateReport = reportsFragmentView.findViewById(R.id.button_show_report);
        spinnerCategories = reportsFragmentView.findViewById(R.id.spinner_categories);

        userShoppingCategories.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull  Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    ArrayList<String>listCategories = new ArrayList<>();
                    listCategories.add("All");
                    ArrayAdapter<String>adapterCategories = new ArrayAdapter<String>(getContext(),android.R.layout.simple_spinner_item,listCategories);
                    adapterCategories.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCategories.setAdapter(adapterCategories);

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String category = document.getData().get("categoryName").toString();
                        listCategories.add(category);
                    }

                    adapterCategories.notifyDataSetChanged();
                    categoriesUploaded=true;
                    if(budgetsUploaded){
                        generateReport.setEnabled(true);
                    }
                }
            }
        });

        spinnerBudget = reportsFragmentView.findViewById(R.id.spinner_budget);
        userBudgets.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull final Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    spinnerMap = new HashMap<>();
                    ArrayList<String>listBudgets = new ArrayList<>();
                    ArrayAdapter<String>adapterBudget = new ArrayAdapter<String>(getContext(),android.R.layout.simple_spinner_item,listBudgets);
                    adapterBudget.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerBudget.setAdapter(adapterBudget);
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

                    adapterBudget.notifyDataSetChanged();
                    budgetsUploaded=true;
                    if(categoriesUploaded){
                        generateReport.setEnabled(true);

                    }

                }
            }
        });

        final EditText text_date_start=reportsFragmentView.findViewById(R.id.edit_text_date_start);
        final EditText text_date_end =reportsFragmentView.findViewById(R.id.edit_text_date_stop);
        text_date_start.setInputType(InputType.TYPE_NULL);
        text_date_end.setInputType(InputType.TYPE_NULL);

        Calendar c=Calendar.getInstance();
        setDate(c,text_date_end);
        c.set(Calendar.DAY_OF_MONTH, 1);
        setDate(c,text_date_start);

        text_date_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar=Calendar.getInstance();
                int day=calendar.get(Calendar.DAY_OF_MONTH);
                int month=calendar.get(Calendar.MONTH);
                int year=calendar.get(Calendar.YEAR);

                datePicker=new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        text_date_start.setText(dayOfMonth+"/"+(month+1)+"/"+year);
                    }
                },year,month,day);
                datePicker.show();
            }
        });


        text_date_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar=Calendar.getInstance();
                int day=calendar.get(Calendar.DAY_OF_MONTH);
                int month=calendar.get(Calendar.MONTH);
                int year=calendar.get(Calendar.YEAR);

                datePicker=new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        text_date_end.setText(dayOfMonth+"/"+(month+1)+"/"+year);
                    }
                },year,month,day);
                datePicker.show();
            }
        });


        RadioGroup radioGroup=reportsFragmentView.findViewById(R.id.radio_group);
        final RadioButton expenses=reportsFragmentView.findViewById(R.id.radio_expenses);
        final RadioButton incomes=reportsFragmentView.findViewById(R.id.radio_incomes);
        final RecyclerView recyclerView = reportsFragmentView.findViewById(R.id.recycler_view_reports);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        final ProgressBar progressBar = reportsFragmentView.findViewById(R.id.progress_bar_reports);
        final TextView emptyView = reportsFragmentView.findViewById(R.id.empty_view_reports);
        generateReport.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 progressBar.setVisibility(View.VISIBLE);

                 if (budgetsUploaded && categoriesUploaded) {
                     if (firestoreRecyclerAdapter != null) {
                         firestoreRecyclerAdapter.stopListening();
                     }

                     String date_start_string = text_date_start.getText().toString();
                     String date_stop_string = text_date_end.getText().toString();
                     SimpleDateFormat df=new SimpleDateFormat("dd/MM/yyyy");
                     try {
                         date_start = df.parse(date_start_string);
                         date_stop = df.parse(date_stop_string);


                     } catch (ParseException e) {
                         e.printStackTrace();
                     }
                     String budgetName = spinnerBudget.getSelectedItem().toString();
                     String budgetId=spinnerMap.get(budgetName);
                     String categoryName = spinnerCategories.getSelectedItem().toString();
                     budgetTransactions = rootRef.collection("transactions").document(budgetId).collection("budget_transactions");

                     if (categoryName.equals("All")) {
                         query = budgetTransactions.whereGreaterThanOrEqualTo("date", date_start).whereLessThanOrEqualTo("date", date_stop);

                     } else {
                         query = budgetTransactions.whereGreaterThanOrEqualTo("date", date_start).whereLessThanOrEqualTo("date",date_stop);
                         query = query.whereEqualTo("category.categoryName", categoryName).orderBy("date", Query.Direction.ASCENDING);
                     }


                     if (expenses.isChecked()) {
                         query = query.whereEqualTo("isIncome", false);
                     }
                     if (incomes.isChecked()) {
                         query = query.whereEqualTo("isIncome", true);
                     }

                     attachRecyclerAdapter(query,budgetId,progressBar,emptyView,recyclerView);
                     firestoreRecyclerAdapter.startListening();
                     query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                         @Override
                         public void onComplete(@NonNull Task<QuerySnapshot> task) {
                             if (task.isSuccessful()){
                                 float sum=0;
                                 for (QueryDocumentSnapshot document : task.getResult()) {
                                     String amount = document.getData().get("amount").toString();
                                     float trans_amount=Float.valueOf(amount);
                                     sum=sum+trans_amount;
                                 }
                                 TextView text=reportsFragmentView.findViewById(R.id.text_sum);
                                 NumberFormat formatter = NumberFormat.getNumberInstance();
                                 formatter.setMinimumFractionDigits(2);
                                 formatter.setMaximumFractionDigits(2);
                                 text.setText(formatter.format(sum));
                             }

                         }
                     });
                     isGeneratedOnce = true;

                 }
             }
        });

        spinnerCategories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(isGeneratedOnce) {
                 firestoreRecyclerAdapter.stopListening();
                 String budgetName = spinnerBudget.getSelectedItem().toString();
                 String budgetId=spinnerMap.get(budgetName);
                 String categoryName = spinnerCategories.getSelectedItem().toString();
                 String date_start_string = text_date_start.getText().toString();
                 String date_stop_string = text_date_end.getText().toString();
                 SimpleDateFormat df=new SimpleDateFormat("dd/MM/yyyy");
                    budgetTransactions = rootRef.collection("transactions").document(budgetId).collection("budget_transactions");
                    try {
                     date_start = df.parse(date_start_string);
                     date_stop = df.parse(date_stop_string);

                    } catch (ParseException e) {
                     e.printStackTrace();
                    }
                 if (categoryName.equals("All")) {

                     query = budgetTransactions.whereGreaterThanOrEqualTo("date", date_start).whereLessThanOrEqualTo("date", date_stop);

                 } else {
                     query = budgetTransactions.whereGreaterThanOrEqualTo("date", date_start).whereLessThanOrEqualTo("date",date_stop);
                     query = query.whereEqualTo("category.categoryName", categoryName).orderBy("date", Query.Direction.ASCENDING);
                 }

                 if (expenses.isChecked()) {
                     query = query.whereEqualTo("isIncome", false);
                 }
                 if (incomes.isChecked()) {
                     query = query.whereEqualTo("isIncome", true);
                 }

                 attachRecyclerAdapter(query,budgetId,progressBar,emptyView,recyclerView);
                 firestoreRecyclerAdapter.startListening();

                    query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()){
                                float sum=0;
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String amount = document.getData().get("amount").toString();
                                    float trans_amount=Float.valueOf(amount);
                                    sum=sum+trans_amount;
                                }
                                TextView text=reportsFragmentView.findViewById(R.id.text_sum);
                                NumberFormat formatter = NumberFormat.getNumberInstance();
                                formatter.setMinimumFractionDigits(2);
                                formatter.setMaximumFractionDigits(2);
                                text.setText(formatter.format(sum));
                            }

                        }
                    });

             }

         }

         @Override
         public void onNothingSelected(AdapterView<?> parent) {

         }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
         @Override
         public void onCheckedChanged(RadioGroup group, int checkedId) {
             if(isGeneratedOnce) {
                 firestoreRecyclerAdapter.stopListening();
                 String budgetName = spinnerBudget.getSelectedItem().toString();
                 String budgetId=spinnerMap.get(budgetName);
                 String categoryName = spinnerCategories.getSelectedItem().toString();
                 String date_start_string = text_date_start.getText().toString();
                 String date_stop_string = text_date_end.getText().toString();
                 SimpleDateFormat df=new SimpleDateFormat("dd/MM/yyyy");
                 budgetTransactions = rootRef.collection("transactions").document(budgetId).collection("budget_transactions");
                 try {
                     date_start = df.parse(date_start_string);
                     date_stop = df.parse(date_stop_string);


                 } catch (ParseException e) {
                     e.printStackTrace();
                 }
                 if (categoryName.equals("All")) {

                     query = budgetTransactions.whereGreaterThanOrEqualTo("date", date_start).whereLessThanOrEqualTo("date", date_stop);

                 } else {
                     query = budgetTransactions.whereGreaterThanOrEqualTo("date", date_start).whereLessThanOrEqualTo("date",date_stop);
                     query = query.whereEqualTo("category.categoryName", categoryName).orderBy("date", Query.Direction.ASCENDING);
                 }

                 switch (checkedId) {
                     case R.id.radio_expenses:
                         query_new = query.whereEqualTo("isIncome", false);
                         break;

                     case R.id.radio_incomes:
                         query_new = query.whereEqualTo("isIncome", true);
                         break;

                     case R.id.radio_all:
                         query_new=query;
                         break;

                 }

                 attachRecyclerAdapter(query_new,budgetId,progressBar,emptyView,recyclerView);
                 firestoreRecyclerAdapter.startListening();

                 query_new.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                     @Override
                     public void onComplete(@NonNull Task<QuerySnapshot> task) {
                         if (task.isSuccessful()){
                             float sum=0;
                             for (QueryDocumentSnapshot document : task.getResult()) {
                                 String amount = document.getData().get("amount").toString();
                                 float trans_amount=Float.valueOf(amount);
                                 sum=sum+trans_amount;
                             }
                             TextView text=reportsFragmentView.findViewById(R.id.text_sum);
                             NumberFormat formatter = NumberFormat.getNumberInstance();
                             formatter.setMinimumFractionDigits(2);
                             formatter.setMaximumFractionDigits(2);
                             text.setText(formatter.format(sum));
                         }

                     }
                 });

             }
         }
     });

        return reportsFragmentView;

    }

    private void setDate(Calendar c, EditText text_date_end) {
        SimpleDateFormat df=new SimpleDateFormat("dd/MM/yyyy");
        String date = df.format(c.getTime());
        text_date_end.setText(date);
    }


    private void attachRecyclerAdapter(Query query, final String budgetId,final ProgressBar progressBar,final TextView emptyView,final RecyclerView recyclerView) {
        firestoreRecyclerOptions = new FirestoreRecyclerOptions.Builder<Transaction>()
                .setQuery(query, Transaction.class)
                .build();
        firestoreRecyclerAdapter = new FirestoreRecyclerAdapter<Transaction, TransactionViewHolder>(firestoreRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull TransactionViewHolder holder, int position, @NonNull Transaction model) {
                holder.setTransaction(model,userEmail,budgetId);
            }
            @NonNull
            @Override
            public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
                return new TransactionViewHolder(view);
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


}


