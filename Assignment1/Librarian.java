// 管理员类，表示大学图书馆的管理员
public class Librarian {
    private final String id;
    private final String name;

    public Librarian(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // 借书操作
    public void lendBook(Student student, Book book) {
        if (!book.isBorrowed()) {
            book.borrow();
            student.borrowBook();
            System.out.println(name + " 借出图书：" + book.getTitle() + " 给学生：" + student.getName());
        } else {
            System.out.println("图书已被借出。");
        }
    }

    // 还书操作
    public void receiveBook(Student student, Book book) {
        if (book.isBorrowed()) {
            book.returned();
            student.returnBook();
            System.out.println(name + " 接收学生：" + student.getName() + " 归还的图书：" + book.getTitle());
        } else {
            System.out.println("图书未被借出，无需归还。");
        }
    }
}
