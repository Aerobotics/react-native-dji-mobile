package com.aerobotics.DjiMobile;

import android.os.FileObserver;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class RecursiveFileObserver extends FileObserver {

    private List<SingleFileObserver> mObservers;
    private String mPath;
    private int mMask;
    private ReactApplicationContext reactApplicationContext;
    private String eventType;

    public RecursiveFileObserver(String path, String eventType, ReactApplicationContext reactApplicationContext) {
        this(path, ALL_EVENTS);
        this.reactApplicationContext = reactApplicationContext;
        this.eventType = eventType;
    }

    public RecursiveFileObserver(String path, int mask) {
        super(path, mask);
        mPath = path;
        mMask = mask;
    }

    private void postEvent(WritableMap eventData) {
        WritableMap params = Arguments.createMap();
        params.putMap("value", eventData);
        params.putString("type", this.eventType);
        reactApplicationContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("DJIEvent", params);
    }

    @Override
    public void startWatching() {
        if (mObservers != null) return;

        mObservers = new ArrayList<SingleFileObserver>();
        Stack<String> stack = new Stack<String>();
        stack.push(mPath);
        while (!stack.isEmpty()) {
            String parent = stack.pop();
            mObservers.add(new SingleFileObserver(parent, mMask));
            File path = new File(parent);
            File[] files = path.listFiles();
            if (null == files) continue;
            for (File f : files)
            {
                if (f.isDirectory() && !f.getName().equals(".") && !f.getName().equals("..")) {
                    stack.push(f.getPath());
                }
            }
        }

        for (SingleFileObserver sfo : mObservers) {
            sfo.startWatching();
        }
    }

    @Override
    public void stopWatching() {
        if (mObservers == null) return;

        for (SingleFileObserver sfo : mObservers) {
            sfo.stopWatching();
        }
        mObservers.clear();
        mObservers = null;
    }

    @Override
    public void onEvent(int event, String filePath) {
        WritableMap eventInfo = Arguments.createMap();
        switch (event) {
            case FileObserver.CREATE:
                eventInfo.putString("eventName", "create");
                eventInfo.putString("filePath", filePath);
                postEvent(eventInfo);
                break;
            case FileObserver.MODIFY:
                eventInfo.putString("eventName", "modify");
                eventInfo.putString("filePath", filePath);
                postEvent(eventInfo);
                break;
            default:
                break;
        }
    }


    class SingleFileObserver extends FileObserver {
        String mPath;

        public SingleFileObserver(String path) {
            this(path, ALL_EVENTS);
            mPath = path;
        }

        public SingleFileObserver(String path, int mask) {
            super(path, mask);
            mPath = path;
        }

        @Override
        public void onEvent(int event, String path) {
            String newPath = mPath + "/" + path;
            RecursiveFileObserver.this.onEvent(event, newPath);
        }
    }
}
