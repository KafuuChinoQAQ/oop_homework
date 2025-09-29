package l03.example3;

import java.util.Random;

public class TestRandom {

  public static void main(String[] args) {
	  
	  
	  Random random1 = new Random(5);
	  
	  System.out.println("Radom1生成器：");
	  for (int i = 0; i < 100; i++){
		  System.out.printf("%5d", random1.nextInt(1000));
	  }
	  
	  System.out.println();
	  
	  Random random2 = new Random(5);
	  
	  System.out.println("Random2生成器：");
	  for (int i = 0; i < 100; i++){
		  System.out.printf("%5d", random2.nextInt(1000));
	  }
  }
}
