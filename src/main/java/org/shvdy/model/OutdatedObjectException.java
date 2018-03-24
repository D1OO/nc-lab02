package org.shvdy.model;

public class OutdatedObjectException extends Exception {

    public OutdatedObjectException(String s){
        super(s);
    }

    public OutdatedObjectException(String s, Throwable e){
        super(s, e);
    }
}
