package com.example.companyloginapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.*;

public class ProductAdapter extends BaseAdapter {

    private final Context context;
    private final List<JSONObject> productList;
    private final ProductSellCallback callback;
    private static final int PICK_IMAGE_REQUEST = 102;

    public interface ProductSellCallback {
        void onSellClicked(int productId);
        void onDeleteClicked(int productId);
    }

    public ProductAdapter(Context context, List<JSONObject> productList, ProductSellCallback callback) {
        this.context = context;
        this.productList = productList;
        this.callback = callback;
    }

    @Override
    public int getCount() {
        return productList.size();
    }

    @Override
    public Object getItem(int position) {
        return productList.get(position);
    }

    @Override
    public long getItemId(int position) {
        try {
            return productList.get(position).getInt("id");
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        JSONObject product = productList.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_product, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.product_image);
        TextView nameView = convertView.findViewById(R.id.item_name);
        TextView infoView = convertView.findViewById(R.id.item_price_qty);
        Button sellButton = convertView.findViewById(R.id.sell_button);

        try {
            int id = product.getInt("id");
            String name = product.getString("itemName");
            int quantity = product.getInt("quantity");
            double price = product.getDouble("price");

            nameView.setText(name);
            infoView.setText("Price: $" + price + " | Qty: " + quantity);

            String imageUrl = "http://coms-3090-024.class.las.iastate.edu:8080/inventory/image/" + id;
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_placeholder_image)
                    .into(imageView);


            if (quantity <= 0) {
                sellButton.setText("Delete");
                sellButton.setBackgroundTintList(context.getResources().getColorStateList(android.R.color.holo_red_light));
                sellButton.setOnClickListener(v -> callback.onDeleteClicked(id));
            } else {
                sellButton.setText("Sell");
                sellButton.setBackgroundTintList(context.getResources().getColorStateList(android.R.color.holo_green_light));
                sellButton.setOnClickListener(v -> callback.onSellClicked(id));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }

    private void pickImageForProduct(int itemId) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        ((Activity) context).startActivityForResult(intent, itemId);
    }

    public void uploadImageForItem(int itemId, Uri imageUri) {
        try {
            File imageFile = new File(getRealPathFromURI(imageUri));
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", imageFile.getName(),
                            RequestBody.create(imageFile, MediaType.parse("image/jpeg")))
                    .build();

            Request request = new Request.Builder()
                    .url("http://coms-3090-024.class.las.iastate.edu:8080/inventory/upload-image/" + itemId)
                    .post(requestBody)
                    .build();

            OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    // Optionally show a toast or refresh
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getRealPathFromURI(Uri uri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        return null;
    }
}
