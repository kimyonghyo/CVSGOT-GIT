/*****************************************************
 * PROGRAM ID    : Common	
 * PROGRAM NAME	 : ������ 
 * CREATED BY	 : ���¼�
 * CREATION DATE : 2012/01/13
 *****************************************************
 *****************************************************
 *  ��������    /  ������  / �������

 ******************************************************/
package tesco.got;

public class Common { 
    private Common(){}
    /**
     * <code>STX</code> ��Ŷ����
     */
    public final static String STX = String.valueOf((char)2);
    /**
     * <code>ETX</code> ??Ŷ�� ��
     */
    public final static String ETX = String.valueOf((char)3);
    /**
     * <code>FS</code> ������
     */
    public final static String FS = String.valueOf((char)28);
    
    /**
     * <code>GS</code> ������2
     */
    public final static String GS = String.valueOf((char)29);

    /**
     * <code>RS</code> ������3
     */
    public final static String RS = String.valueOf((char)30);
        
    /*public final static String INVALID_COMMAND = "IVC";
    public final static String INVALID_PACKET = "IVP";
    public final static String SESSION_TIMEOUT = "STO";
    public final static String COMMAND_TIMEOUT = "CTO";  */
    
    /**
     * <code>ERROR</code> �����߻�
     */
    public final static String ERROR = "ERR";  
    /**
     * <code>NOT_LOGIN</code> �α������� �ʾ���
     */
    public final static String NOT_LOGIN = "NLI";
    /**
     * <code>SESSION_TIME_OUT</code> ������ ����Ǿ���
     */
    public final static String SESSION_TIME_OUT = "STO";
    /**
     * <code>INVALID_COMMAND</code> �߸��� ����Դϴ�.
     */
    public final static String INVALID_COMMAND = "IVC";
    /**
     * <code>COMMAND_TIMEOUT</code> ���� ������ ����� ���޵��� �ʾ���
     */
    public final static String COMMAND_TIMEOUT = "CTO";
    /**
     * <code>store_code</code> �����ڵ�
     */
    public static String store_code = "";
    /**
     * <code>NVU</code> ������
     */
    public final static String NVU = "NVU";
    
    /**
     * <code>LOGIN</code> �α���
     */
    public final static String LOGIN = "LGI";
    /**
     * <code>LOGOUT</code> �α׾ƿ� - �׽�Ʈ ��
     */
    public final static String LOGOUT = "LGO";
    /**
     * <code>IMS</code> ��ǰ ������ ��ȸ
     */
    public final static String IMS = "IMS";
    /**
     * <code>OOS</code> ��ǰ��ȸ(��ǰ������)
     */
    public final static String OOS = "OOS";
    /**
     * <code>DIS</code> �����Ǹż�����ȸ(��ǰ������)
     */
    public final static String SID = "SID";
    /**
     * <code>POP</code> Price Card
     */
    public final static String POP = "POP"; 
    /**
     * <code>RTC</code> RTC �� ���
     */
    public final static String RTC = "RTC";
    /**
     * <code>TRI</code> ����� 
     */
    public final static String TRI = "TRI";
    /**
     * <code>CST</code> ��ǰ��� 
     */
    public final static String OSI = "OSI";    
    /**
     * <code>INP</code> �԰�����
     */
    public final static String INP = "INP";
    /**
     * <code>ADS</code> �������
     */
    public final static String ADS = "ADS";
    /**
     * <code>RTI</code> ��ǰ���
     */
    public final static String RTI = "RTI";
    /**
     * <code>FSS</code> �ż��������
     */
    public final static String FSS = "FSS";    
    /**
     * <code>FSD</code> �ż�������� ��¥��ȸ
     */
    public final static String FSD = "FSD";  
    /**
	 * <code>DGV</code> �����л�ǰ��_�������
	 */
    public final static String DGV = "DGV";
    /**
	 * <code>LGB</code> �α׺�
	 */
    public final static String LGB = "LGB";
    /**
  	 * <code>IAC</code> �˼�Ȯ��
  	 */
    public final static String IAC = "IAC";
    /**
  	 * <code>IPO</code> ���չ���
  	 */
    public final static String IOD = "IOD";     
//    /**
//  	 * <code>IOM</code> ���չ��� ������ ��ȸ
//  	 */
//    public final static String IOM = "IOM";    
//    /**
//  	 * <code>IMO</code> ���չ��� ��������(�׷μ���)
//  	 */
//    public final static String IMO = "IMO";
//    /**
//  	 * <code>IFO</code> ���չ��� �ż�����
//  	 */
//    public final static String IFO = "IFO";
//    /**
//  	 * <code>IPO</code> ���չ��� ������
//  	 */
//    public final static String IPO = "IPO";      
}

