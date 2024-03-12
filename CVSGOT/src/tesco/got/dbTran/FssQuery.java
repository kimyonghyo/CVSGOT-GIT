/*****************************************************
 * PROGRAM ID    : FssQuery
 * PROGRAM NAME  : 신선재고조사 조회
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

public class FssQuery {
	private int    _i_store_code = 0;
	private String _barcode      = "";
    private String _survey_date  = "";
	
	/**
     * @param store_code
     * @param survey_date
     * @param item_code
     */
    public FssQuery(String store_code, String barcode, String survey_date ) {
    	this._i_store_code = Integer.parseInt(store_code); 
    	this._barcode 	   = barcode;
    	this._survey_date   = survey_date;
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
            
            String status	   = "";
            String item_code   = "";
            String short_descr = "";           
            String uom         = "";            
            
            sql = new StringBuffer();
            sql.append("SELECT DISTINCT DECODE(C.BUNDLE_ITEM_NO,NULL,'0','2') AS STATUS, A.ITEM_NO, B.SHORT_DESCR,");
            sql.append("       DECODE(B.UNIT_OF_MEASURE,4,'kg',5,'kg',33,'kg','개') AS UOM "				  );
            sql.append("  FROM PLU A, ITEM B, BUNDLE_ITEM C"		  );
            sql.append(" WHERE A.ITEM_NO  = B.ITEM_NO "				  );
            sql.append("   AND A.ITEM_NO  = C.BUNDLE_ITEM_NO(+) "	  );
            sql.append("   AND A.ITEM_NO  = ? "				  		  );
            sql.append("   AND A.STORE_NO = ? "		  				  );
            
			pstmt = conn.prepareStatement(sql.toString());			
			pstmt.setString(1, this._barcode);
			pstmt.setInt(2, this._i_store_code);
			
			rs = pstmt.executeQuery();
			
			if(!rs.next()){ 
				throw new GotException("존재하지 않는 상품코드입니다. : " + this._barcode);				
			}
			
			status		= rs.getString("STATUS");
			item_code	= rs.getString("ITEM_NO");
			short_descr	= rs.getString("SHORT_DESCR");
			uom			= rs.getString("UOM");
						
			rs.close();                
            pstmt.close();
            
            if(status.equalsIgnoreCase("2")){
				throw new GotException( "Pack상품 입니다. 조사대상이 아님. : " + this._barcode);
			}
            
            sql = new StringBuffer();
            sql.append("SELECT COUNT(*) CNT   ");
            sql.append("  FROM AUDITMST_LOG   ");
            sql.append(" WHERE STORE_NO = ?   ");
            sql.append("   AND LOG_DATE = ?   ");
            sql.append("   AND ITEM_NO = ?    ");
            
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setInt(1, this._i_store_code);
			pstmt.setString(2,this._survey_date);
			pstmt.setString(3, item_code);
			
			rs = pstmt.executeQuery();
			
			int cnt = 0;
			
			if(rs.next()){
				cnt	= rs.getInt("CNT");				
			}
			
			if(cnt <= 0){
				throw new GotException( "조사대상이 아님 : " + this._barcode);
			}
            
            StringBuffer tmp = new StringBuffer();
            tmp.append(item_code);
            tmp.append(Common.FS);
            tmp.append(short_descr);      
            tmp.append(Common.FS);
            tmp.append(uom);   
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