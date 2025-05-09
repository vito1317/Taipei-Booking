package com.taipeibooking.dto;


public class PaymentRequestDTO {

    private String paymentNonce;

    public PaymentRequestDTO() {
    }

    public PaymentRequestDTO(String paymentNonce) {
        this.paymentNonce = paymentNonce;
    }

    public String getPaymentNonce() {
        return paymentNonce;
    }

    public void setPaymentNonce(String paymentNonce) {
        this.paymentNonce = paymentNonce;
    }

}
