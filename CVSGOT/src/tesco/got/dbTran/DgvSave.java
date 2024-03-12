/*****************************************************
 * PROGRAM ID    : DgvSave
 * PROGRAM NAME  : ������ ��ǰ�� ������� ����
 * CREATION DATE : 2013
 *****************************************************
 *****************************************************
 *  ��������       /  ������                                  / �������
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
    private String dgv_type = "";      // ����Ÿ��
    
    private ArrayList dgvList = null;          // ���۵� ��ǰ�ǹ�ȣ ���    
    
    public DgvSave(String store_code, String dgv_type, ArrayList dgvList) {
    	this._s_store_code = store_code;
    	this._i_store_code = Integer.parseInt(store_code);  
        this.dgv_type = dgv_type;
        this.dgvList = dgvList;   
    }
          
    /**
     * ������� ���
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
            	cstmt.setString(++paramIndex, this._s_store_code);   					// 1. IN �����ڵ�        
            	cstmt.setString(++paramIndex, this.dgvList.get(0).toString());  		// 2. IN ��ǰ�ǹ�ȣ(From)
            	cstmt.setString(++paramIndex, this.dgvList.get(1).toString());  		// 3. IN ��ǰ�ǹ�ȣ(To)
            	cstmt.registerOutParameter(++paramIndex, java.sql.Types.INTEGER);   	// 4. OUT ����ڵ�(0=����, Not 0=����)
            	cstmt.registerOutParameter(++paramIndex, java.sql.Types.VARCHAR);   	// 5. OUT ����޼���
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
            	// ��ǰ���� �������
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
                    pstmt.setString(++paramIndex, this._s_store_code);  // 01.�����ڵ�
                    pstmt.setString(++paramIndex, dgvNo);       		// 02.��ǰ�ǹ�ȣ
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
                      
                    // ��ϵ� �����Ͱ� ������ INSERT �Ѵ�
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
