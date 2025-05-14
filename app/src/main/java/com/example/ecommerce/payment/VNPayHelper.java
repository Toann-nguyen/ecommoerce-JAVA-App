package com.example.ecommerce.payment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class VNPayHelper {
    // VNPAY Config
    public static final String VNP_PAY_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    public static final String VNP_RETURN_URL = "https://example.com/vnpay_return"; // Replace with your return URL
    private static final String VNP_TMN_CODE = "27SZLBXF"; // Your Terminal ID
    private static final String VNP_HASH_SECRET = "8VOS32E0QNFSLDWO4EN5Z5F7WVN1OF5A"; // Your Secret Key
    private static final String VNP_VERSION = "2.1.0";
    private static final String VNP_COMMAND = "pay";

    // Generate payment URL
    public static String generatePaymentUrl(Context context, String orderId, double amount, String orderInfo) {
        Map<String, String> vnpParams = new HashMap<>();

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
        String createDate = formatter.format(calendar.getTime());

        // Calculate amount in VND (VNPAY requires amount in VND x 100)
        long amountInVND = Math.round(amount * 100);

        // Add required parameters
        vnpParams.put("vnp_Version", VNP_VERSION);
        vnpParams.put("vnp_Command", VNP_COMMAND);
        vnpParams.put("vnp_TmnCode", VNP_TMN_CODE);
        vnpParams.put("vnp_Amount", String.valueOf(amountInVND));
        vnpParams.put("vnp_CreateDate", createDate);
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_IpAddr", "127.0.0.1"); // You can get real IP if needed
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_OrderInfo", orderInfo);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_ReturnUrl", VNP_RETURN_URL);
        vnpParams.put("vnp_TxnRef", orderId);

        // Sort parameters before hashing
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);

        // Build hash data and query
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnpParams.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                try {
                    hashData.append(URLEncoder.encode(fieldValue, "UTF-8"));

                    // Build query
                    query.append(URLEncoder.encode(fieldName, "UTF-8"));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        // Create secure hash
        String secureHash = hmacSHA512(VNP_HASH_SECRET, hashData.toString());

        // Append secure hash to the query
        query.append("&vnp_SecureHash=").append(secureHash);

        // Return the full payment URL
        return VNP_PAY_URL + "?" + query;
    }

    // Hmac SHA512 algorithm to create secure hash
    private static String hmacSHA512(String key, String data) {
        try {
            Mac sha512Hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA512");
            sha512Hmac.init(secretKey);
            byte[] hmacData = sha512Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hmacData);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    // Helper method to convert bytes to hex
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // Open payment URL in browser
    public static void openPaymentUrl(Context context, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(intent);
    }
}