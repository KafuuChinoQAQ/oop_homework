package l03.example3;

import java.util.Calendar;
import java.util.Date;

public class TestDate {

  public static void main(String[] args) {
	  
	  
	  Date date = new Date();
	   
	  System.out.println(date.getDay());
	  System.out.println(date.getTime());
	  System.out.println(date.toString());
	  System.out.println(date.toLocaleString());
	  System.out.println(Calendar.getInstance().getTime().toString());
  }
}
