/*****************************************************
 * PROGRAM ID    : DgvSave
 * PROGRAM NAME  : 디지털 상품권 재고조사 저장
 * CREATION DATE : 2013
 *****************************************************
 *****************************************************
 *  변경일자       /  변경자                                  / 변경사유
 *  
 ******************************************************/ 
package tesco.got.dbTran;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import awoo.dbUtil.ConnPool;
import awoo.dbUtil.OraConnFactory;

public class DgvSave {

	private String _s_store_code = "";
	private int    _i_store_code = 0;	
    private String dgv_type = "";      // 조사타입
    
    private ArrayList dgvList = null;          // 전송된 상품권번호 목록    
    
    public DgvSave(String store_code, String dgv_type, ArrayList dgvList) {
    	this._s_store_code = store_code;
    	this._i_store_code = Integer.parseInt(store_code);  
        this.dgv_type = dgv_type;
        this.dgvList = dgvList;   
    }
          
    /**
     * 재고조사 등록
     * @throws GotException
     * @throws Exception
     */
    public void executeQuery() throws GotException,Exception {
        Connection conn = null;
        ConnPool cp = null;
        PreparedStatement pstmt = null;
        CallableStatement cstmt = null;
        
        ResultSet rs = null;
          
        StringBuffer sql = null;
                    
        try{
          
            cp = ConnPool.getInstance();
            conn = cp.getConnection();

            int paramIndex = 0;
                        
            if (dgv_type.equalsIgnoreCase("1")) {		// box
     	            	
            	int resultCode = 0;
            	String resultMsg = "";
            	
            	cstmt = conn.prepareCall("BEGIN PDA_DGV_RANGE_STOCK(?,?,?,?,?); END;");
            	cstmt.setString(++paramIndex, this._s_store_code);   					// 1. IN 점포코드        
            	cstmt.setString(++paramIndex, this.dgvList.get(0).toString());  		// 2. IN 상품권번호(From)
            	cstmt.setString(++paramIndex, this.dgvList.get(1).toString());  		// 3. IN 상품권번호(To)
            	cstmt.registerOutParameter(++paramIndex, java.sql.Types.INTEGER);   	// 4. OUT 결과코드(0=정상, Not 0=에러)
            	cstmt.registerOutParameter(++paramIndex, java.sql.Types.VARCHAR);   	// 5. OUT 결과메세지
            	cstmt.execute();
            	
            	resultCode = cstmt.getInt(4);
            	resultMsg = cstmt.getString(5);
            	
            	if (cstmt != null) {
            		cstmt.close();
            		cstmt = null;
            	}            
            	
            	if (resultCode != 0)
            		throw new Exception(resultMsg);
            	
            	conn.commit();
            	
            } else {
            	// 단품으로 재고조사
            	for (int i=0; i<this.dgvList.size(); i++) {
                    String dgvNo =this.dgvList.get(i).toString();
                
                    sql = new StringBuffer();
                    sql.append("SELECT COUNT(*) AS CNT ");
                    sql.append("  FROM PDA_DGV_STOCK ");
                    sql.append(" WHERE STOCK_DATE = TO_CHAR(SYSDATE, 'YYYYMMDD') ");
                    sql.append("   AND STORE_NO = ? ");
                    sql.append("   AND DGV_NO = ? ");

                    boolean isExist = false;
                    paramIndex = 0;
                    
                    pstmt = conn.prepareStatement(sql.toString());
                    pstmt.setString(++paramIndex, this._s_store_code);  // 01.점포코드
                    pstmt.setString(++paramIndex, dgvNo);       		// 02.상품권번호
                    rs = pstmt.executeQuery();
                    if (rs.next()) {
                        if (rs.getInt("CNT") > 0)
                            isExist = true;
                        else
                            isExist = false;
                    }
                    if (rs != null) {
                        rs.close();
                        rs = null;
                    }
                    if (pstmt != null) {
                    	pstmt.close();
                    	pstmt = null;
                    }
                      
                    // 등록된 데이터가 없으면 INSERT 한다
                    if (!isExist) {
                        sql = new StringBuffer();
                        sql.append(" INSERT INTO PDA_DGV_STOCK(STOCK_DATE, STORE_NO, WRITE_TIME, TYPE, DGV_NO, TRANSFER_YN) ");
                        sql.append(" VALUES (TO_CHAR(SYSDATE, 'YYYYMMDD'), ?, TO_CHAR(SYSDATE, 'HH24MISS'), ?, ?, 'N') ");
                          
                        paramIndex = 0;
                        pstmt = conn.prepareStatement(sql.toString());
                        pstmt.setString(++paramIndex, this._s_store_code);
                        pstmt.setString(++paramIndex, this.dgv_type);
                        pstmt.setString(++paramIndex, dgvNo);
                        pstmt.executeUpdate();
                        
                        if (pstmt != null) {
                            pstmt.close();
                            pstmt = null;
                        }
                    }                                                    
                }            
            }         
        } catch(SQLException se) {
            try {
                conn.rollback();
                conn.close();
            } catch (Exception e) {}
              
            conn = OraConnFactory.getInstance().getConnection();
            throw se;
              
        } catch(Exception e) {
            throw e;
              
        } finally {
            try{
                if (rs != null)
                    rs.close();
            } catch(Exception e){}
              
            try{
                if (pstmt != null)
                    pstmt.close();
            } catch(Exception e){}
            
            try{
                if (cstmt != null)
                	cstmt.close();
            } catch(Exception e){}
              
            try{
                cp.releaseConnection(conn);
            } catch(Exception e){}
        }
    }
}
