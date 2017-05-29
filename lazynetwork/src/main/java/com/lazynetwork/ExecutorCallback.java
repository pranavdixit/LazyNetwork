package com.lazynetwork;

/**
 * Created by pranav.dixit on 19/05/17.
 */

public interface ExecutorCallback <E extends RecordCallback>{
    void execute(E object);
}
