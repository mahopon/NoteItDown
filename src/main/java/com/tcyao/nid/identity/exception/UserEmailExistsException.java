package com.tcyao.nid.identity.exception;

public class UserEmailExistsException extends RuntimeException {
    public UserEmailExistsException(String msg) {
        super("Registration with existing email: " + msg);
    }
}
