package com.instacart.library.truetime;

public class InvalidNtpServerResponse extends Exception {
    InvalidNtpServerResponse(String detailMessage) {
        super(detailMessage);
    }
}
