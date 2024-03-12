/*****************************************************
 * PROGRAM ID    : AdsQuery
 * PROGRAM NAME  : 재고조정 조회
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

public class AdsQuery {
	private int    _i_store_code = 0;
	private String _barcode      = "";
	
	/**
     * @param store_code
     * @param item_code
     */
    public AdsQuery(String store_code, String barcode) {
    	this._i_store_code = Integer.parseInt(store_code); 
    	this._barcode      = barcode;     
    }
    
    public String executeQuery() throws GotException,Exception{
    	StringBuffer sql = null;
    	
        Connection conn = null;
        ConnPool cp = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        String item_code = "";
        String short_descr = "";            
        String bundle_item	= "";
        String on_hand_qty = "";
        String count_qty = "";
        String seq = "1";
        
        try{
            cp = ConnPool.getInstance();
            conn = cp.getConnection();
            
//            sql = new StringBuffer();
//            sql.append("SELECT SEQ_NO   ");
//            sql.append("  FROM INV_ADJ_COUNT_SEQ   ");
//            sql.append(" WHERE WORK_DATE = TO_CHAR(SYSDATE, 'YYYYMMDD')   ");
//            sql.append("   AND STORE_NO  = ? ");
//   
//			pstmt = conn.prepareStatement(sql.toString());
//			pstmt.setInt(1,  this._i_store_code);
//			rs = pstmt.executeQuery();
//			
//			if(rs.next()){	    				
//				seq	= rs.getString("SEQ_NO");
//				rs.close();
//			}else{
//    			rs.close();
//    			pstmt.close();
//    			
//				sql = new StringBuffer();
//				sql.append("INSERT INTO INV_ADJ_COUNT_SEQ (STORE_NO,WORK_DATE,SEQ_NO)");
//				sql.append("VALUES (?, TO_CHAR(SYSDATE, 'YYYYMMDD'), 1)"			  );
//				
//				pstmt = conn.prepareStatement(sql.toString());
//				pstmt.setInt(1,  this._i_store_code);
//				pstmt.executeUpdate();    				    						
//			}    			
//			
//			pstmt.close();  
			
            sql = new StringBuffer();
            sql.append("SELECT A.ITEM_NO, "													 );
            sql.append("	   B.SHORT_DESCR, "												 );                       
            sql.append("       DECODE(NVL(C.BUNDLE_ITEM_NO,'S'),'S','S','B') AS BUNDLE_ITEM,");
            sql.append("       NVL((SELECT SUM(ON_HAND_QTY)"								 );
            sql.append("         	  FROM STOCK_ON_HAND"									 );
            sql.append("         	 WHERE STORE_NO = A.STORE_NO " 						     );
            sql.append("         	   AND ITEM_NO  = A.ITEM_NO "							 );
            sql.append("         	   AND LOC_NO   = 20),0) ON_HAND_QTY,"		     	     );
            sql.append("       NVL((SELECT MAX(COUNT_QTY) "									 );
            sql.append("		      FROM INV_ADJ_COUNT"									 );
			sql.append("		     WHERE WORK_DATE = TO_CHAR(SYSDATE, 'yyyyMMdd')"		 );
			sql.append("  		       AND STORE_NO  = A.STORE_NO"							 );
			sql.append("  		       AND LOC_NO	 = '20'"								 );
			sql.append("  		       AND ITEM_NO 	 = A.ITEM_NO"							 );
//			sql.append("  		       AND SEQ_NO  	 = ?"									 );
			sql.append("  		       AND STATUS    = 10),0) COUNT_QTY"					 );
            sql.append("  FROM PLU A, ITEM B, BUNDLE_ITEM C"				 				 );            
            sql.append(" WHERE A.ITEM_NO  = B.ITEM_NO"									 	 );
            sql.append("   AND A.ITEM_NO  = C.BUNDLE_ITEM_NO(+)"							 );
            sql.append("   AND A.ITEM_NO  = ?"										 	 	 );
            sql.append("   AND A.STORE_NO = ?"											 	 );
            
			pstmt = conn.prepareStatement(sql.toString());
//			pstmt.setString(1, seq);
			pstmt.setString(1, this._barcode);					
			pstmt.setInt(2, this._i_store_code);
			
			rs = pstmt.executeQuery();
			
			if(!rs.next()){ 
				throw new GotException( "존재하지 않는 상품코드입니다. : " + this._barcode);
			}
			
			item_code	= rs.getString("ITEM_NO");
			short_descr = rs.getString("SHORT_DESCR");			
			bundle_item	= rs.getString("BUNDLE_ITEM");
			on_hand_qty = rs.getString("ON_HAND_QTY");
			count_qty   = rs.getString("COUNT_QTY");
			
			rs.close();                
            pstmt.close();
            
            if(bundle_item.equalsIgnoreCase("B")){
            	throw new GotException("번들상품입니다. 폐기등록 할 수 없습니다. : " + this._barcode);
            }
            
            StringBuffer tmp = new StringBuffer();
            tmp.append(item_code);
            tmp.append(Common.FS);
            tmp.append(short_descr);
            tmp.append(Common.FS);
            tmp.append(on_hand_qty);
            tmp.append(Common.FS);
            tmp.append(count_qty);            
            tmp.append(Common.FS);
            
            return tmp.toString();                
        }
        catch(SQLException se){
        	try
        	{
        		conn.close();
        	}
        	catch (Exception e) {}        	
        	conn = OraConnFactory.getInstance().getConnection();
        	throw se;
        }
        catch(Exception e){
        	throw e;
        }
        finally{
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