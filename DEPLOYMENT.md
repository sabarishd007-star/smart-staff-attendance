# Docker deployment

1. Copy `.env.example` to `.env` and replace all placeholder secrets.
2. Start the stack with `docker compose up --build -d`.
3. Open `http://localhost`.

The database is intentionally not published to the host. The API is available to
the Nginx frontend only; Nginx proxies `/api/` to the backend container.

On the first database initialization only, MySQL executes `schema.sql` followed
by the database-trigger audit script. To apply those scripts to an existing
database volume, run the SQL manually in MySQL; initialization scripts are not
re-run for an existing named volume.
