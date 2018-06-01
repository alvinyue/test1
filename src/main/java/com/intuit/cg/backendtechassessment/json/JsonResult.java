package com.intuit.cg.backendtechassessment.json;

public class JsonResult<T> {

    T result;

    public JsonResult(T result) {
        this.result = result;
    }
    public T getResult() {
        return result;
    }

}
