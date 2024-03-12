/*****************************************************
 * PROGRAM ID    : ImsSave
 * PROGRAM NAME  : 진열수(Pres Stock) 저장부분
 * CREATION DATE : 2014
 *****************************************************/ 
package tesco.got.dbTran;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import awoo.dbUtil.ConnPool;
import awoo.dbUtil.OraConnFactory;

public class ImsSave {
	private int    _i_store_code = 0; 
	private String _item = null;
	private int _pres_stock = 0;
	private String _requestor = null;
  
    public ImsSave(String store_code, String item, String pres_stock, String requestor) {
    	this._i_store_code = Integer.parseInt(store_code);        
    	this._item        = item;
    	this._pres_stock  = Integer.parseInt(pres_stock);
    	this._requestor   = requestor;
    }    

	public int executeQuery() throws GotException,Exception {
		StringBuffer sql = null;
		
		Connection conn = null;
        ConnPool cp = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
          
		try {
			
			if(this._pres_stock <= 1)
			{
				throw new GotException("적정재고 값 최소단위는 2입니다.변경 적정재고를 확인해 주시기 바랍니다.");
			}
						
			cp = ConnPool.getInstance();
			conn = cp.getConnection();		
			
			// 2015-08-25 추가. 이종욱. ACTIVE DATE 가 저장 당일 이후인 경우 적정재고 변경 불가.
			sql = new StringBuffer();			
			sql.append("SELECT CASE WHEN NVL(A.ACTIVATE_DATE - SYSDATE, 0) <= 0 "									   );
			sql.append("				  			 THEN 'Y' ELSE 'N' END AS ACTIVE_DATE "									   );
            sql.append("  FROM PLU A	 "					   );
            sql.append(" WHERE A.STORE_NO  = ?"							   );
            sql.append("   AND A.ITEM_NO   = ?"							   );
            sql.append("   AND NOT EXISTS (SELECT BUNDLE_ITEM_NO FROM BUNDLE_ITEM WHERE BUNDLE_ITEM_NO = A.ITEM_NO)"	   );
            pstmt = conn.prepareStatement(sql.toString());	                
            
            pstmt.setInt(1, _i_store_code);
            pstmt.setString(2, _item);
            
            rs = pstmt.executeQuery();	  	               
                        
            if(rs.next() && rs.getString("ACTIVE_DATE").equalsIgnoreCase("N")){            	
            	throw new Exception("Active date가 금일 이후인 상품은 적정재고를 변경할 수 없습니다.");
            }
            // END
			
			int resultCnt	  = 0;
			
			//cp = ConnPool.getInstance();
			//conn = cp.getConnection();		
			                        
			//해당 item이 승인 또는 반려된 건인지 확인
			sql = new StringBuffer();			
			sql.append("SELECT STATUS "									   );
            sql.append("  FROM PRES_STOCK_REQUEST	 "					   );
            sql.append(" WHERE STORE_NO  = ?"							   );
            sql.append("   AND ITEM_NO   = ?"							   );
            sql.append("   AND REQUEST_DATE = TO_DATE(TO_CHAR(SYSDATE,'YYYYMMDD'),'YYYYMMDD')"	   );
            pstmt = conn.prepareStatement(sql.toString());	                
            
            pstmt.setInt(1, _i_store_code);
            pstmt.setString(2, _item);
            
            rs = pstmt.executeQuery();	  	               
                        
            if(rs.next()){
            	int status = rs.getInt("STATUS");
            	rs.close();
                pstmt.close();		
                
            	if( status == 2 ){	//승인
            		throw new Exception("이미 승인된 항목입니다.");
            	}
            	else if( status == 3 ){	//반려
            		throw new Exception("이미 반려된 항목입니다.");
            	}
            	else {	//업데이트
            		sql = new StringBuffer();
                	sql.append("UPDATE PRES_STOCK_REQUEST    ");
                	sql.append("   SET PRES_STOCK    = ?   ");
                	sql.append(" WHERE STORE_NO  = ?");
                	sql.append("   AND ITEM_NO   = ?   ");
                	sql.append("   AND REQUEST_DATE = TO_DATE(TO_CHAR(SYSDATE,'YYYYMMDD'),'YYYYMMDD')  ");
                	
                	pstmt = conn.prepareStatement(sql.toString());	                	
                	pstmt.setInt(1, this._pres_stock);
                	pstmt.setInt(2, this._i_store_code);
                	pstmt.setString(3, this._item);
                	
                	resultCnt = pstmt.executeUpdate();
                	pstmt.close();
            	}
            }
            else{	//신규
            	rs.close();
                pstmt.close();		
                
                sql = new StringBuffer();
            	sql.append("INSERT INTO PRES_STOCK_REQUEST( STORE_NO, ITEM_NO, REQUEST_DATE, PRES_STOCK, STATUS, REQUESTOR ) ");
            	sql.append("VALUES( ?, ?, TO_DATE(TO_CHAR(SYSDATE,'YYYYMMDD'),'YYYYMMDD'), ?, 1, ? ) " );
            	
            	pstmt = conn.prepareStatement(sql.toString());
            	pstmt.setInt(1, this._i_store_code);
            	pstmt.setString(2, this._item);
            	pstmt.setInt(3, this._pres_stock);
            	pstmt.setString(4, this._requestor);
            	
            	resultCnt = pstmt.executeUpdate();
            	pstmt.close();
            }
            
            return resultCnt;
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
	
	

