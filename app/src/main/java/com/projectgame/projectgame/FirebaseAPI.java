package com.projectgame.projectgame;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

import java.util.Map;

public interface FirebaseAPI {

    @POST("databases/(default)/documents/{collection}")
    Call<Void> saveDocument(@Path("collection") String collection, @Body Map<String, Object> document);
    Call<Map<String, Object>> getUserData(@Path("collection") String collection, @Path("documentId") String documentId);
}
