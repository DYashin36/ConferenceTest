package userPart;

import java.time.LocalTime;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

import DBPart.DBConnection;



public class mainUserClass 
{
	private static  DBConnection conn;
	private static String currentLogin;
	private static String currentUserRole;
	//����� ��������� ������
	private static void showEnabledRooms(String[] roomIDs)
	{
		
		String rooms="��������� ���������:";
		for(int i=0;i<roomIDs.length;i++)
		{
			if(i!=rooms.length()-1)
			{
				rooms+=roomIDs[i]+",";
			}
			else rooms+=roomIDs[i];
		}
		System.out.println(rooms);
	}
	//����� ��������� ������������ �������
	private static void speakerOptions()
	{
		boolean stopMark=false;
		Scanner scan;
		while(!stopMark)
		{
			System.out.println("������� 1, ����� ����������� ������ ��������\n"
					+ "������� 2, ����� ����������� ������ ����� ��������\n������� 3, ����� ���������� ����������\n"
					+ "������� 4, ����� �������� ����� ������\n"
					+ "������� 5, ����� ����� �������� ������������� �������\n"
					+ "������� 6, ����� ��������� ���� ������\n������� 7, ����� ������� ������\n"
					+ "� ������ ����� ����� ������� ��������� �����\n");
			scan= new Scanner(System.in).useDelimiter("\n");
			String switched = scan.next();
			try {
				switch(switched.substring(0,1))
				{
				
				case "1":
				{
					//��������� � ����� ������� ��������
					String[] mas = conn.getTalksList();
					
					if(mas.length>0)
					{for(int i=0;i<mas.length;i++)
					{
						System.out.println(mas[i]);
					}}
					else System.out.println("�������� �� ����������");
					System.out.println();
					break;
				}
				case "2":
				{
					//��������� � ����� ������� ��������, � ������� �������
					//������������ - ������
					String[] mas = conn.getUserTalksList();
					if(mas.length>0)
					{for(int i=0;i<mas.length;i++)
					{
						System.out.println(mas[i]);
					}}
					else System.out.println("�������� �� ����������");
					System.out.println();
					break;
				}
				case "3":
				{
					//��������� � ����� ����������
					String[] mas = conn.getSchedule();
					for(int i=0;i<mas.length;i++)
					{
						System.out.println(mas[i]);
					}
					System.out.println();
					break;
				}
				case "4"://���������� ������ �������
				{//���� ��������
					System.out.println("������� ID �������");
					String buf = scan.next();
					int talkID = Integer.parseInt(buf.substring(0,buf.length()-1));
					System.out.println("������� �������� �������");
					String talkHeader=scan.next();
					//scan.close();
					scan = new Scanner(System.in);
					if(!conn.checkTalkExistance(talkID))
					{
						String[] roomIDs = conn.getEnabledRooms();
						showEnabledRooms(roomIDs);
						System.out.println("������� ����� ���������");
						int roomID=scan.nextInt();
					
						//���� ��������� ����� ������� ���� � ������� ���������
						if(conn.checkRoomEnable(roomID, roomIDs))
						{//������ �������� ������� ������ � ��������� �������
							boolean mark=true;
							while(mark)//������, ���� �� ����� ������� ��������� ��� ��������� � ���������� ���������
							{
								System.out.println("������� ����� ������ �������");
								LocalTime time = LocalTime.parse(scan.next());
								System.out.println("������� ����� ��������� �������");
								LocalTime time2 = LocalTime.parse(scan.next());
								if(conn.scheduleTheTalk(time, time2, roomID, talkHeader,talkID))
								{mark=false;}
							}	
						}
						else System.out.println("������� �� ����������");
					}
					
					else
					{
						System.out.println("������ � ����� ID ��� ����������");
					}
					break;
				}
				case "5":
				{
					System.out.println("������� ID �������");
					String buf = scan.next();
					int talkID = Integer.parseInt(buf.substring(0,buf.length()-1));
					//���� ������ ����������, ��������� �������
					if(conn.checkTalkExistance(talkID))
					{
						conn.addSpeakerToTalk(talkID);
					}else {System.out.println("������ �� ����������");}
					break;
				}
				case "6":
				{
					System.out.println("������� ID �������");
					String buf = scan.next();
					int talkID = Integer.parseInt(buf.substring(0,buf.length()-1));
					
					scan = new Scanner(System.in);
					if(conn.checkTalkExistance(talkID))
					{
						String[] roomIDs = conn.getEnabledRooms();
						showEnabledRooms(roomIDs);
						
						System.out.println("������� ����� ���������");
						int roomID=scan.nextInt();
						if(conn.checkRoomEnable(roomID, roomIDs))
						{
						System.out.println("������� ����� ������ �������");
						LocalTime time = LocalTime.parse(scan.next());
						System.out.println("������� ����� ��������� �������");
						LocalTime time2 = LocalTime.parse(scan.next());
						if(conn.checkReschedule(talkID, time, time2, roomID, buf))
						{
							conn.reScheduling(talkID, time, time2,roomID);
							System.out.println("������� ��������");
						}
						else {System.out.println("������� ����������");}
						}
						else {System.out.println("������� ����������");}
					}
					else System.out.println("������ �� ����������");break;
				}
					case "7":
					{
						
						System.out.println("������� ID �������");
						String buf = scan.next();
						int talkID = Integer.parseInt(buf.substring(0,buf.length()-1));
						
						
						if(conn.checkTalkExistance(talkID)&&conn.checkAuthors(talkID))
						{
							conn.deleteTalk(talkID);
							System.out.println("������� �������");
						}
						else
						{
							System.out.println("������� � ����� ID � ����� ���������� �� �������");
						}
						break;
					}
				default:{stopMark=true;
				System.out.println("�����");
				conn.closeConnection();break;}
				}
			} catch (Exception e) {
				System.out.println("error: "+e.getMessage());
				e.printStackTrace();
			}
			
		}
	}
	
