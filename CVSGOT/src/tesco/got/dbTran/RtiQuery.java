/*****************************************************
 * PROGRAM ID    : RtiQuery
 * PROGRAM NAME  : 반품등록 조회
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


public class RtiQuery {
	private String _s_store_code = "";
	private int    _i_store_code = 0;
	private String _barcode      = "";
	
	/**
     * @param store_code
     * @param item_code
     */
    public RtiQuery(String store_code, String barcode) {
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
            
            String status  	   = "";
            String item_code   = "";
            String short_descr = "";           
            String supplier_no = "";
            String supplier_nm = "";
            String cost       = "";
            String packQty     = "";
            String stock_cate  = "";
            String soh_qty     = "0";
            String dc_code     = "";
            String dc_name     = "";
            
            sql = new StringBuffer();

            sql.append("SELECT DECODE(B.VEND_RETURNABLE,'1','Y', 'N') AS STATUS, "					);
            sql.append("       A.ITEM_NO, "														  	);
            sql.append("       B.SHORT_DESCR, "														);
            sql.append("  	   A.SUPPLIER_NO, "														);
            sql.append("   	   D.VENDOR_KOR_NM AS SUPPLIER_NM, "									);
            sql.append("       NVL(A.UNIT_COST, 0) COST, "												);
            sql.append("       NVL(DECODE(A.STOCK_CATE, 3, E.QTY, A.SUPP_PACK_SIZE),1) PACKAGE_QTY,");
            sql.append("       DECODE(A.STOCK_CATE,1,'D',2,'C',3,'W',NULL,'D') STOCK_CATE, "		);
            sql.append("       NVL((SELECT SUM(ON_HAND_QTY)"					  					);
            sql.append("              FROM STOCK_ON_HAND "						  					);
            sql.append("             WHERE ITEM_NO  = A.ITEM_NO"			  						);
            sql.append("               AND STORE_NO = A.STORE_NO),0) ON_HAND_QTY, "		  			);
            sql.append("       NVL(TO_CHAR(A.SOURCE_WH), ' ') AS DC_CODE, "							);
            sql.append("       NVL((SELECT STORE_NAME FROM STORES "									);
            sql.append("             WHERE STORE_NO = A.SOURCE_WH), ' ') DC_NAME"					);
            sql.append("  FROM PLU A, ITEM B, SUPS D, BUNDLE_ITEM E"				);
            sql.append(" WHERE A.ITEM_NO 	 = B.ITEM_NO "												);                        
            sql.append("   AND A.ITEM_NO 	 = E.SINGLE_ITEM_NO(+) "								);
            sql.append("   AND A.SUPPLIER_NO = D.SUPPLIER_NO"										);            
            sql.append("   AND A.PRIMARY_PACK_NO = E.BUNDLE_ITEM_NO(+) "							);            
            sql.append("   AND A.ITEM_NO 	 = ? "													);
            sql.append("   AND A.STORE_NO    = ? "													);
//            sql.append("   AND NOT EXISTS (SELECT BUNDLE_ITEM_NO FROM BUNDLE_ITEM WHERE BUNDLE_ITEM_NO = A.ITEM_NO) ");
            
			pstmt = conn.prepareStatement(sql.toString());
			pstmt.setString(1, this._barcode);
			pstmt.setInt(2, this._i_store_code);
			
			rs = pstmt.executeQuery();
			
			if(!rs.next()){ 
				throw new GotException( "존재하지 않는 상품코드입니다. : " + this._barcode);
			}
			
			status		= rs.getString("STATUS");
			item_code	= rs.getString("ITEM_NO");
			short_descr	= rs.getString("SHORT_DESCR");
			supplier_no	= rs.getString("SUPPLIER_NO");
			supplier_nm = rs.getString("SUPPLIER_NM");		
			cost		= rs.getString("COST");
			packQty		= rs.getString("PACKAGE_QTY");
			stock_cate	= rs.getString("STOCK_CATE");
			soh_qty		= rs.getString("ON_HAND_QTY");
			dc_code		= rs.getString("DC_CODE");
			dc_name		= rs.getString("DC_NAME");		
			
			rs.close();                
            pstmt.close();
            
            if(stock_cate.equals("D")) { // 직납
	            if(status.equalsIgnoreCase("N")){
	            	throw new GotException("반품불가 상품입니다. : " + this._barcode);
	            }            
            }
            
            StringBuffer tmp = new StringBuffer();            
            tmp.append(item_code);      
            tmp.append(Common.FS);
            tmp.append(short_descr);
            tmp.append(Common.FS);
            
            if(stock_cate.equals("D")) { // 직납
            	if(!supplier_no.equals(" "))
            		tmp.append(supplier_no + "-" + supplier_nm);
            	else
            		tmp.append("미지정");
            	
	            tmp.append(Common.FS);
            } else {					 // DC, 센터
            	if(!dc_code.equals(" "))
            		tmp.append(dc_code + "-" + dc_name);
            	else
            		tmp.append("미지정");

	            tmp.append(Common.FS);
            }
            
            tmp.append(cost); 
            tmp.append(Common.FS);
            tmp.append(packQty);
            tmp.append(Common.FS);
            tmp.append(stock_cate);
            tmp.append(Common.FS);
            tmp.append(soh_qty);
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