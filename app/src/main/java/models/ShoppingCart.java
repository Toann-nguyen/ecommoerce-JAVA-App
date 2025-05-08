package models;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ShoppingCart {
    private static final String TAG = "ShoppingCart";
    private static final String PREF_NAME = "ShoppingCartPrefs";
    private static final String CART_ITEMS_KEY = "cart_items";

    private static ShoppingCart instance;
    private List<CartItem> cartItems;
    private final Context context;

    private ShoppingCart(Context context) {
        this.context = context.getApplicationContext();
        this.cartItems = loadCartItems();
    }

    public static ShoppingCart getInstance(Context context) {
        if (instance == null) {
            instance = new ShoppingCart(context);
        }
        return instance;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void addItem(Product product, int quantity) {
        boolean itemExists = false;
        for (CartItem item : cartItems) {
            if (item.getProduct().getId().equals(product.getId())) {
                item.setQuantity(item.getQuantity() + quantity);
                itemExists = true;
                break;
            }
        }

        if (!itemExists) {
            cartItems.add(new CartItem(product, quantity));
        }

        saveCartItems();
    }

    public void updateItemQuantity(String productId, int quantity) {
        for (CartItem item : cartItems) {
            if (item.getProduct().getId().equals(productId)) {
                item.setQuantity(quantity);
                break;
            }
        }
        saveCartItems();
    }

    public void removeItem(String productId) {
        cartItems.removeIf(item -> item.getProduct().getId().equals(productId));
        saveCartItems();
    }

    public void clearCart() {
        cartItems.clear();
        saveCartItems();
    }

    public int getItemCount() {
        int count = 0;
        for (CartItem item : cartItems) {
            count += item.getQuantity();
        }
        return count;
    }

    public double getCartTotal() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getTotalPrice();
        }
        return total;
    }

    private void saveCartItems() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(cartItems);
        editor.putString(CART_ITEMS_KEY, json);
        editor.apply();
        Log.d(TAG, "Giỏ hàng đã được lưu: " + json);
    }

    private List<CartItem> loadCartItems() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(CART_ITEMS_KEY, null);
        Type type = new TypeToken<ArrayList<CartItem>>() {
        }.getType();

        if (json == null) {
            return new ArrayList<>();
        } else {
            try {
                return gson.fromJson(json, type);
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi đọc giỏ hàng: " + e.getMessage());
                return new ArrayList<>();
            }
        }
    }
}