# EcoPickup ♻️

EcoPickup is a beginner-friendly marketplace and doorstep pickup scheduler for old, reusable, and electronic-waste items.

A user can use the same account in two modes:

- **Selling mode** — list old items, manage listings, review buyer requests, and schedule pickups.
- **Buying mode** — browse the marketplace, request items from other sellers, and track accepted pickups.

The current project is designed as a college micro-project, but its frontend and backend are organized separately so another developer can extend it without redesigning everything.

## Main features

- Signup and login
- One account with Buying and Selling modes
- Marketplace search, categories, conditions, and sorting
- Create and view item listings
- Send, accept, and reject item requests
- Schedule a free doorstep pickup
- Pickup confirmation and status timeline
- Expected and final reward/payment model
- Basic admin dashboard statistics
- H2 database for easy local development
- Optional MySQL configuration
- Responsive HTML and CSS interface

## Technology used

### Frontend

- HTML5
- CSS3
- Vanilla JavaScript
- Browser `fetch()` API

### Backend

- Java 17
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA / Hibernate
- Maven Wrapper
- H2 database by default
- Optional MySQL support

React, Node.js, and npm are not required.

## Project structure

```text
EcoPickup/
├── frontend/
│   ├── index.html
│   ├── login.html
│   ├── signup.html
│   ├── marketplace.html
│   ├── item-details.html
│   ├── seller-dashboard.html
│   ├── buyer-dashboard.html
│   ├── add-item.html
│   ├── my-listings.html
│   ├── requests.html
│   ├── schedule-pickup.html
│   ├── confirmation.html
│   ├── track-pickup.html
│   ├── admin-dashboard.html
│   ├── style.css
│   └── script.js
├── backend/
│   ├── src/main/java/com/ecopickup/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── exception/
│   │   ├── model/
│   │   └── repository/
│   ├── src/main/resources/
│   ├── pom.xml
│   ├── mvnw
│   └── mvnw.cmd
├── .gitignore
└── README.md
```

## Requirements

Install the following before running the project:

- **Java Development Kit (JDK) 17 or newer**
- **Git** to clone the repository
- Any IDE or editor, such as VS Code, IntelliJ IDEA, or Eclipse

You do not need to install Maven separately because the Maven Wrapper is included.

Check Java installation:

```bash
java -version
```

## Download the project

### Option 1: Clone with Git

```bash
git clone https://github.com/kekius-maximus45/EcoPickup.git
cd EcoPickup/backend
```

### Option 2: Download ZIP

1. Open the GitHub repository.
2. Select **Code → Download ZIP**.
3. Extract the ZIP file.
4. Open the extracted folder in VS Code or another IDE.
5. Open a terminal in the `backend` folder.

## Run the application

### Windows PowerShell or Command Prompt

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

### macOS or Linux

```bash
cd backend
chmod +x mvnw
./mvnw spring-boot:run
```

Wait until the terminal displays a message similar to:

```text
Tomcat started on port 8080
Started EcoPickupApplication
```

Then open:

```text
http://localhost:8080
```

Do not open the HTML files directly for normal use. Spring Boot serves the frontend and provides the APIs needed by the forms and dashboards.

Stop the application with `Ctrl + C` in the terminal.

## Demo accounts

The database is automatically created and filled with sample items the first time the application starts.

| Account | Email | Password |
|---|---|---|
| Demo user / seller | `aarav@example.com` | `password` |
| Demo recycler / buyer | `buyer@greenloop.in` | `password` |

Normal accounts can switch between Buying and Selling modes. The names above describe their initial demo purpose only.

You can also create a new account from the Signup page.

## How the frontend connects to the backend

The project does not need React to communicate with Spring Boot.

The frontend uses JavaScript's `fetch()` function to call URLs such as:

```javascript
fetch('/api/items')
```

During a Maven build or run, the backend copies the files from `frontend/` into Spring Boot's static resources. Therefore, both parts are served from the same address:

```text
Frontend: http://localhost:8080
API:      http://localhost:8080/api/...
```

