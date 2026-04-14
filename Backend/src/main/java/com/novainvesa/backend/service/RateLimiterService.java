package com.novainvesa.backend.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiter en memoria con ventana deslizante.
 * Almacena timestamps de peticiones por clave (IP + acción).
 * Para el MVP es suficiente; en producción distribuida se reemplaza por Redis.
 */
@Service
public class RateLimiterService {

    /** Mapa: clave → deque de timestamps (epoch millis) */
    private final ConcurrentHashMap<String, Deque<Long>> buckets = new ConcurrentHashMap<>();

    /** Máx 5 intentos de login por IP en 15 minutos */
    private static final int LOGIN_MAX_REQUESTS = 5;
    /** Máx 10 registros por IP en 15 minutos */
    private static final int REGISTER_MAX_REQUESTS = 10;
    /** Ventana de tiempo: 15 minutos en milisegundos */
    private static final long WINDOW_MS = 15 * 60 * 1000L;

    /**
     * Verifica si la IP puede hacer login.
     * @return true si se permite, false si se superó el límite
     */
    public boolean allowLogin(String ip) {
        return allow("login:" + ip, LOGIN_MAX_REQUESTS);
    }

    /**
     * Verifica si la IP puede registrarse.
     * @return true si se permite, false si se superó el límite
     */
    public boolean allowRegister(String ip) {
        return allow("register:" + ip, REGISTER_MAX_REQUESTS);
    }

    // ─── Privados ──────────────────────────────────────────────────────────

    private synchronized boolean allow(String key, int maxRequests) {
        long now = Instant.now().toEpochMilli();
        long windowStart = now - WINDOW_MS;

        Deque<Long> timestamps = buckets.computeIfAbsent(key, k -> new ArrayDeque<>());

        // Eliminar timestamps fuera de la ventana
        while (!timestamps.isEmpty() && timestamps.peekFirst() < windowStart) {
            timestamps.pollFirst();
        }

        if (timestamps.size() >= maxRequests) {
            return false;
        }

        timestamps.addLast(now);
        return true;
    }
}
