import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

interface InventoryItem {
    void processSale(int quantity);
    void restock(int amount);
    String getProductDetails();
    double calculateDiscount();
}

abstract class BaseProduct implements InventoryItem, Serializable {
    protected final String id;
    protected final String name;
    protected final String category;
    protected double basePrice;
    protected int currentStock;
    protected int totalAvailable;
    protected int salesCount;

    public BaseProduct(String id, String name, String category, double basePrice, 
                      int totalAvailable, int initialStock) {
        if(initialStock > totalAvailable) 
            throw new IllegalArgumentException("Initial stock exceeds total available");
        
        this.id = id;
        this.name = name;
        this.category = category;
        this.basePrice = basePrice;
        this.totalAvailable = totalAvailable;
        this.currentStock = initialStock;
        this.salesCount = 0;
    }

    public String getId() { return id; }
    public int getCurrentStock() { return currentStock; }
    public int getTotalAvailable() { return totalAvailable; }
    public int getSalesCount() { return salesCount; }
    public double getPrice() { return basePrice; }

    @Override
    public void restock(int amount) {
        if(amount <= 0 || (currentStock + amount) > totalAvailable) {
            throw new IllegalArgumentException("Invalid restock amount");
        }
        currentStock += amount;
    }

    @Override
    public void processSale(int quantity) {
        if(quantity > currentStock)
            throw new IllegalStateException("Insufficient stock");
        
        currentStock -= quantity;
        salesCount += quantity;
        autoRestock();
    }

    private void autoRestock() {
        if(currentStock <= 0) {
            restock((int)(0.2 * totalAvailable));
        }
    }
}

class ElectronicsProduct extends BaseProduct {
    private final String warranty;

    public ElectronicsProduct(String id, String name, double basePrice, 
                             int totalAvailable, int initialStock, String warranty) {
        super(id, name, "Electronics", basePrice, totalAvailable, initialStock);
        this.warranty = warranty;
    }

    @Override
    public String getProductDetails() {
        return String.format("[Electronics] %s | Warranty: %s | Stock: %d/%d",
                           name, warranty, currentStock, totalAvailable);
    }

    @Override
    public double calculateDiscount() {
        return basePrice * 0.15;
    }
}

class ClothingProduct extends BaseProduct {
    private final String size;

    public ClothingProduct(String id, String name, double basePrice,
                          int totalAvailable, int initialStock, String size) {
        super(id, name, "Clothing", basePrice, totalAvailable, initialStock);
        this.size = size;
    }

    @Override
    public String getProductDetails() {
        return String.format("[Clothing] %s | Size: %s | Stock: %d/%d",
                           name, size, currentStock, totalAvailable);
    }

    @Override
    public double calculateDiscount() {
        return basePrice * 0.10;
    }
}

class Customer implements Serializable {
    private final String name;
    private final String contact;
    private final List<Purchase> purchaseHistory = new ArrayList<>();

    public Customer(String name, String contact) {
        this.name = name;
        this.contact = contact;
    }

    public String getName() { return name; }
    public String getContact() { return contact; }

    public void addPurchase(Purchase purchase) {
        purchaseHistory.add(purchase);
    }

    public void printPurchaseHistory() {
        System.out.println("\n=== Purchase History (" + name + ") ===");
        purchaseHistory.forEach(Purchase::printInvoice);
    }
}

class Purchase implements Serializable {
    private final String invoiceId;
    private final Date purchaseDate;
    private final Customer customer;
    private final Map<BaseProduct, Integer> items;
    private final double totalWithTax;

    public Purchase(Customer customer, Map<BaseProduct, Integer> items) {
        this.customer = customer;
        this.items = new HashMap<>(items);
        this.invoiceId = "INV-" + System.currentTimeMillis();
        this.purchaseDate = new Date();
        this.totalWithTax = calculateTotal();
    }

    private double calculateTotal() {
        double subtotal = items.entrySet().stream()
                .mapToDouble(e -> e.getKey().getPrice() * e.getValue())
                .sum();
        return subtotal * 1.15; // 15% VAT
    }

    public void printInvoice() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        System.out.println("\nInvoice ID: " + invoiceId);
        System.out.println("Date: " + sdf.format(purchaseDate));
        System.out.println("Customer: " + customer.getName() + " (" + customer.getContact() + ")");

        items.forEach((product, qty) -> 
            System.out.printf("%-20s %-5d x $%.2f = $%.2f%n", 
                            product.getId(), qty, 
                            product.getPrice(), 
                            product.getPrice() * qty));

