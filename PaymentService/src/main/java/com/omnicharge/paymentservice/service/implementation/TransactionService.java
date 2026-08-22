package com.omnicharge.paymentservice.service.implementation;

import com.omnicharge.paymentservice.configuration.RabbitMQConfig;
import com.omnicharge.paymentservice.dto.CustomPaymentRequestDTO;
import com.omnicharge.paymentservice.dto.NotificationEvent;
import com.omnicharge.paymentservice.dto.PaymentSagaEvent;
import com.omnicharge.paymentservice.dto.PlanResponseDTO;
import com.omnicharge.paymentservice.dto.RechargeResponseDTO;
import com.omnicharge.paymentservice.dto.TransactionRequestDTO;
import com.omnicharge.paymentservice.dto.TransactionResponseDTO;
import com.omnicharge.paymentservice.dto.UserResponseDTO;
import com.omnicharge.paymentservice.entity.Transaction;
import com.omnicharge.paymentservice.enums.PaymentMethod;
import com.omnicharge.paymentservice.enums.TransactionStatus;
import com.omnicharge.paymentservice.exception.AccessDeniedException;
import com.omnicharge.paymentservice.exception.ServiceUnavailableException;
import com.omnicharge.paymentservice.exception.TransactionNotFoundException;
import com.omnicharge.paymentservice.exception.UnauthorizedException;
import com.omnicharge.paymentservice.feignClient.IOperatorPlanClient;
import com.omnicharge.paymentservice.feignClient.IRechargeClient;
import com.omnicharge.paymentservice.feignClient.IUserClient;
import com.omnicharge.paymentservice.mapper.Mapper;
import com.omnicharge.paymentservice.repository.ITransactionRepository;
import com.omnicharge.paymentservice.service.ITransactionService;
import com.omnicharge.paymentservice.support.AuthenticatedUserContext;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService implements ITransactionService {

    private final ITransactionRepository transactionRepository;
    private final Mapper mapper;
    private final IRechargeClient rechargeClient;
    private final IOperatorPlanClient operatorPlanClient;
    private final IUserClient userClient;
    private final RabbitTemplate rabbitTemplate;
    private final AuthenticatedUserContext authenticatedUserContext;

    @Override
    public TransactionResponseDTO createTransaction(TransactionRequestDTO dto) {
        throw new UnsupportedOperationException(
                "Use POST /transaction/process to initiate a payment."
        );
    }

    @Override
    public List<TransactionResponseDTO> getAllTransactionsByUserId(Long userId) {
        return transactionRepository.findByUserId(userId)
                .stream()
                .map(mapper::toTransactionResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionResponseDTO getTransactionByRechargeId(Long rechargeId) {
        Long loggedInUserId = getLoggedInUserId();
        String userRole = getLoggedInUserRole();

        Transaction transaction = transactionRepository.findByRechargeId(rechargeId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction Not Found!!"));

        // Allow access if user is admin or if it's their own transaction
        if (!userRole.contains("ADMIN") && !loggedInUserId.equals(transaction.getUserId())) {
            log.warn("Authorization denied: userId={} attempted to access transaction for rechargeId={} owned by userId={}",
                    loggedInUserId, rechargeId, transaction.getUserId());
            throw new AccessDeniedException("Access denied: You are not authorized to view this transaction.");
        }

        return mapper.toTransactionResponseDTO(transaction);
    }

    @Override
    public Page<TransactionResponseDTO> getMyTransactions(Pageable pageable) {
        Long userId = getLoggedInUserId();
        return transactionRepository.findByUserId(userId, pageable)
                .map(mapper::toTransactionResponseDTO);
    }

    /**
     * Custom payment processing logic.
     * <p>
     * Flow:
     * 1. Fetch the Recharge record from RechargeProcessing and validate ownership.
     * 2. Fetch the Plan to derive the amount.
     * 3. Create the Transaction with status PENDING.
     * 4. Evaluate the client-supplied "paymentResponse":
     *    - "pass"  → mark SUCCESS, publish SAGA_COMPLETED event, send success notification.
     *    - "fail"  → mark FAILED,  publish SAGA_FAILED event,     send failure notification.
     * 5. Return the saved transaction.
     */
    @Override
    @Retry(name = "RECHARGEPROCESSING", fallbackMethod = "processPaymentFallback")
    public TransactionResponseDTO processPayment(CustomPaymentRequestDTO dto) {
        Long userId = getLoggedInUserId();
        String userEmail = getLoggedInUserEmail();
        String userRole = getLoggedInUserRole();

        // Step 1 – validate recharge exists and belongs to the current user
        RechargeResponseDTO recharge = rechargeClient.getRechargeById(userRole, userEmail, dto.getRechargeId());
        if (recharge == null) {
            throw new TransactionNotFoundException("Recharge not found for id: " + dto.getRechargeId());
        }
        if (!userId.equals(recharge.getUserId())) {
            log.warn("Ownership violation: userId={} tried to pay for rechargeId={} owned by userId={}",
                    userId, dto.getRechargeId(), recharge.getUserId());
            throw new AccessDeniedException("Access denied: recharge does not belong to the current user.");
        }

        // Step 2 – fetch plan to get authoritative amount
        PlanResponseDTO plan = operatorPlanClient.getPlanById(userRole, userEmail, recharge.getPlanId());
        if (plan == null || plan.getAmount() == null) {
            throw new TransactionNotFoundException("Plan not found or has no amount for planId: " + recharge.getPlanId());
        }

        String contactNo = fetchContactNo(userEmail, userRole);
        Double amount = plan.getAmount();

        // Step 3 – persist transaction as PENDING
        Transaction txn = new Transaction();
        txn.setAmount(amount);
        txn.setPaymentMethod(PaymentMethod.valueOf(dto.getPaymentMethod().toUpperCase()));
        txn.setStatus(TransactionStatus.PENDING);
        txn.setRechargeId(dto.getRechargeId());
        txn.setUserId(userId);
        txn.setUserEmail(userEmail);
        txn.setUserContactNo(contactNo);
        transactionRepository.save(txn);

        // Step 4 – evaluate the simulated gateway response
        boolean paymentPassed = "pass".equalsIgnoreCase(dto.getPaymentResponse());

        if (paymentPassed) {
            txn.setStatus(TransactionStatus.SUCCESS);
        } else {
            txn.setStatus(TransactionStatus.FAILED);
            txn.setFailureReason("Payment gateway returned 'fail' response.");
        }

        Transaction saved = transactionRepository.save(txn);

        // Step 5 – publish SAGA event so RechargeProcessing updates its state
        String sagaEventType = paymentPassed
                ? RabbitMQConfig.SAGA_ROUTING_COMPLETED
                : RabbitMQConfig.SAGA_ROUTING_FAILED;
        publishSagaEvent(saved, sagaEventType);

        // Step 6 – send notification
        if (paymentPassed) {
            sendPaymentSuccessNotification(saved);
        } else {
            sendPaymentFailedNotification(saved);
        }

        return mapper.toTransactionResponseDTO(saved);
    }

    /**
     * Fallback triggered when RechargeProcessing is unavailable after all retries.
     * Saves a FAILED audit transaction and publishes a saga FAILED event so the
     * recharge is rolled back on the other side.
     */
    public TransactionResponseDTO processPaymentFallback(CustomPaymentRequestDTO dto, Exception e) {

        try {
            Long userId = getLoggedInUserId();
            String userEmail = getLoggedInUserEmail();

            Transaction failedTxn = new Transaction();
            failedTxn.setRechargeId(dto.getRechargeId());
            failedTxn.setUserId(userId);
            failedTxn.setUserEmail(userEmail);
            failedTxn.setStatus(TransactionStatus.FAILED);
            failedTxn.setFailureReason("RechargeProcessing unavailable during payment processing. Recharge cancelled.");
            failedTxn.setPaymentMethod(PaymentMethod.valueOf(dto.getPaymentMethod().toUpperCase()));
            Transaction saved = transactionRepository.save(failedTxn);

            publishFailedSagaEventForRecharge(saved, dto.getRechargeId());

            return mapper.toTransactionResponseDTO(saved);

        } catch (Exception ex) {
            log.error("Could not save audit transaction or publish saga event during fallback for rechargeId={}: {}",
                    dto.getRechargeId(), ex.getMessage(), ex);
        }

        throw new ServiceUnavailableException(
                "Payment processing failed - RechargeProcessing is unavailable. " +
                        "Your recharge has been cancelled. Please try again later.");
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  SAGA helpers
    // ──────────────────────────────────────────────────────────────────────────

    private void publishSagaEvent(Transaction txn, String eventType) {
        try {
            PaymentSagaEvent event = PaymentSagaEvent.builder()
                    .sagaId(txn.getTransactionId().toString())
                    .transactionId(txn.getTransactionId())
                    .rechargeId(txn.getRechargeId())
                    .userId(txn.getUserId())
                    .userEmail(txn.getUserEmail())
                    .userContactNo(txn.getUserContactNo())
                    .amount(txn.getAmount())
                    .eventType(eventType)
                    .failureReason(txn.getFailureReason())
                    .build();

            rabbitTemplate.convertAndSend(RabbitMQConfig.SAGA_EXCHANGE, eventType, event);
            
        } catch (Exception e) {
            log.error("SAGA PUBLISH FAILED - transactionId={}, rechargeId={}, error={}",
                    txn.getTransactionId(), txn.getRechargeId(), e.getMessage(), e);
        }
    }

    private void publishFailedSagaEventForRecharge(Transaction savedTxn, Long rechargeId) {
        try {
            PaymentSagaEvent event = PaymentSagaEvent.builder()
                    .sagaId(savedTxn.getTransactionId().toString())
                    .transactionId(savedTxn.getTransactionId())
                    .rechargeId(rechargeId)
                    .userId(savedTxn.getUserId())
                    .userEmail(savedTxn.getUserEmail())
                    .eventType(RabbitMQConfig.SAGA_ROUTING_FAILED)
                    .failureReason("RechargeProcessing was unavailable during payment processing.")
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.SAGA_EXCHANGE,
                    RabbitMQConfig.SAGA_ROUTING_FAILED,
                    event);
        } catch (Exception ex) {
            log.error("Could not publish saga FAILED event for rechargeId={}: {}",
                    rechargeId, ex.getMessage(), ex);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Notification helpers
    // ──────────────────────────────────────────────────────────────────────────

    private void sendPaymentSuccessNotification(Transaction txn) {
        try {
            NotificationEvent event = new NotificationEvent(
                    "Your recharge of Rs." + txn.getAmount() + " was successful! " +
                            "Transaction ID: " + txn.getTransactionId() + ". Your plan is now active.",
                    txn.getUserEmail(), txn.getUserContactNo(), "PAYMENT_SUCCESS");
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, event);
        } catch (Exception e) {
            log.error("Failed to publish success notification: {}", e.getMessage());
        }
    }

    private void sendPaymentFailedNotification(Transaction txn) {
        try {
            NotificationEvent event = new NotificationEvent(
                    "Your recharge payment of Rs." + txn.getAmount() + " failed. " +
                            "Please check your details and try again.",
                    txn.getUserEmail(), txn.getUserContactNo(), "PAYMENT_FAILED");
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, event);
        } catch (Exception e) {
            log.error("Failed to publish failed notification: {}", e.getMessage());
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Utility helpers
    // ──────────────────────────────────────────────────────────────────────────

    private String fetchContactNo(String email, String role) {
        try {
            UserResponseDTO user = userClient.getUserByEmail(role, email, email);
            return user != null ? user.getContactNo() : null;
        } catch (Exception e) {
            
            return null;
        }
    }

    private Long getLoggedInUserId() {
        String userId = authenticatedUserContext.getUserIdHeader();
        if (userId == null || userId.isBlank()) {
            throw new UnauthorizedException("X-User-Id header is missing");
        }
        return Long.parseLong(userId);
    }

    private String getLoggedInUserEmail() {
        return authenticatedUserContext.getEmail();
    }

    private String getLoggedInUserRole() {
        return authenticatedUserContext.getRole();
    }
}
