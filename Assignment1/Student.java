// 学生类，表示大学里的学生
public class Student {
    private final String id;
    private final String name;
    private int borrowedBooks;

    public Student(String id, String name) {
        this.id = id;
        this.name = name;
        this.borrowedBooks = 0;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getBorrowedBooks() {
        return borrowedBooks;
    }

    public void borrowBook() {
        borrowedBooks++;
    }

    public void returnBook() {
        if (borrowedBooks > 0) {
            borrowedBooks--;
        }
    }
}
