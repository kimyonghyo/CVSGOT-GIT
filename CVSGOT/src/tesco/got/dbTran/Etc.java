/*****************************************************
 * PROGRAM ID    : Etc
 * PROGRAM NAME	 : 기타 데이터베이스 작업
 * CREATED BY	 : 김한영(duloveme@hotmail.com)
 * CREATION DATE : 2005. 5. 30
 *****************************************************
 *****************************************************
 *  변경일자    /  변경자  / 변경사유

 ******************************************************/ 
package tesco.got.dbTran;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;

import awoo.dbUtil.ConnPool;
import awoo.dbUtil.OraConnFactory;

public class Etc {
    private static Etc thisObj = new Etc();
    private Etc(){}
        
    /**
     * @return DataManager
     */
    private static Etc getInstance(){
        if(thisObj == null){
            thisObj = new Etc();
        }
        return thisObj;
    }
    
    public static String getStore4digit(String store_code) {
    	return new DecimalFormat("0000").format(Integer.parseInt(store_code));
    }
    
    /*
     * ID   : getItemCodebyBarcode
     * DESC : 입력된 바코드(OCC코드/일반바코드)로 LEVEL2코드를 찾아 반환
     */
    public synchronized static String getItemCodebyBarcode(String StoreCode, String barcode) throws Exception{
        String strSql = "";
        Connection conn = null;
        ConnPool cp = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        int storeNo   = Integer.parseInt(StoreCode);
        String itemNo = "";
        
        try{
            cp = ConnPool.getInstance();
            conn = cp.getConnection();
                        
            strSql = "SELECT NVL(FNC_PDA_GET_ITEM_NO(?, ?), '') ITEM_NO FROM DUAL";
                        
	        pstmt = conn.prepareStatement(strSql);
	        pstmt.setInt(1,  storeNo);
	        pstmt.setString(2, barcode);	        
	        rs = pstmt.executeQuery();
        
	        if (rs.next()){
	        	itemNo = rs.getString("ITEM_NO");     
	        }
	        
            rs.close();
            pstmt.close();
            
	        return itemNo;	        
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