        System.out.printf("Total (incl. tax): $%.2f%n", totalWithTax);
    }
}

class Store implements Serializable {
    private final Map<String, BaseProduct> inventory = new HashMap<>();
    private final Map<String, Customer> customers = new HashMap<>();

    public void addProduct(BaseProduct product) {
        inventory.put(product.getId(), product);
    }

    public void processSale(Customer customer, Map<String, Integer> cart) {
        Map<BaseProduct, Integer> validCart = new HashMap<>();

        cart.forEach((productId, qty) -> {
            BaseProduct p = inventory.get(productId);
            if(p != null && p.getCurrentStock() >= qty) {
                validCart.put(p, qty);
            }
        });

        if(!validCart.isEmpty()) {
            Purchase purchase = new Purchase(customer, validCart);
            validCart.forEach((product, qty) -> product.processSale(qty));
            customer.addPurchase(purchase);
        }
    }

    public Customer findOrCreateCustomer(String name, String contact) {
        return customers.computeIfAbsent(contact, k -> new Customer(name, contact));
    }

    public Customer getCustomerByContact(String contact) {
        return customers.get(contact);
    }

    public void generateSalesReport() {
        System.out.println("\n=== Best Selling Products ===");
        inventory.values().stream()
            .sorted((p1, p2) -> p2.getSalesCount() - p1.getSalesCount())
            .limit(3)
            .forEach(p -> System.out.printf("%s - Sold: %d%n", 
                p.getProductDetails(), p.getSalesCount()));
    }
}

public class StoreManagementSystem {
    private static final Scanner scanner = new Scanner(System.in);
    private static Store store = new Store();

    public static void main(String[] args) {
        initializeSampleData();

        while(true) {
            System.out.println("\n1. Add Product\n2. Process Sale\n3. View Customer History\n4. Generate Report\n5. Exit");
            int choice = getIntInput("Choose option: ");

            switch(choice) {
                case 1: addProduct(); break;
                case 2: processSale(); break;
                case 3: viewCustomerHistory(); break;
                case 4: store.generateSalesReport(); break;
                case 5: System.exit(0);
            }
        }
    }

    private static void addProduct() {
        try {
            String type = getInput("Product type (Electronics/Clothing): ");
            String id = getInput("Product ID: ");
            String name = getInput("Name: ");
            double price = getDoubleInput("Price: ");
            int total = getIntInput("Total Available: ");
            int initial = getIntInput("Initial Stock: ");

            BaseProduct product;
            if(type.equalsIgnoreCase("Electronics")) {
                String warranty = getInput("Warranty: ");
                product = new ElectronicsProduct(id, name, price, total, initial, warranty);
            } else {
                String size = getInput("Size: ");
                product = new ClothingProduct(id, name, price, total, initial, size);
            }

            store.addProduct(product);
            System.out.println("Product added successfully!");
        } catch(Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void processSale() {
        String contact = getInput("Customer contact: ");
        Customer customer = store.getCustomerByContact(contact);

        if (customer == null) {
            String name = getInput("Customer name: ");
            customer = store.findOrCreateCustomer(name, contact);
        }

        Map<String, Integer> cart = new HashMap<>();
        while(true) {
            String productId = getInput("Product ID (type 'done' to finish): ");
            if(productId.equalsIgnoreCase("done")) break;

            int qty = getIntInput("Quantity: ");
            cart.put(productId, qty);
        }

        store.processSale(customer, cart);
        System.out.println("Sale processed successfully!");
    }

    private static void viewCustomerHistory() {
        String contact = getInput("Customer contact: ");
        Customer customer = store.getCustomerByContact(contact);

        if(customer != null) {
            customer.printPurchaseHistory();
        } else {
            System.out.println("No customer found with that contact.");
        }
    }

    // Helper methods
    private static String getInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private static int getIntInput(String prompt) {
        while(true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine());
            } catch(NumberFormatException e) {
                System.out.println("Invalid number!");
            }
        }
    }

    private static double getDoubleInput(String prompt) {
        while(true) {
            try {
                System.out.print(prompt);
                return Double.parseDouble(scanner.nextLine());
            } catch(NumberFormatException e) {
                System.out.println("Invalid price!");
            }
        }
    }

    private static void initializeSampleData() {
        store.addProduct(new ElectronicsProduct("E1", "Laptop", 1200.0, 100, 50, "2 Years"));
        store.addProduct(new ClothingProduct("C1", "T-Shirt", 25.0, 200, 100, "XL"));
    }
}
