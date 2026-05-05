
# OmniCharge

Spring Boot microservices project for recharge, payment, auth, notification, and operator plan management.

## Docker

Build and start the full stack:

```bash
docker compose up --build
```

Important notes:

- The config server still pulls from the remote Git repo configured in `OmniCharge-config-server`, so that container needs outbound internet access.
- PostgreSQL databases are created automatically by `docker/postgres/init-multiple-dbs.sh`.
- `RechargeProcessing` has a local Maven dependency on `UserManagement`, so its Docker build uses the repository root as context and installs that module first.
