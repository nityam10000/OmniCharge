# OmniCharge Frontend

Production-grade React + TypeScript frontend for the OmniCharge microservices platform.

## Tech Stack
- **React 19** + **TypeScript** (strict mode)
- **Redux Toolkit** — global state (auth)
- **React Hook Form** + **Zod** — form validation
- **Axios** — API client with auto token refresh interceptor
- **Tailwind CSS v3** — utility-first styling
- **Recharts** — admin analytics charts
- **React Router v6** — lazy-loaded routes with guards
- **React Hot Toast** — notifications

## Quick Start

```bash
npm install
# Set your API Gateway URL:
echo "VITE_API_BASE_URL=http://localhost:8087" > .env
npm run dev
```

## Environment Variables
| Variable | Default | Description |
|---|---|---|
| `VITE_API_BASE_URL` | `http://localhost:8087` | API Gateway base URL |

## Routes
| Path | Access | Description |
|---|---|---|
| `/login` | Public | Password login |
| `/login/otp` | Public | OTP login (send + verify) |
| `/signup` | Public | Register new account |
| `/forgot-password` | Public | Request password reset OTP |
| `/reset-password` | Public | Set new password with OTP |
| `/dashboard` | User | Overview stats + recent recharges |
| `/recharge` | User | Browse operators/plans, pay via Razorpay |
| `/my-recharges` | User | Recharge history |
| `/my-transactions` | User | Transaction records |
| `/profile` | User | View/edit profile |
| `/admin` | Admin | Dashboard with charts |
| `/admin/users` | Admin | Manage users + role assignment |
| `/admin/operators` | Admin | CRUD operators |
| `/admin/plans` | Admin | CRUD plans |
| `/admin/recharges` | Admin | Paginated recharge management |

## Missing Backend Endpoints (add these for full functionality)
| Endpoint | Purpose |
|---|---|
| `POST /auth/logout` | Invalidate refresh token server-side |
| `GET /users/count` | Total user count for admin stats |
| `GET /recharge/count` | Total recharge count for admin stats |
| `GET /recharge/revenue` | Total revenue sum for admin stats |
| `GET /users?search=` | Search users by name/email |
| `POST /operators/{id}/logo` | Upload operator logo image |

## Project Structure
```
src/
├── core/api/          # Axios client + all API service functions
├── features/          # Domain pages (auth, dashboard, recharge, admin, profile)
├── layouts/           # DashboardLayout, AdminLayout, Sidebar
├── routes/            # Router + ProtectedRoute / AdminRoute guards
├── shared/            # Reusable components, typed hooks
├── store/             # Redux store + authSlice
└── types/             # Shared TypeScript interfaces
```
