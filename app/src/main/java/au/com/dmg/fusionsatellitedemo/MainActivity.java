package au.com.dmg.fusionsatellitedemo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Random;

import au.com.dmg.fusion.Message;
import au.com.dmg.fusion.MessageHeader;
import au.com.dmg.fusion.data.MessageCategory;
import au.com.dmg.fusion.data.MessageClass;
import au.com.dmg.fusion.data.MessageType;
import au.com.dmg.fusion.data.PaymentType;
import au.com.dmg.fusion.data.UnitOfMeasure;
import au.com.dmg.fusion.request.SaleToPOIRequest;
import au.com.dmg.fusion.request.paymentrequest.AmountsReq;
import au.com.dmg.fusion.request.paymentrequest.PaymentData;
import au.com.dmg.fusion.request.paymentrequest.PaymentRequest;
import au.com.dmg.fusion.request.paymentrequest.PaymentTransaction;
import au.com.dmg.fusion.request.paymentrequest.SaleData;
import au.com.dmg.fusion.request.paymentrequest.SaleItem;
import au.com.dmg.fusion.request.paymentrequest.SaleTransactionID;
import au.com.dmg.fusion.request.transactionstatusrequest.TransactionStatusRequest;
import au.com.dmg.fusion.response.SaleToPOIResponse;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonRefund = findViewById(R.id.button_refund);
        Button buttonPayment = findViewById(R.id.button_payment);
        Button buttonTranactionStatus = findViewById(R.id.button_transaction_status);
        Button buttonPreAuth = findViewById(R.id.button_pre_auth);

        buttonRefund.setOnClickListener(v -> {
            sendRefundRequest();
        });
        buttonPayment.setOnClickListener(v -> {
            sendPaymentRequest();
        });
        buttonTranactionStatus.setOnClickListener(v -> {
            sendTransactionStatusRequest();
        });
        buttonPreAuth.setOnClickListener(v -> {
            sendPreAuth();
        });

        Intent intent = getIntent();
        if (isResponseIntent(intent)) {
            handleResponseIntent(intent);
        }
    }

    private void sendPaymentRequest() {
        SaleToPOIRequest request = new SaleToPOIRequest.Builder()
                .messageHeader(new MessageHeader.Builder()
                        .messageClass(MessageClass.Service)
                        .messageCategory(MessageCategory.Payment)
                        .messageType(MessageType.Request)
                        .serviceID(generateRandomServiceID())
                        .build())
                .request(new PaymentRequest.Builder()
                        .saleData(new SaleData.Builder()
                                .operatorLanguage("en")
                                .saleTransactionID(new SaleTransactionID.Builder()
                                        .timestamp(Instant.ofEpochMilli(System.currentTimeMillis()))
                                        .transactionID("2371289312323")
                                        .build())
                                .build())
                        .paymentTransaction(
                                new PaymentTransaction.Builder()
                                        .amountsReq(new AmountsReq.Builder()
                                                .currency("AUD")
                                                .requestedAmount(new BigDecimal(1.0))
                                                .build())
                                        .addSaleItem(new SaleItem.Builder()
                                                .itemID(1)
                                                .productCode("X")
                                                .unitOfMeasure(UnitOfMeasure.Other)
                                                .itemAmount(new BigDecimal(1.0))
                                                .unitPrice(new BigDecimal(1.0))
                                                .quantity(new BigDecimal(1.0))
                                                .productLabel("Stuff")
                                                .build())
                                        .paymentData(new PaymentData.Builder()
                                                .paymentType(PaymentType.Normal)
                                                .build())
                                        .build()
                        )
                        .build()
                )
                .build();

        sendRequest(request);
    }

    private String generateRandomServiceID(){
        StringBuilder serviceId = new StringBuilder();

        Random rand = new Random();
        for (int i=0; i<10; ++i) {
            serviceId.append(rand.nextInt(10));
        }
        return serviceId.toString();
    }

    private void sendRequest(SaleToPOIRequest request) {
        Intent intent = getPackageManager().getLaunchIntentForPackage("au.com.dmg.axispay");
        if (intent == null) {
            Toast.makeText(this, "AxisPay not Available.", Toast.LENGTH_SHORT).show();
            return;
        }
        // wrapper of request.
        Message message = new Message(request);

        intent.setAction("au.com.axispay.action.SaleToPOIRequest");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra(Message.INTENT_EXTRA_MESSAGE, message.toJson());
        intent.putExtra(Message.INTENT_EXTRA_PARENT_ID, this.getPackageName());
        intent.putExtra(Message.INTENT_EXTRA_VERSION, Message.VERSION_UNDEFINED);

        startActivity(intent);
    }

    private void sendRefundRequest() {
        SaleToPOIRequest request = new SaleToPOIRequest.Builder()
                .messageHeader(
                        new MessageHeader.Builder()
                                .messageClass(MessageClass.Service)
                                .messageCategory(MessageCategory.Payment)
                                .messageType(MessageType.Request)
                                .serviceID(generateRandomServiceID())
                                .build()
                )
                .request(
                        new PaymentRequest.Builder()
                                .saleData(
                                        new SaleData.Builder()
                                                .operatorLanguage("en")
                                                .saleTransactionID(
                                                        new SaleTransactionID.Builder()
                                                                .transactionID("x")
                                                                .timestamp(Instant.ofEpochMilli(System.currentTimeMillis()))
                                                                .build()
                                                ).build()
                                )
                                .paymentTransaction(
                                        new PaymentTransaction.Builder()
                                                .amountsReq(
                                                        new AmountsReq.Builder()
                                                                .currency("AUD")
                                                                .requestedAmount(new BigDecimal(1.0))
                                                                .build()
                                                )
                                                .paymentData(
                                                        new PaymentData.Builder()
                                                                .paymentType(PaymentType.Refund)
                                                                .build()
                                                )
                                                .build()
                                ).build()
                )
                .build();

        sendRequest(request);
    }

    private void sendTransactionStatusRequest() {
        SaleToPOIRequest request = new SaleToPOIRequest.Builder()
                .messageHeader(
                        new MessageHeader.Builder()
                                .messageClass(MessageClass.Service)
                                .messageCategory(MessageCategory.TransactionStatus)
                                .messageType(MessageType.Request)
                                .serviceID(generateRandomServiceID())
                                .build()
                )
                .request(new TransactionStatusRequest())
                .build();

        sendRequest(request);
    }

    private void sendPreAuth() {
        SaleToPOIRequest request = new SaleToPOIRequest.Builder()
                .messageHeader(
                        new MessageHeader.Builder()
                                .messageClass(MessageClass.Service)
                                .messageCategory(MessageCategory.Payment)
                                .messageType(MessageType.Request)
                                .serviceID(generateRandomServiceID())
                                .build()
                )
                .request(
                        new PaymentRequest.Builder()
                                .saleData(
                                        new SaleData.Builder()
                                                .operatorLanguage("en")
                                                .saleTransactionID(
                                                        new SaleTransactionID.Builder()
                                                                .transactionID("x")
                                                                .timestamp(Instant.ofEpochMilli(System.currentTimeMillis()))
                                                                .build()
                                                ).build()
                                )
                                .paymentTransaction(
                                        new PaymentTransaction.Builder()
                                                .amountsReq(
                                                        new AmountsReq.Builder()
                                                                .currency("AUD")
                                                                .requestedAmount(new BigDecimal(1.0))
                                                                .build()
                                                )
                                                .paymentData(
                                                        new PaymentData.Builder()
                                                                .paymentType(PaymentType.PreAuthorization)
                                                                .build()
                                                )
                                                .build()
                                ).build()
                )
                .build();

        sendRequest(request);
    }


    private Boolean isResponseIntent(Intent intent) {
        if (intent == null) {
            return false;
        }
        return intent.hasExtra(Message.INTENT_EXTRA_MESSAGE);
    }

    private void handleResponseIntent(Intent intent) {
        Message message = null;
        try {
            message = Message.fromJson(intent.getStringExtra(Message.INTENT_EXTRA_MESSAGE));
        } catch (Exception e) {
            Toast.makeText(this, "Error reading intent.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return;
        }
        handleResponse(message.getResponse());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (isResponseIntent(intent)) {
            handleResponseIntent(intent);
        }
    }

    private void handleResponse(SaleToPOIResponse response) {
        TextView textViewJson = findViewById(R.id.textView_response_json);
        Log.d("Response", response.toJson());
        textViewJson.setText(response.toJson());
    }
}