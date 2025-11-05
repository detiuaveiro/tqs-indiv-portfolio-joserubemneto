# ZeroMonos Logs Directory

This directory contains application logs generated during runtime.

## Log Files

- `application.log` - All application logs
- `application-YYYY-MM-DD.log` - Daily rotated logs
- `error.log` - Error logs only (ERROR level)
- `performance.log` - Method execution times
- `audit.log` - Business operation audit trail

## Retention Policy

- Application logs: 30 days
- Error logs: 90 days
- Performance logs: 7 days
- Audit logs: 365 days

## Notes

These files are automatically generated and rotated by Logback.
Do not manually edit or delete while the application is running.

