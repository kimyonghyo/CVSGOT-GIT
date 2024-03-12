/*****************************************************
 * PROGRAM ID    : SidQuery
 * PROGRAM NAME	 : 상품조회(일일 판매 수량 조회)
 * CREATION DATE : 2013
 *****************************************************
 *****************************************************
 *  변경일자    /  변경자  / 변경사유
 ******************************************************/

package tesco.got.dbTran;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import tesco.got.Common;
import awoo.dbUtil.ConnPool;
import awoo.dbUtil.OraConnFactory;

public class SidQuery {
	private String _s_store_code = "";
	private int    _i_store_code = 0;
	private String _barcode      = "";
    
    /**
     * @param store_code
     * @param item_code
     */
    public SidQuery(String store_code, String barcode) {
    	this._s_store_code = store_code;
    	this._i_store_code = Integer.parseInt(store_code);    	
        this._barcode      = barcode;     
    }
    
    public String executeQuery() throws GotException,Exception{
    	StringBuffer sql = null;
        
        Connection conn = null;
        ConnPool cp = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try{
            cp = ConnPool.getInstance();
            conn = cp.getConnection();
            
            String status = "";
            String item_no = "";
            String short_descr = "";
            String saledate_count = "";
            String work_date = "";
            int sale_qty = 0;
            int order_qty = 0;
            int receipt_qty = 0;
     
            sql = new StringBuffer();
            sql.append("SELECT DECODE(A.STATUS, 1, '0', '2') AS STATUS, ");
            sql.append("       A.ITEM_NO, "							 	 );
            sql.append("  	   B.SHORT_DESCR, "							 );
            sql.append("      (SELECT LPAD(NVL(TRUNC(SYSDATE)-TO_DATE(MIN(SALE_DATE),'yyyymmdd'),0),2,'0') ");
            sql.append("         FROM ITEM_SALES "										);
            sql.append("        WHERE STORE_NO = A.STORE_NO "			 				);
            sql.append("     	  AND ITEM_NO  = A.ITEM_NO  "			 			    );
            sql.append("     	  AND SALE_DATE BETWEEN TO_CHAR(SYSDATE-14,'yyyymmdd') ");
            sql.append("     	  AND TO_CHAR(SYSDATE-1,'yyyymmdd')) AS SALEDATE_COUNT ");
            sql.append("  FROM PLU A, ITEM B "			  );
            sql.append(" WHERE A.ITEM_NO  = B.ITEM_NO "	  );
            sql.append("   AND A.STORE_NO = ? "		  	  );
            sql.append("   AND A.ITEM_NO  = ? "		  	  );
            				
			pstmt = conn.prepareStatement(sql.toString());
			pstmt.setInt(1,this._i_store_code);
			pstmt.setString(2,this._barcode);
			rs = pstmt.executeQuery();
			
			if(!rs.next()){ 
				throw new GotException( "존재하지 않는 상품코드입니다. : " + this._barcode);
//			    this._barcode = Etc.convertItemCodeRtc(this._barcode);
//			    rs.close();
//			    
//				pstmt.setInt(1,this._i_store_code);
//				pstmt.setString(2,this._barcode);
//				
//			    rs = pstmt.executeQuery();
//			    if (!rs.next()){
//			        throw new GotException( "존재하지 않는 상품코드입니다. : " + this._barcode);
//			    }			    
			}           

			status 		   = rs.getString("STATUS");
			item_no		   = rs.getString("ITEM_NO");
			short_descr	   = rs.getString("SHORT_DESCR");
			saledate_count = rs.getString("SALEDATE_COUNT");

			rs.close();                
            pstmt.close();                  
                        
            StringBuffer tmp = new StringBuffer(status);
            tmp.append(Common.FS);
            tmp.append(item_no);
            tmp.append(Common.FS);
            tmp.append(short_descr);
            tmp.append(Common.FS);
            tmp.append(saledate_count);         
                        
            sql = new StringBuffer();
			sql.append("SELECT A.WORK_DATE, "						); 
			sql.append("       B.SALE_QTY,  "						); 
			sql.append("       C.ORDER_QTY, "						); 
			sql.append("       D.RECEIPT_QTY"  						);
			sql.append(" FROM ("									);
			sql.append("      	SELECT TO_CHAR(SYSDATE - (ROWNUM - 1), 'yyyyMMdd')  WORK_DATE");
			sql.append("          FROM CODES"												  );
			sql.append("         WHERE ROWNUM <= 15"										  ); 
			sql.append("      ) A, "														  );
			sql.append("      (");
			sql.append("        SELECT  SALE_DATE, NVL(MAX(SALE_QTY),0) SALE_QTY"			  							  ); 
			sql.append("          FROM ITEM_SALES  "																	  );
			sql.append("         WHERE SALE_DATE BETWEEN TO_CHAR(SYSDATE-14,'yyyymmdd') AND TO_CHAR(SYSDATE,'yyyymmdd')"  ); 
			sql.append("           AND STORE_NO = ?"																	  );
			sql.append("           AND ITEM_NO  = ?"																	  );
			sql.append("         GROUP BY SALE_DATE"																	  );
			sql.append("      ) B,"																						  );
			sql.append("      ("																						  );
			sql.append("      	SELECT ORDER_DATE, ORDER_QTY FROM V_PDA_ORDER WHERE STORE_NO = ? AND ITEM_NO = ?"		  );
			sql.append("      ) C, "																					  );
			sql.append("      ("																						  );
			sql.append("      	SELECT RECEIPT_DATE, RECEIPT_QTY FROM V_PDA_RECEIPT WHERE STORE_NO = ? AND ITEM_NO = ?"	  );
			sql.append("      ) D"																						  );
			sql.append("  WHERE A.WORK_DATE = B.SALE_DATE(+)"															  );
			sql.append("    AND A.WORK_DATE = C.ORDER_DATE(+)"															  );
			sql.append("    AND A.WORK_DATE = D.RECEIPT_DATE(+)"														  );
			sql.append("    AND ("																						  );
			sql.append("    		B.SALE_QTY > 0    "																  	  ); 
			sql.append("         OR C.ORDER_QTY > 0   "															  		  );
			sql.append("         OR D.RECEIPT_QTY > 0 "															 	 	  );
			sql.append("        )"																						  );
			sql.append("  ORDER BY WORK_DATE DESC"																		  );

            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setInt(1,this._i_store_code);
			pstmt.setString(2, item_no);
			pstmt.setInt(3,this._i_store_code);
			pstmt.setString(4, item_no);
			pstmt.setInt(5,this._i_store_code);
			pstmt.setString(6, item_no);
            
            rs = pstmt.executeQuery();
            
			int rownum = 0;
			
			while(rs.next()){
	        	work_date   = rs.getString("WORK_DATE");		        	
	        	order_qty   = rs.getInt("ORDER_QTY");
	        	receipt_qty = rs.getInt("RECEIPT_QTY");
	        	sale_qty    = rs.getInt("SALE_QTY");
	        	
	        	tmp.append(Common.FS);
	       	
	        	tmp.append(work_date);
	       		tmp.append(Common.GS);
	       		tmp.append(order_qty);
	       		tmp.append(Common.GS);
	       		tmp.append(receipt_qty);
	       		tmp.append(Common.GS);
	       		tmp.append(sale_qty);
	       		
	       		rownum++;
            }
			
            rs.close();
            pstmt.close();
            
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
