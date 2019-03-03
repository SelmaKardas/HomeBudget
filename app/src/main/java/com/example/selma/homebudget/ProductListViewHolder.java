package com.example.selma.homebudget;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProductListViewHolder extends RecyclerView.ViewHolder {
    private TextView productNameTextView;
    private TextView productCategoryTextView;
    private boolean izActiveProduct;
    public ProductListViewHolder(@NonNull View itemView) {
        super(itemView);
        productNameTextView = itemView.findViewById(R.id.product_name_text_view);
        productCategoryTextView=itemView.findViewById(R.id.product_category_text_view);

    }

    public void setProduct(Product product,final String userEmail, final String userName, ShoppingList shoppingList) {
       final String productName=product.getProductName();
       final String shoppingListName=shoppingList.getShoppingListName();
       final String shoppingListId=shoppingList.getShoppingListId();
        String productCategory=product.getCategory().getCategoryName();
        izActiveProduct=product.getizActiveProduct();
        productCategoryTextView.setText(productCategory);
        productNameTextView.setText(productName);

        if (!izActiveProduct){
            productNameTextView.setPaintFlags(productNameTextView.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
            productNameTextView.setTextColor(Color.GRAY);
            productCategoryTextView.setPaintFlags(productNameTextView.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
            productCategoryTextView.setTextColor(Color.GRAY);}
        else{
            productNameTextView.setPaintFlags( productNameTextView.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
            productNameTextView.setTextColor(0xFF008577);
            productCategoryTextView.setPaintFlags( productCategoryTextView.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
            productCategoryTextView.setTextColor(0xFF7AC6BE);


        }

        final String productId=product.getProductId();
        itemView.setOnClickListener(new View.OnClickListener() {
            final FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
            final Map<String, Object> map = new HashMap<>();
            @Override
            public void onClick(View v) {

                if (izActiveProduct) {
                    map.put("izActiveProduct", false);

                } else {
                    map.put("izActiveProduct", true);

                }

                rootRef.collection("products").document(shoppingListId).collection("shoppingListProducts").document(productId).update(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (izActiveProduct) {
                            rootRef.collection("shoppingLists").document(userEmail).collection("userShoppingLists").document(shoppingListId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if(documentSnapshot.contains("users")) {
                                        Map<String, Object> map1 = (Map<String, Object>) documentSnapshot.get("users");
                                        String notificationMessage = userName + " just bought " + productName + " from " + shoppingListName + "'s list!";

                                        Notification notification = new Notification(notificationMessage, userEmail);
                                        for (Map.Entry<String, Object> entry : map1.entrySet()) {
                                            String sharedUserEmail = entry.getKey();
                                            if (!sharedUserEmail.equals(userEmail)) {
                                                rootRef.collection("notifications").document(sharedUserEmail)
                                                        .collection("userNotifications").document()
                                                        .set(notification);

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


        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Delete product");
                final FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        rootRef.collection("products").document(shoppingListId).collection("shoppingListProducts").document(productId).delete();

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
