# EcoPickup Backend

This folder contains the Spring Boot API, database model and backend configuration for EcoPickup. The separate `../frontend` folder is copied into the application during the Maven build, so Spring Boot serves both modules together.

## Run locally

Requirements: Java 17 or newer. Maven does not need to be installed because the Maven Wrapper is included.

```powershell
.\mvnw.cmd spring-boot:run
```

Then open `http://localhost:8080`.

Demo accounts use the password `password`:

- Seller: `aarav@example.com`
- Buyer/recycler: `buyer@greenloop.in`

The local H2 database is stored under `data/`. Its console is available at `http://localhost:8080/h2-console` with JDBC URL `jdbc:h2:file:./data/ecopickup`, user `sa`, and an empty password.

## API overview

- `POST /api/auth/signup` and `POST /api/auth/login`
- `GET/POST /api/items`
- `GET /api/items/{id}`
- `POST /api/requests`
- `PATCH /api/requests/{id}/status`
- `POST /api/pickups`
- `GET /api/pickups/{pickupId}`
- `PATCH /api/pickups/{pickupId}/status`
- `GET/PUT /api/rewards/pickup/{pickupId}`
- `GET /api/dashboard/admin`

## MySQL profile

Set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`, then run:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=mysql"
```

The default profile uses H2 so the college demonstration works without installing a database server.

## Project folders

```text
E-waste website/
├── frontend/   HTML, CSS, JavaScript and images
└── backend/    Spring Boot, APIs, JPA entities and database configuration
```

Always run Maven commands from the `backend` folder.
