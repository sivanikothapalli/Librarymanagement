package Demo;
import java.sql.*;
import java.util.Scanner;

public class Librarymanagement{
	
	    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/library_management";
	    private static final String USERNAME = "root";
	    private static final String PASSWORD = "root";

	    public static void main(String[] args) {
	        initializeDatabase();

	        Scanner scanner = new Scanner(System.in);
	        Librarymanagement libraryManager = new Librarymanagement();

	        while (true) {
	            System.out.println("1. Add Book");
	            System.out.println("2. Display Books");
	            System.out.println("3. Issue Book");
	            System.out.println("4. Return Book");
	            System.out.println("5. Exit");

	            System.out.print("Enter your choice: ");
	            int choice = scanner.nextInt();

	            switch (choice) {
	                case 1:
	                    libraryManager.addBook();
	                    break;
	                case 2:
	                    libraryManager.displayBooks();
	                    break;
	                case 3:
	                    libraryManager.issueBook();
	                    break;
	                case 4:
	                    libraryManager.returnBook();
	                    break;
	                case 5:
	                    System.out.println("Exiting program. Goodbye!");
	                    System.exit(0);
	                default:
	                    System.out.println("Invalid choice. Please try again.");
	            }
	        }
	    }

	    private static void initializeDatabase() {
	        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/", USERNAME, PASSWORD)) {
	            String createDatabase = "CREATE DATABASE IF NOT EXISTS library_management";
	            String useDatabase = "USE library_management";

	            String createBooksTable = "CREATE TABLE IF NOT EXISTS books (" +
	                    "id INT PRIMARY KEY AUTO_INCREMENT," +
	                    "title VARCHAR(255) NOT NULL," +
	                    "author VARCHAR(255) NOT NULL," +
	                    "quantity INT NOT NULL" +
	                    ")";

	            String createTransactionsTable = "CREATE TABLE IF NOT EXISTS transactions (" +
	                    "id INT PRIMARY KEY AUTO_INCREMENT," +
	                    "book_id INT," +
	                    "user_name VARCHAR(255)," +
	                    "issue_date DATE," +
	                    "return_date DATE," +
	                    "FOREIGN KEY (book_id) REFERENCES books(id)" +
	                    ")";

	            try (Statement statement = connection.createStatement()) {
	                statement.executeUpdate(createDatabase);
	                statement.executeUpdate(useDatabase);
	                statement.executeUpdate(createBooksTable);
	                statement.executeUpdate(createTransactionsTable);
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }

	    private Connection getConnection() throws SQLException {
	        return DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
	    }

	    public void addBook() {
	        try (Connection connection = getConnection()) {
	            String insertQuery = "INSERT INTO books (title, author, quantity) VALUES (?, ?, ?)";

	            try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
	                Scanner scanner = new Scanner(System.in);

	                System.out.print("Enter book title: ");
	                String title = scanner.nextLine();
	                System.out.print("Enter author: ");
	                String author = scanner.nextLine();
	                System.out.print("Enter quantity: ");
	                int quantity = scanner.nextInt();

	                preparedStatement.setString(1, title);
	                preparedStatement.setString(2, author);
	                preparedStatement.setInt(3, quantity);

	                int rowsAffected = preparedStatement.executeUpdate();

	                if (rowsAffected > 0) {
	                    System.out.println("Book added successfully!");
	                } else {
	                    System.out.println("Failed to add book.");
	                }
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }

	    public void displayBooks() {
	        try (Connection connection = getConnection()) {
	            String selectQuery = "SELECT * FROM books";

	            try (Statement statement = connection.createStatement()) {
	                ResultSet resultSet = statement.executeQuery(selectQuery);

	                System.out.println("Books in the library:");

	                while (resultSet.next()) {
	                    int id = resultSet.getInt("id");
	                    String title = resultSet.getString("title");
	                    String author = resultSet.getString("author");
	                    int quantity = resultSet.getInt("quantity");

	                    System.out.println("ID: " + id + ", Title: " + title + ", Author: " + author + ", Quantity: " + quantity);
	                }
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }

	    public void issueBook() {
	        try (Connection connection = getConnection()) {
	            displayBooks();

	            Scanner scanner = new Scanner(System.in);
	            System.out.print("Enter the ID of the book you want to issue: ");
	            int bookId = scanner.nextInt();
	            scanner.nextLine(); // Consume newline

	            System.out.print("Enter your name: ");
	            String userName = scanner.nextLine();

	            if (isBookAvailable(connection, bookId)) {
	                String issueQuery = "INSERT INTO transactions (book_id, user_name, issue_date) VALUES (?, ?, NOW())";
	                try (PreparedStatement preparedStatement = connection.prepareStatement(issueQuery)) {
	                    preparedStatement.setInt(1, bookId);
	                    preparedStatement.setString(2, userName);

	                    int rowsAffected = preparedStatement.executeUpdate();

	                    if (rowsAffected > 0) {
	                        System.out.println("Book issued successfully!");
	                    } else {
	                        System.out.println("Failed to issue book.");
	                    }
	                }
	            } else {
	                System.out.println("The selected book is not available for issuance.");
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }

	    private boolean isBookAvailable(Connection connection, int bookId) throws SQLException {
	        String availabilityQuery = "SELECT quantity FROM books WHERE id = ?";

	        try (PreparedStatement preparedStatement = connection.prepareStatement(availabilityQuery)) {
	            preparedStatement.setInt(1, bookId);

	            ResultSet resultSet = preparedStatement.executeQuery();

	            if (resultSet.next()) {
	                int quantity = resultSet.getInt("quantity");
	                return quantity > 0;
	            } else {
	                return false;
	            }
	        }
	    }

	    public void returnBook() {
	        try (Connection connection = getConnection()) {
	            Scanner scanner = new Scanner(System.in);

	            System.out.print("Enter your name: ");
	            String userName = scanner.nextLine();

	            String issuedBooksQuery = "SELECT t.id, b.title FROM transactions t " +
	                    "JOIN books b ON t.book_id = b.id " +
	                    "WHERE t.user_name = ? AND t.return_date IS NULL";

	            try (PreparedStatement preparedStatement = connection.prepareStatement(issuedBooksQuery)) {
	                preparedStatement.setString(1, userName);

	                ResultSet resultSet = preparedStatement.executeQuery();

	                if (resultSet.next()) {
	                    System.out.println("Books issued to you:");
	                    do {
	                        int transactionId = resultSet.getInt("id");
	                        String bookTitle = resultSet.getString("title");
	                        System.out.println("Transaction ID: " + transactionId + ", Book Title: " + bookTitle);
	                    } while (resultSet.next());

	                    System.out.print("Enter the Transaction ID of the book you want to return: ");
	                    int transactionId = scanner.nextInt();

	                    String returnQuery = "UPDATE transactions SET return_date = NOW() WHERE id = ?";
	                    try (PreparedStatement returnStatement = connection.prepareStatement(returnQuery)) {
	                        returnStatement.setInt(1, transactionId);

	                        int rowsAffected = returnStatement.executeUpdate();

	                        if (rowsAffected > 0) {
	                            System.out.println("Book returned successfully!");
	                        } else {
	                            System.out.println("Failed to return book. Please check the Transaction ID.");
	                        }
	                    }
	                } else {
	                    System.out.println("No books are currently issued to you.");
	                }
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
	}
