# Store Management System

A comprehensive Java-based inventory and sales management system for retail stores. This application provides a command-line interface for managing products, processing sales, tracking customer purchase history, and generating sales reports.

## 🎯 Features

- **Product Management**: Add electronics and clothing products with category-specific attributes
- **Inventory Tracking**: Real-time stock monitoring with automatic restocking
- **Sales Processing**: Complete purchase workflow with invoice generation
- **Customer Management**: Track customer purchase history by contact information
- **Sales Analytics**: Generate reports for best-selling products
- **Tax Calculation**: Automatic 15% VAT calculation
- **Error Handling**: Robust input validation and exception management
- **Serialization**: Persistent data storage capabilities

## 🛠️ Technologies Used

- **Language**: Java 8+
- **Design Patterns**: 
  - Abstract Factory (for product creation)
  - Strategy (for discount calculations)
  - Observer (for inventory monitoring)
- **Data Structures**: HashMap, ArrayList, Stream API
- **Libraries**: Java Standard Library (Scanner, SimpleDateFormat)

## 📋 Project Structure
StoreManagementSystem/
├── StoreManagementSystem.java  # Main application class
├── BaseProduct.java           # Abstract base class for products
├── ElectronicsProduct.java    # Electronics product implementation
├── ClothingProduct.java      # Clothing product implementation
├── Customer.java             # Customer entity class
├── Purchase.java             # Purchase/invoice entity class
├── Store.java                # Core business logic
└── README.md                 # Project documentation
