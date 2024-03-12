/*****************************************************
 * PROGRAM ID    : IacQuery
 * PROGRAM NAME	 : 검수확인 조회
 * CREATION DATE : 2013/11/11
 *****************************************************
 *****************************************************
 *  변경일자   /  변경자 / 변경사유
 ******************************************************/  

package tesco.got.dbTran;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import tesco.got.Common;
import awoo.dbUtil.ConnPool;
import awoo.dbUtil.OraConnFactory;

public class IacQuery {
	private String _s_store_code = "";
	private int    _i_store_code = 0;
	private String _barcode      = "";
	private String _recpt_date 	 = "";
	private String _tgNo 		 = "";
	private String _searchCode 	 = "";

    /**
     * @param store_code
     * @param item_code
     */
    public IacQuery(String store_code, String barcode, String recpt_date, String tgNo, String searchCode) {
    	this._s_store_code = store_code;
    	this._i_store_code = Integer.parseInt(store_code); 
    	this._barcode      = barcode;    
        this._recpt_date   = recpt_date;
        this._tgNo		   = tgNo;
        this._searchCode   = searchCode;
    }
    
    public String executeQuery() throws GotException,Exception{
    	StringBuffer sql = null;
    	
        Connection conn = null;
        ConnPool cp = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        String sp_flag		= "";
        String sp_item		= "";
        String convert_item = "";
        String rs_code 		= "";
        String item_no 		= "";
        String short_descr 	= "";
        String receipt_date	= "";
        String tg_no		= "";
        String rec_qty		= "";
        String accept_qty	= "";        
        String status_time  = "";
        String on_hand_qty 	= "";
        
        StringBuffer tmp = new StringBuffer();
        
        try{
            cp = ConnPool.getInstance();
            conn = cp.getConnection();

            sql = new StringBuffer();
            sql.append("SELECT DECODE(PRIMARY_PACK_NO, NULL, '2', '0') AS SP_FLAG, ITEM_NO");
            sql.append("  FROM PLU "		  );
            sql.append(" WHERE STORE_NO 		= ?");
            sql.append("   AND PRIMARY_PACK_NO  = ?");

            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setInt(1, this._i_store_code);
            pstmt.setString(2, this._barcode);         
                       
            rs = pstmt.executeQuery();
            
            if(rs.next()){
            	sp_flag = rs.getString("SP_FLAG");
                sp_item = rs.getString("ITEM_NO");
            }
            
            rs.close();
            pstmt.close();

            if(sp_flag.equalsIgnoreCase("0")){
            	convert_item = sp_item;
            }else{
            	convert_item = this._barcode;
            }

            if(this._searchCode.equalsIgnoreCase("1")){
            	
            	if(this._tgNo == "" || this._tgNo == null || this._tgNo.equalsIgnoreCase("")){
            		
            		sql = new StringBuffer();
            		sql.append("select decode(b.tg_no, null, '2', '0') AS RS_CODE, c.item_no,   ");
            		sql.append("   	   d.short_descr, to_char(a.receipt_date, 'yyyymmdd') AS receipt_date, a.tg_no, b.rec_qty,   ");
            		sql.append("   	   nvl((select accept_qty from fsm_trf_item_acceptance where tg_no = b.tg_no and item_no = b.item_no),0) accept_qty,");
            		sql.append("   	   to_char(a.status_date, 'HH24:MI') status_time, ");
            		sql.append("   	   e.on_hand_qty ");
            		sql.append("  from bols a, bol_items b, plu c, item d, stock_on_hand e "		);
            		sql.append(" where a.tg_no       = b.tg_no(+) "				);
            		sql.append("   and b.item_no     = c.item_no "				);
            		sql.append("   and c.item_no 	 = d.item_no  "				);
            		sql.append("   and to_char(a.receipt_date, 'yyyymmdd') = ? ");            		
            		sql.append("   and a.store_no    = ? "						);     
            		sql.append("   and b.store_no(+)    = ? "					);
            		sql.append("   and c.store_no    = ? "						);
            		sql.append("   and c.item_no     = ? "						);
            		sql.append("   and c.store_no     = e.store_no (+) "		);
            		sql.append("   and c.item_no     = e.item_no (+) "			);
            		sql.append(" order by a.receipt_date desc, a.status_date desc");
            		
            		pstmt = conn.prepareStatement(sql.toString());                    
                    pstmt.setString(1, this._recpt_date);
                    pstmt.setInt(2, this._i_store_code);
                    pstmt.setInt(3, this._i_store_code);
                    pstmt.setInt(4, this._i_store_code);
                    pstmt.setString(5, convert_item);
                               
                    rs = pstmt.executeQuery();
                    
                    int rowCnt = 0;
                	while(rs.next()) {	
                		rs_code		 = rs.getString("RS_CODE");
                    	item_no		 = rs.getString("item_no");
                    	short_descr	 = rs.getString("short_descr");
                    	receipt_date = rs.getString("receipt_date");
                    	tg_no		 = rs.getString("tg_no");
                    	rec_qty		 = rs.getString("rec_qty");
                    	accept_qty	 = rs.getString("accept_qty");
                    	status_time  = rs.getString("status_time");
                    	on_hand_qty	 = rs.getString("on_hand_qty");
                    	
                		if(!rs_code.equalsIgnoreCase("2")) {                         
	                        tmp.append(item_no);
	                        tmp.append(Common.FS);
	                        tmp.append(short_descr);		
	                        tmp.append(Common.FS);
	                        tmp.append(receipt_date);		
	                        tmp.append(Common.FS);
	                        tmp.append(tg_no);
	                        tmp.append(Common.FS);
	                        tmp.append(rec_qty);
	                        tmp.append(Common.FS);
	                        tmp.append(accept_qty);
	                        tmp.append(Common.FS);
	                        tmp.append(status_time);
	                        tmp.append(Common.FS);
	                        tmp.append(on_hand_qty);
	                        tmp.append(Common.FS);
                		}
                		
                		rowCnt++;
                	}
                    
                    rs.close();
            		pstmt.close();
            		
                	if(rowCnt == 0)
                		throw new GotException("해당되지 않는 상품입니다. : " + this._barcode);
            		
                	if(rowCnt == 1) { 
	            		if(rs_code.equalsIgnoreCase("2")){
	                    	throw new GotException("입고 기록이 없습니다.");
	                    }
                	}            		
            	} else {            		
            		sql = new StringBuffer();
            		sql.append("select decode(b.tg_no, null, '2', '0') as rs_code, c.item_no, "									);
            		sql.append("  	   d.short_descr, to_char(a.receipt_date, 'yyyymmdd') as receipt_date, a.tg_no, b.rec_qty, ");
            		sql.append("   	   nvl((select accept_qty from fsm_trf_item_acceptance where tg_no = b.tg_no and item_no = b.item_no),0) accept_qty,");
            		sql.append("   	   to_char(a.status_date, 'HH24:MI') status_time, ");
            		sql.append("   	   e.on_hand_qty ");
            		sql.append("  from bols a, bol_items b, plu c, item d, stock_on_hand e  ");
            		sql.append(" where a.tg_no       = b.tg_no(+) "			);
            		sql.append("   and b.item_no     = c.item_no "			);
            		sql.append("   and c.item_no 	 = d.item_no  "			);
            		sql.append("   and c.item_no 	 = ? "		    		);
            		sql.append("   and a.store_no    = ? "					);
            		sql.append("   and b.store_no(+)    = ? "					);
            		sql.append("   and c.store_no    = ? "					);
            		sql.append("   and a.tg_no       = ? "					);
            		sql.append("   and rownum        = 1 "					);
            		sql.append("   and c.store_no     = e.store_no (+) "	);
            		sql.append("   and c.item_no     = e.item_no (+) "		);
            		sql.append(" order by a.receipt_date desc"				);
            		
            		pstmt = conn.prepareStatement(sql.toString());
                    pstmt.setString(1, convert_item);
                    pstmt.setInt(2, this._i_store_code);
                    pstmt.setInt(3, this._i_store_code);
                    pstmt.setInt(4, this._i_store_code);
                    pstmt.setString(5, this._tgNo);
                    
                    rs = pstmt.executeQuery();
                    
                    if(rs.next()){
                    	rs_code		 = rs.getString("rs_code");
                    	item_no		 = rs.getString("item_no");
                    	short_descr	 = rs.getString("short_descr");
                    	receipt_date = rs.getString("receipt_date");
                    	tg_no		 = rs.getString("tg_no");
                    	rec_qty		 = rs.getString("rec_qty");
                    	accept_qty	 = rs.getString("accept_qty");
                    	status_time  = rs.getString("status_time");
                    	on_hand_qty  = rs.getString("on_hand_qty");
                    }else{
                    	throw new GotException("해당되지 않는 상품입니다. : " + this._barcode);
                    }
                    
                    rs.close();
                    pstmt.close();
                    
                    if(rs_code.equalsIgnoreCase("2")){
                    	throw new GotException("입고 기록이 없습니다.");
                    }
                    
                    tmp.append(item_no);				// 아이템번호
                    tmp.append(Common.FS);
                    tmp.append(short_descr);			//	상품명
                    tmp.append(Common.FS);
                    tmp.append(receipt_date);		
                    tmp.append(Common.FS);
                    tmp.append(tg_no);					
                    tmp.append(Common.FS);
                    tmp.append(rec_qty);					
                    tmp.append(Common.FS);
                    tmp.append(accept_qty);
                    tmp.append(Common.FS);          
                    tmp.append(status_time);
                    tmp.append(Common.FS);
                    tmp.append(on_hand_qty);
                    tmp.append(Common.FS);
            	}            	
            }else{
            	throw new GotException("직납건은 처리할 수 없습니다 : " + this._barcode);
            }
                  
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
