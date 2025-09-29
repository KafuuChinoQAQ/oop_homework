package l03.example1;

public class TestCircle {
  /** Main method */
  public static void main(String[] args) {
    // Create a circle with radius 5.0
    Circle myCircle = new Circle(5.0);
    System.out.println("myCircle的半径： " +  myCircle.radius);
    System.out.println("myCircle的面积： " +  myCircle.getArea());

    System.out.println("-----------------------------------------------");
    
    // Create a circle with radius 1
    Circle yourCircle = new Circle();
    System.out.println("yourCircle的半径： " +  yourCircle.radius);
    System.out.println("yourCircle的面积： " +  yourCircle.getArea());

    System.out.println("-----------------------------------------------");
    
    // Modify circle radius
    yourCircle.radius = 100;
    System.out.println("yourCircle的半径： " +  yourCircle.radius);
    System.out.println("yourCircle的面积： " +  yourCircle.getArea());
  }
}
