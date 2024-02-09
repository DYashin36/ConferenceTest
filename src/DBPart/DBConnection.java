package DBPart;

import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalTime;
import java.util.ArrayList;
//import java.util.List;
import java.util.List;

public class DBConnection 
{
	private String currentLogin;
	public String getLogin() {return currentLogin;}
	
	private String currentUserRole;
	public String getRole() {return currentUserRole;}
	
	private Connection connection;
	private ResultSet currentUsefulSet=null;
	
	public DBConnection()
	{
		try {
			//�������� ���������� � ��
			connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/ConferenceDB","Daniil","1111");
		} catch (SQLException e) {
			System.out.println("Error:"+e.getMessage());
		}
		
	}
	//����� �������� ���������� � ��
	public void closeConnection() {try {
		connection.close();
	} catch (SQLException e) {
		System.out.println("Error:"+e.getMessage());
		e.printStackTrace();
	}}
	
	//�������� �������������� ������� ������
	public boolean checkAuthors(int talkID)
	{
		boolean returnedValue=false;
		
		try {
			Statement state = connection.createStatement();
			//�������� ��� �������, � ������� ���� ������ ������
			ResultSet set = state.executeQuery(String.format("select * from talk where (speakers like '%s,%s,%s')","%",currentLogin,"%"));
			while(set.next()&&!returnedValue)
			{
				//��������� �� �� ���������� talkID - ��� ����������, ����� �� �����
				//����� ������� ������
				if(talkID==set.getInt("talkID"))
					returnedValue=true;
			}
		} catch (SQLException e) {
			System.out.println("Error:"+e.getMessage());
			e.printStackTrace();
		}
		//������������ ������, ���� ������ � �������� talkID � ������ �������� �����
		//�������� ������������
		return returnedValue;
	}
	
	//�������� �������
	public void deleteTalk(int talkID)
	{
		try {
			//������ �� �������� �� ����������, �������������� ������,
			//(����. �����������, ������� ������� ���������)
			String delString=String.format("delete from schedule where (TalkID=%d)",talkID);
			ResultSet set = TalkSearch(talkID);//�������� ������ �� talkID
			Statement state = connection.createStatement();
			while(set.next())
			{
				String authors = set.getString("Speakers");
				//���� ������ 1 - ������� ������ � �� ����������, � �� ��������
				if(authors.equals(String.format(",%s,",currentLogin)))
				{
					String delTalk = String.format("delete from talk where (TalkID=%d)",talkID);
					
					state.execute(delString);
					state.execute(delTalk);
				}
				else//� ��������� ������ - �������� ������ �������� �������
				{
					authors=authors.replace(","+currentLogin+",", ",");
					String updTalk = String.format("update talk set Speakers='%s' where(TalkID=%d)",authors,talkID);
					state.execute(updTalk);
				}
			}

			
		}
		catch(Exception e)
		{
			System.out.println("error:"+e.getMessage());
			e.printStackTrace();
		}
	}
	//�������� ������������ ��������� ������� ��� ���������� �������
	public boolean checkRoomEnable(int RoomID,String[] enabled)
	{
		boolean returnedValue=false;
		
		for(int i=0;i<enabled.length;i++)
		{
			if(RoomID==Integer.parseInt(enabled[i]))
			{
				i=enabled.length+1;
				returnedValue=true;
			}
		}
		return returnedValue;
	}
	
