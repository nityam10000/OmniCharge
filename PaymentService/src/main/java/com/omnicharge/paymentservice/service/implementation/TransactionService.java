package com.omnicharge.paymentservice.service.implementation;

import com.omnicharge.paymentservice.configuration.RabbitMQConfig;
import com.omnicharge.paymentservice.dto.NotificationEvent;
import com.omnicharge.paymentservice.dto.PaymentSagaEvent;
import com.omnicharge.paymentservice.dto.PaymentVerifyRequestDTO;
import com.omnicharge.paymentservice.dto.PlanResponseDTO;
import com.omnicharge.paymentservice.dto.RazorpayOrderRequestDTO;
import com.omnicharge.paymentservice.dto.RazorpayOrderResponseDTO;
import com.omnicharge.paymentservice.dto.RechargeResponseDTO;
import com.omnicharge.paymentservice.dto.TransactionRequestDTO;
import com.omnicharge.paymentservice.dto.TransactionResponseDTO;
import com.omnicharge.paymentservice.dto.UserResponseDTO;
import com.omnicharge.paymentservice.entity.Transaction;
import com.omnicharge.paymentservice.enums.PaymentMethod;
import com.omnicharge.paymentservice.enums.TransactionStatus;
import com.omnicharge.paymentservice.exception.AccessDeniedException;
import com.omnicharge.paymentservice.exception.PaymentProcessingException;
import com.omnicharge.paymentservice.exception.ServiceUnavailableException;
import com.omnicharge.paymentservice.exception.TransactionNotFoundException;
import com.omnicharge.paymentservice.exception.UnauthorizedException;
import com.omnicharge.paymentservice.feignClient.IOperatorPlanClient;
import com.omnicharge.paymentservice.feignClient.IRechargeClient;
import com.omnicharge.paymentservice.feignClient.IUserClient;
import com.omnicharge.paymentservice.mapper.Mapper;
import com.omnicharge.paymentservice.repository.ITransactionRepository;
import com.omnicharge.paymentservice.service.ITransactionService;
import com.omnicharge.paymentservice.service.RazorpayRefundService;
import com.omnicharge.paymentservice.support.AuthenticatedUserContext;
import com.omnicharge.paymentservice.support.RazorpayGateway;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
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
    private final RazorpayRefundService razorpayRefundService;
    private final AuthenticatedUserContext authenticatedUserContext;
    private final RazorpayGateway razorpayGateway;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Override
    public TransactionResponseDTO createTransaction(TransactionRequestDTO dto) {
        throw new UnsupportedOperationException(
                "Use POST /transaction/create-order to initiate a Razorpay payment."
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

    @Override
    @Retry(name = "RECHARGEPROCESSING", fallbackMethod = "createOrderFallback")
    public RazorpayOrderResponseDTO createOrder(RazorpayOrderRequestDTO dto) {
        try {
            Long userId = getLoggedInUserId();
            String userEmail = getLoggedInUserEmail();
            String userRole = getLoggedInUserRole();

            RechargeResponseDTO recharge = rechargeClient.getRechargeById(
                    userRole, userEmail, dto.getRechargeId());
            if (recharge == null) {
                throw new TransactionNotFoundException("Recharge not found for id: " + dto.getRechargeId());
            }

            if (!userId.equals(recharge.getUserId())) {
                log.warn("Ownership violation: userId={} tried to pay for rechargeId={} owned by userId={}",
                        userId, dto.getRechargeId(), recharge.getUserId());
                throw new AccessDeniedException("Access denied: recharge does not belong to the current user.");
            }

            PlanResponseDTO plan = operatorPlanClient.getPlanById(
                    userRole, userEmail, recharge.getPlanId());
            if (plan == null || plan.getAmount() == null) {
                throw new TransactionNotFoundException("Plan not found or has no amount for planId: " + recharge.getPlanId());
            }

            String contactNo = fetchContactNo(userEmail, userRole);
            Double amount = plan.getAmount();

            String razorpayOrderId = razorpayGateway.createOrderId(
                    amount,
                    "recharge_" + dto.getRechargeId(),
                    razorpayKeyId,
                    razorpayKeySecret
            );

            Transaction txn = new Transaction();
            txn.setAmount(amount);
            txn.setPaymentMethod(PaymentMethod.valueOf(dto.getPaymentMethod().toUpperCase()));
            txn.setStatus(TransactionStatus.PENDING);
            txn.setRechargeId(dto.getRechargeId());
            txn.setUserId(userId);
            txn.setUserEmail(userEmail);
            txn.setUserContactNo(contactNo);
            txn.setRazorpayOrderId(razorpayOrderId);
            transactionRepository.save(txn);

            return new RazorpayOrderResponseDTO(razorpayKeyId, razorpayOrderId, amount, "INR");

        } catch (Exception e) {
            log.error("Razorpay order creation failed: {}", e.getMessage(), e);
            throw new PaymentProcessingException("Razorpay order creation failed: " + e.getMessage());
        }
    }

    public RazorpayOrderResponseDTO createOrderFallback(RazorpayOrderRequestDTO dto, Exception e) {
        log.error("All retries exhausted for createOrder. rechargeId={}, error={}",
                dto.getRechargeId(), e.getMessage());

        try {
            Long userId = getLoggedInUserId();
            String userEmail = getLoggedInUserEmail();

            Transaction failedTxn = new Transaction();
            failedTxn.setRechargeId(dto.getRechargeId());
            failedTxn.setUserId(userId);
            failedTxn.setUserEmail(userEmail);
            failedTxn.setStatus(TransactionStatus.FAILED);
            failedTxn.setFailureReason(
                    "RechargeProcessing unavailable during order creation. Recharge cancelled.");
            failedTxn.setPaymentMethod(PaymentMethod.valueOf(dto.getPaymentMethod().toUpperCase()));
            Transaction saved = transactionRepository.save(failedTxn);

            publishFailedSagaEventForRecharge(saved, dto.getRechargeId());

        } catch (Exception ex) {
            log.error("Could not save audit transaction or publish saga event during fallback for rechargeId={}: {}",
                    dto.getRechargeId(), ex.getMessage(), ex);
        }

        throw new ServiceUnavailableException(
                "Order creation failed - RechargeProcessing is unavailable. " +
                        "Your recharge has been cancelled. Please try again later.");
    }

    @Override
    public TransactionResponseDTO verifyPayment(PaymentVerifyRequestDTO dto) {
        Transaction txn = transactionRepository
                .findByRazorpayOrderId(dto.getRazorpayOrderId())
                .orElseThrow(() -> new TransactionNotFoundException(
                        "Transaction not found for orderId: " + dto.getRazorpayOrderId()));

        if (txn.getStatus() == TransactionStatus.SUCCESS
                || txn.getStatus() == TransactionStatus.FAILED) {
            return mapper.toTransactionResponseDTO(txn);
        }

        boolean signatureValid = verifySignature(
                dto.getRazorpayOrderId(),
                dto.getRazorpayPaymentId(),
                dto.getRazorpaySignature());

        if (signatureValid) {
            txn.setStatus(TransactionStatus.SUCCESS);
            txn.setRazorpayPaymentId(dto.getRazorpayPaymentId());
        } else {
            txn.setStatus(TransactionStatus.FAILED);
            txn.setFailureReason("Signature mismatch");

            if (dto.getRazorpayPaymentId() != null && !dto.getRazorpayPaymentId().isBlank()) {
                txn.setRazorpayPaymentId(dto.getRazorpayPaymentId());
            }
        }

        Transaction saved = transactionRepository.save(txn);

        String eventType = signatureValid
                ? RabbitMQConfig.SAGA_ROUTING_COMPLETED
                : RabbitMQConfig.SAGA_ROUTING_FAILED;

        publishSagaEvent(saved, eventType);

        if (signatureValid) {
            sendPaymentSuccessNotification(saved);
        } else {
            sendPaymentFailedNotification(saved);

            if (saved.getRazorpayPaymentId() != null && !saved.getRazorpayPaymentId().isBlank()) {
                razorpayRefundService.refundAndNotify(
                        saved,
                        "Signature mismatch during payment verification for rechargeId=" + saved.getRechargeId()
                );
            }
        }

        return mapper.toTransactionResponseDTO(saved);
    }

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
                    .razorpayPaymentId(txn.getRazorpayPaymentId())
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
                    .failureReason("RechargeProcessing was unavailable during order creation.")
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.SAGA_EXCHANGE,
                    RabbitMQConfig.SAGA_ROUTING_FAILED,
                    event
            );
        } catch (Exception ex) {
            log.error("Could not publish saga FAILED event for rechargeId={}: {}",
                    rechargeId, ex.getMessage(), ex);
        }
    }

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
                    "Your recharge payment of Rs." + txn.getAmount() + " failed due to a verification error. " +
                            "If any amount was deducted, a refund is being initiated automatically.",
                    txn.getUserEmail(), txn.getUserContactNo(), "PAYMENT_FAILED");
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, event);
        } catch (Exception e) {
            log.error("Failed to publish failed notification: {}", e.getMessage());
        }
    }

    private boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    razorpayKeySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString().equals(signature);
        } catch (Exception e) {
            log.error("Exception during signature verification: {}", e.getMessage(), e);
            return false;
        }
    }

    private String fetchContactNo(String email, String role) {
        try {
            UserResponseDTO user = userClient.getUserByEmail(role, email, email);
            return user != null ? user.getContactNo() : null;
        } catch (Exception e) {
            log.warn("Could not fetch contactNo for email={}: {}", email, e.getMessage());
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
