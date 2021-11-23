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
import au.com.dmg.fusion.data.ReversalReason;
import au.com.dmg.fusion.data.UnitOfMeasure;
import au.com.dmg.fusion.request.SaleToPOIRequest;
import au.com.dmg.fusion.request.cardacquisitionrequest.CardAcquisitionRequest;
import au.com.dmg.fusion.request.paymentrequest.AmountsReq;
import au.com.dmg.fusion.request.paymentrequest.OriginalPOITransaction;
import au.com.dmg.fusion.request.paymentrequest.POITransactionID;
import au.com.dmg.fusion.request.paymentrequest.PaymentData;
import au.com.dmg.fusion.request.paymentrequest.PaymentRequest;
import au.com.dmg.fusion.request.paymentrequest.PaymentTransaction;
import au.com.dmg.fusion.request.paymentrequest.SaleData;
import au.com.dmg.fusion.request.paymentrequest.SaleItem;
import au.com.dmg.fusion.request.paymentrequest.SaleTransactionID;
import au.com.dmg.fusion.request.reversalrequest.ReversalRequest;
import au.com.dmg.fusion.request.transactionstatusrequest.MessageReference;
import au.com.dmg.fusion.request.transactionstatusrequest.TransactionStatusRequest;
import au.com.dmg.fusion.response.SaleToPOIResponse;

public class MainActivity extends AppCompatActivity {

