package com.b1project.udooneo.utils;

import com.b1project.udooneo.listeners.FSWatcherListener;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Copyright (C) 2015 Cyril Bosselut <bossone0013@gmail.com>
 * <p>
 * This file is part of NeoJava examples for UDOO
 * <p>
 * NeoJava examples for UDOO is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This libraries are distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class FSWatcher extends Thread {
    private static final String TAG = FSWatcher.class.getSimpleName();
    private final WatchService mWatchService;
    private final Map<WatchKey,Path> mKeys;
    private WatchEvent.Kind<?>[] mEventTypes;
    private boolean shouldStopWatcher = false;
    private boolean mRecursive = false;
    private FSWatcherListener mListener;

    public FSWatcher(Path dir, boolean recursive, FSWatcherListener listener, WatchEvent.Kind<?>... eventTypes) throws IOException{
        mWatchService = FileSystems.getDefault().newWatchService();
        mKeys = new HashMap<>();
        mEventTypes = eventTypes;
        mRecursive = recursive;
        mListener = listener;
        if(recursive){
            addAll(dir);
        }
        else {
            add(dir);
        }
    }

    @Override
    public void run() {
        while (!shouldStopWatcher){
            WatchKey key;
            try{
                key = mWatchService.take();
            }
            catch (InterruptedException e){
                System.err.println("\r" + TAG + ": Watch service interrupted");
                System.out.print("#:");
                return;
            }

            Path dir = mKeys.get(key);
            if(dir == null){
                System.err.println("\r" + TAG + ": key does not exist");
                System.out.print("#:");
                continue;
            }

            for (WatchEvent<?> event: key.pollEvents()){
                WatchEvent.Kind<?> kind = event.kind();

                if(kind == OVERFLOW){
                    continue;
                }
                @SuppressWarnings("unchecked") WatchEvent<Path> watchEvent = (WatchEvent<Path>) event;
                Path source = watchEvent.context();
                Path path = dir.resolve(source);
                /*System.out.println("\r" + TAG + ": " + kind.name() + " [" + source + "]");
                System.out.print("#:");*/

                if (kind == ENTRY_CREATE){
                    if(mRecursive){
                        if(Files.isDirectory(path)){
                            try {
                                addAll(path);
                            } catch (IOException e) {
                                System.err.println("\r" + TAG + ": I/O error while adding path (" + path + ")");
                                System.out.print("#:");
                            }
                        }
                    }
                    if(mListener != null){
                        mListener.onNewFile(path);
                    }
                }
                else if(kind == ENTRY_MODIFY){
                    if(mListener != null){
                        mListener.onFileChanged(path);
                    }
                }
                else if(kind == ENTRY_DELETE){
                    if(mListener != null){
                        mListener.onFileDeleted(path);
                    }
                }
            }
            if(!key.reset()){
                mKeys.remove(key);
                if(mKeys.isEmpty()){
                    shouldStopWatcher = true;
                }
            }
        }
    }

    private void add(Path dir) throws IOException{
        WatchKey key = dir.register(mWatchService, mEventTypes);
        mKeys.put(key, dir);
    }

    public void addAll(final Path root) throws IOException{
        Files.walkFileTree(root, new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                add(dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if(Files.isDirectory(file)){
                    add(file);
                }
                return super.visitFile(file, attrs);
            }
        });
    }
}
