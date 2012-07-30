/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.openapi.vfs.impl.win32;

import com.intellij.openapi.util.io.win32.FileInfo;
import com.intellij.openapi.util.io.win32.IdeaWin32;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.impl.local.LocalFileSystemBase;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

import static com.intellij.util.BitUtil.isSet;
import static com.intellij.util.BitUtil.notSet;

/**
 * @author Dmitry Avdeev
 */
public class Win32LocalFileSystem extends LocalFileSystemBase {
  public static boolean isAvailable() {
    return IdeaWin32.isAvailable();
  }

  private static final ThreadLocal<Win32LocalFileSystem> THREAD_LOCAL = new ThreadLocal<Win32LocalFileSystem>() {
    @Override
    protected Win32LocalFileSystem initialValue() {
      return new Win32LocalFileSystem();
    }
  };

  public static Win32LocalFileSystem getWin32Instance() {
    if (!isAvailable()) throw new RuntimeException("Native filesystem for Windows is not loaded");
    Win32LocalFileSystem fileSystem = THREAD_LOCAL.get();
    fileSystem.myFsCache.clearCache();
    return fileSystem;
  }

  private final Win32FsCache myFsCache = new Win32FsCache();

  private Win32LocalFileSystem() { }

  @NotNull
  @Override
  public String[] list(@NotNull VirtualFile file) {
    if (isInvalidSymLink(file)) {
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }

    return myFsCache.list(file.getPath());
  }

  @Override
  public boolean exists(@NotNull VirtualFile fileOrDirectory) {
    if (fileOrDirectory.getParent() == null) return true;
    return myFsCache.getInfo(fileOrDirectory) != null;
  }

  @Override
  public boolean isDirectory(@NotNull VirtualFile file) {
    final FileInfo fileInfo = myFsCache.getInfo(file);
    return fileInfo != null && isSet(fileInfo.attributes, FileInfo.FILE_ATTRIBUTE_DIRECTORY);
  }

  @Override
  public boolean isWritable(@NotNull VirtualFile file) {
    final FileInfo fileInfo = myFsCache.getInfo(file);
    return fileInfo != null && notSet(fileInfo.attributes, FileInfo.FILE_ATTRIBUTE_READONLY);
  }

  @Override
  public long getTimeStamp(@NotNull VirtualFile file) {
    final FileInfo fileInfo = myFsCache.getInfo(file);
    return fileInfo != null ? fileInfo.getTimestamp() : DEFAULT_TIMESTAMP;
  }

  @Override
  public long getLength(@NotNull VirtualFile file) {
    final FileInfo fileInfo = myFsCache.getInfo(file);
    return fileInfo != null ? fileInfo.length : DEFAULT_LENGTH;
  }

  @NotNull
  @Override
  public Set<WatchRequest> addRootsToWatch(@NotNull Collection<String> rootPaths, boolean watchRecursively) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeWatchedRoots(@NotNull Collection<WatchRequest> watchRequests) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<WatchRequest> replaceWatchedRoots(@NotNull Collection<WatchRequest> watchRequests,
                                               @Nullable Collection<String> recursiveRoots,
                                               @Nullable Collection<String> flatRoots) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getBooleanAttributes(@NotNull VirtualFile file, int flags) {
    return myFsCache.getBooleanAttributes(file, flags);
  }
}
