/*****************************************************
 * PROGRAM ID    : AdsSave
 * PROGRAM NAME  : 재고조정 저장부분
 * CREATION DATE : 2013/11/11
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

public class AdsSave {
	private int    _i_store_code = 0;
    
	private Object[] _items = null;
	private String _user_id = "";
  
    public AdsSave(String store_code, Object[] items, String user_id) {
    	this._i_store_code = Integer.parseInt(store_code);      
    	this._items		   = items;
    	this._user_id	   = user_id;
    }    
	/** 
	 * @return
	 * @throws GotException
	 * @throws Exception
	 */
	public int executeQuery() throws GotException,Exception {
		StringBuffer sql = null;		

		Connection conn = null;
        ConnPool cp = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;        
        
		try {
			cp = ConnPool.getInstance();
			conn = cp.getConnection();

			String seq		 = "1";
			String item_code = "";
			String qty		 = "";            			
            int rejCount	 = 0;                   

            for(int i = 3; i < this._items.length; i++){
            	
            	Object[] tmp = StringUtil.split(this._items[i].toString(),Common.GS);

                item_code = tmp[0].toString();
                qty		  = tmp[1].toString();
                
                // 상신 찾기
            	sql = new StringBuffer();
    			sql.append("SELECT ITEM_NO "										);
    			sql.append("  FROM INV_ADJ_COUNT "									);
    			sql.append(" WHERE WORK_DATE = TO_CHAR(SYSDATE, 'YYYYMMDD') "		);
    			sql.append("   AND STORE_NO  = ? "									);
    			sql.append("   AND LOC_NO    = '20' "								);
    			sql.append("   AND ITEM_NO   = trim(?) "							);
    			sql.append("   AND STATUS    = 30"									);
    			
    			pstmt = conn.prepareStatement(sql.toString());	
                pstmt.setInt(1, this._i_store_code);
                pstmt.setString(2, item_code);                              
                rs = pstmt.executeQuery();
                
                if(rs.next()) {
                	rejCount++;
                	continue;
                } 
            
    			sql = new StringBuffer();
    			sql.append("UPDATE INV_ADJ_COUNT");
    			sql.append("   SET COUNT_TIME  = TO_CHAR(SYSDATE, 'HH24MISS'), "    );
    			sql.append("       COUNT_QTY   = NVL(COUNT_QTY,0) + TO_NUMBER(?), " );
    			sql.append("       EMPLOYEE_NO = ?, "           				    );
    			sql.append("       ON_HAND_QTY = (SELECT ON_HAND_QTY "			    );
    			sql.append("					    FROM STOCK_ON_HAND "			);
    			sql.append("					   WHERE STORE_NO = ? "				);
    			sql.append("					     AND ITEM_NO  = trim(?) "		);    			
    			sql.append("					     AND LOC_NO   = '20') "			);
    			sql.append(" WHERE WORK_DATE = TO_CHAR(SYSDATE, 'YYYYMMDD') "		);
    			sql.append("   AND STORE_NO  = ? "									);
    			sql.append("   AND LOC_NO    = '20' "								);
    			sql.append("   AND ITEM_NO   = trim(?) "							);
    			sql.append("   AND STATUS    = 10"									);
    			
    			pstmt = conn.prepareStatement(sql.toString());	
                pstmt.setString(1, qty);
                pstmt.setString(2, this._user_id);
                pstmt.setInt(3, this._i_store_code);
                pstmt.setString(4, item_code);
                pstmt.setInt(5, this._i_store_code);
                pstmt.setString(6, item_code);
                
                if(pstmt.executeUpdate() <=0){                	
                	try {
                		seq = SetSeqNo(conn);
                		
                		if(seq == null) {
                			rejCount++;	
                		} else {
                			sql = new StringBuffer();
		                	sql.append("INSERT INTO INV_ADJ_COUNT "									  );
		                	sql.append("("															  );
		                	sql.append("	WORK_DATE, STORE_NO, LOC_NO, ITEM_NO, SEQ_NO, COUNT_TIME,");
		                	sql.append("    COUNT_QTY, EMPLOYEE_NO, STATUS, ON_HAND_QTY, REASON_CODE ");
		                	sql.append(")"															  );
		                	sql.append("SELECT TO_CHAR(SYSDATE, 'YYYYMMDD'), ");
		                	sql.append(" 	   ?, "							  );
		                	sql.append("	   '20', "					  	  );
		                	sql.append("	   trim(?), "					  );		
		                	sql.append("       ?, "							  );
		                	sql.append("	   TO_CHAR(SYSDATE, 'HH24MISS'), ");
		                	sql.append("       TO_NUMBER(?), "				  );
		                	sql.append("	   ?, "							  );
		                	sql.append("	   10, "						  );
		                	sql.append("       ON_HAND_QTY, "				  );
		                	sql.append("       11 "				  			  ); // 기본은 '재고조정오류'로 등록
		                	sql.append("  FROM STOCK_ON_HAND "				  );
		                	sql.append(" WHERE STORE_NO = ? "				  );
		                	sql.append("   AND LOC_NO   = '20'"				  );
		                	sql.append("   AND ITEM_NO  = trim(?)"			  );
		        			
		                	pstmt = conn.prepareStatement(sql.toString());
		                	pstmt.setInt(1, this._i_store_code);
		                	pstmt.setString(2, item_code);
		                	pstmt.setString(3, seq);
		                	pstmt.setString(4, qty);
		                	pstmt.setString(5, this._user_id);
		                	pstmt.setInt(6, this._i_store_code);
		                	pstmt.setString(7, item_code);
		                	
		                	if(pstmt.executeUpdate()<=0){
		                		rejCount++;	
		                	} 
                		}
                	} catch (SQLException se) {
                		rejCount++;
                	} 
                	
                	pstmt.close();	                	
                }
                
                pstmt.close();                
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
		        if (rs != null)
		            rs.close();
		    } catch(Exception e){}
		    try{
		        if (pstmt != null)
		            pstmt.close();
		    } catch(Exception e){}
		   try{
		        cp.releaseConnection(conn);
		    } catch(Exception e){}
		}
	}
	
	public String SetSeqNo(Connection conn) throws GotException,Exception {
		StringBuffer sql = null;		
        PreparedStatement pstmt = null;
        ResultSet rs = null;    
        String seq = "1";
        
        sql = new StringBuffer();
        
        try {
	        sql.append("SELECT SEQ_NO + 1 AS SEQ_NO ");
	        sql.append("  FROM INV_ADJ_COUNT_SEQ   ");
	        sql.append(" WHERE WORK_DATE = TO_CHAR(SYSDATE, 'YYYYMMDD')   ");
	        sql.append("   AND STORE_NO  = ? ");
	
			pstmt = conn.prepareStatement(sql.toString());
			pstmt.setInt(1,  this._i_store_code);
			rs = pstmt.executeQuery();
			
			if(rs.next()){	    				
				seq	= rs.getString("SEQ_NO");
				rs.close();
				
				sql = new StringBuffer();
				sql.append("UPDATE INV_ADJ_COUNT_SEQ SET SEQ_NO = ? WHERE WORK_DATE = TO_CHAR(SYSDATE, 'YYYYMMDD') AND STORE_NO = ?");
				
				pstmt = conn.prepareStatement(sql.toString());
				pstmt.setString(1, seq);
				pstmt.setInt(2, this._i_store_code);				
				pstmt.executeUpdate();    			
				
			} else {
				sql = new StringBuffer();
				sql.append("INSERT INTO INV_ADJ_COUNT_SEQ (STORE_NO,WORK_DATE,SEQ_NO)");
				sql.append("VALUES (?, TO_CHAR(SYSDATE, 'YYYYMMDD'), ?)"			  );
				
				pstmt = conn.prepareStatement(sql.toString());
				pstmt.setInt(1,  this._i_store_code);
				pstmt.setString(2, seq);
				pstmt.executeUpdate();    
			}
			
			return seq;
			
        } catch(Exception e) {
            return null;		      
		} finally {
			try{
		        if (rs != null)
		            rs.close();
		    } catch(Exception e){}
		    try{
		        if (pstmt != null)
		            pstmt.close();
		    } catch(Exception e){}
		}
	}
}