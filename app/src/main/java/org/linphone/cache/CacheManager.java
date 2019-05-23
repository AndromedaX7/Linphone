package org.linphone.cache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.linphone.R;
import org.linphone.adapter.ChatAdapter;
import org.linphone.app.App;
import org.linphone.http.HttpInterface;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class CacheManager {

    private static HashMap<String, Observable<byte[]>> cached = new HashMap<>();
    public static HashMap<String, String> pathCache = new HashMap<>();

    private static String TAG = "CacheManager";

    public static void cache(String path) {
        Log.e(TAG, "cache: "+path );
        File dir = getCachedDir();
        File lock = new File(dir, splitFileName(path) + ".lock");
        if (lock.exists()) {
            File img = new File(dir, splitFileName(path) + ".img.lock");
            if (img.exists())
                pathCache.put(path, new File(getCachedDir(), splitFileName(path) + ".png").getAbsolutePath());
            notifyAdapter();
        } else {
            Observable<byte[]> observable = cached.get(path);
            if (observable == null) {
                observable = HttpInterface.getResource(path);
                cached.put(path, observable);
                observable
                        .observeOn(Schedulers.io())
                        .subscribeOn(Schedulers.io())
                        .subscribe((buff -> CacheManager.downloadComplete(path, buff)), CacheManager::onErr, CacheManager::onComplete)
                        .isDisposed();
            } else if (pathCache.get(path) != null) {
                notifyAdapter();
            }
        }
    }

    private static void onComplete() {
    }

    public static File getCachedDir() {
        File filePath = App.app().getFilesDir();
        File root = new File(filePath, "imMp");
        root.mkdirs();
        return root;
    }


    public static String splitFileName(String httpPath) {
        if (httpPath==null) throw new RuntimeException("http is null");
        String[] split = httpPath.split("/");
        return split[split.length - 1];
    }

    private static void downloadComplete(String path, byte[] buff) {
        File dir = getCachedDir();
        File mp4 = new File(dir, splitFileName(path) + ".mp4");
        try {
            FileOutputStream outputStream = new FileOutputStream(mp4);
            outputStream.write(buff);
            outputStream.flush();
            outputStream.close();
            File mLock = new File(getCachedDir(), splitFileName(path) + ".lock");
            mLock.createNewFile();
            getFrameAtTime(path, 0);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void onErr(Throwable o) {
        o.printStackTrace();
    }


    public static void getFrameAtTime(String path, long timeUs) {
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(new File(getCachedDir(), splitFileName(path) + ".mp4").getAbsolutePath());
                Bitmap frameAtTime = null;
                try {
                    frameAtTime = mmr.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                File file = new File(getCachedDir(), splitFileName(path) + ".png");
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                FileOutputStream outputStream = new FileOutputStream(file);
                if (frameAtTime == null) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.outWidth = 200;
                    options.outHeight = 200;
                    frameAtTime = BitmapFactory.decodeResource(App.app().getResources(), R.drawable.video_play_placeholder, options);
                }
                frameAtTime.compress(Bitmap.CompressFormat.PNG, 50, out);
                outputStream.write(out.toByteArray());
                outputStream.flush();
                frameAtTime.recycle();
                File mLock = new File(getCachedDir(), splitFileName(path) + ".img.lock");
                mLock.createNewFile();
                emitter.onNext(path);
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(CacheManager::frame, CacheManager::onErr, CacheManager::onComplete)
                .isDisposed();

    }

    private static void frame(String path) {
        notifyAdapter();
    }

    public static void notifyAdapter() {
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    public static void clear() {
        adapter = null;
    }

    private static ChatAdapter adapter;

    public static void setAdapter(ChatAdapter adapter) {
        CacheManager.adapter = adapter;
    }
}
