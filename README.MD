# Dream Dev Voting System

> A permission-based, state-aware election management API built with Spring Boot and MongoDB.

---

## Table of Contents

1. [Overview](#overview)
2. [Core Design Principles](#core-design-principles)
3. [Domain Model](#domain-model)
4. [ID Formats & Naming Conventions](#id-formats--naming-conventions)
5. [Actors & Roles](#actors--roles)
6. [Permission Reference](#permission-reference)
7. [Application Flow](#application-flow)
8. [CSV Upload Formats](#csv-upload-formats)
9. [Metrics & Queries](#metrics--queries)
10. [Diagram Design Guide](#diagram-design-guide)
11. [Planned Endpoints & Future Work](#planned-endpoints--future-work)
12. [Known Constraints & Future Restrictions](#known-constraints--future-restrictions)
13. [Tech Stack](#tech-stack)

---

## Overview

The Dream Dev Voting System is a Spring Boot REST API backed by MongoDB, designed to manage state-wide and nation-wide elections in Nigeria. The system enforces strict permission-based access, ensures voters can only participate in elections tied to their state of residence, and maintains a fully auditable vote trail.

Every action in the system — uploading elections, approving voters, casting votes — is gated by a permission check. No operation proceeds without the correct permission assigned to the right actor.

---

## Core Design Principles

### 1. Permission-First Architecture

Every sensitive action requires a specific permission. Permissions are assigned to either Electorates (admins) or Voters, and checked before any service logic executes. If the permission is absent, a `PermissionNotFoundException` is thrown immediately.

### 2. One Election = One Race

Each `Election` document represents a single race — one candidate wins. A gubernatorial election in Kogi State is its own entry. A senatorial election in Lagos is a separate entry. They may share the same date but are never grouped or merged.

> **Note:** This is a deliberate departure from the earlier model where an election could represent a collection of votes within a time window.

### 3. Category is Part of Election, Not a Separate Entity

There is no standalone `Category` collection. `Category` is an enum field directly on the `Election` document. This keeps the model lean — an election already describes what type of race it is.

**Available categories:**
- `PRESIDENTIAL`
- `GUBERNATORIAL`
- `SENATORIAL`
- `STATE_REPRESENTATIVE`
- `STATE_ASSEMBLY`
- `LOCAL_COUNSELOR`

### 4. State-Tied Voting

Elections are tied to a `State`. When a voter is approved, they are dynamically granted a state permission (`CAN_VOTE_[STATE]`) based on their state of residence. They can only vote in elections belonging to their state or `NATIONAL` elections.

### 5. Nation-Wide Elections Use `NATIONAL` State

Presidential elections and other country-wide races use `State.NATIONAL`. All approved voters receive `CAN_VOTE_NATIONAL` automatically upon approval alongside their home state permission, ensuring they can always participate in national elections.

### 6. Immutable Vote Trail

The voter ID is SHA-256 hashed before being stored on the `Vote` document. No vote can be directly traced back to a voter, but double-voting is still detectable because the same voter ID always produces the same hash.

### 7. Two-Step Voting

Voting is intentionally a two-step process: **initiate** then **confirm** via a JWT link. This prevents accidental votes and provides an audit window. The JWT expires in five minutes and is single-use.

### 8. Immutable Election Schedule

Elections are immutable after creation. The date, start time, and stop time cannot be changed once uploaded. Voting outside the defined time window is rejected.

---

## Domain Model

### User (Abstract Base Class)

All system participants inherit from `User`.

| Field         | Type              | Description                          |
|---------------|-------------------|--------------------------------------|
| `id`          | `String`          | MongoDB document ID                  |
| `firstName`   | `String`          |                                      |
| `lastName`    | `String`          |                                      |
| `dateOfBirth` | `String`          | Format: `YYYY-MM-DD`                 |
| `citizenship` | `CitizenshipType` | `NATURALIZATION`, `REGISTRATION`, `JUS_SOLI`, `JUS_SANGUINIS`, `DUAL_CITIZENSHIP` |
| `createdAt`   | `LocalDateTime`   | Auto-set on creation                 |
| `permissions` | `List<Permission>`| Assigned permissions                 |

---

### Voter *(extends User)*

| Field               | Type           | Description                                        |
|---------------------|----------------|----------------------------------------------------|
| `voterId`           | `String`       | System-generated (`NIG-[Initials]-[Timestamp]`)    |
| `stateOfResidence`  | `State`        | Nigerian state the voter lives in                  |
| `status`            | `VoterStatus`  | `PENDING` or `APPROVED`                            |
| `statePermissions`  | `List<String>` | Dynamic state-based permissions e.g. `CAN_VOTE_KOGI` |

Only approved voters with `CAN_VOTE` and the correct state permission can vote.

---

### Electorate *(extends User)*

| Field          | Type     | Description      |
|----------------|----------|------------------|
| `electorateId` | `String` | Admin identifier |

Responsible for approving voters and uploading data. Electorates cannot vote.

---

### Candidate *(extends User)*

| Field          | Type     | Description                                            |
|----------------|----------|--------------------------------------------------------|
| `candidateId`  | `String` | Embeds the election ID as prefix (`ELECTGUB2026-CAND-001`) |
| `numberOfVote` | `int`    | Incremented each time a vote for this candidate is confirmed |
| `uploadedBy`   | `String` | Electorate ID of who uploaded this candidate           |

The election a candidate belongs to is inferred from their `candidateId` prefix — there is no separate `electionId` or `categoryId` field on the candidate.

---

### Election

Immutable after creation.

| Field        | Type            | Description                              |
|--------------|-----------------|------------------------------------------|
| `id`         | `String`        | MongoDB document ID                      |
| `electionId` | `String`        | e.g. `ELECTGUB2026`                      |
| `category`   | `Category`      | Enum: `GUBERNATORIAL`, `PRESIDENTIAL`, etc. |
| `state`      | `State`         | Nigerian state or `NATIONAL`             |
| `date`       | `LocalDate`     | Election date                            |
| `startTime`  | `LocalTime`     | Voting opens                             |
| `stopTime`   | `LocalTime`     | Voting closes                            |
| `createdAt`  | `LocalDateTime` | Upload timestamp                         |

---

### Vote

Partially immutable — only `voteStatus` and `jwtToken` can change after creation.

| Field           | Type            | Description                                     |
|-----------------|-----------------|-------------------------------------------------|
| `id`            | `String`        | MongoDB document ID                             |
| `electionId`    | `String`        | Links to the election                           |
| `candidateId`   | `String`        | Who the vote is cast for                        |
| `hashedVoterId` | `String`        | SHA-256 hash of voter ID (anonymity)            |
| `voteStatus`    | `VoteStatus`    | `DEFAULTED` (initiated) → `VOTED` (confirmed)  |
| `jwtToken`      | `String`        | Confirmation token (expires in 5 minutes)       |
| `createdAt`     | `LocalDateTime` | When the vote was initiated                     |
| `updatedAt`     | `LocalDateTime` | When the vote was confirmed                     |

> There is no direct relationship between `Vote` and `Voter`. The `hashedVoterId` is used solely to detect double-voting while preserving anonymity.

---

## ID Formats & Naming Conventions

All IDs follow strict patterns validated before processing. Non-conforming IDs are rejected.

| Entity        | Format                         | Example                 |
|---------------|--------------------------------|-------------------------|
| Election ID   | `[TYPE][YEAR]`                 | `ELECTGUB2026`          |
| Candidate ID  | `[ElectionId]-CAND-[NNN]`      | `ELECTGUB2026-CAND-005` |
| Voter ID      | `NIG-[Initials]-[Timestamp]`   | `NIG-JD-060420261200`   |
| Electorate ID | `ELECTORATE-[NNN]`             | `ELECTORATE-001`        |

> **Important:** The Candidate ID embeds the Election ID as its prefix. The system extracts the election ID from the candidate ID automatically during voting to look up and validate the election. There is no Category ID — category is an enum field on the Election.

---

## Actors & Roles

### Electorate (Admin)

Electorates are the administrators of the system. They upload elections and candidates via CSV. They approve voters and can assign or remove permissions from other Electorates.

> An Electorate cannot grant itself vote-related permissions — this restriction is planned for a future release.

### Voter

Voters self-register via the API. They begin with `PENDING` status and no permissions. An Electorate with `CAN_APPROVE_VOTER` must approve them. Upon approval they receive `CAN_VOTE`, their state permission (`CAN_VOTE_[STATE]`), and `CAN_VOTE_NATIONAL`.

### Candidate

Candidates are data entities uploaded by an Electorate via CSV. They do not interact with the system directly. Their `numberOfVote` is incremented automatically when a voter confirms a vote for them.

---

## Permission Reference

| Permission              | What It Allows                                       | Assigned To |
|-------------------------|------------------------------------------------------|-------------|
| `CAN_VOTE`              | Initiate and confirm a vote                          | Voter       |
| `CAN_VOTE_[STATE]`      | Vote in elections for a specific state (dynamic)     | Voter       |
| `CAN_UPLOAD_FILE`       | Upload elections and candidates via CSV              | Electorate  |
| `CAN_APPROVE_VOTER`     | Approve a registered voter                           | Electorate  |
| `CAN_VIEW_METRICS`      | View vote counts and analytics                       | Electorate  |
| `CAN_VIEW_ELECTORATE`   | View electorate records                              | Electorate  |
| `CAN_VIEW_VOTE`         | View all votes                                       | Electorate  |
| `CAN_UPDATE_ELECTORATE` | Add or remove permissions from other Electorates     | Electorate  |

> `CAN_VOTE_[STATE]` is a dynamic string permission stored in a separate `statePermissions` list on the `Voter`. It is generated at runtime via `Permission.forState(state)` and is not a fixed enum value.

---

## Application Flow

### Setup Flow (Admin)

The following upload order must be respected when seeding the system:

1. **Upload Elections CSV** — Each row represents a single election race with its category, state, date and time window.
2. **Upload Candidates CSV** — Each candidate's ID must start with a valid election ID already in the system.

> Uploading candidates before elections won't cause an immediate failure, but voting will fail later because the election ID extracted from the candidate ID won't match any stored election.

---

### Voter Registration & Approval Flow

1. Voter registers via `POST /api/v1/voters/register` with personal details and state of residence.
2. Voter receives a `voterId` (format: `NIG-[Initials]-[Timestamp]`) and status `PENDING`.
3. An Electorate with `CAN_APPROVE_VOTER` calls the approve endpoint with the `voterId`.
4. Voter is granted `CAN_VOTE`, `CAN_VOTE_[THEIR_STATE]`, and `CAN_VOTE_NATIONAL`. Status becomes `APPROVED`.

---

### Voting Flow

1. **Initiate Vote** — Voter calls `POST /api/v1/voters/initiate_vote` with their `voterId` and the `candidateId`.
2. System extracts the `electionId` from the `candidateId` prefix and validates the election is active today within its start/stop time window.
3. System checks the voter has `CAN_VOTE` and the correct state permission for the election's state.
4. System checks the voter has not already voted in this election (via hashed voter ID).
5. A `Vote` document is created with status `DEFAULTED`. A JWT confirmation link is returned.
6. **Confirm Vote** — Voter follows the JWT link. System validates the token, marks the vote as `VOTED`, and increments the candidate's `numberOfVote`.

---

## CSV Upload Formats

All CSV uploads validate headers before processing. Uploading the wrong file to the wrong endpoint is rejected with `400 Bad Request`. Duplicate records (same ID) are skipped — the response shows how many were saved vs skipped.

### Elections CSV

```csv
electionId,category,state,date,startTime,stopTime
ELECTGUB2026,GUBERNATORIAL,KOGI,2026-04-06,00:00,23:55
ELECTPRES2026,PRESIDENTIAL,NATIONAL,2026-04-15,08:00,18:00
ELECTSEN2026,SENATORIAL,LAGOS,2026-04-20,09:00,17:00
```

### Candidates CSV

```csv
candidateId,lastName,firstName,dateOfBirth,citizenship
ELECTGUB2026-CAND-001,Okafor,Chinedu,1995-04-12,NATURALIZATION
ELECTGUB2026-CAND-002,Adebayo,Tolu,1998-09-23,REGISTRATION
```

---

## Metrics & Queries

Planned query methods for the analytics layer:

```java
// Total votes cast across all elections
int getTotalVotes();

// Total votes cast for a specific candidate
int getVotesByCandidate(String candidateId);

// Votes per candidate for a given election
Map<Candidate, Integer> getVotesPerCandidateByElection(String electionId);

// Political apathy rate
double getApathyRate(); // (registered - voted) / registered

// Total registered voters
long getTotalRegisteredVoters();

// Total voters who have confirmed a vote
long getTotalVotersTurnout();
```

---

## Diagram Design Guide

### Use Case Diagram

| Actor       | Use Cases                                      |
|-------------|------------------------------------------------|
| Electorate  | Upload CSV, Approve Voter, Assign Permissions  |
| Voter       | Register, Initiate Vote, Confirm Vote          |
| All         | View Results                                   |

---

### Class Diagram

Include the following classes:

- `User` (abstract base)
- `Voter`, `Electorate`, `Candidate` — all extend `User`
- `Vote`
- `Election`

**Relationships:**

| From         | To          | Type        | Note                                    |
|--------------|-------------|-------------|-----------------------------------------|
| `Voter`      | `User`      | Inheritance |                                         |
| `Electorate` | `User`      | Inheritance |                                         |
| `Candidate`  | `User`      | Inheritance |                                         |
| `Candidate`  | `Election`  | Many-to-One | Inferred via `candidateId` prefix       |
| `Vote`       | `Candidate` | Many-to-One |                                         |
| `Voter`      | `Electorate`| Many-to-One | `approvedBy` reference                  |

> Do not draw a direct relationship between `Vote` and `Voter`. Use `hashedVoterId` on `Vote` to represent the anonymised link. There is no `Category` entity — it is an enum on `Election`.

---

## Planned Endpoints & Future Work

- `GET /api/v1/voters/{voterId}` — Individual voter details
- `GET /api/v1/electorates/{electorateId}` — Individual electorate details
- `PUT /api/v1/voters/{voterId}` — Update voter record
- `PUT /api/v1/electorates/{electorateId}` — Update electorate record
- `GET /api/v1/elections?state={state}` — Elections by state
- `GET /api/v1/elections?state={state}&date={date}` — Elections by state and date
- `GET /api/v1/metrics` — Full analytics dashboard

---

## Known Constraints & Future Restrictions

- **Electorate self-permission** — An Electorate can currently assign itself any permission. A future restriction should prevent Electorates from granting themselves vote-related permissions.
- **Dynamic state permissions are strings** — `CAN_VOTE_[STATE]` is stored as a plain string. If the `State` enum changes, existing permissions will not automatically migrate.
- **No JWT revocation** — Expired vote confirmation tokens are rejected but there is no re-issue flow for a voter who missed their window.
- **No update endpoints yet** — Voter and Electorate records cannot be updated after creation.
- **Candidate voting** — Candidates cannot vote unless they are also separately registered as Voters.

---

## Tech Stack

| Layer       | Technology                     |
|-------------|--------------------------------|
| Language    | Java 22                        |
| Framework   | Spring Boot 3.4.x              |
| Database    | MongoDB (Spring Data)          |
| CSV Parsing | OpenCSV                        |
| Auth Tokens | Custom JwtService              |
| API Docs    | Springdoc OpenAPI (Swagger UI) |
| Validation  | Hibernate Validator            |
| Testing     | JUnit 5 + Spring Boot Test     |

---

> *Dream Dev — Built with intention.*