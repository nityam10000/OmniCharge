import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import type { PayloadAction } from '@reduxjs/toolkit';
import { authApi, usersApi } from '../../core/api/services';
import type { AuthState, LoginRequest, OtpRequest, VerifyOtpRequest } from '../../types';

const getErrMsg = (err: unknown, fallback: string): string => {
  const e = err as { response?: { data?: { message?: string } } };
  return e.response?.data?.message || fallback;
};

const initialState: AuthState = {
  user: null,
  accessToken: localStorage.getItem('accessToken'),
  refreshToken: localStorage.getItem('refreshToken'),
  isAuthenticated: !!localStorage.getItem('accessToken'),
  loading: false,
  error: null,
};

export const loginThunk = createAsyncThunk('auth/login', async (creds: LoginRequest, { rejectWithValue }) => {
  try {
    const { data } = await authApi.login(creds);
    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('refreshToken', data.refreshToken);
    const profileRes = await usersApi.getProfile();
    return { ...data, user: profileRes.data };
  } catch (err) {
    return rejectWithValue(getErrMsg(err, 'Login failed'));
  }
});

export const verifyOtpThunk = createAsyncThunk('auth/verifyOtp', async (req: VerifyOtpRequest, { rejectWithValue }) => {
  try {
    const { data } = await authApi.verifyOtp(req);
    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('refreshToken', data.refreshToken);
    const profileRes = await usersApi.getProfile();
    return { ...data, user: profileRes.data };
  } catch (err) {
    return rejectWithValue(getErrMsg(err, 'OTP verification failed'));
  }
});

export const sendOtpThunk = createAsyncThunk('auth/sendOtp', async (req: OtpRequest, { rejectWithValue }) => {
  try {
    await authApi.sendOtp(req);
    return req.email;
  } catch (err) {
    return rejectWithValue(getErrMsg(err, 'Failed to send OTP'));
  }
});

export const fetchProfileThunk = createAsyncThunk('auth/fetchProfile', async (_, { rejectWithValue }) => {
  try {
    const { data } = await usersApi.getProfile();
    return data;
  } catch (err) {
    return rejectWithValue(getErrMsg(err, 'Failed to fetch profile'));
  }
});

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    logout(state) {
      state.user = null;
      state.accessToken = null;
      state.refreshToken = null;
      state.isAuthenticated = false;
      state.error = null;
      localStorage.clear();
    },
    clearError(state) { state.error = null; },
    setTokens(state, action: PayloadAction<{ accessToken: string; refreshToken: string }>) {
      state.accessToken = action.payload.accessToken;
      state.refreshToken = action.payload.refreshToken;
      localStorage.setItem('accessToken', action.payload.accessToken);
      localStorage.setItem('refreshToken', action.payload.refreshToken);
    },
  },
  extraReducers: (builder) => {
    const setPending = (state: AuthState) => { state.loading = true; state.error = null; };
    const setRejected = (state: AuthState, action: PayloadAction<unknown>) => {
      state.loading = false;
      state.error = action.payload as string;
    };

    builder
      .addCase(loginThunk.pending, setPending)
      .addCase(loginThunk.fulfilled, (state, action) => {
        state.loading = false;
        state.isAuthenticated = true;
        state.accessToken = action.payload.accessToken;
        state.refreshToken = action.payload.refreshToken;
        state.user = action.payload.user;
      })
      .addCase(loginThunk.rejected, setRejected)

      .addCase(verifyOtpThunk.pending, setPending)
      .addCase(verifyOtpThunk.fulfilled, (state, action) => {
        state.loading = false;
        state.isAuthenticated = true;
        state.accessToken = action.payload.accessToken;
        state.refreshToken = action.payload.refreshToken;
        state.user = action.payload.user;
      })
      .addCase(verifyOtpThunk.rejected, setRejected)

      .addCase(sendOtpThunk.pending, setPending)
      .addCase(sendOtpThunk.fulfilled, (state) => { state.loading = false; })
      .addCase(sendOtpThunk.rejected, setRejected)

      // FIX: fetchProfileThunk also sets loading to false and handles rejection
      .addCase(fetchProfileThunk.pending, (state) => { state.loading = true; })
      .addCase(fetchProfileThunk.fulfilled, (state, action) => {
        state.loading = false;
        state.user = action.payload;
        state.isAuthenticated = true;
      })
      .addCase(fetchProfileThunk.rejected, (state) => {
        // Profile fetch failed — token probably expired; clear auth
        state.loading = false;
        state.isAuthenticated = false;
        state.user = null;
        state.accessToken = null;
        state.refreshToken = null;
        localStorage.clear();
      });
  },
});

export const { logout, clearError, setTokens } = authSlice.actions;
export default authSlice.reducer;
