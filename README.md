# Smart ERP System

A complete Enterprise Resource Planning (ERP) desktop app built with Java Swing and MySQL. It covers core business operations like sales, purchases, inventory, customers, suppliers, employees, and payments in one place. The system includes role-based access control, audit logging, dashboards, and PDF reporting for day-to-day operations. It is designed for small to mid-size businesses that want an offline, full-featured ERP with a modern desktop UI.

## 📋 Overview

Smart ERP System is a feature-rich, user-friendly desktop application designed to streamline business operations across multiple departments. It offers integrated modules for managing customers, suppliers, employees, inventory, and financial transactions with a professional GUI and role-based access control.

## ✨ Key Features

### Core Modules
- **Customer Management** - Create, update, and manage customer profiles and relationships
- **Supplier Management** - Track supplier information and purchase history
- **Employee Management** - Manage employee records, roles, and permissions
- **Product Inventory** - Monitor stock levels, movements, and low-stock alerts
- **Sales Management** - Create and track sales orders with itemized details
- **Purchase Management** - Handle vendor purchases and procurement workflows
- **Payment Processing** - Manage customer and vendor payments
- **Ledger System** - Track financial transactions and account balances

### Advanced Features
- **Dashboard & Analytics** - Real-time business metrics and KPIs
- **Reporting** - Generate PDF reports for inventory, sales, and financial data
- **User Authentication** - Secure login with role-based access control
- **Permission Management** - Granular control over user permissions and features
- **Audit Logging** - Complete audit trail of all system activities
- **Low Stock Alerts** - Automatic notifications for inventory below threshold
- **Unit Management** - Flexible product measurement units

## 🏗️ Architecture

The application follows a **three-tier architecture** pattern:

```
┌─────────────────────────────────────┐
│   Presentation Layer (UI)           │
│   - Swing GUI Components            │
│   - Forms & Panels                  │
│   - Dashboard & Reporting           │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│   Business Logic Layer (Services)   │
│   - Service Classes                 │
│   - Business Rules & Validation     │
│   - Report Generation               │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│   Data Access Layer (DAOs)          │
│   - Database Operations             │
│   - Query Handling                  │
│   - Generic DAO Pattern             │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│   Database Layer                    │
│   - MySQL Database                  │
│   - Schema & Tables                 │
└─────────────────────────────────────┘
```

## 🛠️ Technology Stack

| Component | Technology |
|-----------|-----------|
| **Language** | Java 8+ |
| **UI Framework** | Java Swing with FlatLaf |
| **IDE** | Apache NetBeans |
| **Build System** | Apache Ant (build.xml) |
| **Database** | MySQL 8.0+ |
| **JDBC Driver** | MySQL Connector/J 9.6.0 |
| **PDF Generation** | OpenPDF 1.3.39 |
| **Password Hashing** | JBCrypt 0.4 |
| **Modern Look & Feel** | FlatLaf 3.6 |

## 📦 Project Structure

```
Smart ERP System/
├── src/
│   └── javaapplication1/
│       └── erp/
│           ├── app/                  # Application entry points
│           ├── dao/                  # Data Access Objects
│           ├── model/                # Entity models
│           ├── service/              # Business logic services
│           ├── ui/                   # Swing UI components
│           │   ├── components/       # Reusable UI components
│           │   ├── dashboard/        # Dashboard screens
│           │   ├── screens/          # Application screens
│           │   ├── sidebar/          # Navigation sidebar
│           │   └── theme/            # UI theming
│           ├── report/               # Report generation
│           ├── util/                 # Utility classes
│           └── resources/            # Configuration files
├── build/                            # Compiled classes & artifacts
├── lib/                              # External libraries (JARs)
├── nbproject/                        # NetBeans project configuration
└── build.xml                         # Ant build file
```

## 🚀 Getting Started

### Prerequisites

- **Java Development Kit (JDK)** 8 or higher
- **Apache NetBeans** 11.0 or later (or any Java IDE)
- **MySQL Server** 8.0 or higher
- **Ant** (usually included with NetBeans)

### Installation

1. **Clone or download the project**
   ```bash
   # If using version control
   git clone <repository-url>
   cd "Smart ERP System"
   ```

2. **Install and configure MySQL**
   - Install MySQL Server 8.0+
   - Create a new database named `ERP`
   ```sql
   CREATE DATABASE ERP;
   ```

3. **Configure Database Connection**
   - Navigate to `src/javaapplication1/resources/db.properties`
   - Update database credentials:
   ```properties
   db.host=127.0.0.1
   db.port=3306
   db.name=ERP
   db.user=root
   db.password=your_password
   ```

