package com.me.tiny_payment_system.api.errors;

import java.time.Instant;

public record ApiError(String code, String message, Instant timestamp) {
}
