/*****************************************************
 * PROGRAM ID    : ImsQuery
 * PROGRAM NAME	 : 상품 마스터 조회
 * CREATION DATE : 2013
 *****************************************************
 *****************************************************
 *  변경일자   /  변경자  / 변경사유
 ******************************************************/  
package tesco.got.dbTran;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import tesco.got.Common;
import awoo.dbUtil.ConnPool;
import awoo.dbUtil.OraConnFactory;
import awoo.util.Logger;

public class ImsQuery {
	private String _s_store_code = "";
	private int    _i_store_code = 0;
	private String _barcode      = "";
	private Logger _logger = null;
    
    /**
     * @param store_code
     * @param item_code
     */
    public ImsQuery(String store_code, String barcode, Logger logger) {
    	this._s_store_code = store_code;
    	this._i_store_code = Integer.parseInt(store_code);    	
        this._barcode      = barcode;            
        this._logger = logger;
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
            String long_descr = "";			// 아이템명칭
            String normal_price = "";		// 매입단가
            String last_sold_date = "";		// 마지막 판매일
            String last_receipt_date = "";	// 마지막 입고일
            String on_hand = "";			// 재고수량
            String supplier_no = "";		// 업체코드
            String supplier_name = "";		// 업체명칭
            String pres_stock	= "0";		// P.STOCK
            String active_date	= "";		// Active_date
            String activate_flag  = "";		// Activate_flag
            String item_status = "";		// 아이템 상태
            String package_qty = "0";		// 입수
            String sp_yn = "";				// 싱글피킹YN
            double avg2;					// 2주간평균매출
            String pog_yn = "";				// POG YN
            String leadtime = "";			// 배송조회 시간
            String daily_yn = "";			// 배송조회 yn            
            String event_group = "";		// 행사 그룹
            String event_start = "";		// 행사 시작일
            String event_end = "";			// 행사 종료일
            String pack_size1 = "";
            String pack_size2 = "";
            String receipt = "";   
            String activateDateDesc = "";   // 장기미납표기
            
            sql = new StringBuffer();
            sql.append("SELECT /*+ rule */   ");
            sql.append("       B.ITEM_NO,    ");
            sql.append("       B.LONG_DESCR, ");
            sql.append("       ROUND(DECODE(A.PRICING_METHOD,4,NVL(A.EVENT_PRICE,A.NORMAL_PRICE),NVL(A.NORMAL_PRICE,0))+ITEM_REAL_PRICE(A.ITEM_NO, ?),0) NORMAL_PRICE, ");
            sql.append("       NVL(B.STATUS, '0') AS ITEM_STATUS, "	  );
            sql.append("       A.SUPPLIER_NO,  "					  );
            sql.append("       E.VENDOR_KOR_NM AS SUPPLIER_NAME, "	  );
            sql.append("       NVL(A.PRES_STOCK,0) PRES_STOCK, "	  );
//            sql.append("       NVL((SELECT MAX(HFACINGS * VFACINGS * DFACINGS) 	   ");
//            sql.append("             FROM LOC_FACE_CAPA 						   ");
//            sql.append("            WHERE STORE_NO = A.STORE_NO 				   ");
//            sql.append("              AND ITEM_NO  = A.ITEM_NO), 0) PRES_STOCK,   ");
            sql.append("       NVL(TO_CHAR(A.ACTIVATE_DATE,'YYYY-MM-DD'),' ') ACTIVE_DATE,"    );
            sql.append("       NVL2(A.PRIMARY_PACK_NO, 'Y', 'N') ACTIVATE_FLAG,  ");
            sql.append("       NVL((SELECT SUM(ON_HAND_QTY)"					  );
            sql.append("              FROM STOCK_ON_HAND "						  );
            sql.append("             WHERE ITEM_NO  = A.ITEM_NO"				  );
            sql.append("               AND STORE_NO = ?"						  );
            sql.append("			   AND LOC_NO = 20), 0) ON_HAND_QTY, "		  );
            sql.append("       NVL((SELECT DISTINCT PACK_SIZE "					  );
            sql.append("			  FROM ORDER_MANUAL "						  );
            sql.append("			 WHERE STORE_NO = ?	"						  );
            sql.append("			   AND ITEM_NO  = A.ITEM_NO "				  );
            sql.append("			   AND ORDER_DATE = TO_CHAR(SYSDATE,'yyyymmdd')),0) AS PACK_SIZE1, ");
            sql.append("   	   NVL(DECODE(A.STOCK_CATE, 1, A.SUPP_PACK_SIZE, G.QTY ),1) AS PACK_SIZE2, ");
            sql.append("       NVL(TO_CHAR(A.LAST_DATE_SOLD,'yyyy-mm-dd'),' ')    LAST_DATE_SOLD ,     ");
            sql.append("       NVL(TO_CHAR(A.LAST_RECEIPT_DATE,'yyyy-mm-dd'),' ') LAST_RECEIPT_DATE ,  ");
            sql.append("       NVL((SELECT MAX(DECODE(NVL(ITEM_NO,'N'), 'N','N', 'Y')) "				);
            sql.append("         	   FROM LOC_FACE_CAPA  "							    			);
            sql.append("        	  WHERE STORE_NO = ? "							  					);
            sql.append("        		AND ITEM_NO  = A.ITEM_NO"									    );
            sql.append("            ), 'N')  AS POG_YN, "												);
            sql.append("       'N' AS SP_YN, "														    );
            sql.append("      (SELECT NVL(ROUND(SUM(SALE_WEIGHT)/(TRUNC(SYSDATE)-TO_DATE(MIN(SALE_DATE),'YYYYMMDD')),1), '0') ");
            sql.append("         FROM ITEM_SALES "																   		       );
            sql.append("        WHERE STORE_NO = ?    "																   		   );
            sql.append("          AND ITEM_NO  = A.ITEM_NO    "														   		   );
            sql.append("          AND SALE_DATE BETWEEN TO_CHAR (SYSDATE - 14, 'YYYYMMDD') "		 				   		   );
            sql.append("                            AND TO_CHAR (SYSDATE -  1, 'YYYYMMDD')) AS AVG2, "				   		   );
            sql.append("       DECODE(TO_CHAR(A.ACTIVATE_DATE, 'yyyymmdd'), '20601231', '지역상품', '20701231', '행사상품', '20711231', '행사상품', '20301231', '미취급상품', '20441231', '미취급상품', '20501231', '장기미납상품', '20771231', '미취급 예정상품', '20801231', '발주중단상품', NVL2(A.ACTIVATE_DATE, '일반상품', '미취급상품')) ACTIVATE_DATE_DESC");
            sql.append(" FROM PLU  A, 						");
            sql.append("  	  ITEM B,           			");
            sql.append("      SUPS E,       				");
            sql.append("      BUNDLE_ITEM G	     			");
            sql.append("WHERE A.ITEM_NO 	= B.ITEM_NO     ");
            sql.append("  AND E.SUPPLIER_NO (+) = A.SUPPLIER_NO "); 
            sql.append("  AND A.ITEM_NO 	= G.SINGLE_ITEM_NO(+) ");
            sql.append("  AND A.PRIMARY_PACK_NO = G.BUNDLE_ITEM_NO(+) ");
            sql.append("  AND NOT EXISTS (SELECT BUNDLE_ITEM_NO FROM BUNDLE_ITEM WHERE BUNDLE_ITEM_NO = A.ITEM_NO) ");
            sql.append("  AND A.ITEM_NO 	= ?				");
            sql.append("  AND A.STORE_NO    = ?    			");            
                                    
            pstmt = conn.prepareStatement(sql.toString());
            
            pstmt.setInt(1,this._i_store_code);
            pstmt.setInt(2,this._i_store_code);
            pstmt.setInt(3,this._i_store_code);
            pstmt.setInt(4,this._i_store_code);
            pstmt.setString(5,this._s_store_code);
            pstmt.setString(6,this._barcode);
            pstmt.setInt(7,this._i_store_code);                        
            
            rs = pstmt.executeQuery();
            
            if (!rs.next()){
            	throw new GotException("존재하지 않는 상품입니다. : " + this._barcode);
            }
            
            long_descr		  = rs.getString("LONG_DESCR");
            normal_price 	  = rs.getString("NORMAL_PRICE");
            item_no			  = rs.getString("ITEM_NO");
            supplier_no		  = rs.getString("SUPPLIER_NO");
            supplier_name	  = rs.getString("SUPPLIER_NAME");
            on_hand			  = rs.getString("ON_HAND_QTY");                       
            last_receipt_date = rs.getString("LAST_RECEIPT_DATE");
            last_sold_date	  = rs.getString("LAST_DATE_SOLD");
            pres_stock		  = rs.getString("PRES_STOCK");
            active_date		  = rs.getString("ACTIVE_DATE");
            activate_flag	  = rs.getString("ACTIVATE_FLAG");
            item_status		  = rs.getString("ITEM_STATUS");        
            pog_yn			  = rs.getString("POG_YN");
            sp_yn			  = rs.getString("SP_YN");
            avg2			  = rs.getDouble("AVG2");
            pack_size1		  = rs.getString("PACK_SIZE1");
            pack_size2		  = rs.getString("PACK_SIZE2");
            activateDateDesc  = rs.getString("ACTIVATE_DATE_DESC");
            
            rs.close();
            pstmt.close();        
            
            if(pack_size1.equalsIgnoreCase("0")){
            	package_qty = pack_size2;
            }else{
            	package_qty = pack_size1;
            }
            
            StringBuffer tmp = new StringBuffer(item_no);
            tmp.append(Common.FS);
            tmp.append(long_descr);
            tmp.append(Common.FS);
            tmp.append(normal_price);
            tmp.append(Common.FS);
            tmp.append(item_status);           
            tmp.append(Common.FS);
            tmp.append(on_hand);
            tmp.append(Common.FS);
            tmp.append(package_qty);
            tmp.append(Common.FS);
            tmp.append(last_receipt_date);
            tmp.append(Common.FS);
            tmp.append(last_sold_date);            
            tmp.append(Common.FS);            
            tmp.append(avg2);
            tmp.append(Common.FS);
            tmp.append(pres_stock);
            tmp.append(Common.FS);
            tmp.append(pog_yn);          
            tmp.append(Common.FS);
            tmp.append(sp_yn); // single pack
            tmp.append(Common.FS);
            tmp.append(activate_flag);
            
            // LEADTIME, 배송가능일 조회            
            sql = new StringBuffer();
            sql.append("SELECT LPAD(B.LEAD_TIME,2,'0') AS LEADTIME, ");
            sql.append("       NVL(C.D_MON,'Y')||      ");
            sql.append("       NVL(C.D_TUE,'Y')||      ");
            sql.append("       NVL(C.D_WED,'Y')||      ");
            sql.append("       NVL(C.D_THU,'Y')||      ");
            sql.append("       NVL(C.D_FRI,'Y')||      ");
            sql.append("       NVL(C.D_SAT,'Y')||      ");
            sql.append("       NVL(C.D_SUN,'Y') AS DAILYYN ");
            sql.append("  FROM PLU A, SUPS B, SUPS_ATTR C  ");
            sql.append(" WHERE A.SUPPLIER_NO = B.SUPPLIER_NO    ");
            sql.append("   AND B.SUPPLIER_NO = C.SUPPLIER_NO(+) ");
            sql.append("   AND A.STORE_NO    = ?   	   ");
            sql.append("   AND A.ITEM_NO     = ?	   ");            
            sql.append("   AND C.STORE_NO(+) = ?	   ");

            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setInt(1, this._i_store_code);
            pstmt.setString(2, item_no);
            pstmt.setString(3, Etc.getStore4digit(this._s_store_code));
            
            rs = pstmt.executeQuery();
           
            if(rs.next()){
            	leadtime = rs.getString("LEADTIME");
                daily_yn = rs.getString("DAILYYN");
            }
            
            rs.close();
            pstmt.close();
                        
            tmp.append(Common.FS);
            tmp.append(leadtime);
            tmp.append(Common.FS);
            tmp.append(daily_yn);            
            
            /*
             * 행사일자 조회  
             */
            sql = new StringBuffer();
            sql.append("SELECT EVENT_GROUP, START_DATE, END_DATE");
            sql.append("  FROM V_PDA_PROMO  ");
            sql.append(" WHERE STORE_NO = ? ");
            sql.append("   AND ITEM_NO  = ? ");
            
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setInt(1, this._i_store_code);
            pstmt.setString(2, item_no);
            
            rs = pstmt.executeQuery();
          
            if(rs.next()){
            	event_group = rs.getString("EVENT_GROUP");
                event_start = rs.getString("START_DATE");
                event_end	= rs.getString("END_DATE"); 	
            }           
            
            rs.close();
            pstmt.close();
            
            tmp.append(Common.FS);
            tmp.append(event_group);
            tmp.append(Common.FS);
            tmp.append(event_start);
            tmp.append(Common.FS);
            tmp.append(event_end);
            tmp.append(Common.FS);
         
            sql = new StringBuffer();
            sql.append("SELECT RECEIPT_DATE||' 수량:'||QTY AS RECEIPT");
            sql.append("  FROM EXPECTEDORDER_V ");
            sql.append(" WHERE STORE_NO = ?    ");
            sql.append("   AND ITEM_NO  = ?    ");
            
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setString(1, this._s_store_code);
            pstmt.setString(2, item_no);
            
            rs = pstmt.executeQuery();
            
            int rownum = 0;           
           	
        	while(rs.next()) {
            	receipt	= rs.getString("RECEIPT");
            	
            	tmp.append(receipt);
            	tmp.append(Common.GS);
            	
            	rownum++;
            }
        	
            if(rownum == 0) {
            	tmp.append("입고 데이터 없음");            	
            }
            
            tmp.append(Common.FS);
                        
            /*********** 1주간 판매/매입수량 리스트 Start ***********/
            
            sql = new StringBuffer();
            sql.append("SELECT TO_CHAR (SYSDATE - ROWNUM, 'yyyyMMdd') WORK_DATE");
            sql.append("  FROM CODES");
            sql.append(" WHERE ROWNUM <= 7");
            
            pstmt = conn.prepareStatement(sql.toString());           
            rs = pstmt.executeQuery();
            
            rownum = 0;
            String []arrDate = new String[7];
            
        	while(rs.next()) {           	
            	arrDate[rownum++] = rs.getString("WORK_DATE");             	
            }            

            sql = new StringBuffer();
            sql.append("SELECT GB||'-'||D_1||'-'||D_2||'-'||D_3||'-'||D_4||'-'||D_5||'-'||D_6||'-'||D_7 AS HIST_DATA");
            sql.append("  FROM ( "																					 );
			sql.append("        SELECT '입고' GB, WORK_DATE, NVL (R.RECEIPT_QTY, 0) QTY "								 );
			sql.append("          FROM (SELECT TO_CHAR (SYSDATE - ROWNUM, 'yyyyMMdd') WORK_DATE "					 );
			sql.append("                  FROM CODES "																 );
			sql.append("                 WHERE ROWNUM <= 7) A, "													 );
			sql.append("               V_PDA_RECEIPT R "															 );
			sql.append("         WHERE A.WORK_DATE   = R.RECEIPT_DATE(+) "											 );
			sql.append("           AND R.STORE_NO(+) = ? "															 );
			sql.append("           AND R.ITEM_NO(+)  = ? "										 					 );
            sql.append("        UNION ALL "																			 );
            sql.append("        SELECT '판매' GB, WORK_DATE, NVL (S.SALE_QTY, 0) QTY "								 );
            sql.append("          FROM (SELECT TO_CHAR (SYSDATE - ROWNUM, 'yyyyMMdd') WORK_DATE "					 );
            sql.append("                  FROM CODES "																 );
            sql.append("                 WHERE ROWNUM <= 7) A, "													 );
            sql.append("               ITEM_SALES S "															 	 );
            sql.append("         WHERE A.WORK_DATE   = S.SALE_DATE(+) "											 	 );
            sql.append("           AND S.STORE_NO(+) = ? "															 );
            sql.append("           AND S.ITEM_NO(+)  = ?) PIVOT (SUM (QTY) "										 );
            sql.append("                                         FOR WORK_DATE "									 );
            sql.append("                                         IN  ('"+arrDate[0]+"' AS \"D_1\", "				 );
            sql.append("                                              '"+arrDate[1]+"' AS \"D_2\", "				 );
            sql.append("                                              '"+arrDate[2]+"' AS \"D_3\", "				 );
            sql.append("                                              '"+arrDate[3]+"' AS \"D_4\", "				 );
            sql.append("                                              '"+arrDate[4]+"' AS \"D_5\", "				 );
            sql.append("                                              '"+arrDate[5]+"' AS \"D_6\", "				 );
            sql.append("                                              '"+arrDate[6]+"' AS \"D_7\") "				 );    
            sql.append("       ) "																					 );        
            sql.append("  ORDER BY GB");           
                        
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setInt(1, this._i_store_code);
            pstmt.setString(2, item_no);
            pstmt.setInt(3, this._i_store_code);
            pstmt.setString(4, item_no);      

            rs = pstmt.executeQuery();
                                  	
        	while(rs.next()) {
            	receipt	= rs.getString("HIST_DATA");
            	
            	tmp.append(receipt);
            	tmp.append(Common.GS);            	
            }                   
        	
        	tmp.append(Common.FS);
        	
        	/*********** 1주간 판매/매입수량 리스트 End ***********/ 
        	
        	tmp.append(activateDateDesc); // 장기미납표기
        	        	
        	tmp.append(Common.FS);
        	
        	this._logger.writeEntry("IMS \tQ\t " + this._i_store_code +  "\t" + item_no + "\t on_hand_qty : " + on_hand);
     	
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