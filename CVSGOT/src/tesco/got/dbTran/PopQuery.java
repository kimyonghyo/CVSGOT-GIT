/*****************************************************
 * PROGRAM ID    : PopQuery
 * PROGRAM NAME	 : Price Card(조회)
 * CREATION DATE : 2013
 *****************************************************
 *****************************************************
 *  변경일자    /  변경자  / 변경사유
 *    
 ******************************************************/

package tesco.got.dbTran;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;

import tesco.got.Common;
import awoo.dbUtil.ConnPool;
import awoo.dbUtil.OraConnFactory;

public class PopQuery {
	private String _s_store_code = "";
	private int    _i_store_code = 0;
	private String _barcode      = "";
    
    /**
     * @param store_code
     * @param item_code
     */
    public PopQuery(String store_code,String item_code){
    	this._s_store_code = store_code;
    	this._i_store_code = Integer.parseInt(store_code);   
    	this._barcode = item_code;
    }
    
    /**
     * @return
     * @throws Exception
     */
    public String executeQuery() throws GotException,Exception{
    	StringBuffer sql = new StringBuffer();
        
        Connection conn = null;
        ConnPool cp = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try{
            cp = ConnPool.getInstance();
            conn = cp.getConnection();
            
            String short_descr = "";
            String normal_price = "";
            String item_no = "";
			
			sql.append("SELECT NVL(B.SHORT_DESCR,' ') SHORT_DESCR, "													    );
			sql.append("	   ROUND(DECODE(A.PRICING_METHOD, '4', NVL(A.EVENT_PRICE+item_real_price(a.item_no, ?),0), ");
			sql.append("										   NVL(A.NORMAL_PRICE,0)+item_real_price(a.item_no, ?))");
			sql.append("            ,0) NORMAL_PRICE, "		); 
			sql.append("	   NVL(A.ITEM_NO,' ') ITEM_NO " );
			sql.append("  FROM PLU A, ITEM B"				);
			sql.append(" WHERE A.ITEM_NO  = B.ITEM_NO"   	);
			sql.append("   AND A.ITEM_NO  = ?"     			);
			sql.append("   AND A.STORE_NO = ?"     			);
			
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setInt(1, this._i_store_code);
            pstmt.setInt(2, this._i_store_code);
            pstmt.setString(3, this._barcode);
            pstmt.setInt(4, this._i_store_code);
            
            rs = pstmt.executeQuery();
            if(!rs.next()){
            	throw new GotException( "존재하지 않는 상품코드입니다. : " + this._barcode);
            }     
            
            short_descr  = rs.getString("SHORT_DESCR");
            normal_price = rs.getString("NORMAL_PRICE");
            item_no      = rs.getString("ITEM_NO");
            
            rs.close();
            pstmt.close();
            
            StringBuffer tmp = new StringBuffer(item_no);
            tmp.append(Common.FS);
            tmp.append(short_descr);
            tmp.append(Common.FS);
            tmp.append(normal_price);
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
