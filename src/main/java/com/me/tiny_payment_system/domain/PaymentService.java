package com.me.tiny_payment_system.domain;

import com.me.tiny_payment_system.api.dto.CreatePaymentRequest;
import com.me.tiny_payment_system.api.dto.PaymentDto;
import com.me.tiny_payment_system.domain.db.PaymentEntity;
import com.me.tiny_payment_system.domain.db.PaymentRepository;
import com.me.tiny_payment_system.domain.db.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("10000.00");

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    public PaymentService(PaymentRepository paymentRepository, UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public PaymentDto createPayment(CreatePaymentRequest request) {
        if (!userRepository.existsById(request.userId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found id = " + request.userId());
        }
        if (request.amount().compareTo(MAX_AMOUNT) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount too large");
        }

        PaymentEntity payment = new PaymentEntity(request.userId(), request.amount(), PaymentStatus.NEW);
        PaymentEntity saved = paymentRepository.save(payment);
        log.info("Payment created: id={}", saved.getId());

        return toDto(saved);
    }

    public PaymentDto getPayment(Long id) {
        return toDto(findPaymentOrThrow(id));
    }

    @Transactional
    public PaymentDto confirmPayment(Long id) {
        PaymentEntity payment = findPaymentOrThrow(id);
        if (payment.getStatus() != PaymentStatus.NEW) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment status must be NEW");
        }
        payment.setStatus(PaymentStatus.SUCCEEDED);
        PaymentEntity saved = paymentRepository.save(payment);
        log.info("Payment has been confirmed: id={}", id);

        return toDto(saved);
    }

    private PaymentEntity findPaymentOrThrow(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Payment with id=" + id + " not found"));
    }

    private PaymentDto toDto(PaymentEntity p) {
        return new PaymentDto(
                p.getId(),
                p.getUserId(),
                p.getAmount(),
                p.getStatus(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}