	//��������� ������� �/��� ������� ���������� ������� (����� ��������)
	public void reScheduling(int talkID, LocalTime startTime,LocalTime endTime,int RoomID)
	{
		String updateString =String.format("UPDATE SCHEDULE SET STARTTIME='"+startTime+"',ENDTIME='"+endTime+"',RoomID=%d WHERE (TALKID=%d)",RoomID,talkID);
		try{
			Statement statement = connection.createStatement();
			statement.execute(updateString);
		}
		catch(Exception e)
		{
			System.out.println("error:"+e.getMessage());
		}
		
	}
	//�������� ������������� �������
	public boolean checkTalkExistance(int talkID)
	{
		//����������� ��� ���������� ����� � ���� � ��������
		currentUsefulSet = TalkSearch(talkID);
		int rowCount=0;
		
		try {
			if(currentUsefulSet.last()) {rowCount=currentUsefulSet.getRow();}
			currentUsefulSet.beforeFirst();
		} catch (SQLException e) {
			System.out.println("error:"+e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return (rowCount!=0);
		
	}
	//�������� ����������� ��������
	public boolean checkReschedule(int talkID, LocalTime startTime,LocalTime endTime,int RoomID,String TalkHeader)
	{
		boolean returnedValue=true;
		String statestring="";
		int buf=-1;
		try { statestring = "SELECT * FROM schedule";//����������� ��� ���������� � ����������
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery(statestring);
		while(resultSet.next()&&returnedValue)//���� ��� ������ �� �������, ���� ���� �� ��������� �������������
		{
			if(resultSet.getInt("RoomID")==RoomID)//������ ����������� ��� ���������� �������
			{if(resultSet.getInt("TalkID")!=talkID)//������ �� ������������ ��� � �����
			{
				Time sT = resultSet.getTime("startTime");
				Time eT = resultSet.getTime("endTime");
				//�������� ������������: ������ ������� �� ������ �������,
				//������ �� ������������� ����� ������ �������
				//������ �� ���������� �� ����� �������
				//����� ������/����� �� ���������
				if(sT.toLocalTime().isAfter(startTime)&&eT.toLocalTime().isBefore(endTime)||sT.toLocalTime().equals(startTime)||
						eT.toLocalTime().equals(endTime)||(sT.toLocalTime().isBefore(startTime))&&eT.toLocalTime().isAfter(startTime)||
					sT.toLocalTime().isAfter(startTime)&&(sT.toLocalTime().isBefore(endTime)))
				returnedValue=false;}}
		}}
		catch(Exception e)
			{
			System.out.println("Error:"+e.getMessage());
			}
		return returnedValue;
	}
	//��������� ������� � �������� ��������� ������
	public String[] getEnabledRooms()
	{
		String[] returnedArr=null;
		List<String> rooms= new ArrayList<String>();
		String statestring = "SELECT RoomID FROM Room";
		try {
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery(statestring);
		
		while(resultSet.next())
		{
			rooms.add(resultSet.getString("RoomID"));
		}
		returnedArr = new String[rooms.size()];
		for(int i=0;i<returnedArr.length;i++)
		{
			returnedArr[i]=rooms.get(i);
		}
		//return returnedArr;
		}
		catch(Exception e)
		{
			System.out.println("error: "+e.getMessage());
		}
		return returnedArr;
		
	}
	//����� ���������� � ���������� 
	public boolean scheduleTheTalk(LocalTime startTime,LocalTime endTime,int RoomID,String TalkHeader,int talkID)
	{
		boolean returnedValue=true;
		try {
			//��������
			String statestring = "SELECT * FROM schedule";
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(statestring);
			while(resultSet.next()&&returnedValue)
			{
				if(resultSet.getInt("RoomID")==RoomID)
				{Time sT = resultSet.getTime("startTime");
				Time eT = resultSet.getTime("endTime");
				
				if(sT.toLocalTime().isAfter(startTime)&&eT.toLocalTime().isBefore(endTime)||sT.toLocalTime().equals(startTime)||
						eT.toLocalTime().equals(endTime)||(sT.toLocalTime().isBefore(startTime))&&eT.toLocalTime().isAfter(startTime)||
					sT.toLocalTime().isAfter(startTime)&&(sT.toLocalTime().isBefore(endTime)))
					returnedValue=false;}
			}
			if(returnedValue)//���� ��������, �� ��������� �������� ����� ������ � ������� � ����������� 
			{
				returnedValue=false;
				addTalk(talkID, TalkHeader);
				String insertString = String.format("INSERT INTO SCHEDULE VALUES(%d,'%s','"+startTime+"','"+endTime+"',%d)",RoomID,TalkHeader,talkID);
				Statement insertStatement = connection.createStatement();
				statement.execute(insertString);
				System.out.println("Scheduled succesfully");
				returnedValue=true;
			}
		}
		catch(Exception e)
		{
			System.out.println("error: "+e.getMessage());
			e.printStackTrace();
		}
		return returnedValue;
	}
	//���������� �������
	public void addTalk(int talkID,String TalkHeader) {
		try
		{String insertStatement=String.format("INSERT INTO TALK VALUES(%d,'%s',',%s,')",talkID,TalkHeader,currentLogin);
		Statement statement = connection.createStatement();
		statement.execute(insertStatement);
		System.out.println("��������� �������");}
		catch(Exception e)
		{
			System.out.println("error: "+e.getMessage());
			e.printStackTrace();
		}
	}
	
	//����� ������� �� ID, ���������� ���
	private ResultSet TalkSearch(int talkID)
	{
		//boolean returnedValue=true;
		ResultSet resultSet = null;
		try{
			String insertStatement=String.format("SELECT * FROM Talk WHERE (TalkID=%d);",talkID);
		//System.out.println(insertStatement);
		Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		resultSet = statement.executeQuery(insertStatement);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			//returnedValue=false;
		}
		return resultSet;
	}
	
	//��������� ��������� �� ����������
	public void changeListenerToSpeaker(String login)
	{
		if(login.endsWith("\r"))//����� ������ ������� �������, ������� ��������� ��� ����� � �������
		login=login.substring(0,login.length()-1);
		
		String insertStatement=String.format("update user set role='Speaker' where (Login='%s');",login);
		
		try {
			Statement statement = connection.createStatement();
			statement.execute(insertStatement);
			
		} catch (SQLException e) {
			System.out.println("error:"+e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//�������� ������������� ������������ 
	public boolean checkUserExistance(String login)
	{
		currentUsefulSet=userSearch(login);//��� ����������� ������������� ��������� �������������� ���
		int lastRow=0;
		try {
			if(currentUsefulSet.last())
		
			lastRow=currentUsefulSet.getRow();//���������� ������� ���� �� ���������� ����� ����
		currentUsefulSet.beforeFirst();}
		catch(Exception e)
		{
			System.out.println("error:"+e.getMessage());
		}
		
		return(lastRow!=0);
	}
	
	//����� ������������ �� ������, ���������� ���
	private ResultSet userSearch(String login)
	{
		
		ResultSet resultSet = null;
		try{
			if(login.endsWith("\r"))
			login=login.substring(0,login.length()-1);
			String insertStatement="SELECT * FROM USER WHERE (LOGIN = '"+login+"');";
			
		//��������� ��������� � ��� ������� ��� ����������� �������� �����
		Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		resultSet = statement.executeQuery(insertStatement);
		}
		catch(Exception e)
		{
			System.out.println("error:"+e.getMessage());
		}
		return resultSet;
		}
		
	
	//��������� ������ �������� ����������� ������������
	public String[] getUserTalksList() 
	{
		List<String> list = new ArrayList<String>();
		try 
		{
			String insertStatement=String.format("SELECT * FROM Talk");
			
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(insertStatement);
			String logbuf=currentLogin;
			
			if(logbuf.endsWith("\r"))
				logbuf=logbuf.substring(0,logbuf.length()-1);
			
			while(resultSet.next())
			{
				String buf = resultSet.getString("Speakers");
				
				//buf=buf.substring(1,buf.length()-1);
				//� �������������� ���� ����������� ������, � ������� ����� ���� � ������������ �������� 
				if(buf.contains(","+currentLogin+","))
					list.add(String.format("ID:%d; �������� �������:%s; ����������:%s;",
							resultSet.getInt("TalkID"),resultSet.getString("TalkHeader"),buf));
			}
			
		}
		catch(Exception e)
		{
			System.out.println("error:"+e.getMessage());
		}
		//������� ���������� � ������ �����
		String[] returnedValue = new String[list.size()];
		for(int i=0;i<returnedValue.length;i++)
		{
			returnedValue[i]=list.get(i);
		}
		return returnedValue;
	}
	
	//���������� � ������ �������� ������� �������, ����� ��������������� ��
	//����� �����������/�����������, ������ ��� �������� �� ���������
	public void addSpeakerToTalk(int talkID)
	{
		try {
			ResultSet res = TalkSearch(talkID);
			while(res.next())
			{
				String speakers = res.getString("Speakers");//�������� ���� ��������
				
				if(speakers.contains(","+currentLogin+","))//���� ������� ������������ ��� ������ - ��������
				{System.out.println("�� ��� ��������� ��������");}
				else
				{
					//����� ��������� ������, �������� � ������ ������� ������� ������������
					String query = String.format("update talk set Speakers='%s' where (TalkID=%d)",speakers+currentLogin+",",talkID);
					Statement statement = connection.createStatement();
					statement.execute(query);
					System.out.println("�� ��������� � �������� �������");
			}}
		}
		catch(Exception e)
		{
			System.out.println("error:"+e.getMessage());
		}
	}
	
	//��������� ������ ��������  (��������)
	public  String[] getTalksList() 
	{
		List<String> list = new ArrayList<String>();
		try 
		{
			//Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/ConferenceDB","Daniil","1111");
			String insertStatement=String.format("SELECT * FROM Talk");
			
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(insertStatement);
			
			while(resultSet.next())
			{
				String buf = resultSet.getString("Speakers");
				
				buf=buf.substring(1,buf.length()-1);
				//��������� � �������������� ������
				list.add(String.format("ID:%d; �������� �������:%s; ����������:%s;",
						resultSet.getInt("TalkID"),resultSet.getString("TalkHeader"),buf));
			}
			
		}
		catch(Exception e)
		{
			System.out.println("error:"+e.getMessage());
		}
		String[] returnedValue = new String[list.size()];
		for(int i=0;i<returnedValue.length;i++)
		{
			returnedValue[i]=list.get(i);
		}
		return returnedValue;
	}
	//��������� ���������� (��������)
	public String[] getSchedule()
	{
		List<String> list = new ArrayList<String>();
		try 
		{
			//Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/ConferenceDB","Daniil","1111");
			String insertStatement=String.format("SELECT * FROM SCHEDULE");
			
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(insertStatement);
			
			while(resultSet.next())
			{
				list.add(String.format("StartTime:%s; EndTime:%s; TalkHeader:%s; Room:%s",
						resultSet.getTime("startTime"),resultSet.getString("endTime"),
						resultSet.getString("TalkHeader"),resultSet.getString("RoomID")));
			}
		}
		catch(Exception e)
		{
			System.out.println("error:"+e.getMessage());
		}
		String[] returnedValue = new String[list.size()];
		for(int i=0;i<returnedValue.length;i++)
		{
			returnedValue[i]=list.get(i);
		}
		return returnedValue;
	}
	
	//��������� ������ ������������� (��������)
	public String[] getUserList()
	{
		List<String> list = new ArrayList<String>();
		try 
		{
			//��������� ������  - ����� ������
			String insertStatement=String.format("SELECT * FROM USER");
			
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(insertStatement);
			
			while(resultSet.next())
			{
				list.add(String.format("User:%s; Role:%s; Password:%s",
				resultSet.getString("Login"),resultSet.getString("role"),resultSet.getString("password")));
			}
			
			
		}
		catch(Exception e)
		{
			System.out.println("error:"+e.getMessage());
		}
		//�������� �������� �� ���������� � ������ �����
		String[] returnedValue = new String[list.size()];
		for(int i=0;i<returnedValue.length;i++)
		{
			returnedValue[i]=list.get(i);
		}
		return returnedValue;
	}

	public void deleteUser(String login)
	{
		try 
		{
			//�������� ������������ �� ������� user
			//����� ������
			Statement statement = connection.createStatement();
			
			//�������� ������������ ��� ������� �������
			
			deleteUserAsSpeaker(login);
			if(login.endsWith("\r"))//������� ������ �������� �������
				login=login.substring(0,login.length()-1);
			
			
			String deleteStatement=String.format("DELETE FROM USER WHERE(LOGIN='%s')",login);
			statement.executeUpdate(deleteStatement);
		}
		catch(Exception e)
		{
			System.out.println("error:"+e.getMessage());
			e.printStackTrace();
		}
	}
	//�������� ������������ ��� ����������
	/**
	 * @param login
	 */
	private void deleteUserAsSpeaker(String login)
	{
		
		try {
			Statement state = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			//�������� ��� �������
			ResultSet set = state.executeQuery(String.format("select * from talk"));
			while(set.next())
			{
				String str = set.getString("Speakers");//�������� ������ �������� ���������� �������
				if(str.equals(","+login+","))//���� ������ ���� (���������)
				{
					int bufInt=set.getInt("talkID");//��������� ��
					//������� ������� �� ���������� ��� ���������� ����. �����������
					state.executeUpdate(String.format("delete from schedule where(talkID=%d);",bufInt));
					//��������� ��� - �������� ���������� ������ �� �� 
					set = state.executeQuery(String.format("select * from talk where (talkID=%d)",bufInt));
					set.next();
					//������� ������
					state.executeUpdate(String.format("delete from talk where(talkId=%d);",bufInt));
					//��������� ��� ������
					//��� ����������, ��� ��� ����� ������� execute ��� �����������
					set = state.executeQuery(String.format("select * from talk"));
				}
				else
					if(str.contains(","+login+","))//���� �������� �����, ������ �������� ������ �������� � �������
						//�� ���� ��� ������ ����� ������ ������ � ������ �������� ���� ��������
					{
						str=str.replace(","+login+",",",");
						state.executeUpdate(String.format("update talk set speakers='%s' where(talkId=%d);",str,set.getInt("talkID")));
						set = state.executeQuery(String.format("select * from talk"));
					}
			}
			set.close();
		
		} catch (SQLException e) {
			System.out.println("error:"+e.getMessage());
			e.printStackTrace();
		}
	}
	
	//����������� ������������
	public void userRegistration(String login, String password,String role,boolean adminRegistration)
	{
		try 
		{
			//Insert ������ � ������������
			if(login.endsWith("\r"))
			login=login.substring(0,login.length()-1);
			
			if(password.endsWith("\r"))
				password=password.substring(0,password.length()-1);
			
			String insertStatement=String.format("INSERT INTO USER VALUES('%s','%d','%s')",login,password.hashCode(),role);
			
			Statement statement = connection.createStatement();
			statement.executeUpdate(insertStatement);
			if(!adminRegistration)
			{//��������� �������� � ����
			currentLogin = login;
			currentUserRole=role;}
		}
		catch(Exception e)
		{
			System.out.println("error:"+e.getMessage());
		}
	}
	//����������� ������������ (��������� �������������� ������������� ������ � �������� �� ��)
	public boolean userAuthorization(String login, String password)
	{
		boolean enterMark=true;
		try 
		{
			if(password.endsWith("\r"))
				password=password.substring(0,password.length()-1);
			
			while(currentUsefulSet.next())
			{
				String testStr = currentUsefulSet.getString("password");
			if(testStr.equals(password.hashCode()+""))
					{
						//���� �������, �� ��������� �������� � ����
						currentLogin = login.toLowerCase();
						currentUserRole = currentUsefulSet.getString("role");
					}
				else
					{enterMark=false;}//����� ���������� ������ ����� ������
			}
			
		}
		catch(Exception e)
		{
			System.out.println("error:"+e.getMessage());
			//e.printStackTrace();
		}
		return enterMark;
	}
	
	
	private static void deshifr(String encrypt)
	{
		String helpStr = "testAAAAtest";
		String returnedValue="";
		try {
			KeyGenerator keygen = KeyGenerator.getInstance("AES");
			
			String[] encrypted=encrypt.split(",");
			byte[] mas = new byte[encrypted.length];
			for(int i=0;i<encrypted.length;i++)
			{
				mas[i]=Byte.parseByte(encrypted[i]);
			}
			
			String transform="AES/ECB/PKCS5Padding";
			Cipher cipher = Cipher.getInstance(transform);
			SecureRandom sr = new SecureRandom();
            sr.setSeed(helpStr.getBytes());
            
            keygen.init(256,sr);
			Key key = keygen.generateKey();
            
			cipher.init(Cipher.DECRYPT_MODE,key);
			//String res = cipher.doFinal()
			//byte[] encrypted = cipher.doFinal(startPassword.getBytes());
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*//���������� ������ ��� �������� � �� 
	private static String shifratePassword(String startPassword)
	{
		String helpStr = "testAAAAtest";
		String returnedValue="";
		try {
			KeyGenerator keygen = KeyGenerator.getInstance("AES");
			
			
			
			String transform="AES/ECB/PKCS5Padding";
			Cipher cipher = Cipher.getInstance(transform);
			SecureRandom sr = new SecureRandom();
            sr.setSeed(helpStr.getBytes());
            
            keygen.init(256,sr);
			Key key = keygen.generateKey();
            
			cipher.init(Cipher.ENCRYPT_MODE,key);
			byte[] encrypted = cipher.doFinal(startPassword.getBytes());
			
			for(int i=0;i<encrypted.length;i++)
			{
				returnedValue+=(int)encrypted[i]+",";
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returnedValue;
	}*/
	
	
}
