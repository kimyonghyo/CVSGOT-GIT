/*****************************************************
 * PROGRAM ID    : FssSave
 * PROGRAM NAME  : 신선재고조사 저장부분
 * CREATION DATE : 2013
 *****************************************************/ 
package tesco.got.dbTran;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import tesco.got.Common;
import awoo.dbUtil.ConnPool;
import awoo.dbUtil.OraConnFactory;
import awoo.util.StringUtil;

public class FssSave {
	private int    _i_store_code = 0;	
	private String _ip 			 = "";
	private String _survey_date  = "";
	private Object[] _items 	 = null;
	private String _user_id 	 = "";
	
  
    public FssSave(String store_code,  String ip, String survey_date, Object[] items, String user_id) {
    	this._i_store_code = Integer.parseInt(store_code);        
    	this._ip 		   = ip;
    	this._survey_date  = survey_date;
    	this._items		   = items;
    	this._user_id	   = user_id;    	
    }   
    
	/** 
	 * @return
	 * @throws GotException
	 * @throws Exception
	 */
	public void executeQuery() throws GotException,Exception {
		StringBuffer sql = null;	
		Connection conn = null;
        ConnPool cp = null;
        PreparedStatement pstmt = null;

		try {
			cp = ConnPool.getInstance();
			conn = cp.getConnection();

			String item_code = "";
			String qty		 = "";			
			
            sql = new StringBuffer();			
            sql.append("INSERT INTO FF_PDA_COUNTING 	");
            sql.append("(");
            sql.append("	STORE_NO, COUNT_DATE, PDA_ID, ITEM_NO, COUNTED_QTY, CREATE_DATE, CREATE_TIME, CREATE_RESNO, STATUS");
            sql.append(")");
            sql.append("VALUES "					  	 );
            sql.append("( "						  	 	 );
            sql.append(" 	?, "						 );
            sql.append("    ?, "						 );
            sql.append("    LPAD(?,3,'0'), "			 );
            sql.append("    ?, "						 );
            sql.append("    TO_NUMBER(?), "				 );
            sql.append("    TO_CHAR(SYSDATE,'YYYYMMDD'),");
            sql.append("    TO_CHAR(SYSDATE,'HH24MISS'),");
            sql.append("    ?, "						 );
            sql.append("    'N'"						 );
            sql.append(")");

			pstmt = conn.prepareStatement(sql.toString());	
   
        	for(int i = 5; i < this._items.length; i++){
				Object[] tmp = StringUtil.split(this._items[i].toString(),Common.GS);
				
				item_code = tmp[0].toString();
				qty		  = tmp[1].toString();	                	          
				
				pstmt.setInt(1, this._i_store_code);
				pstmt.setString(2, this._survey_date);
				pstmt.setString(3, this._ip);
				pstmt.setString(4, item_code);	                
				pstmt.setString(5, qty);
				pstmt.setString(6, this._user_id);
				
				pstmt.executeUpdate();	        
			}        	     				
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