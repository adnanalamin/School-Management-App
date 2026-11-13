# EduManage — Multi-School SaaS Mobile Platform

**EduManage** is a multi-tenant, enterprise-grade School Management SaaS mobile application built for Android smartphones and tablets using **Kotlin**, **Jetpack Compose**, **Material 3**, and **Firebase**.

---

## 🏛 Multi-Tenant Architecture & Data Isolation

EduManage is designed from the ground up for strict multi-school tenant isolation:
- **Zero Cross-Tenant Leaks:** Every database record, sub-collection, query, and session is bound to an authenticated `schoolId`.
- **Tenant Context Barrier (`TenantContext`):** All repositories and services assert an active verified `schoolId` before reading or writing data.
- **Hierarchical Sub-Collections:**
  ```text
  schools/{schoolId}
    ├── students/
    ├── teachers/
    ├── parents/
    ├── classes/
    ├── sections/
    ├── attendance/
    ├── fees/
    ├── exams/
    ├── results/
    ├── homework/
    ├── notices/
    ├── library/
    ├── transport/
    ├── events/
    ├── documents/
    └── messages/
  ```

---

## 👥 Role-Based Access Control (RBAC)

EduManage implements 9 distinct system roles with 28 granular permission flags:

1. **SUPER_ADMIN** (Level 100): Global SaaS oversight and school provisioning.
2. **SCHOOL_ADMIN** (Level 80): Full operational management within their school tenant.
3. **TEACHER** (Level 60): Class assignments, attendance marking, homework, results entry.
4. **ACCOUNTANT** (Level 50): Fee invoices, payments collection, receipts, and finance reports.
5. **LIBRARIAN** (Level 50): Book cataloging, issue and return logs, fines.
6. **TRANSPORT_MANAGER** (Level 50): Route definitions, bus allocation, vehicle stops.
7. **STAFF** (Level 40): General notices, staff attendance, communication.
8. **PARENT** (Level 20): Linked strictly to their children's records (attendance, fees, grades).
9. **STUDENT** (Level 10): Timetable, assigned homework submissions, personal grades, and notices.

---

## 📁 Project Structure (Feature-First Architecture)

```text
app/src/main/java/com/example/
├── core/
│   ├── config/          # Environment profiles (Dev, Staging, Prod) & SaaS config
│   ├── constants/       # Collection names, tenant headers, shared keys
│   ├── error/           # Sealed domain errors (AppError) & centralized sanitization
│   ├── navigation/      # AppScreen routes & RouteGuard evaluation
│   ├── security/        # UserRole enum, Permission flags & TenantContext barrier
│   ├── theme/           # Material 3 Color palette, Typography, Shapes & EduManageTheme
│   └── utils/           # ValidationUtils, DateTimeUtils
├── features/
│   └── foundation/      # Phase 1 Foundation Hub (Multi-Tenant, RBAC & Design tokens)
├── shared/
│   ├── components/      # Reusable M3 widgets (EduButton, EduTextField, EduCard, EduBadge)
│   └── models/          # Multi-tenant data models (TenantSchool, UserProfile)
└── services/
    └── storage/         # PreferencesService (DataStore) for persistent session state
```

---

## 🚀 Master Development Roadmap (30 Phases)

- [x] **Phase 1: Project Foundation & Architecture** (COMPLETED)
- [x] **Phase 2: Design System & Shared Component Library** (COMPLETED)
- [x] **Phase 3: App Navigation & Route Guards** (COMPLETED)
- [x] **Phase 4: Authentication & Session Persistence** (COMPLETED)
- [x] **Phase 5: School Registration & Onboarding** (COMPLETED)
- [x] **Phase 6: Multi-Tenant Architecture & Firestore Schema** (COMPLETED)
- [x] **Phase 7: Role & Granular Permission Enforcement** (COMPLETED)
- [x] **Phase 8: School Admin Dashboard & Analytics** (COMPLETED)
- [x] **Phase 9: Complete Student Lifecycle Management** (COMPLETED)
- [x] **Phase 10: Parent Portal & Multi-Child Linking** (COMPLETED)
- [x] **Phase 11: Teacher & Faculty Management** (COMPLETED)
- [x] **Phase 12: Class & Section Management** (COMPLETED)
- [x] **Phase 13: Examination Scheduling & Timetable System** (COMPLETED)
- [x] **Phase 14: Academic Grade Sheets & GPA Grading Engine** (COMPLETED)
- [x] **Phase 15: Circulars, Announcements & Noticeboard Broadcast System** (COMPLETED)
- [x] **Phase 16: Homework & Assignments Pipeline** (COMPLETED)
- [x] **Phase 17: Library Book Inventory & Lending System** (COMPLETED)
- [x] **Phase 18: School Transport & Route Fleet Management** (COMPLETED)
- [x] **Phase 19: School Events & Academic Calendar** (COMPLETED)
- [x] **Phase 20: Institutional Documents, Syllabi, ID Cards & Certificates** (COMPLETED)
- [x] **Phase 21: Leave Request & Approval Workflow** (COMPLETED)
- [x] **Phase 22: Secure Internal Messaging System** (COMPLETED)
- [x] **Phase 23: Executive Reports & Data Analytics** (COMPLETED)
- [ ] **Phase 24: School Customization & Branding Settings**
- [ ] **Phase 25: Security Audit, Testing & Performance Tuning**
- [ ] **Phase 26: Production Release & SaaS Distribution Preparation**
