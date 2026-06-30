# E-Commerce Web Application (MVC)

A full-featured E-Commerce web application built using **Spring Boot**, **MongoDB**, and **Thymeleaf**. This project implements the Model-View-Controller (MVC) architecture and provides a comprehensive shopping experience with secure payments, user authentication, and an admin dashboard.

## Tech Stack

- **Backend:** Java 21, Spring Boot 3.4
- **Database:** MongoDB
- **Frontend:** Thymeleaf, Bootstrap 5, FontAwesome
- **Security:** Spring Security
- **Payments:** Stripe API
- **Utilities:** Lombok, Java Dotenv, Spring Boot Mail

## Key Features

- **User Authentication & Authorization:** Secure registration and login with roles (Admin/User) using Spring Security.
- **Product Management:** Browse products, view details, and filter by categories.
- **Shopping Cart & Wishlist:** Add items to cart, manage wishlist, and proceed to checkout.
- **Order & Payment Processing:** Secure payment integration via Stripe.
- **Product Reviews & Ratings:** Users can leave reviews and rate products.
- **Admin Dashboard:** Manage users, products, categories, and view orders.
- **Email Notifications:** Automated emails for order confirmation and user registration.

## Project Structure

- `model`: Contains MongoDB document entities (User, Product, Order, Cart, etc.).
- `repository`: Spring Data MongoDB repositories for data access.
- `service`: Business logic layer.
- `controller`: Spring MVC controllers handling HTTP requests and returning Thymeleaf views.
- `config`: Configuration classes (Security, MongoDB, Stripe, Thymeleaf).
- `dto`: Data Transfer Objects for passing data between layers.

## Prerequisites

- Java 21
- Maven
- MongoDB (running locally or a remote cluster)
- Stripe Account (for payments)

## Setup & Installation

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd Ecommerce-MVC
   ```

2. **Configure Environment Variables:**
   Create a `.env` file in the root directory and add the following properties (as required by your setup):
   ```env
   # Add your environment variables here
   # Example:
   # STRIPE_SECRET_KEY=sk_test_...
   ```
   *(Note: You may need to check `application.properties` or `application.yml` for specific environment variable keys expected by the application)*

3. **Build and Run the Application:**
   Using Maven wrapper:
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Access the Application:**
   Open your browser and navigate to `http://localhost:8080`
