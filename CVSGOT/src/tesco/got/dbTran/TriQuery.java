/*****************************************************
 * PROGRAM ID    : TriQuery
 * PROGRAM NAME  : RegTrash(폐기등록) 조회
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

public class TriQuery {
	private String _s_store_code = "";
	private int    _i_store_code = 0;
	private String _barcode      = "";   
	
	/**
     * @param store_code
     * @param item_code
     */
    public TriQuery(String store_code, String barcode) {
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
            
            String item_code = "";
            String short_descr = "";
            String on_hand_qty	= "";
            String bundle_item_no = "";            
            
            sql = new StringBuffer();
            
			sql.append("SELECT A.ITEM_NO, "													);
			sql.append("       B.SHORT_DESCR, "												);
			sql.append("       C.ON_HAND_QTY, "												);
			sql.append("       DECODE(NVL(D.BUNDLE_ITEM_NO,'S'),'S','S','B') AS BUNDLE_ITEM");
			sql.append("  FROM PLU A, ITEM B, STOCK_ON_HAND C, BUNDLE_ITEM D"				);
			sql.append(" WHERE A.ITEM_NO  = B.ITEM_NO "										);
			sql.append("   AND A.ITEM_NO  = C.ITEM_NO "										);
			sql.append("   AND A.STORE_NO = C.STORE_NO "									);
			sql.append("   AND A.ITEM_NO  = D.BUNDLE_ITEM_NO(+)"							);
			sql.append("   AND A.ITEM_NO  = ?"												);
			sql.append("   AND A.STORE_NO = ?"												);
			            
			pstmt = conn.prepareStatement(sql.toString());
			pstmt.setString(1, this._barcode);
			pstmt.setInt(2, this._i_store_code);			
			
			rs = pstmt.executeQuery();
			
			if(!rs.next()){ 
				throw new GotException( "존재하지 않는 상품코드입니다. : " + this._barcode);
			}
			
			item_code	   = rs.getString("ITEM_NO");
			short_descr    = rs.getString("SHORT_DESCR");
			on_hand_qty	   = rs.getString("ON_HAND_QTY");
			bundle_item_no = rs.getString("BUNDLE_ITEM");
			
			if(bundle_item_no.equalsIgnoreCase("B")){
				throw new GotException("번들상품입니다.\n재고등록할 수 없습니다.");
			}
			
			rs.close();                
            pstmt.close();                     
   
			StringBuffer tmp = new StringBuffer();
            tmp.append(item_code);
            tmp.append(Common.FS);
            tmp.append(short_descr);
            tmp.append(Common.FS);
            tmp.append(on_hand_qty);
            tmp.append(Common.FS);
            tmp.append(on_hand_qty);
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