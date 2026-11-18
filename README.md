Retail Management System
A Java Swing desktop application designed to simplify product inventory
management and billing operations for a small retail store. The system
uses MySQL for persistent data storage and JDBC for database
connectivity.
Project Team
  Name             Register Number
  Daksh Kumar      RA2511027010093
Sashrik Das      RA2511027010087
Shivansh Singh   RA2511027010056
Faculty Guide
Dr. Srividhya S.
Assistant Professor
Department of Data Science and Business Systems
SRM Institute of Science and Technology, Kattankulathur
Course Details
- Course Code: 21CSC206P
- Course Name: Advanced Object Oriented and Programming
- Department: Data Science and Business Systems
- Institution: SRM Institute of Science and Technology,
  Kattankulathur
- Project Type: Minor Project
- Report Date: November 2026
Overview
The Retail Management System provides two main functions:
- Inventory Management: Add products, view available stock, edit
  product details, and remove products from active inventory.
- Billing: Search for products, add quantities to a cart,
  calculate the bill total, and generate a bill.
The application validates product information and requested quantities
against available stock. During checkout, the sale record, sale-item
records, and stock reduction are handled as a single database
transaction. If an error occurs, the transaction is rolled back to help
prevent partially completed bills and inconsistent stock records.
Features
- Display active products with product ID, name, price, and stock
  quantity.
- Add products after validating the entered details.
- Edit product names, prices, and stock quantities.
- Remove products from active inventory while retaining historical
  references.
- Search for products by name in the billing screen.
- Add products and quantities to a shopping cart.
- Validate cart quantities against available stock.
- Calculate line subtotals and the overall bill total.
- Generate and store sales and sale-item details.
- Update stock only when checkout succeeds.
- Use database transactions with commit and rollback.
Technology Stack
  Component                  Technology
  Programming Language       Java
Graphical User Interface   Java Swing
Database                   MySQL
Database Connectivity      JDBC
JDBC Driver                MySQL Connector/J
Currency Calculations      BigDecimal
Application Architecture
The project follows a layered structure that separates the user
interface, models, validation, database access, and persistent storage.
  Layer                   Main Classes / Components      Responsibility
  Presentation            MainApp, InventoryPanel,   Navigation, forms,
BillingPanel, Theme, Ui  tables, events,
styling, and messages

  Model                   Product, Cart, Bill      Represent products,
cart lines, and
generated bills

  Validation and          Validator,                   Validate input and
Exceptions              ValidationException,         report application or
ShopException,               stock errors
InsufficientStockException   

  Data Access             ProductDAO, BillingDAO,    Execute SQL operations
DBConnection                 and manage database
connectivity
Database                MySQL retail_db              Store products, sales,
and sale items
Database Design
The application uses three principal tables:
products
Stores the current product catalogue and inventory.
- product_id --- Primary key
- name
- price
- stock_qty
- is_active
sales
Stores one record for each completed bill.
- sale_id --- Primary key
- sale_date
- total_amount
sale_items
Stores the products and quantities included in each sale.
- sale_item_id --- Primary key
- sale_id --- Foreign key
- product_id --- Foreign key
- quantity
- unit_price
Foreign keys connect sale items to their corresponding sale and product
records.
How Billing Works
1. The user searches for a product and selects a quantity.
2. The application checks the requested quantity against available
   stock.
3. The selected item is added to the cart.
4. The cart calculates line subtotals and the overall total using
   BigDecimal.
5. When the user generates the bill, the billing layer rechecks current
   prices and stock in the database.
6. The application inserts the sale and sale-item records and decreases
   the relevant stock quantities.
7. The transaction is committed only after all operations succeed. If
   an error occurs, the transaction is rolled back.
Exception Handling
The application uses custom exceptions to handle application-specific
errors:
- ShopException --- Base application exception.
- ValidationException --- Represents invalid user input.
- InsufficientStockException --- Represents stock-related failures.
Database exceptions are translated into application-level errors and
displayed through the shared UI error helper.
Requirements
- Java Development Kit (JDK)
- MySQL Server
- MySQL Connector/J
- Java IDE or development environment
- A local MySQL server connection
Database Configuration
The application connects to a MySQL database named retail_db through
JDBC. DBConnection uses DriverManager to create the connection, and
the default configuration is intended for a local MySQL server.

Before running the application, ensure that:
1. MySQL Server is installed and running.
2. The retail_db database and required tables are created.
3. MySQL Connector/J is available to the Java project.
4. The database connection settings in the project match your local
   MySQL configuration.
Note: The report identifies the database name and local connection
approach, but does not provide the exact database username, password,
or complete SQL setup script. Configure these according to your local
environment. Do not commit database passwords or other credentials to
the repository.

Testing
The report defines the following functional test scenarios. These are
expected checks; execute them in your environment to record actual
results.
  Test ID                 Test Scenario           Expected Result
  TC-01                   Load inventory from     Active products appear
database                with their details and
stock

  TC-02                   Add a product with      Product is inserted and
valid details           appears after refresh

  TC-03                   Enter an invalid price  Validation error
or stock value          appears and invalid
data is not saved

  TC-04                   Search for a product by Matching product names
name                    are displayed

  TC-05                   Add a quantity within   Item is added and
available stock         subtotal/total are
updated

  TC-06                   Request more than the   Cart rejects the
available stock         request with a stock
warning

  TC-07                   Generate a bill with a  Sale and sale items are
valid cart              stored and stock is
reduced

  TC-08                   Cause a checkout        Transaction is rolled
failure                 back and stock remains
consistent
TC-09                   Remove an inventory     Product is removed from
item                    active inventory
Current Scope and Limitations
The current implementation is designed as a local desktop application
focused on inventory and billing. The report does not establish the
inclusion of:
- User login and role-based permissions
- Audit logs
- Receipt printing or export
- Sales trend reports and analytics
Possible future enhancements include authentication, receipt printing,
sales reports, barcode support, and role-based access control.
References
- Oracle Java Platform, Standard Edition
  Documentation
- Oracle Java Tutorials --- Creating a GUI With
  Swing
- Oracle JDBC Basics
- MySQL 8.0 Reference
  Manual
- MySQL Connector/J Developer
  Guide
Developed as a minor project for the course 21CSC206P --- Advanced
Object Oriented and Programming, SRM Institute of Science and
Technology, Kattankulathur.
