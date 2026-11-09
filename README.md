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
- [ ] **Phase 2: Design System & Shared Component Library**
- [ ] **Phase 3: App Navigation & Route Guards**
- [ ] **Phase 4: Authentication & Session Persistence**
- [ ] **Phase 5: School Registration & Onboarding**
- [ ] **Phase 6: Multi-Tenant Architecture & Firestore Schema**
- [ ] **Phase 7: Role & Granular Permission Enforcement**
- [ ] **Phase 8: School Admin Dashboard & Analytics**
- [ ] **Phase 9: Complete Student Lifecycle Management**
- [ ] **Phase 10: Parent Portal & Multi-Child Linking**
- [ ] **Phase 11: Teacher & Faculty Management**
- [ ] **Phase 12: Class & Section Management**
- [ ] **Phase 13: Subject & Timetable Routine System**
- [ ] **Phase 14: Daily & Monthly Attendance Management**
- [ ] **Phase 15: Homework & Assignments Pipeline**
- [ ] **Phase 16: Examination Management & Scheduling**
- [ ] **Phase 17: Result Grading & GPA Processing**
- [ ] **Phase 18: Fee Invoicing & Payment Collection**
- [ ] **Phase 19: Notices & Targeted Announcements**
- [ ] **Phase 20: Push Notifications & Message Center**
- [ ] **Phase 21: Leave Request & Approval Workflow**
- [ ] **Phase 22: Library Book Inventory & Lending**
- [ ] **Phase 23: School Transport & Route Fleet**
- [ ] **Phase 24: School Events & Academic Calendar**
- [ ] **Phase 25: Documents, ID Cards & Certificates**
- [ ] **Phase 26: Secure Internal Messaging System**
- [ ] **Phase 27: Executive Reports & Data Analytics**
- [ ] **Phase 28: School Customization & Branding Settings**
- [ ] **Phase 29: Security Audit, Testing & Performance Tuning**
- [ ] **Phase 30: Production Release & SaaS Distribution Preparation**
