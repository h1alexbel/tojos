/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2022 Yegor Bugayenko
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.yegor256.tojos;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class is thread-safe.
 *
 * @since 0.3.0
 */
public final class MnSynchronized implements Mono {

    /**
     * The ReadWriteLock.
     */
    private final ReadWriteLock locks;

    /**
     * The wrapped mono.
     */
    private final Mono wrapped;

    /**
     * Ctor.
     *
     * @param mono The mono
     */
    public MnSynchronized(final Mono mono) {
        this.wrapped = mono;
        this.locks = new ReentrantReadWriteLock();
    }

    @Override
    public Collection<Map<String, String>> read() {
        final Lock lock = this.locks.writeLock();
        lock.lock();
        try {
            return this.wrapped.read();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void write(final Collection<Map<String, String>> rows) {
        final Lock rlck = this.locks.readLock();
        final Lock wlck = this.locks.writeLock();
        wlck.lock();
        rlck.lock();
        try {
            this.wrapped.write(rows);
        } finally {
            wlck.unlock();
            rlck.unlock();
        }
    }

    @Override
    public void close() throws IOException {
        this.wrapped.close();
    }
}