    SaleToPOIResponse response = null;

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent data) {
        super.onActivityResult(requestCode, responseCode, data);
        if (data != null) {
            this.onNewIntent(data);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonRefund = findViewById(R.id.button_refund);
        Button buttonCashout = findViewById(R.id.button_cashout);
        Button buttonPayment = findViewById(R.id.button_payment);
        Button buttonTranactionStatus = findViewById(R.id.button_transaction_status);
        Button buttonPreAuth = findViewById(R.id.button_pre_auth);
        Button buttonCompletion = findViewById(R.id.button_completion);
        Button buttonCardAcquisition = findViewById(R.id.button_card_acquisition);
        Button buttonReversal = findViewById(R.id.button_reversal);

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
        buttonCompletion.setOnClickListener(v -> {
            sendCompletion();
        });
        buttonCashout.setOnClickListener(v -> {
            sendCashOut();
        });
        buttonCardAcquisition.setOnClickListener(v -> {
            sendCardAcquisitionRequest();
        });
        buttonReversal.setOnClickListener(v -> {
            sendReversal();
        });
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
                                        .transactionID(generateTransactionId())
                                        .build())
                                .build())
                        .paymentTransaction(
                                new PaymentTransaction.Builder()
                                        .amountsReq(new AmountsReq.Builder()
                                                .currency("AUD")
                                                .requestedAmount(new BigDecimal(3000.0))
                                                .build())
                                        .addSaleItem(new SaleItem.Builder()
                                                .itemID(1)
                                                .productCode("5")
                                                .unitOfMeasure(UnitOfMeasure.Litre)
                                                .itemAmount(new BigDecimal(1.0))
                                                .unitPrice(new BigDecimal(1.0))
                                                .quantity(new BigDecimal(1.0))
                                                .productLabel("Unleaded")
                                                .build())
                                        .build()
                        )
                        .paymentData(new PaymentData.Builder()
                                .paymentType(PaymentType.Normal)
                                .build())
                        .build()
                )
                .build();

        sendRequest(request);
    }


    private String generateRandomServiceID() {
        StringBuilder serviceId = new StringBuilder();

        Random rand = new Random();
        for (int i = 0; i < 10; ++i) {
            serviceId.append(rand.nextInt(10));
        }
        return serviceId.toString();
    }

    private void sendRequest(SaleToPOIRequest request) {
        Intent intent = new Intent(Message.INTENT_ACTION_SALETOPOI_REQUEST);

        // wrapper of request.
        Message message = new Message(request);
        Log.d("Request", message.toJson());

        intent.putExtra(Message.INTENT_EXTRA_MESSAGE, message.toJson());
        // name of this app, that get's treated as the POS label by the terminal.
        intent.putExtra(Message.INTENT_EXTRA_APPLICATION_NAME, "DemoPOS");
        intent.putExtra(Message.INTENT_EXTRA_APPLICATION_VERSION, "1.0.0");

        startActivityForResult(intent, 100);
    }

    private void sendCardAcquisitionRequest() {
        SaleToPOIRequest request = new SaleToPOIRequest.Builder()
                .messageHeader(new MessageHeader.Builder()
                        .serviceID(generateRandomServiceID())
                        .messageClass(MessageClass.Service)
                        .messageCategory(MessageCategory.CardAcquisition)
                        .messageType(MessageType.Request)
                        .build()
                )
                .request(new CardAcquisitionRequest.Builder()
                        .saleData(new SaleData.Builder()
                                .saleTransactionID(new SaleTransactionID.Builder()
                                        .transactionID(generateTransactionId())
                                        .timestamp(Instant.now())
                                        .build())
                                .tokenRequestedType("Customer")
                                .build())
                        .build()
                )
                .build();

        sendRequest(request);
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
                                                                .transactionID(generateTransactionId())
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
                                                .build()
                                )
                                .paymentData(
                                        new PaymentData.Builder()
                                                .paymentType(PaymentType.Refund)
                                                .build()
                                )
                                .build()
                )
                .build();

        sendRequest(request);
    }

    String lastTxid = null;
    private void sendReversal(){
        if(lastTxid == null){
            Toast.makeText(this, "Send a transaction first.", Toast.LENGTH_SHORT).show();
            return;
        }
        SaleToPOIRequest request = new SaleToPOIRequest.Builder()
                .messageHeader(
                        new MessageHeader.Builder()
                                .messageClass(MessageClass.Service)
                                .messageCategory(MessageCategory.Reversal)
                                .messageType(MessageType.Request)
                                .serviceID(generateRandomServiceID())
                                .build()
                )
                .request(new ReversalRequest.Builder()
                        .reversalReason(ReversalReason.SignatureDeclined)
                        .originalPOITransaction(new OriginalPOITransaction.Builder()
                                .POIID("x")
                                .saleID("z")
                                .POITransactionID(new POITransactionID(lastTxid, Instant.ofEpochMilli(System.currentTimeMillis())))
                                .build())
                        .build())
                .build();

        sendRequest(request);
    }

    private String lastServiceID = null;

    private void sendTransactionStatusRequest() {
        if (lastServiceID == null) {
            Toast.makeText(this, "Perform a transaction first.", Toast.LENGTH_SHORT).show();
            return;
        }

        SaleToPOIRequest request = new SaleToPOIRequest.Builder()
                .messageHeader(
                        new MessageHeader.Builder()
                                .messageClass(MessageClass.Service)
                                .messageCategory(MessageCategory.TransactionStatus)
                                .messageType(MessageType.Request)
                                .serviceID(lastServiceID)
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
                                                                .transactionID(generateTransactionId())
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
                                                .build()
                                )
                                .paymentData(
                                        new PaymentData.Builder()
                                                .paymentType(PaymentType.FirstReservation)
                                                .build()
                                )
                                .build()
                )
                .build();

        sendRequest(request);
    }

    private void sendCompletion() {
        if (response == null) {
            Toast.makeText(this, "No prior transaction to perform completion", Toast.LENGTH_SHORT).show();
            return;
        }

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
                                                                .transactionID(generateTransactionId())
                                                                .timestamp(Instant.ofEpochMilli(System.currentTimeMillis()))
                                                                .build()
                                                ).build()
                                )
                                .paymentTransaction(
                                        new PaymentTransaction.Builder()
                                                .amountsReq(
                                                        new AmountsReq.Builder()
                                                                .currency("AUD")
                                                                .requestedAmount(response.getPaymentResponse().getPaymentResult().getAmountsResp().getAuthorizedAmount())
                                                                .build()
                                                )
                                                .originalPOITransaction(
                                                        new OriginalPOITransaction.Builder()
                                                                .POIID("")
                                                                .POITransactionID(response.getPaymentResponse().getPoiData().getPOITransactionID())
                                                                .saleID(response.getPaymentResponse().getSaleData().getSaleTransactionID().getTransactionID())
                                                                .reuseCardDataFlag(true)
                                                                .build()
                                                )
                                                .build()
                                )
                                .paymentData(
                                        new PaymentData.Builder()
                                                .paymentType(PaymentType.Completion)
                                                .build()
                                )
                                .build()
                )
                .build();

        sendRequest(request);
    }

    private void sendCashOut() {
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
                                                                .transactionID(generateTransactionId())
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
                                                .build()
                                )
                                .paymentData(
                                        new PaymentData.Builder()
                                                .paymentType(PaymentType.CashAdvance)
                                                .build()
                                )
                                .build()
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
        Log.d("Response", intent.getStringExtra(Message.INTENT_EXTRA_MESSAGE));
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

    /*
     * Required if your activity is a SingleTop
     * */
    @Override
    protected void onNewIntent(Intent intent) {
        Log.d("Demo", "OnNewIntent(" + intent);
        super.onNewIntent(intent);
        if (isResponseIntent(intent)) {
            handleResponseIntent(intent);
        }
    }

    private void handleResponse(SaleToPOIResponse response) {
        this.response = response;
        TextView textViewJson = findViewById(R.id.textView_response_json);
        Log.d("Response", response.toJson());
        textViewJson.setText(response.toJson());

        this.lastServiceID = response.getMessageHeader().getServiceID();
        if(response.getPaymentResponse() != null) {
            this.lastTxid = response.getPaymentResponse().getPoiData().getPOITransactionID().getTransactionID();
        }
    }

    /*
     * For this test app, we will just use random numbers.
     * */
    private String generateTransactionId() {
        String s = "";
        Random r = new Random();
        for (int i = 0; i < 10; ++i) {
            int x = r.nextInt(10);
            s += x;
        }
        return s;
    }
}