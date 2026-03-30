# Voting System - Backend Design Documentation

## Overview

This voting system is designed to manage elections, candidates, and voters with strict validation rules and role-based permissions. It ensures secure, time-bound voting and maintains integrity through controlled access and partially immutable entities.

---

## Core Domain Model

### User (Base Class)

All system participants inherit from the `User` class.

**Fields:**

* `id`
* `firstName`
* `lastName`
* `DOB`
* `stateOfOrigin`
* `citizenship`
* `createdAt`
* `permissionList`

---

### Subclasses of User

#### Voter

* `voterId`
* `approvedBy` (Electorate)
* `updatedAt`
* `status` → `PENDING`, `APPROVED`

Only approved voters with `can_vote` permission can vote.

---

#### Electorate

* `electorateId`
* `updatedAt`

Responsible for approving voters.
Electorate cannot vote.

---

#### Candidate

* `categoryId`
* `uploadedBy` (Admin)
* `electionId`

Candidates must belong to a valid election and category.

---

## Supporting Entities

### Election

(Replaces `ElectionSchedule`)

* `id`
* `electionId`
* `date`
* `startTime`
* `endTime`

Immutable after creation.
Used to validate voting period.

---

### Category

* `id`
* `type`
* `categoryId`
* `createdAt`

Uploaded via CSV by Admin.

---

### Vote

A partially immutable entity.

**Fields:**

* `id`
* `hashedVoterId`
* `candidatID` 
* `createdAt`
* `status` → `DEFAULTED`, `VOTED`
* `numberOfVotes`
* `updatedAt`
* `jwt` (expires in five minutes)

Only `status` can change after creation.

---

## Permissions and Roles

| Role       | Permissions     |
|------------| --------------- |
| Electorate | `can_upload`    |
| Electorate | `approve_voter` |
| Voter      | `can_vote`      |
| Candidate  | None by default |

---

## System Flow

### 1. Voter Registration

Users register as voters.
Initial status is `PENDING`.

### 2. Approval Process

Electorate approves voters.
Status becomes `APPROVED`, and permission `can_vote` is assigned.

---

### 3. Data Upload (Admin Only)

Admin uploads:

* Election data
* Candidate list
* Categories

Uploads are done via CSV files.

---

### 4. Voting Process

Only users with a valid `voterId` and `can_vote` permission can vote.

The system validates:

* Election date
* Start and end time
* Candidate belongs to the active election

---

### Restrictions

* Electorate cannot vote
* Candidates cannot vote unless they are also registered as voters
* Voting outside the election window is not allowed

---

## ERD Structure

### Relationships

* `User` (1) → (1) `Voter`

* `User` (1) → (1) `Electorate`

* `User` (1) → (1) `Candidate`

* `Candidate` (Many) → (1) `Category`

* `Candidate` (Many) → (1) `Election`

* `Vote` (Many) → (1) `Candidate`

* `Voter` (Many) → (1) `Electorate` (approvedBy)

---

### ERD Notes

* Use inheritance (generalization) for `User` and its subclasses
* Use Many-to-One relationships:

    * Candidate to Category
    * Candidate to Election
    * Vote to Candidate
* Do not create a direct relationship between Vote and Voter; use `hashedVoterId` for anonymity

---

## Metrics and Queries

### Total Votes

```java
int getTotalVotes();
```

### Total Votes by Category

```java
int getVotesByCategory(categoryId);
```

### Fetch All Categories

```java
List<Category> getAllCategories();
```

### Votes per Candidate per Category

```java
Map<Candidate, Integer> getVotesPerCandidateByCategory(categoryId);
```

---

## Diagram Design Guide

### Use Case Diagram

**Actors:**

* Admin
* Electorate
* Voter

**Use Cases:**

* Register Voter
* Approve Voter
* Upload CSV
* Vote
* View Results

---

### Class Diagram

Include:

* `User` (abstract)
* `Voter`, `Electorate`, `Candidate`
* `Vote`
* `Election`
* `Category`

**Relationships:**

* Inheritance (User to subclasses)
* Associations (Vote to Candidate, Candidate to Election, etc.)

---

## Key Design Decisions

* Vote anonymity is maintained using `hashedVoterId`
* Election schedule is immutable
* Role-based permission system is enforced
* Data initialization is handled via CSV uploads
* Voting is restricted to defined time windows
* Responsibilities are clearly separated across roles

---

## Conclusion

This design provides a secure, scalable, and structured approach to building a voting system. It enforces strong domain boundaries, ensures data integrity, and supports extensibility for future enhancements.

You can now proceed to implement the ERD, use case diagram, and class diagram using tools such as draw.io.
