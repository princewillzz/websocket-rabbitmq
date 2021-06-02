package com.untanglechat.chatapp.models;

public enum AuthenticationStatusEnum {

    UNAUTHENTICATED(Status.UNAUTHENTICATED), 
    UNAUTHORIZED(Status.UNAUTHORIZED),
    AUTHENTICATED(Status.AUTHENTICATED),
    AUTHORIZED(Status.AUTHORIZED);

    private final String status;

    AuthenticationStatusEnum(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return this.status.toString();
    }

    public class Status {
        public static final String UNAUTHENTICATED = "UNAUTHENTICATED";
        public static final String UNAUTHORIZED = "UNAUTHORIZED";
        public static final String AUTHENTICATED = "AUTHENTICATED";
        public static final String AUTHORIZED = "AUTHORIZED";
    }
}
