package com.aerobotics.DjiMobile;

import android.os.FileObserver;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class FileObserverDelegate extends FileObserver {
    public static final int CHANGES_ONLY = CREATE | DELETE | CLOSE_WRITE | MOVE_SELF | MOVED_FROM | MOVED_TO;

    List<SingleFileObserver> mObservers;
    String mPath;
    int mMask;
    public String directoryPath;
    ReactApplicationContext reactApplicationContext;
    public FileObserverDelegate(String path, ReactApplicationContext reactApplicationContext) {
        this(path, ALL_EVENTS);
        this.reactApplicationContext = reactApplicationContext;
        directoryPath = path;
    }

    public FileObserverDelegate(String path, int mask) {
        super(path, mask);
        mPath = path;
        mMask = mask;
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
        Log.i("REACT", "event occurred:"+filePath + " " + event);
        WritableMap params = Arguments.createMap();
        WritableMap eventInfo = Arguments.createMap();
        switch (event) {
            case FileObserver.CREATE:
                eventInfo.putString("eventName", "create");
                eventInfo.putString("fileName", filePath);
                params.putMap("value", eventInfo);
                params.putString("type", "DJIFlightLogEvent");
                reactApplicationContext
                        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit("DJIEvent", params);
                break;
            case FileObserver.MODIFY:
                eventInfo.putString("eventName", "modify");
                eventInfo.putString("fileName", filePath);
                params.putMap("value", eventInfo);
                params.putString("type", "DJIFlightLogEvent");
                reactApplicationContext
                        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit("DJIEvent", params);
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
            FileObserverDelegate.this.onEvent(event, newPath);
        }
    }
}
