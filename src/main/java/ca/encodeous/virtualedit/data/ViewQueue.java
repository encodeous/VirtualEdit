package ca.encodeous.virtualedit.data;

import org.bukkit.entity.Player;
import oshi.util.tuples.Pair;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ViewQueue {
    private final Lock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    private final Queue<Pair<Player, Runnable>> queue = new LinkedList<>();
    private final Set<Pair<Player, Runnable>> lockedPlayer = new HashSet<>();

    public void offerAndLock(Pair<Player, Runnable> action) {
        lock.lock();
        try {
            if (this.lockedPlayer.add(action)) {
                boolean empty = this.queue.isEmpty();
                this.queue.offer(action);
                if (empty) {
                    this.notEmpty.signal();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public Pair<Player, Runnable> poll() throws InterruptedException {
        lock.lock();
        try {
            if (this.queue.isEmpty()) {
                this.notEmpty.await();
            }
            return this.queue.poll();
        } finally {
            lock.unlock();
        }
    }

    public void unlock(Pair<Player, Runnable> action) {
        lock.lock();
        try {
            this.lockedPlayer.remove(action);
        } finally {
            lock.unlock();
        }
    }

    public void remove(Pair<Player, Runnable> player) {
        lock.lock();
        try {
            if (this.lockedPlayer.remove(player)) {
                this.queue.remove(player);
            }
        } finally {
            lock.unlock();
        }
    }

    public void clear() {
        lock.lock();
        try {
            this.lockedPlayer.clear();
            this.queue.clear();
        } finally {
            lock.unlock();
        }
    }
}
