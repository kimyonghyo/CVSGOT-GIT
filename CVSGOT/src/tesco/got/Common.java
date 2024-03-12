/*****************************************************
 * PROGRAM ID    : Common	
 * PROGRAM NAME	 : 공통상수 
 * CREATED BY	 : 이태성
 * CREATION DATE : 2012/01/13
 *****************************************************
 *****************************************************
 *  변경일자    /  변경자  / 변경사유

 ******************************************************/
package tesco.got;

public class Common { 
    private Common(){}
    /**
     * <code>STX</code> 패킷시작
     */
    public final static String STX = String.valueOf((char)2);
    /**
     * <code>ETX</code> ??킷의 끝
     */
    public final static String ETX = String.valueOf((char)3);
    /**
     * <code>FS</code> 구분자
     */
    public final static String FS = String.valueOf((char)28);
    
    /**
     * <code>GS</code> 구분자2
     */
    public final static String GS = String.valueOf((char)29);

    /**
     * <code>RS</code> 구분자3
     */
    public final static String RS = String.valueOf((char)30);
        
    /*public final static String INVALID_COMMAND = "IVC";
    public final static String INVALID_PACKET = "IVP";
    public final static String SESSION_TIMEOUT = "STO";
    public final static String COMMAND_TIMEOUT = "CTO";  */
    
    /**
     * <code>ERROR</code> 에러발생
     */
    public final static String ERROR = "ERR";  
    /**
     * <code>NOT_LOGIN</code> 로그인하지 않았음
     */
    public final static String NOT_LOGIN = "NLI";
    /**
     * <code>SESSION_TIME_OUT</code> 세선이 만료되었음
     */
    public final static String SESSION_TIME_OUT = "STO";
    /**
     * <code>INVALID_COMMAND</code> 잘못된 명령입니다.
     */
    public final static String INVALID_COMMAND = "IVC";
    /**
     * <code>COMMAND_TIMEOUT</code> 소켓 연결후 명령이 전달되지 않았음
     */
    public final static String COMMAND_TIMEOUT = "CTO";
    /**
     * <code>store_code</code> 점포코드
     */
    public static String store_code = "";
    /**
     * <code>NVU</code> 버전업
     */
    public final static String NVU = "NVU";
    
    /**
     * <code>LOGIN</code> 로그인
     */
    public final static String LOGIN = "LGI";
    /**
     * <code>LOGOUT</code> 로그아웃 - 테스트 중
     */
    public final static String LOGOUT = "LGO";
    /**
     * <code>IMS</code> 상품 마스터 조회
     */
    public final static String IMS = "IMS";
    /**
     * <code>OOS</code> 결품조회(상품마스터)
     */
    public final static String OOS = "OOS";
    /**
     * <code>DIS</code> 일일판매수량조회(상품마스터)
     */
    public final static String SID = "SID";
    /**
     * <code>POP</code> Price Card
     */
    public final static String POP = "POP"; 
    /**
     * <code>RTC</code> RTC 라벨 출력
     */
    public final static String RTC = "RTC";
    /**
     * <code>TRI</code> 폐기등록 
     */
    public final static String TRI = "TRI";
    /**
     * <code>CST</code> 결품등록 
     */
    public final static String OSI = "OSI";    
    /**
     * <code>INP</code> 입고예정상세
     */
    public final static String INP = "INP";
    /**
     * <code>ADS</code> 재고조정
     */
    public final static String ADS = "ADS";
    /**
     * <code>RTI</code> 반품등록
     */
    public final static String RTI = "RTI";
    /**
     * <code>FSS</code> 신선재고조사
     */
    public final static String FSS = "FSS";    
    /**
     * <code>FSD</code> 신선재고조사 날짜조회
     */
    public final static String FSD = "FSD";  
    /**
	 * <code>DGV</code> 디지털상품권_재고조사
	 */
    public final static String DGV = "DGV";
    /**
	 * <code>LGB</code> 로그북
	 */
    public final static String LGB = "LGB";
    /**
  	 * <code>IAC</code> 검수확인
  	 */
    public final static String IAC = "IAC";
    /**
  	 * <code>IPO</code> 통합발주
  	 */
    public final static String IOD = "IOD";     
//    /**
//  	 * <code>IOM</code> 통합발주 아이템 조회
//  	 */
//    public final static String IOM = "IOM";    
//    /**
//  	 * <code>IMO</code> 통합발주 수동발주(그로서리)
//  	 */
//    public final static String IMO = "IMO";
//    /**
//  	 * <code>IFO</code> 통합발주 신선발주
//  	 */
//    public final static String IFO = "IFO";
//    /**
//  	 * <code>IPO</code> 통합발주 행사발주
//  	 */
//    public final static String IPO = "IPO";      
}