	private static void adminOptions()
	{
		Scanner scan= new Scanner(System.in).useDelimiter("\n");
		try{
			boolean stopMark=false;
		System.out.println("������� 1, ����� ����������� ������ �������������\n"
				+ "������� 2, ����� �������� ������������\n������� 3, ����� ������� ������������ �����������\n"
			+ "������� 4, ����� ������� ������������\n� ������ ����� ����� ������� ��������� �����");
		//���� ������ ����� �� ������ ������
		while(!stopMark)
		{
			System.out.println("������� ������");
			String switched = scan.next();//���������
			switch(switched.substring(0,switched.length()-1))
			{ 
			case "1":
				{
					//�������� ������ ������������� �������� ����� � �������
					String[] mas = conn.getUserList();
					for(int i=0;i<mas.length;i++)
					{
						System.out.println(mas[i]);
					}
					System.out.println();
					break;
				}
				case "3":
			{
				System.out.println("������� ����� ������������");
				
				String log = scan.next();
				if(conn.checkUserExistance(log))//�������� ������������� ������������ 
				{conn.changeListenerToSpeaker(log);//���� ����������, �� ������ ����
				}
				else//� ��������� ������ - ��������� �� ������
				{
					System.out.println("������� ������������ �� ����������");
				}
				break;
			}
			case "2":
			{
				System.out.println("������� ����� ������ ������������");//���������
				String login = scan.next();
				if(!conn.checkUserExistance(login))//��������� ������������� ������������
				{
					System.out.println("������� ������ ������ ������������");
					String password=scan.next();
					System.out.println("������� ���� ������ ������������: a-admin, s-speaker, ������ ������ - listener");
					String roleMarker = scan.next();
					switch(roleMarker.charAt(0))//�������� ����� �����������
					{	
						case 'a':{conn.userRegistration(login, password, "admin",true);break;}
						case 's':{conn.userRegistration(login, password, "Speaker",true);break;}
						default:{conn.userRegistration(login, password, "Listener",true);break;}
					}
				}
				else System.out.println("������������ � ����� ������ ��� ����������");
				break;
			}
			case "4":
			{
				System.out.println("������� ����� ���������� ������������");
				String login = scan.next();
				if(conn.checkUserExistance(login))//�������� ����� �������� �������������
				conn.deleteUser(login);
				else
					System.out.println("������������ � ����� ������� �� ����������");
				break;
			}
			default:{
				stopMark=true;System.out.println("�����");break;}
			}
			
			
		
		
	}conn.closeConnection();}
		catch(Exception e)
		{
			
		}
		finally {scan.close();}
		}
	
	public static void showSystemRequest()
	{
			
		switch(currentUserRole)
		{
			case "admin":
			{
				adminOptions();
				break;
			}
			case "Speaker":
			{
				speakerOptions();
				break;
			}
			case "Listener":
			{
				System.out.println("�������� ������ �����������");
			}
		}
		
	}
	

	//����������� ������������
	public static void userAuthorization()
	{
		System.out.println("������� �����");
		Scanner scanner = new Scanner(System.in).useDelimiter("\n");
		String login = scanner.next();
		System.out.println("������� ������");
		String password = scanner.next();
		
		if(!conn.checkUserExistance(login))//�������� ������������� ������������
		{
			System.out.println("������������ � ������ ������� �� ���������������");}
		else
		{if(conn.userAuthorization(login, password))//���� ������ ����������
		{
			login=conn.getLogin();
			currentUserRole=conn.getRole();
			System.out.println("Correct password\n");
			showSystemRequest();
		} else  System.out.println("Incorrect password");
		}
		scanner.close();
	}
	
	//����������� ������������
	public static  void userRegistration()
	{
		//���� ������ ������������
		System.out.println("������� �����");
		//��������� ��� ������������� �������� � ������� � ��
		Scanner scanner = new Scanner(System.in).useDelimiter("\n");
		String login = scanner.next();
		System.out.println("������� ������");
		String password = scanner.next();
		
		//��� ������������ ����������
		if(conn.checkUserExistance(login))
		{
			//��������� �� ������
			System.out.println("������������ � ������ ������� ���������������");}
		else {
		//����������� ������� ������ DBConnection
			//�� ��������� ������������ �������������� ��� ���������
		conn.userRegistration(login, password, "Listener",false);
		System.out.println("����������� �������");
		//���������� ������ � ������ ��� ���������� ������
		login=conn.getLogin();
		currentUserRole=conn.getRole();
		scanner.close();
		//����� �� ����� ����� ������������
		showSystemRequest();
		}
	}

	public static void main(String[] args) 
	{
		
		conn = new DBConnection();//�������� ����������� � ��
		System.out.println("������� 1, ����� ������������������\n������� 2, ����� �����\n"
				+ "����� ������ ���������� ������� � ����������");
		
		Scanner scanner = new Scanner(System.in);
		String inputedVariant = scanner.next();
		//����� �������� ������
		switch(inputedVariant)
		{
		case "1":
		{ 
			System.out.println("�����������");
			userRegistration();//����� ��� �����������
			break;
			}
		case "2":
		{ 
			System.out.println("����");
			userAuthorization();//����� ��� �����������
			break;
			}
		default:
		{
			System.out.println("�����");break;
			}
		}

	}

}
