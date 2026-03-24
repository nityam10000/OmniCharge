package com.omnicharge.paymentservice.service.implementation;

import com.omnicharge.paymentservice.configuration.RabbitMQConfig;
import com.omnicharge.paymentservice.dto.*;
import com.omnicharge.paymentservice.entity.Transaction;
import com.omnicharge.paymentservice.enums.PaymentMethod;
import com.omnicharge.paymentservice.enums.TransactionStatus;
import com.omnicharge.paymentservice.exception.TransactionNotFoundException;
import com.omnicharge.paymentservice.feignClient.IOperatorPlanClient;
import com.omnicharge.paymentservice.feignClient.IRechargeClient;
import com.omnicharge.paymentservice.feignClient.IUserClient;
import com.omnicharge.paymentservice.mapper.Mapper;
import com.omnicharge.paymentservice.repository.ITransactionRepository;
import com.omnicharge.paymentservice.service.ITransactionService;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
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
        List<Transaction> transactions = transactionRepository.findByUserId(userId);
        return transactions.stream().map(mapper::toTransactionResponseDTO).collect(Collectors.toList());
    }

    @Override
    public TransactionResponseDTO getTransactionByRechargeId(Long rechargeId) {
        Transaction transaction = transactionRepository.findByRechargeId(rechargeId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction Not Found!!"));
        return mapper.toTransactionResponseDTO(transaction);
    }

    @Override
    public List<TransactionResponseDTO> getMyTransactions() {
        Long userId = getLoggedInUserId();
        List<Transaction> transactions = transactionRepository.findByUserId(userId);
        return transactions.stream()
                .map(mapper::toTransactionResponseDTO)
                .toList();
    }

    @Override
    @Retry(name = "RECHARGEPROCESSING", fallbackMethod = "createOrderFallback")
    public RazorpayOrderResponseDTO createOrder(RazorpayOrderRequestDTO dto) {
        try {
            Long userId = getLoggedInUserId();
            String userEmail = getLoggedInUserEmail();
            String userRole = getLoggedInUserRole();

            RechargeResponseDTO recharge = rechargeClient.getRechargeById(userRole, userEmail, dto.getRechargeId());
            if (recharge == null) {
                throw new RuntimeException("Recharge not found for id: " + dto.getRechargeId());
            }

            // Security: ensure the recharge belongs to the logged-in user
            if (!userId.equals(recharge.getUserId())) {
                log.warn("Ownership violation: userId={} attempted to pay for rechargeId={} owned by userId={}",
                        userId, dto.getRechargeId(), recharge.getUserId());
                throw new RuntimeException("Access denied: recharge does not belong to the current user.");
            }

            PlanResponseDTO plan = operatorPlanClient.getPlanById(userRole, userEmail, recharge.getPlanId());
            if (plan == null || plan.getAmount() == null) {
                throw new RuntimeException("Plan not found or has no amount for planId: " + recharge.getPlanId());
            }

            // Fetch contactNo here — user is authenticated and request context is live.
            // Stored on the Transaction so notifications don't need a Feign call later.
            String contactNo = fetchContactNo(userEmail, userRole);

            Double amount = plan.getAmount();
            log.info("Server-side amount fetched: {} for rechargeId={}, planId={}", amount, dto.getRechargeId(), recharge.getPlanId());

            RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", (int)(amount * 100));
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "recharge_" + dto.getRechargeId());

            Order razorpayOrder = client.orders.create(orderRequest);
            String razorpayOrderId = razorpayOrder.get("id");
            log.info("Razorpay order created: orderId={}, amount={}, rechargeId={}", razorpayOrderId, amount, dto.getRechargeId());

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

            return new RazorpayOrderResponseDTO(razorpayOrderId, amount, "INR");

        } catch (Exception e) {
            log.error("Razorpay order creation failed: {}", e.getMessage(), e);
            throw new RuntimeException("Razorpay order creation failed: " + e.getMessage());
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

    public RazorpayOrderResponseDTO createOrderFallback(RazorpayOrderRequestDTO dto, Exception e) {
        log.error("All retry attempts exhausted for createOrder. rechargeId={}, error={}", dto.getRechargeId(), e.getMessage());
        throw new RuntimeException("Order creation failed after retries. Please try again later.");
    }

    @Override
    public TransactionResponseDTO verifyPayment(PaymentVerifyRequestDTO dto) {
        log.info("=== verifyPayment called ===");

        Transaction txn = transactionRepository
                .findByRazorpayOrderId(dto.getRazorpayOrderId())
                .orElseThrow(() -> new TransactionNotFoundException(
                        "Transaction not found for orderId: " + dto.getRazorpayOrderId()
                ));

        log.info("Transaction found: transactionId={}, status={}", txn.getTransactionId(), txn.getStatus());

        if (txn.getStatus() == TransactionStatus.SUCCESS || txn.getStatus() == TransactionStatus.FAILED) {
            log.warn("verifyPayment called on already-processed transaction: id={}, status={}",
                    txn.getTransactionId(), txn.getStatus());
            return mapper.toTransactionResponseDTO(txn);
        }

        boolean signatureValid = verifySignature(
                dto.getRazorpayOrderId(),
                dto.getRazorpayPaymentId(),
                dto.getRazorpaySignature()
        );
        log.info("Signature verification result: {}", signatureValid);

        if (signatureValid) {
            txn.setStatus(TransactionStatus.SUCCESS);
            txn.setRazorpayPaymentId(dto.getRazorpayPaymentId());
            log.info("Payment SUCCESS for rechargeId={}", txn.getRechargeId());
            updateRechargeStatusWithRetry(txn.getRechargeId(), "SUCCESS");
        } else {
            txn.setStatus(TransactionStatus.FAILED);
            txn.setFailureReason("Signature mismatch");
            log.warn("Payment FAILED for rechargeId={} — signature mismatch", txn.getRechargeId());
            updateRechargeStatusWithRetry(txn.getRechargeId(), "FAILED");
        }

        Transaction saved = transactionRepository.save(txn);
        log.info("Transaction saved with status: {}", saved.getStatus());

        if (signatureValid) {
            sendPaymentSuccessNotification(saved);
        } else {
            sendPaymentFailedNotification(saved);
        }

        return mapper.toTransactionResponseDTO(saved);
    }

    @Retry(name = "RECHARGEPROCESSING", fallbackMethod = "updateRechargeStatusFallback")
    public void updateRechargeStatusWithRetry(Long rechargeId, String status) {
        rechargeClient.updateRechargeStatus("ROLE_ADMIN", "system@omnicharge.com", rechargeId, status);
        log.info("Recharge status updated to {} for rechargeId={}", status, rechargeId);
    }

    public void updateRechargeStatusFallback(Long rechargeId, String status, Exception e) {
        log.error("Failed to update recharge status after all retries. rechargeId={}, status={}, error={}",
                rechargeId, status, e.getMessage());
    }

    private void sendPaymentSuccessNotification(Transaction txn) {
        try {
            NotificationEvent event = new NotificationEvent(
                    "Your recharge of Rs." + txn.getAmount() + " was successful! " +
                            "Transaction ID: " + txn.getTransactionId() + ". Your plan is now active.",
                    txn.getUserEmail(),
                    txn.getUserContactNo(),
                    "PAYMENT_SUCCESS"
            );
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, event);
            log.info("Success notification published for transactionId={}, email={}", txn.getTransactionId(), txn.getUserEmail());
        } catch (Exception e) {
            log.error("Failed to publish success notification: {}", e.getMessage());
        }
    }

    private void sendPaymentFailedNotification(Transaction txn) {
        try {
            NotificationEvent event = new NotificationEvent(
                    "Your recharge payment of Rs." + txn.getAmount() + " failed. " +
                            "Please try again. If money was deducted, it will be refunded in 5-7 business days.",
                    txn.getUserEmail(),
                    txn.getUserContactNo(),
                    "PAYMENT_FAILED"
            );
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, event);
            log.info("Failed notification published for transactionId={}", txn.getTransactionId());
        } catch (Exception e) {
            log.error("Failed to publish failed notification: {}", e.getMessage());
        }
    }

    private boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(razorpayKeySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            String computedSignature = hexString.toString();
            boolean match = computedSignature.equals(signature);
            if (!match) log.warn("SIGNATURE MISMATCH — computed: {} | received: {}", computedSignature, signature);
            return match;
        } catch (Exception e) {
            log.error("Exception during signature verification: {}", e.getMessage(), e);
            return false;
        }
    }

    private Long getLoggedInUserId() {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String userId = request.getHeader("X-User-Id");
        if (userId == null || userId.isBlank()) throw new RuntimeException("X-User-Id header is missing");
        return Long.parseLong(userId);
    }

    private String getLoggedInUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }

    private String getLoggedInUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().iterator().next().getAuthority();
    }
}