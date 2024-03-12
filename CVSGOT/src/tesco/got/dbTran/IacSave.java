/*****************************************************
 * PROGRAM ID    : IacSave
 * PROGRAM NAME  : 검수확인 저장부분
 * CREATION DATE : 2013/11/11
 * 2014.07.01 김진승이사 Hub& Spoke 대출입 관련 제외 
 *****************************************************/ 
package tesco.got.dbTran;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import tesco.got.Common;
import tesco.got.Configuration;
import awoo.dbUtil.ConnPool;
import awoo.dbUtil.OraConnFactory;
import awoo.util.FileLogger;
import awoo.util.StringUtil;
import awoo.util.Logger;

public class IacSave {

	private String _s_store_code = "";      
	private int    _i_store_code = 0;	
	private Object[] _data = null;
	private Configuration config = null;
	private Logger logger = null;
	
    public IacSave(String store_code, Object[] data) {
    	this._s_store_code = store_code;
    	this._i_store_code = Integer.parseInt(store_code); 
    	this._data         = data;  
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
			
			config = Configuration.getInstance();
	        logger = new FileLogger(config.getLogFolder());

			String item_code	= "";
			String recpt_date	= "";
			String tg_no		= "";
			String wh_qty		= "";
            String accept_qty	= "";
            String search_cd	= "";
			
            String status_cd    = "";
			String item_exist   = "";
			
            int rejCount = 0;
            
            Object[] arrItems = null;
                             
            for(int i = 3; i < this._data.length; i++) {

            	arrItems = StringUtil.split(this._data[i].toString(), Common.GS);
            	
                item_code  = arrItems[0].toString();
                recpt_date = arrItems[1].toString();
                tg_no	   = arrItems[2].toString();
                wh_qty	   = arrItems[3].toString();
                accept_qty = arrItems[4].toString();
                search_cd  = arrItems[5].toString();
                
                sql = new StringBuffer();
                sql.append("SELECT B.ITEM_NO, B.STATUS_CONF "			  ); 
                sql.append("  FROM BOLS A, FSM_TRF_ITEM_ACCEPTANCE B"	  );
        		sql.append(" WHERE A.STORE_NO = B.STORE_NO(+) "			  );
        		sql.append("   AND A.TG_NO    = B.TG_NO(+) "			  );  
        		sql.append("   AND A.STORE_NO = ?  "					  );
        		sql.append("   AND A.TG_NO    = ?  " 					  );
        		sql.append("   AND A.STATUS   = 30 " 					  );
//2014.07.01
//				sql.append("   AND A.TRUCK_NO IS NULL"					  );
        		sql.append("   AND B.ITEM_NO(+) = ?	"					  );
        		sql.append("   AND B.TRF_TYPE(+)= ? "                     );
        		
                pstmt = conn.prepareStatement(sql.toString());
                pstmt.setInt(1, this._i_store_code);
                pstmt.setString(2, tg_no);
                pstmt.setString(3, item_code);
                pstmt.setString(4, search_cd);
                
				rs = pstmt.executeQuery();
								
				if(rs.next()){
					item_exist = rs.getString("ITEM_NO");
					status_cd  = rs.getString("STATUS_CONF");					
				} 

				rs.close();
				pstmt.close();				    		
				
				if( item_exist != null ) { 			
					if(!status_cd.equals("10"))  { // 해당  TG 최초 등록 상태가 아닌 경우 반려
						rejCount++;						
						continue;
					} else {					
						sql = new StringBuffer();
						
						if(wh_qty.equals(accept_qty)) { // 조사수량 일치시 저장 필요없음
							sql.append("DELETE FROM FSM_TRF_ITEM_ACCEPTANCE ");
				            sql.append(" WHERE TG_NO    = ? ");
		            		sql.append("   AND ITEM_NO  = ? ");
		            		sql.append("   AND TRF_TYPE = ? ");
		            		sql.append("   AND STORE_NO = ? ");
		            		
		            		pstmt = conn.prepareStatement(sql.toString());
							pstmt.setString(1,tg_no);
							pstmt.setString(2,item_code);
							pstmt.setString(3,search_cd);
							pstmt.setInt(4, this._i_store_code);
						} else {	
							sql.append("UPDATE FSM_TRF_ITEM_ACCEPTANCE   ");
				            sql.append("   SET ACCEPT_QTY = ?    ");
				            sql.append(" WHERE TG_NO    = ? ");
		            		sql.append("   AND ITEM_NO  = ? ");
		            		sql.append("   AND TRF_TYPE = ? ");
		            		sql.append("   AND STORE_NO = ? ");
				            
							pstmt = conn.prepareStatement(sql.toString());
							pstmt.setString(1,accept_qty);
							pstmt.setString(2,tg_no);
							pstmt.setString(3,item_code);
							pstmt.setString(4,search_cd);
							pstmt.setInt(5, this._i_store_code);
						}
						
						pstmt.executeUpdate();
						pstmt.close();
					}					
				} else {
/*					sql = new StringBuffer();	
		            sql.append("INSERT INTO FSM_TRF_ITEM_ACCEPTANCE");
		            sql.append("(");
		            sql.append("	FROM_STORE, TRANSFER_DATE, RECEIPT_DATE, TG_NO, TRF_TYPE, STORE_NO, ITEM_NO, REC_QTY, ACCEPT_QTY, CREATE_DATE, STATUS_CONF ");
		            sql.append(")");
		            sql.append("SELECT FROM_STORE, TRANSFER_DATE, RECEIPT_DATE, ?, ?, ?, ?, ?, ?, SYSDATE, '10' ");
		            sql.append("  FROM BOLS 	   ");
		            sql.append(" WHERE TG_NO    = ?");
		            sql.append("   AND STORE_NO = ?");
		            sql.append("   AND ? <> ?");
*/
//20140701
					sql = new StringBuffer();	
		            sql.append("INSERT INTO FSM_TRF_ITEM_ACCEPTANCE");
		            sql.append("(");
		            sql.append("	FROM_STORE, TRANSFER_DATE, RECEIPT_DATE, IN_PRICE , TG_NO, TRF_TYPE, STORE_NO, ITEM_NO, REC_QTY, ACCEPT_QTY, CREATE_DATE, STATUS_CONF ");
		            sql.append(")");
		            sql.append("SELECT FROM_STORE, TRANSFER_DATE, RECEIPT_DATE, TRUCK_NO, ?, ?, ?, ?, ?, ?, SYSDATE, '10' ");
		            sql.append("  FROM BOLS 	   ");
		            sql.append(" WHERE TG_NO    = ?");
		            sql.append("   AND STORE_NO = ?");
		            sql.append("   AND ? <> ?");
		            
					pstmt = conn.prepareStatement(sql.toString());
					pstmt.setString(1, tg_no);
					pstmt.setString(2, search_cd);
					pstmt.setInt(3, this._i_store_code);
					pstmt.setString(4, item_code);
					pstmt.setString(5, wh_qty);
					pstmt.setString(6, accept_qty);
					pstmt.setString(7, tg_no);
					pstmt.setInt(8, this._i_store_code);
					pstmt.setString(9, wh_qty);
					pstmt.setString(10, accept_qty);
					
					pstmt.executeUpdate();
					pstmt.close();
				}
				
				sql = new StringBuffer();					
				sql.append("UPDATE BOL_ITEMS 		");
				sql.append("   SET INITIATED = 1 	");
				sql.append(" WHERE STORE_NO  = ?	");
				sql.append("   AND TG_NO 	 = ?	");
				sql.append("   AND ITEM_NO   = ?	");
				
				pstmt = conn.prepareStatement(sql.toString());
				pstmt.setInt(1, this._i_store_code);
				pstmt.setString(2,tg_no);							
				pstmt.setString(3,item_code);
				
				
				//logger.writeEntry("sql : " + sql.toString());
				logger.writeEntry("this._s_store_code : "+ this._s_store_code + " tg_no : " + tg_no + " item_code : " + item_code);

				
				pstmt.executeUpdate();	
				pstmt.close();
            }
            
            conn.commit();
            
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
}