/*****************************************************
 * PROGRAM ID    : RtcQuery
 * PROGRAM NAME	 : RTC라벨출력 (조회)
 * CREATION DATE : 2013
 *****************************************************
 *****************************************************
 *  변경일자   /  변경자 / 변경사유
 *  
 ******************************************************/  

package tesco.got.dbTran;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import tesco.got.Common;
import awoo.dbUtil.ConnPool;
import awoo.dbUtil.OraConnFactory;

public class RtcQuery {
	private String _s_store_code = "";
	private int    _i_store_code = 0;
	private String _barcode      = "";

    /**
     * @param store_code
     * @param item_code
     */
    public RtcQuery(String store_code, String barcode) {
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
            
            String item_no			= "";
            String short_descr	= "";
            String price	= "";
            String shelf_no = "";
            String s_row = "";
            String s_col = "";
            String max_gb	= "";
            String max_rate	= "";
            String curDate = "";

            sql = new StringBuffer();
            sql.append("SELECT ITEM_NO ");
            sql.append("     , SHORT_DESCR");
            sql.append("     , DECODE(PRICING_METHOD, 4, EVENT_PRICE, NORMAL_PRICE) AS PRICE ");
            sql.append("     , TO_CHAR(SYSDATE, 'yyyyMMdd') AS CURDATE");
            sql.append("  FROM PLU ");
            sql.append(" WHERE STORE_NO = ?");
            sql.append("   AND ITEM_NO  = ?");
            
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setString(1,this._s_store_code);
            pstmt.setString(2,this._barcode);
            rs = pstmt.executeQuery();
           
            if (!rs.next()){
            	throw new GotException("존재하지 않는 상품입니다. : " + this._barcode);
            }
            
            item_no		 = rs.getString("ITEM_NO");
            short_descr	 = rs.getString("SHORT_DESCR");
            price		 = rs.getString("PRICE");
            curDate		 = rs.getString("CURDATE");
            
            rs.close();
            pstmt.close();

            sql = new StringBuffer(); 
            sql.append("SELECT ITEM_NO, NVL(min(DGNAME)||min(SHELFID), '') AS SHELF_NO, NVL(min(HFACINGS), '') AS S_ROW, NVL(min(VFACINGS), '') AS S_COL");
            sql.append("  FROM LOC_FACE_CAPA");
            sql.append(" WHERE STORE_NO = ? ");
            sql.append("   AND ITEM_NO  = ? ");
            sql.append(" GROUP BY ITEM_NO   ");
            
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setString(1, this._s_store_code);
            pstmt.setString(2, item_no);
            rs = pstmt.executeQuery();
            if (rs.next()){
            	shelf_no = rs.getString("SHELF_NO");
                s_row	 = rs.getString("S_ROW");
                s_col    = rs.getString("S_COL");                
            }
                        
            rs.close();
            pstmt.close();                 
            
            SimpleDateFormat sdf = new SimpleDateFormat("HH");
            int currentHour = Integer.parseInt(sdf.format(new Date()));
            
        	// RTC 가능 시간은 07시~23시 이다.
            if( !(currentHour >= 7 && currentHour <= 23) ) {
            	throw new GotException("RTC 가능 시간이 아닙니다.\n가능 시간 : 07시~23시");
            }
                                                          
            StringBuffer tmp = new StringBuffer(short_descr);
            tmp.append(Common.FS);
            tmp.append(shelf_no);
            tmp.append(Common.FS);
            tmp.append(s_row);
            tmp.append(Common.FS);
            tmp.append(s_col);
            tmp.append(Common.FS);
            tmp.append("70"); //max_rate
            tmp.append(Common.FS);
            tmp.append(price);
            tmp.append(Common.FS);
            tmp.append(1);
            tmp.append(Common.FS);
            tmp.append(item_no);
            tmp.append(Common.FS);
            tmp.append(curDate);
            
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
