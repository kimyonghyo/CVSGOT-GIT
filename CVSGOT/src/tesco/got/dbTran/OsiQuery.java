/*****************************************************
 * PROGRAM ID    : OsiQuery
 * PROGRAM NAME	 : 결품등록 조회
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

import tesco.got.Common;
import awoo.dbUtil.ConnPool;
import awoo.dbUtil.OraConnFactory;

public class OsiQuery {
	private int    _i_store_code = 0; 	
	private String _barcode      = "";
	private String _group_no     = "";

    /**
     * @param store_code
     * @param item_code
     */
    public OsiQuery(String store_code, String barcode, String group_no) {    	
    	this._i_store_code = Integer.parseInt(store_code);    	
        this._barcode      = barcode;   
        this._group_no	   = group_no;
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
            
            String item_no = "";
            String short_descr = "";
            String on_hand_qty = "";
            String pog = "";
            String cqty = "";
            String status = "";
            String l_sale = "";
            String receipt = "";
            String bundle_item_no = "";
            
            sql = new StringBuffer();            
            
            sql.append("SELECT A.ITEM_NO AS ITEM_NO, "                                       );
            sql.append("       B.SHORT_DESCR AS SHORT_DESCR, "                               );
            sql.append("       C.ON_HAND_QTY AS ON_HAND_QTY, "                               );
//            sql.append("       DECODE (F.ITEM_NO, NULL, 'N', 'Y') AS POG, "                  );
			sql.append("       NVL((SELECT MAX(DECODE(NVL(ITEM_NO,'N'), 'N','N', 'Y')) "	 );
			sql.append("              FROM LOC_FACE_CAPA "								 	 );
			sql.append("             WHERE STORE_NO = A.STORE_NO"						 	 );
			sql.append("               AND ITEM_NO  = A.ITEM_NO" 						 	 );
			sql.append("            ), 'N') AS POG, "									 	 );
            sql.append("       NVL (E.COUNT_QTY, 0) AS C_QTY, "                              );
            sql.append("       A.STATUS AS STATUS, "                                         );
            sql.append("       NVL (TO_CHAR (A.LAST_DATE_SOLD, 'YYYYMMDD'), ' ') AS L_SALE," );
            sql.append("       DECODE(NVL(G.BUNDLE_ITEM_NO,'S'),'S','S','B') AS BUNDLE_ITEM");
            sql.append("  FROM PLU A, "                                     				 );
            sql.append("       ITEM B, "                                     				 );
            sql.append("       STOCK_ON_HAND C, "                                     		 );                                    			
            sql.append("       OUT_OF_SHELF_ANALYSIS E, "                              		 );
//            sql.append("      (SELECT ITEM_NO "                                     		 );
//            sql.append("         FROM POG_UPLOAD "                                     		 );
//            sql.append("        WHERE STORE_NO = ? "                                     	 );
//            sql.append("          AND YYYYWW = (SELECT MAX (YYYYWW) FROM POG_UPLOAD WHERE STORE_NO = ?)) F,");
            sql.append("	   BUNDLE_ITEM G");
            sql.append(" WHERE A.ITEM_NO 	 = B.ITEM_NO	"              );
            sql.append("   AND A.ITEM_NO 	 = C.ITEM_NO    "              );
            sql.append("   AND A.STORE_NO    = C.STORE_NO   "              );
            sql.append("   AND C.LOC_NO 	 = 20           "              );            
            sql.append("   AND A.ITEM_NO 	 = E.ITEM_NO (+)"              );
            sql.append("   AND A.STORE_NO    = E.STORE_NO(+)"              );
            sql.append("   AND E.WORK_DATE(+)= TO_CHAR(SYSDATE,'yyyymmdd')");
//            sql.append("   AND A.ITEM_NO 	 = F.ITEM_NO (+)"              );
            sql.append("   AND A.ITEM_NO  	 = G.BUNDLE_ITEM_NO(+)"		   );
            sql.append("   AND A.ITEM_NO 	 = ?"              			   );
            sql.append("   AND A.STORE_NO    = ?"              			   );                        
            sql.append("   AND E.GROUP_NO (+)= TRIM(?)   "  			   );
            
            pstmt = conn.prepareStatement(sql.toString());
//            pstmt.setInt(1, this._i_store_code);
//            pstmt.setInt(2, this._i_store_code);
//            pstmt.setString(3, this._barcode);
//            pstmt.setInt(4, this._i_store_code);
//            pstmt.setString(5, this._group_no);            
            pstmt.setString(1, this._barcode);
	        pstmt.setInt(2, this._i_store_code);
	        pstmt.setString(3, this._group_no);            
                       
            rs = pstmt.executeQuery();
            
            if (!rs.next()){
            	throw new GotException("존재하지 않는 상품입니다. : " + this._barcode);
            }
            
            item_no		= rs.getString("ITEM_NO");
            short_descr	= rs.getString("SHORT_DESCR");
            on_hand_qty	= rs.getString("ON_HAND_QTY");
            pog			= rs.getString("POG");
            cqty		= rs.getString("C_QTY");
            status		= rs.getString("STATUS");
            l_sale		= rs.getString("L_SALE");
            bundle_item_no = rs.getString("BUNDLE_ITEM");
                        
            rs.close();
            pstmt.close();
            
			if(bundle_item_no.equalsIgnoreCase("B")){
				throw new GotException("번들상품입니다.\n재고등록할 수 없습니다.");
			}
            
            StringBuffer tmp = new StringBuffer();
            tmp.append(item_no);	 // 아이템번호
            tmp.append(Common.FS); 
            tmp.append(short_descr); //	상품명
            tmp.append(Common.FS);
            tmp.append(on_hand_qty); //	재고
            tmp.append(Common.FS);
            tmp.append(pog);		 //	pog_yn
            tmp.append(Common.FS);
            tmp.append(cqty);		 //	매장수량   
            tmp.append(Common.FS);
            tmp.append(status);		 //	상태    
            tmp.append(Common.FS);
            tmp.append(l_sale);		 //	last_sale    
            tmp.append(Common.FS);
            
            rs = null;
            sql = null;
            sql = new StringBuffer();
            sql.append("SELECT RECEIPT_DATE||':'||QTY AS RECEIPT  ");
            sql.append("  FROM EXPECTEDORDER_V ");
            sql.append(" WHERE STORE_NO = ?    ");
            sql.append("   AND ITEM_NO  = ?    ");
            
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setInt(1, _i_store_code);
            pstmt.setString(2, item_no);
            
            rs = pstmt.executeQuery();
            
            int rownum = 0;
            
            if(!rs.next()){
            	tmp.append("입고 데이터 없음");
            }else{            	
            	rs.close();            	
            	rs = pstmt.executeQuery();
            	
            	while(rs.next()){
                	receipt	= rs.getString("RECEIPT");
                	
                	if(rownum>0){               		 
                		tmp.append(Common.FS);
                	}
               	
                	rownum++;                 	
                	tmp.append(receipt); //	입고예정            	         	                    
                }            	
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
