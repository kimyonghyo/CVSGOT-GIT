/*****************************************************
 * PROGRAM ID    : OsiSave
 * PROGRAM NAME  : 결품등록 저장부분
 * CREATION DATE : 2013
 *****************************************************/ 
package tesco.got.dbTran;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import tesco.got.Common;

import awoo.dbUtil.ConnPool;
import awoo.dbUtil.OraConnFactory;
import awoo.util.StringUtil;

public class OsiSave {
	private int    _i_store_code = 0; 
	private Object[] _items = null;
  
    public OsiSave(String store_code, Object[] items) {
    	this._i_store_code = Integer.parseInt(store_code);        
    	this._items        = items;
    }    

	public int executeQuery() throws GotException,Exception {
		StringBuffer sql = null;
		
		Connection conn = null;
        ConnPool cp = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
          
		try {
			cp = ConnPool.getInstance();
			conn = cp.getConnection();
			
			String	item_code = "";
			String	real_qty  = "";
			String	group_no  = "";
            String	sohQty	  = "";
            int cnt 		  = 0;
			int rejCount	  = 0;
                                    
        	for(int i = 3; i < this._items.length; i++){

	               Object[] tmp = StringUtil.split(this._items[i].toString(),Common.GS);

	                item_code = tmp[0].toString();
	                real_qty  = tmp[1].toString();
	                group_no  = tmp[2].toString();
	                
	                sql = new StringBuffer();			
	                sql.append("SELECT COUNT(*) CNT "							   );
	                sql.append("  FROM OUT_OF_SHELF_ANALYSIS "					   );
	                sql.append(" WHERE STORE_NO  = ?"							   );
	                sql.append("   AND ITEM_NO   = ?"							   );
	                sql.append("   AND WORK_DATE = TO_CHAR(SYSDATE, 'YYYYMMDD')   ");
	                sql.append("   AND GROUP_NO  = ?"							   );
	                
	                pstmt = conn.prepareStatement(sql.toString());	                
	                
	                pstmt.setInt(1, _i_store_code);
	                pstmt.setString(2, item_code);
	                pstmt.setString(3, group_no);
	                
	                rs = pstmt.executeQuery();	  	               
	                
	                if(rs.next()){
	                	cnt = rs.getInt("CNT");
	                }
	                
	                rs.close();
	                pstmt.close();	                
	                
	                sql = new StringBuffer();
	                sql.append("SELECT NVL(ON_HAND_QTY,0) AS QTY  ");
	                sql.append("  FROM STOCK_ON_HAND "			   );
	                sql.append(" WHERE LOC_NO   = 20 "			   );
	                sql.append("   AND STORE_NO = ?  "		   	   );
	                sql.append("   AND ITEM_NO  = trim(?) "		   );
	                
	                pstmt = conn.prepareStatement(sql.toString());
	                pstmt.setInt(1, this._i_store_code);
	                pstmt.setString(2, item_code);
	                
	                rs = pstmt.executeQuery();
	                
	                if(rs.next()){
	                	sohQty = rs.getString("QTY");
	                }
	                rs.close();
	                pstmt.close();
	                
	                if(cnt == 0){	                	
	                	sql = new StringBuffer();
	                	sql.append("INSERT INTO OUT_OF_SHELF_ANALYSIS (WORK_DATE, STORE_NO, GROUP_NO, ITEM_NO, STOCK_ON_HAND, COUNT_QTY, TIME_STAMP, STATUS) ");
	                	sql.append("VALUES (TO_CHAR(SYSDATE, 'YYYYMMDD'), "						   	   );
	                	sql.append("			?, ?, ?, "					   						   );
	                	sql.append("   			TO_NUMBER(nvl(trim(?),0)), TO_NUMBER(nvl(trim(?),0)), ");
	                	sql.append("        	SYSDATE, 'N'" 										   );   
	                	sql.append("        )"													   	   );
	                	
	                	pstmt = conn.prepareStatement(sql.toString());
	                	pstmt.setInt(1, this._i_store_code);
	                	pstmt.setString(2, group_no);
	                	pstmt.setString(3, item_code);
	                	pstmt.setString(4, sohQty);
	                	pstmt.setString(5, real_qty);
	                	
	                	if (pstmt.executeUpdate() <=0 ){
	                        rejCount++;
	                    }
	                	
	                	pstmt.close();
	                	
	                }else{
	                	
	                	sql = new StringBuffer();
	                	sql.append("UPDATE OUT_OF_SHELF_ANALYSIS    ");
	                	sql.append("   SET TIME_STAMP    = SYSDATE,   ");
	                	sql.append("       COUNT_QTY     = TO_NUMBER(?),   ");
	                	sql.append("       STOCK_ON_HAND = TO_NUMBER(?)   ");
	                	sql.append(" WHERE STORE_NO  = ?");
	                	sql.append("   AND ITEM_NO   = ?   ");
	                	sql.append("   AND WORK_DATE = TO_CHAR(SYSDATE, 'YYYYMMDD')   ");
	                	sql.append("   AND GROUP_NO  = ?   ");	                	
	                
	                	pstmt = conn.prepareStatement(sql.toString());	                	
	                	pstmt.setString(1, real_qty);
	                	pstmt.setString(2, sohQty);
	                	pstmt.setInt(3, this._i_store_code);
	                	pstmt.setString(4, item_code);
	                	pstmt.setString(5, group_no);
	                	
	                	if (pstmt.executeUpdate() <=0 ){
	                        rejCount++;
	                	}
	                }
			}
        	
        	return rejCount;
			
		}		
		catch(SQLException se) {
            try {
                conn.rollback();
                conn.close();
            } catch (Exception e) {}                  
            conn = OraConnFactory.getInstance().getConnection();
            throw se;
              
        } catch(Exception e) {
            throw e;		      
		} finally {                  
		    try{
		        if (pstmt != null)
		            pstmt.close();
		    } catch(Exception e){}
		   try{
		        cp.releaseConnection(conn);
		    } catch(Exception e){}
		}
	}
}
	
	

