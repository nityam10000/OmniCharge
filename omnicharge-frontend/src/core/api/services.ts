import api from './axiosClient';
import type {
  LoginRequest, LoginResponse, OtpRequest, VerifyOtpRequest,
  RefreshTokenRequest, ForgotPasswordRequest, ResetPasswordRequest,
  UserRequest, UserResponse, RoleUpdateDTO,
  OperatorRequest, OperatorResponse,
  PlanRequest, PlanResponse,
  RechargeRequest, RechargeResponse, PageResponse,
  TransactionResponse, RazorpayOrderRequest, RazorpayOrderResponse, PaymentVerifyRequest,
} from '../../types';

// ── AUTH ─────────────────────────────────────────────────────────────────────
export const authApi = {
  login: (data: LoginRequest) => api.post<LoginResponse>('/auth/login', data),
  sendOtp: (data: OtpRequest) => api.post('/auth/send-otp', data),
  verifyOtp: (data: VerifyOtpRequest) => api.post<LoginResponse>('/auth/verify-otp', data),
  refreshToken: (data: RefreshTokenRequest) => api.post<LoginResponse>('/auth/refresh-token', data),
  forgotPassword: (data: ForgotPasswordRequest) => api.post('/auth/forgot-password', data),
  resetPassword: (data: ResetPasswordRequest) => api.post('/auth/reset-password', data),
  logout: () => Promise.resolve(),
};

// ── USERS ─────────────────────────────────────────────────────────────────────
// FIX: Backend UserRequest DTO uses { name, email, contactNo, password }
// ProfilePage was incorrectly sending { firstName, lastName, phone } causing silent failures.
const toUserRequest = (data: {
  firstName?: string; lastName?: string; name?: string;
  email: string; phone?: string; contactNo?: string; password?: string;
}): UserRequest => ({
  name: data.name ?? `${data.firstName ?? ''} ${data.lastName ?? ''}`.trim(),
  email: data.email,
  contactNo: data.contactNo ?? data.phone ?? '',
  password: data.password,
});

export const usersApi = {
  register: (data: UserRequest) => api.post<UserResponse>('/users/register', data),
  getProfile: () => api.get<UserResponse>('/users/profile'),
  updateProfile: (data: Parameters<typeof toUserRequest>[0]) =>
    api.put<UserResponse>('/users/profile/update', toUserRequest(data)),
  getAllUsers: () => api.get<UserResponse[]>('/users'),
  getAll: (page = 0, size = 10) => api.get<PageResponse<UserResponse>>(`/users/paginated?page=${page}&size=${size}`),
  getUserById: (id: number) => api.get<UserResponse>(`/users/${id}`),
  getUserByEmail: (email: string) => api.get<UserResponse>(`/users/email/${email}`),
  deleteUser: (id: number) => api.delete<string>(`/users/${id}`),
  updateRole: (id: number, data: RoleUpdateDTO) => api.put<string>(`/users/${id}/role`, data),
  searchUsers: (search: string) => api.get<UserResponse[]>(`/users?search=${encodeURIComponent(search)}`),
};

// ── OPERATORS ─────────────────────────────────────────────────────────────────
export const operatorsApi = {
  create: (data: OperatorRequest) => api.post<OperatorResponse>('/operators/register', data),
  getAll: () => api.get<OperatorResponse[]>('/operators/getList'),
  getAllPaginated: (page = 0, size = 10) => api.get<PageResponse<OperatorResponse>>(`/operators/getList/paginated?page=${page}&size=${size}`),
  getById: (id: number) => api.get<OperatorResponse>(`/operators/${id}`),
  update: (id: number, data: OperatorRequest) => api.put<OperatorResponse>(`/operators/update/${id}`, data),
  delete: (id: number) => api.delete(`/operators/delete/${id}`),
  uploadLogo: (id: number, file: File) => {
    const form = new FormData();
    form.append('logo', file);
    return api.post(`/operators/${id}/logo`, form, { headers: { 'Content-Type': 'multipart/form-data' } });
  },
};

// ── PLANS ─────────────────────────────────────────────────────────────────────
export const plansApi = {
  create: (data: PlanRequest) => api.post<PlanResponse>('/plans/create', data),
  getAll: () => api.get<PlanResponse[]>('/plans'),
  getAllPaginated: (page = 0, size = 10) => api.get<PageResponse<PlanResponse>>(`/plans/paginated?page=${page}&size=${size}`),
  getById: (id: number) => api.get<PlanResponse>(`/plans/${id}`),
  getByOperator: (operatorId: number) => api.get<PlanResponse[]>(`/plans/operator/${operatorId}`),
  update: (id: number, data: PlanRequest) => api.put<PlanResponse>(`/plans/update/${id}`, data),
  delete: (id: number) => api.delete(`/plans/delete/${id}`),
};

// ── RECHARGE ──────────────────────────────────────────────────────────────────
export const rechargeApi = {
  add: (data: RechargeRequest) => api.post<RechargeResponse>('/recharge/add-recharge', data),
  getById: (id: number) => api.get<RechargeResponse>(`/recharge/${id}`),
  getAll: (page = 0, size = 10) => api.get<PageResponse<RechargeResponse>>(`/recharge?page=${page}&size=${size}`),
  getMyRecharges: (page = 0, size = 10) => api.get<PageResponse<RechargeResponse>>(`/recharge/myrecharges?page=${page}&size=${size}`),
  getByUser: (userId: number) => api.get<RechargeResponse[]>(`/recharge/user/${userId}`),
  getByPlan: (planId: number) => api.get<RechargeResponse[]>(`/recharge/plan/${planId}`),
  delete: (id: number) => api.delete<string>(`/recharge/delete-recharge/${id}`),
  updateStatus: (id: number, status: string) => api.put<string>(`/recharge/${id}/status?status=${status}`),
};

// ── TRANSACTIONS ──────────────────────────────────────────────────────────────
export const transactionApi = {
  createOrder: (data: RazorpayOrderRequest) => api.post<RazorpayOrderResponse>('/transaction/create-order', data),
  verifyPayment: (data: PaymentVerifyRequest) => api.post<TransactionResponse>('/transaction/verify', data),
  getMyTransactions: (page = 0, size = 10) => api.get<PageResponse<TransactionResponse>>(`/transaction/mytransactions?page=${page}&size=${size}`),
  getByUserId: (userId: number) => api.get<TransactionResponse[]>(`/transaction/user/${userId}`),
  getByRechargeId: (rechargeId: number) => api.get<TransactionResponse>(`/transaction/recharge/${rechargeId}`),
};
