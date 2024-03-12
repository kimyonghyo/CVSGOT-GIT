package tesco.got;

import java.util.Vector;

public class PreparedStatementPrinter {

	   Vector v = new Vector(1,1);                        
       
	   public void setParam (String param) {              
	      v.addElement("'" + param + "'");                
	   }                                                  
	                                                      
	   public void setParam (double param) {              
	      v.addElement(Double.toString(param));           
	   }                                                  
	                                                      
	   public void setParam (float param) {               
	      v.addElement(Float.toString(param));            
	   }                                                  
	                                                      
	   public void setParam (int param) {                 
	      v.addElement(Integer.toString(param));          
	   }                                                  
	                                                      
	   public void setParam (long param) {                
	      v.addElement(Long.toString(param));             
	   }                                                  
	                                                      
	   // "?"를 vector에 저장해 둔 스트링으로 바꾼다.     
	   // 이원영님의 replace 메소드를 참조했음            
	   public String getStr (String sqlStr) {             
	                                                      
	      int seq = 0;                                    
	      StringBuffer buf = new StringBuffer();          
	                                                      
	      for(int i; (i = sqlStr.indexOf("?")) >= 0; ) {  
	         buf.append(sqlStr.substring(0,i));           
	         buf.append(v.elementAt(seq));                
	         sqlStr = sqlStr.substring(i + "?".length()); 
	         seq++;                                       
	      }                                               
	                                                      
	      buf.append(sqlStr);                             
	      return buf.toString();                          
	   }                                                  

}