Because they use the same host and port, a separate frontend development server and CORS configuration are not required.

## Basic workflow

```text
User creates or logs into one account
                ↓
        Chooses Buying or Selling mode
                ↓
Seller publishes an item in the marketplace
                ↓
Buyer sends an interest request
                ↓
Seller accepts or rejects the request
                ↓
Accepted request receives a pickup schedule
                ↓
Admin assigns a collector (future phase)
                ↓
Item is collected, inspected, and processed
                ↓
Eligible reward/payment is completed
```

A user cannot send a buying request for their own listing.

## Database

### Default H2 database

No database installation is needed for the default setup. H2 creates a local database automatically in:

```text
backend/data/
```

This folder is ignored by Git, so each developer receives a fresh local database.

H2 console:

```text
http://localhost:8080/h2-console
```

Connection details:

```text
JDBC URL: jdbc:h2:file:./data/ecopickup
User:     sa
Password: leave empty
```

### Optional MySQL profile

Create or allow MySQL to create a database named `ecopickup`, then provide these environment variables:

```text
DB_URL=jdbc:mysql://localhost:3306/ecopickup?createDatabaseIfNotExist=true
DB_USERNAME=root
DB_PASSWORD=your_password
```

Run the MySQL profile on Windows:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=mysql"
```

Run it on macOS or Linux:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
```

## Useful API endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/auth/signup` | Create an account |
| POST | `/api/auth/login` | Log in |
| GET | `/api/items` | Browse listings |
| POST | `/api/items` | Publish a listing |
| GET | `/api/items/{id}` | View item details |
| POST | `/api/requests` | Send an item request |
| PATCH | `/api/requests/{id}/status` | Accept or reject a request |
| POST | `/api/pickups` | Schedule a pickup |
| GET | `/api/pickups/{id}` | View pickup details |
| PATCH | `/api/pickups/{id}/status` | Update pickup progress |
| GET/PUT | `/api/rewards/pickup/{id}` | View or update a reward |
| GET | `/api/dashboard/admin` | View dashboard statistics |

## Run tests

From the `backend` folder:

### Windows

```powershell
.\mvnw.cmd test
```

### macOS or Linux

```bash
./mvnw test
```

## Opening in an IDE

### VS Code

1. Open the repository folder.
2. Install the **Extension Pack for Java** if VS Code suggests it.
3. Open `backend/src/main/java/com/ecopickup/EcoPickupApplication.java`.
4. Use the Run button, or run the Maven Wrapper command in the terminal.

### IntelliJ IDEA

1. Select **Open** and choose the repository folder or `backend/pom.xml`.
2. Allow Maven dependencies to load.
3. Run `EcoPickupApplication.java`.

### Eclipse or Spring Tool Suite

1. Import `backend` as an **Existing Maven Project**.
2. Select `backend/pom.xml`.
3. Run `EcoPickupApplication.java` as a Java or Spring Boot application.

## Troubleshooting

### Port 8080 is already in use

Stop the other application using port 8080, or change this line in `backend/src/main/resources/application.properties`:

```properties
server.port=8081
```

Then open `http://localhost:8081`.

### The page shows old JavaScript or CSS

Restart Spring Boot and refresh the browser with `Ctrl + F5`.

### Java version error

Install JDK 17 or newer and ensure `java -version` shows the correct version.

### Reset the demo database

Stop the application and delete the local `backend/data` folder. The application will create a fresh database with demo data the next time it starts.

## Current scope and future improvements

The current version implements the main marketplace, request, scheduling, and tracking demonstration. Future work can include:

- Spring Security and token/session-based authentication
- Administrator and collector login flows
- Collector assignment and status controls
- Image upload and file storage
- Email, SMS, and push notifications
- UPI or bank-transfer reward processing
- Pickup OTP verification
- Listing moderation
- Reports and analytics
- Automated API and browser tests

## Contributing

1. Fork or clone the repository.
2. Create a new branch.
3. Make and test your changes.
4. Commit with a clear message.
5. Open a pull request.

Please do not commit the `backend/target` or `backend/data` folders.
