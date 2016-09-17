package com.instacart.library.truetime;

public class InvalidNtpServerResponseException
      extends Exception {
    InvalidNtpServerResponseException(String detailMessage) {
        super(detailMessage);
    }
}
