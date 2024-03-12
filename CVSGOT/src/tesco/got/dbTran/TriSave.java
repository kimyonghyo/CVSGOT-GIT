/*****************************************************
 * PROGRAM ID    : TriSave
 * PROGRAM NAME  : RegTrash(폐기등록) 저장
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

public class TriSave {
	private int    _i_store_code = 0;
	
	private Object[] _items = null;
	private String _user_id = "";
       
    public TriSave(String store_code, Object[] items, String user_id) {
    	this._i_store_code = Integer.parseInt(store_code);      	
    	this._items   = items;
    	this._user_id = user_id;    	       
    }    
	/**	 
	 * @return
	 * @throws GotException
	 * @throws Exception
	 */
	public String executeQuery() throws GotException,Exception {
	    Connection conn = null;
        ConnPool cp = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;	     
	          
		StringBuffer sql = null;
		String adj_no = "1";
		String item_code = "";
		String adj_qty= "";
        String resaon_code= "";
        
		try {
			cp = ConnPool.getInstance();
			conn = cp.getConnection();
  			
		    sql = new StringBuffer();
		    sql.append("INSERT INTO INV_ADJ_LOG_TMP"	);
		    sql.append("("								);
		    sql.append("	STORE_NO, ADJ_NO, ITEM_NO, LOC_NO, LOG_DATE, ADJ_QTY, EMPLOYEE_NO, REASON_CODE, STOCK_VALUE_ADJ, STATUS");
		    sql.append(")"								);
		    sql.append("SELECT ?,    "					);
		    sql.append(" 	   TO_NUMBER(?), "			); // 재고조정 번호
		    sql.append("       ITEM_NO, "				);
		    sql.append("       '20', "					);
		    sql.append("       SYSDATE, "				);
		    sql.append("       TO_NUMBER(?), "			); // 재고조정 수량
		    sql.append("       ?, "						); // User Id
		    sql.append("       TO_NUMBER(?), "			); // reason code 
		    sql.append("       NVL(UNIT_COST, 0)*TO_NUMBER(?), "); // 재고조정 수량
		    sql.append("       '10'     "				); // status
		    sql.append("  FROM PLU"						);
		    sql.append(" WHERE ITEM_NO  = TRIM(?) "		);
		    sql.append("   AND STORE_NO = ?"			);
		    		    
		    Object[] data = null;
            for(int i = 3; i < this._items.length; i++){
            	            	       			
                data = StringUtil.split(this._items[i].toString(), Common.GS);

                adj_no = SetSeqNo(conn);
                
                if(adj_no == null) {
                	throw new GotException("폐기등록 저장 실패!\n재시도 하세요.");
        		} else {
	                item_code 	= data[0].toString();
	                adj_qty		= data[1].toString();
	                resaon_code	= data[2].toString();
	
	                pstmt = conn.prepareStatement(sql.toString());
	                
	                pstmt.setInt(1, this._i_store_code);
	    			pstmt.setString(2,adj_no);
	    			pstmt.setString(3,adj_qty);
	    			pstmt.setString(4, this._user_id);
	    			pstmt.setString(5,resaon_code);
	    			pstmt.setString(6,adj_qty);			
	    			pstmt.setString(7,item_code);    			
	    			pstmt.setInt(8, this._i_store_code);
	    			
	    			if(pstmt.executeUpdate()<=0){
	    				throw new GotException("폐기등록 저장 실패!\n재시도 하세요.");
	    			} 
        		}
            }              
            		                
            return adj_no;					            
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
	        sql.append("SELECT SEQ_NUMBER+1 AS ADJ_NO  ");
	        sql.append("  FROM SEQUENCE   "				);
	        sql.append(" WHERE STORE_NO = ?"			);
	        sql.append("   AND SEQ_NAME = 'ADJ'"		);

			pstmt = conn.prepareStatement(sql.toString());
			pstmt.setInt(1,  this._i_store_code);
			rs = pstmt.executeQuery();
			
			if(rs.next()){	    				
				seq	= rs.getString("ADJ_NO");
				rs.close();
				
				sql = new StringBuffer();
	   			sql.append("UPDATE SEQUENCE "					);
	   			sql.append("   SET SEQ_NUMBER = SEQ_NUMBER + 1 ");
	   			sql.append(" WHERE STORE_NO   = ?"				);
	   			sql.append("   AND SEQ_NAME   = 'ADJ'"			);
				
				pstmt = conn.prepareStatement(sql.toString());
				pstmt.setInt(1, this._i_store_code);				
				pstmt.executeUpdate();    			
				
			} else {
				sql = new StringBuffer();
				sql.append("INSERT INTO SEQUENCE(STORE_NO, SEQ_NAME, SEQ_NUMBER, TEXT, SEQ_DATE) ");
				sql.append("VALUES(?, 'ADJ', 1, '재고조정번호', SYSDATE)");
				
				pstmt = conn.prepareStatement(sql.toString());
				pstmt.setInt(1,  this._i_store_code);			
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