4. **Initialize Database Schema**
   - Execute the SQL schema file:
   ```bash
   mysql -u root -p ERP < src/javaapplication1/resources/schema.sql
   ```

5. **Open in NetBeans**
   - Open NetBeans IDE
   - Go to File → Open Project
   - Select the "Smart ERP System" folder
   - NetBeans will recognize it as an Ant-based project

## 🔨 Building & Running

### Using NetBeans IDE

1. **Build the Project**
   - Right-click on project → Clean and Build
   - Or press: `Shift + F11`

2. **Run the Application**
   - Right-click on project → Run
   - Or press: `F6`

3. **Debug the Application**
   - Right-click on project → Debug
   - Or press: `Ctrl + F5`

### Using Command Line (Ant)

```bash
# Clean and build
ant clean build

# Run the application
ant run

# Debug
ant debug

# Create JAR file
ant jar
```

## 🗄️ Database Setup

The application uses MySQL for data persistence. The database includes the following main tables:

- **users** - System users and authentication
- **customers** - Customer information
- **suppliers** - Supplier details
- **employees** - Employee records
- **products** - Product catalog
- **inventory_movements** - Stock adjustments
- **sales_orders** - Sales transaction records
- **purchases** - Purchase orders
- **payments** - Payment records
- **audit_logs** - System audit trail

Schema is auto-generated via `schema.sql` during initial setup.

## 🔐 User Authentication

The system includes:
- **Login Form** - Secure user authentication
- **Password Hashing** - JBCrypt encryption for passwords
- **Role-Based Access Control (RBAC)** - Different user roles with specific permissions
- **Session Management** - Active session tracking
- **Audit Trail** - Complete audit log of user activities

### Default Admin Credentials
After initial setup, you may need to create an admin account. Check `schema.sql` for default credentials or create one through the application.

## 📊 Key Classes

### Models (Entity Classes)
- `User.java` - User entity with roles and permissions
- `Customer.java` - Customer profile
- `Supplier.java` - Supplier information
- `Employee.java` - Employee details
- `Product.java` - Product catalog entry
- `SalesOrder.java` & `SalesItem.java` - Sales transactions
- `Purchase.java` & `PurchaseItem.java` - Purchase orders
- `InventoryMovement.java` - Stock movements

### Services (Business Logic)
- `AuthService.java` - Authentication & authorization
- `CustomerService.java` - Customer management logic
- `ProductService.java` - Product management
- `SalesService.java` - Sales order processing
- `PurchaseService.java` - Purchase management
- `InventoryService.java` - Inventory tracking
- `DashboardService.java` - Dashboard metrics
- `RolePermissionService.java` - Permission management

### DAOs (Data Access)
- `GenericDAO.java` - Base DAO with CRUD operations
- `CustomerDAO.java`, `ProductDAO.java`, etc. - Entity-specific DAOs

### UI Components
- `LoginForm.java` - Login screen
- `DashboardFrame.java` - Main application window
- `EntityCrudPanelTemplate.java` - Reusable CRUD interface template
- Various panels for each module (Sales, Inventory, etc.)

## 📝 Usage Examples

### Logging In
1. Launch the application
2. Enter username and password
3. Click Login (auto-validates credentials)

### Creating a Sales Order
1. Navigate to Sales module
2. Click "New Order"
3. Select customer and add items
4. Set prices and quantities
5. Save order to database

### Generating Reports
1. Go to the Report menu
2. Select report type (Sales, Inventory, etc.)
3. Choose date range and filters
4. Generate PDF report

### Managing Inventory
1. View current stock levels in Inventory panel
2. Adjust stock quantities
3. View low-stock alerts
4. Track inventory movements history

## 🐛 Troubleshooting

### Database Connection Issues
- Verify MySQL server is running
- Check database credentials in `db.properties`
- Ensure `ERP` database exists
- Verify MySQL driver is in `lib/` folder

### Build Errors
- Clean and rebuild: `ant clean build`
- Check Java version: `java -version`
- Verify all dependencies in `lib/` folder

### Runtime Issues
- Check NetBeans console for error messages
- Verify audit log for system errors
- Ensure proper database schema initialization

## 📈 Future Enhancements

- Multi-currency support
- Advanced analytics and BI integration
- Mobile companion app
- Cloud synchronization
- Email/SMS notifications
- Advanced forecasting
- Supply chain optimization

## 📄 License

This project is proprietary software. All rights reserved.

## 👥 Support & Contribution

For issues, feature requests, or contributions:
1. Document the issue clearly
2. Provide steps to reproduce (for bugs)
3. Submit through appropriate channels
4. Follow code style and conventions

## 📞 Contact

For support and inquiries, please contact the development team.

---

**Project Status:** Active Development  
**Last Updated:** April 2026  
**Version:** 1.0.0
