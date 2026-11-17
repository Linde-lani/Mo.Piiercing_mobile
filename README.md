# Mo's Tattoos & Body Piercing Studio (Moes App)

An enterprise-grade, full-featured Android application built for **Mo's Tattoos & Body Piercing Studio** (**Mo Piercings**). The app provides a seamless dual-experience platform supporting both **Customer Browsing/Booking** and **Admin Inventory/Booking Management**, powered by real-time Firebase services, secure authentication, and modern Material Design 3 guidelines.

---

## 1. Purpose of the App

The **Moes App** bridges the gap between high-end tattoo and body piercing studios and their clientele. Key business and operational objectives include:
- **Digital Studio Showcase**: Highlighting luxury tattoo designs, body jewellery collections (ear, nose, belly, industrial, tooth gems), new arrivals, and best sellers through organized horizontal carousels and category chips.
- **Appointment & Booking Management**: Allowing customers to schedule tattoo or piercing appointments, select preferred artists/services, track booking statuses (Pending, Confirmed, Cancelled, Resolved), and view booking history.
- **Inventory & Stock Control (Admin Desk)**: Providing studio managers with real-time tracking of total products, category breakdowns (Tattoos vs. Jewellery), featured items, active statuses, and low-stock alerts.
- **E-Commerce & Secure Checkout**: Integrated cart management, wishlist persistence, and secure payments via **Stripe Android SDK**.
- **Real-Time Customer-Admin Communication**: Direct messaging and chat features for consultation, aftercare queries, and customer support.

---

## 2. Design Considerations

### Visual Identity & Theme
- **Luxury Dark Aesthetic**: Designed with a sophisticated dark palette (`#0F0F0F` background, dark surface cards) paired with rich **Moes Gold** (`#C69C35` / `#D4AF37`) and **Forest Green** accents, reflecting a high-end, professional studio environment.
- **Material Design 3 (M3)**: Utilization of Material CardViews, TextInputLayouts with outlined boxes, ChipGroups for category filtering, and BottomNavigation bars.
- **Edge-to-Edge & Insets Handling**: Full support for edge-to-edge display on modern Android devices, correctly consuming system window insets for status bars and navigation bars to prevent overlap.

### Performance & Responsiveness
- **Real-Time Synchronization**: Built on top of **Firebase Firestore** snapshot listeners (`addSnapshotListener`), ensuring instant UI updates across both customer and admin views when products, bookings, or cart items are modified.
- **Efficient Image Loading**: Integrated with **Glide** for performant asynchronous loading of high-resolution remote product images (URLs) and local drawable fallbacks.
- **Optimized Scrolling**: NestedScrollViews and RecyclerViews (`LinearLayoutManager` and `GridLayoutManager`) with recycled view viewholders for smooth 60fps scrolling performance.

---

## 3. Technology Stack

- **Language**: Kotlin 100%
- **Min SDK**: 24 (Android 7.0+) | **Target / Compile SDK**: 35 (Android 15)
- **UI Architecture**: XML Layouts with ViewBinding, CoordinatorLayout, ConstraintLayout
- **Backend & Cloud**: Firebase (Authentication, Firestore, Storage, Analytics, BoM)
- **Identity & Auth**: Google Credential Manager & Google ID Sign-In
- **Payments**: Stripe Android SDK (`21.0.0`)
- **Image Processing**: Glide (`4.16.0`)
- **Testing**: JUnit, AndroidX Test, Espresso

---

## 4. GitHub & GitHub Actions Integration

Throughout the development lifecycle, **GitHub** and **GitHub Actions** played a vital role in ensuring code quality, continuous integration (CI), and automated release tracking:

### Version Control Strategy (GitHub)
- **Branching Model**: Main production branch (`main`), active feature branches (`feature/...`), and pull requests with required code reviews.
- **Issue Tracking**: Tracking feature requests, bug fixes (e.g., RecyclerView layout bindings, Edge-to-Edge insets), and milestone planning.

### Continuous Integration (GitHub Actions Workflows)
Automated GitHub Actions YAML workflows (`.github/workflows/android.yml`) were configured to execute the following pipeline on every push and pull request:
1. **Environment Setup**: Provisions Ubuntu runner with Temurin JDK 17 / 21 and Android SDK build-tools.
2. **Dependency Caching**: Caches Gradle dependencies for faster build execution.
3. **Linting & Code Analysis**: Executes `./gradlew lintDebug` to catch potential formatting, missing string resources, and layout XML issues.
4. **Automated Testing**: Runs unit tests (`./gradlew testDebugUnitTest`) to verify business logic and data models.
5. **Release Build Verification**: Assembles debug and release APKs (`./gradlew assembleDebug assembleRelease`) to guarantee compilation integrity across all screens (`GalleryScreen`, `AdminDashboardActivity`, `gallery_management`, `BookingScreen`, etc.).

---

## 5. Release Notes (From Prototype to Production)

### 🚀 v0.1.0-alpha (Prototype Milestone)
* **Initial Concept & Setup**: Established Android project structure with Gradle Kotlin DSL and namespace configuration (`com.example.moes`).
* **Core Authentication**: Implemented Firebase Authentication (Email/Password & Google Credential Manager sign-in).
* **Layout Wireframes**: Created foundational XML layout files (`activity_main.xml`, `activity_login.xml`, `activity_signup.xml`) establishing the dark luxury design system and color tokens.

### 📦 v0.2.0-beta (Feature Expansion Milestone)
* **Customer Gallery & Catalog**: Introduced dynamic Firestore product loading with category filtering chips (Tattoos, Jewellery, Piercings, Featured).
* **Booking & Scheduling Module**: Built appointment booking flows, date/time selection, and customer booking history dashboards (`my_bookings.kt`).
* **Admin Inventory Management**: Developed `gallery_management.kt` and `AdminDashboardActivity.kt` allowing administrators to add/edit products, toggle featured status, and track live statistics (Total Items, Tattoos, Jewellery, Low Stock).

### 🛠️ v0.3.0-rc (CI/CD Automation & Polish)
* **GitHub Actions CI/CD Pipeline**: Integrated automated build, test, and lint workflows to guard against regression.
* **Edge-to-Edge & UI Fixes**: Updated activities (`GalleryScreen`, `gallery_management`) with edge-to-edge window inset listeners and corrected RecyclerView layout inflation bindings (`product_preview_item`).
* **Stripe & Cart Integration**: Added robust cart summary cards, checkout flow (`CheckoutActivity.kt`), and wishlist persistence (`WishlistActivity.kt`).

### ✨ v1.0.0 (Production Release)
* **Production Readiness**: Full stability across all customer and admin flows.
* **Performance Optimization**: Zero compilation errors, optimized layout hierarchies, robust Firebase snapshot error handling, and comprehensive documentation (`README.md`).
