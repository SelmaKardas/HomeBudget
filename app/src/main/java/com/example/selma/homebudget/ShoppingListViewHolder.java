package com.example.selma.homebudget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ShoppingListViewHolder extends RecyclerView.ViewHolder {
    private TextView shoppingListNameTextView, createdByTextView, dateTextView;
    public ShoppingListViewHolder(@NonNull View itemView) {
        super(itemView);
        shoppingListNameTextView = itemView.findViewById(R.id.shopping_list_name_text_view);
        createdByTextView = itemView.findViewById(R.id.created_by_text_view);
        dateTextView = itemView.findViewById(R.id.date_text_view);
    }

    public void setShoppingList(final Context context, final ShoppingList shoppingListModel) {
        final String shoppingListId=shoppingListModel.getShoppingListId();
        final String shoppingListName = shoppingListModel.getShoppingListName();
        shoppingListNameTextView.setText(shoppingListName);
        final String userEmail=shoppingListModel.getCreatedBy();
        String createdBy = "Created by: " + shoppingListModel.getCreatedBy();
        createdByTextView.setText(createdBy);
        Date date = shoppingListModel.getDate();

        if (date != null) {
            DateFormat dateFormat = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM, Locale.ENGLISH);
            String shoppingListCreationDate = dateFormat.format(date);
            dateTextView.setText(shoppingListCreationDate);
        }

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ShoppingListActivity.class);
                intent.putExtra("shoppingListModel", shoppingListModel);
                v.getContext().startActivity(intent);
            }
        });

        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {

                PopupMenu popup = new PopupMenu(context, v);
                popup.inflate(R.menu.popup_menu);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()) {
                            case R.id.delete_item:
                                deleteShoppingList();
                                return true;
                            case R.id.edit_item:
                                editShoppingList();
                                return true;

                            case R.id.share_item:
                                shareShoppingList();
                                return true;
                        }

                        return false;
                    }

                    private void shareShoppingList() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                        builder.setTitle("Share Shopping List");
                        builder.setMessage("Please insert your friend's email");
                        final EditText editText = new EditText(v.getContext());
                        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                        editText.setHint("Type an email address");
                        editText.setHintTextColor(Color.GRAY);
                        builder.setView(editText);
                        builder.setPositiveButton("Share", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final String friendsEmail = editText.getText().toString().trim();
                                final FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
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

                    private void editShoppingList() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                        builder.setTitle("Edit shopping list name");
                        final EditText editText = new EditText(v.getContext());
                        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                        editText.setText(shoppingListName);
                        editText.setSelection(editText.getText().length());
                        editText.setHint("Type a name");
                        editText.setHintTextColor(Color.GRAY);
                        builder.setView(editText);
                        final FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
                        final Map<String, Object> map = new HashMap<>();
                        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String newShoppingListName = editText.getText().toString().trim();
                                map.put("shoppingListName", newShoppingListName);
                                rootRef.collection("shoppingLists").document(userEmail).collection("userShoppingLists").document(shoppingListId).update(map);

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

                    private void deleteShoppingList() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                        builder.setTitle("Delete shopping list");
                        final FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
                        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                rootRef.collection("shoppingLists").document(userEmail).collection("userShoppingLists").document(shoppingListId).delete();

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
                });

                popup.show();
                return true;
            }
        });


    }

}
