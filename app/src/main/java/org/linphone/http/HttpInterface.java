package org.linphone.http;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public interface HttpInterface {

    static Observable<byte[]> getResource(String url)  {
        return Observable.create(new ObservableOnSubscribe<byte[]>() {
            @Override
            public void subscribe(ObservableEmitter<byte[]> emitter) throws Exception {
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .addHeader("cache-control", "no-cache")
                        .addHeader("postman-token", "4f28fb1c-cdf7-dffc-7796-c3dff4619c63")
                        .build();

                Response response = client.newCall(request).execute();
                emitter.onNext(response.body().bytes());
            }
        });
    }


}
