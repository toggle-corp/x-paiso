package com.togglecorp.paiso;

public interface DatabaseListener<T> {

    // May be called more than once
    void handle(T data);
}
