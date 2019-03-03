package com.example.selma.homebudget;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;

public class TransactionViewHolder extends RecyclerView.ViewHolder {
    private TextView transactionNameTextView;
    private TextView amountTextView;
    private TextView dateTextView;
    private TextView userEmailTextView;
    public TransactionViewHolder(@NonNull View itemView) {
        super(itemView);
        transactionNameTextView = itemView.findViewById(R.id.transaction_name);
        amountTextView=itemView.findViewById(R.id.amount);
        dateTextView=itemView.findViewById(R.id.date);
        userEmailTextView=itemView.findViewById(R.id.userEmail);

    }

    public void setTransaction(Transaction transaction, final String userEmail, final String budgetId) {
        String transactionName=transaction.getTransactionName();
        String transactionCategory=transaction.getCategory().getCategoryName();
        String money= String.valueOf(transaction.getAmount());
        SimpleDateFormat df=new SimpleDateFormat("dd/MM/yyyy");
        String date= df.format(transaction.getDate());
        String userName=transaction.getUserName();
        final String transactionId=transaction.getTransactionId();

        transactionNameTextView.setText(transactionName+", Category: "+transactionCategory);
        amountTextView.setText(money);
        dateTextView.setText(date);
        userEmailTextView.setText(userName);


        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Delete transaction");
                final FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        rootRef.collection("transactions").document(budgetId).collection("budget_transactions").document(transactionId).delete();

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
                return true;
            }
        });



    }

}
