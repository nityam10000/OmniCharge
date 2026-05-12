// ─── Auth ───────────────────────────────────────────────────────────────────
export interface LoginRequest { email: string; password: string; }
export interface LoginResponse { accessToken: string; refreshToken: string; }
export interface RegisterRequest { firstName: string; lastName: string; email: string; password: string; phone: string; }
export interface OtpRequest { email: string; }
export interface VerifyOtpRequest { email: string; otp: string; }
export interface RefreshTokenRequest { refreshToken: string; }
export interface ForgotPasswordRequest { email: string; }
export interface ResetPasswordRequest { email: string; otp: string; newPassword: string; }

// ─── User ────────────────────────────────────────────────────────────────────
export type Role = 'USER' | 'ADMIN';
export interface UserResponse {
  userId: number;
  name: string;
  email: string;
  contactNo: string;
  role: Role;
  createdAt?: string;
}
export interface UserRequest {
  name: string;
  email: string;
  contactNo: string;
  password?: string;
}
export interface RoleUpdateDTO { role: Role; }

// ─── Operator ────────────────────────────────────────────────────────────────
export interface OperatorRequest { name: string; description?: string; logoUrl?: string; }
export interface OperatorResponse { id: number; name: string; description?: string; logoUrl?: string; planCount?: number; }

// ─── Plan ────────────────────────────────────────────────────────────────────
export interface PlanRequest { planName: string; amount: number; validity: string; description?: string; operatorId: number; }
export interface PlanResponse { id: number; planName: string; amount: number; validity: string; description?: string; operatorId: number; operatorName?: string; }

// ─── Recharge ────────────────────────────────────────────────────────────────
export type RechargeStatus = 'PENDING' | 'SUCCESS' | 'FAILED' | 'REFUNDED';
export type PaymentMethod = 'UPI' | 'CARD' | 'NETBANKING';
export interface RechargeRequest { operatorId: number; planId: number; paymentMethod: PaymentMethod; }
export interface RechargeResponse {
  rechargeId: number; userId: number; planId: number; status: RechargeStatus; amount: number; transactionStatus?: string; createdAt: string;
}
export interface PageResponse<T> { content: T[]; totalElements: number; totalPages: number; number: number; size: number; }

// ─── Transaction / Payment ───────────────────────────────────────────────────
export type TxnStatus = 'CREATED' | 'CAPTURED' | 'FAILED' | 'REFUNDED';
export interface TransactionResponse {
  id: number; rechargeId: number; userId: number; amount: number;
  paymentMethod: PaymentMethod; status: TxnStatus;
  razorpayOrderId: string; razorpayPaymentId?: string;
  createdAt: string;
}
export interface RazorpayOrderRequest { rechargeId: number; paymentMethod: PaymentMethod; }
export interface RazorpayOrderResponse { keyId: string; razorpayOrderId: string; amount: number; currency: string; }
export interface PaymentVerifyRequest {
  razorpayOrderId: string; razorpayPaymentId: string; razorpaySignature: string; rechargeId: number;
}

// ─── Redux Auth State ────────────────────────────────────────────────────────
export interface AuthState {
  user: UserResponse | null;
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  loading: boolean;
  error: string | null;
}

// ─── Generic API error ───────────────────────────────────────────────────────
export interface ApiError { message: string; status?: number; }
