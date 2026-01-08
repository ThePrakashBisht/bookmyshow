package com.bookmyshow.bookingservice.service;

import com.bookmyshow.bookingservice.exception.SeatLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisLockService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${booking.lock.duration-seconds:300}")
    private int lockDurationSeconds;

    @Value("${booking.lock.retry-attempts:3}")
    private int retryAttempts;

    @Value("${booking.lock.retry-delay-ms:100}")
    private int retryDelayMs;

    // Key patterns
    private static final String SEAT_LOCK_KEY_PREFIX = "lock:seat:";
    private static final String BOOKING_LOCK_KEY_PREFIX = "lock:booking:";

    /**
     * Attempts to lock multiple seats atomically.
     * Either ALL seats are locked, or NONE are locked.
     *
     * @param showId   The show ID
     * @param seatIds  List of ShowSeat IDs to lock
     * @param userId   User attempting to lock
     * @param lockId   Unique lock identifier (used for release)
     * @return true if all seats were locked successfully
     */
    public boolean lockSeats(Long showId, List<Long> seatIds, Long userId, String lockId) {
        log.info("Attempting to lock {} seats for show {} by user {}",
                seatIds.size(), showId, userId);

        List<String> lockKeys = new ArrayList<>();
        List<String> acquiredLocks = new ArrayList<>();

        try {
            // Generate lock keys
            for (Long seatId : seatIds) {
                lockKeys.add(generateSeatLockKey(showId, seatId));
            }

            // Try to acquire all locks
            for (String lockKey : lockKeys) {
                boolean acquired = tryAcquireLock(lockKey, lockId, lockDurationSeconds);

                if (acquired) {
                    acquiredLocks.add(lockKey);
                    log.debug("Acquired lock: {}", lockKey);
                } else {
                    // Failed to acquire one lock - release all acquired locks
                    log.warn("Failed to acquire lock: {}. Rolling back {} acquired locks.",
                            lockKey, acquiredLocks.size());
                    releaseMultipleLocks(acquiredLocks, lockId);
                    return false;
                }
            }

            log.info("Successfully locked all {} seats with lockId: {}", seatIds.size(), lockId);
            return true;

        } catch (Exception e) {
            log.error("Error while locking seats: {}", e.getMessage(), e);
            // Release any acquired locks on error
            releaseMultipleLocks(acquiredLocks, lockId);
            throw new SeatLockException("Failed to lock seats: " + e.getMessage());
        }
    }

    /**
     * Releases locks for multiple seats.
     */
    public void releaseSeats(Long showId, List<Long> seatIds, String lockId) {
        log.info("Releasing {} seat locks for show {} with lockId: {}",
                seatIds.size(), showId, lockId);

        List<String> lockKeys = new ArrayList<>();
        for (Long seatId : seatIds) {
            lockKeys.add(generateSeatLockKey(showId, seatId));
        }

        releaseMultipleLocks(lockKeys, lockId);
    }

    /**
     * Extends the lock duration (for payment processing).
     */
    public boolean extendLock(Long showId, List<Long> seatIds, String lockId, int additionalSeconds) {
        log.info("Extending lock for {} seats by {} seconds", seatIds.size(), additionalSeconds);

        for (Long seatId : seatIds) {
            String lockKey = generateSeatLockKey(showId, seatId);

            // Verify we own the lock before extending
            Object currentLockId = redisTemplate.opsForValue().get(lockKey);
            if (!lockId.equals(currentLockId)) {
                log.warn("Cannot extend lock - not owned by lockId: {}", lockId);
                return false;
            }

            // Extend the expiration
            Boolean success = redisTemplate.expire(lockKey, additionalSeconds, TimeUnit.SECONDS);
            if (success == null || !success) {
                log.warn("Failed to extend lock: {}", lockKey);
                return false;
            }
        }

        log.info("Successfully extended locks for {} seats", seatIds.size());
        return true;
    }

    /**
     * Checks if a seat is currently locked.
     */
    public boolean isSeatLocked(Long showId, Long seatId) {
        String lockKey = generateSeatLockKey(showId, seatId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
    }

    /**
     * Gets the lock holder for a seat.
     */
    public Optional<String> getLockHolder(Long showId, Long seatId) {
        String lockKey = generateSeatLockKey(showId, seatId);
        Object lockId = redisTemplate.opsForValue().get(lockKey);
        return Optional.ofNullable(lockId != null ? lockId.toString() : null);
    }

    /**
     * Gets remaining TTL for a lock in seconds.
     */
    public long getLockTTL(Long showId, Long seatId) {
        String lockKey = generateSeatLockKey(showId, seatId);
        Long ttl = redisTemplate.getExpire(lockKey, TimeUnit.SECONDS);
        return ttl != null ? ttl : 0;
    }

    // ============ Private Helper Methods ============

    /**
     * Tries to acquire a single lock with retries.
     */
    private boolean tryAcquireLock(String lockKey, String lockId, int expirationSeconds) {
        for (int attempt = 1; attempt <= retryAttempts; attempt++) {
            // SET key value NX EX seconds
            Boolean success = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, lockId, Duration.ofSeconds(expirationSeconds));

            if (Boolean.TRUE.equals(success)) {
                return true;
            }

            if (attempt < retryAttempts) {
                log.debug("Lock attempt {} failed for key: {}. Retrying...", attempt, lockKey);
                try {
                    Thread.sleep(retryDelayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }

        return false;
    }

    /**
     * Releases a lock only if we own it (using Lua script for atomicity).
     */
    private boolean releaseLock(String lockKey, String lockId) {
        // Lua script for atomic check-and-delete
        String luaScript =
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                        "   return redis.call('del', KEYS[1]) " +
                        "else " +
                        "   return 0 " +
                        "end";

        DefaultRedisScript<Long> script = new DefaultRedisScript<>(luaScript, Long.class);

        Long result = redisTemplate.execute(script, List.of(lockKey), lockId);

        boolean released = result != null && result > 0;
        if (released) {
            log.debug("Released lock: {}", lockKey);
        } else {
            log.debug("Lock not released (not owned or expired): {}", lockKey);
        }

        return released;
    }

    /**
     * Releases multiple locks.
     */
    private void releaseMultipleLocks(List<String> lockKeys, String lockId) {
        for (String lockKey : lockKeys) {
            try {
                releaseLock(lockKey, lockId);
            } catch (Exception e) {
                log.error("Error releasing lock {}: {}", lockKey, e.getMessage());
            }
        }
    }

    /**
     * Generates the lock key for a seat.
     */
    private String generateSeatLockKey(Long showId, Long seatId) {
        return SEAT_LOCK_KEY_PREFIX + showId + ":" + seatId;
    }

    /**
     * Generates a unique lock ID.
     */
    public String generateLockId() {
        return UUID.randomUUID().toString();
    }
}