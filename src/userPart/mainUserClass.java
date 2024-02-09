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
	//Вывод доступных комнат
	private static void showEnabledRooms(String[] roomIDs)
	{
		
		String rooms="Доступные аудитории:";
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
	//Выбор вариантов деятельности спикера
	private static void speakerOptions()
	{
		boolean stopMark=false;
		Scanner scan;
		while(!stopMark)
		{
			System.out.println("Введите 1, чтобы просмотреть список докладов\n"
					+ "Введите 2, чтобы просмотреть список ваших докладов\nВведите 3, чтобы посмотреть расписание\n"
					+ "Введите 4, чтобы добавить новый доклад\n"
					+ "Введите 5, чтобы стать спикером существующего доклада\n"
					+ "Введите 6, чтобы перенести свой доклад\nВведите 7, чтобы удалить доклад\n"
					+ "В случае ввода иного символа произойдёт выход\n");
			scan= new Scanner(System.in).useDelimiter("\n");
			String switched = scan.next();
			try {
				switch(switched.substring(0,1))
				{
				
				case "1":
				{
					//Получение и вывод массива докладов
					String[] mas = conn.getTalksList();
					
					if(mas.length>0)
					{for(int i=0;i<mas.length;i++)
					{
						System.out.println(mas[i]);
					}}
					else System.out.println("Докладов не обнаружено");
					System.out.println();
					break;
				}
				case "2":
				{
					//Получение и вывод массива докладов, в которых текущий
					//пользователь - спикер
					String[] mas = conn.getUserTalksList();
					if(mas.length>0)
					{for(int i=0;i<mas.length;i++)
					{
						System.out.println(mas[i]);
					}}
					else System.out.println("Докладов не обнаружено");
					System.out.println();
					break;
				}
				case "3":
				{
					//Получение и вывод расписания
					String[] mas = conn.getSchedule();
					for(int i=0;i<mas.length;i++)
					{
						System.out.println(mas[i]);
					}
					System.out.println();
					break;
				}
				case "4"://Добавление нового доклада
				{//Ввод значений
					System.out.println("Введите ID доклада");
					String buf = scan.next();
					int talkID = Integer.parseInt(buf.substring(0,buf.length()-1));
					System.out.println("Введите название доклада");
					String talkHeader=scan.next();
					//scan.close();
					scan = new Scanner(System.in);
					if(!conn.checkTalkExistance(talkID))
					{
						String[] roomIDs = conn.getEnabledRooms();
						showEnabledRooms(roomIDs);
						System.out.println("Введите номер аудитории");
						int roomID=scan.nextInt();
					
						//Если введенный номер комнаты есть в перечне доступных
						if(conn.checkRoomEnable(roomID, roomIDs))
						{//Вводим значение времени начала и окончания доклада
							boolean mark=true;
							while(mark)//Циклим, пока не будут введены возможные для помещения в расписание параметры
							{
								System.out.println("Введите время начала доклада");
								LocalTime time = LocalTime.parse(scan.next());
								System.out.println("Введите время окончания доклада");
								LocalTime time2 = LocalTime.parse(scan.next());
								if(conn.scheduleTheTalk(time, time2, roomID, talkHeader,talkID))
								{mark=false;}
							}	
						}
						else System.out.println("Комнаты не существует");
					}
					
					else
					{
						System.out.println("Доклад с таким ID уже существует");
					}
					break;
				}
				case "5":
				{
					System.out.println("Введите ID доклада");
					String buf = scan.next();
					int talkID = Integer.parseInt(buf.substring(0,buf.length()-1));
					//Если доклад существует, добавляем спикера
					if(conn.checkTalkExistance(talkID))
					{
						conn.addSpeakerToTalk(talkID);
					}else {System.out.println("Доклад не существует");}
					break;
				}
				case "6":
				{
					System.out.println("Введите ID доклада");
					String buf = scan.next();
					int talkID = Integer.parseInt(buf.substring(0,buf.length()-1));
					
					scan = new Scanner(System.in);
					if(conn.checkTalkExistance(talkID))
					{
						String[] roomIDs = conn.getEnabledRooms();
						showEnabledRooms(roomIDs);
						
						System.out.println("Введите номер аудитории");
						int roomID=scan.nextInt();
						if(conn.checkRoomEnable(roomID, roomIDs))
						{
						System.out.println("Введите время начала доклада");
						LocalTime time = LocalTime.parse(scan.next());
						System.out.println("Введите время окончания доклада");
						LocalTime time2 = LocalTime.parse(scan.next());
						if(conn.checkReschedule(talkID, time, time2, roomID, buf))
						{
							conn.reScheduling(talkID, time, time2,roomID);
							System.out.println("Перенос возможен");
						}
						else {System.out.println("Перенос невозможен");}
						}
						else {System.out.println("Комната недоступна");}
					}
					else System.out.println("Доклад не существует");break;
				}
					case "7":
					{
						
						System.out.println("Введите ID доклада");
						String buf = scan.next();
						int talkID = Integer.parseInt(buf.substring(0,buf.length()-1));
						
						
						if(conn.checkTalkExistance(talkID)&&conn.checkAuthors(talkID))
						{
							conn.deleteTalk(talkID);
							System.out.println("Успешно удалено");
						}
						else
						{
							System.out.println("Доклада с таким ID и вашим авторством не найдено");
						}
						break;
					}
				default:{stopMark=true;
				System.out.println("Выход");
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
		System.out.println("Введите 1, чтобы просмотреть список пользователей\n"
				+ "Введите 2, чтобы добавить пользователя\nВведите 3, чтобы сделать пользователя докладчиком\n"
			+ "Введите 4, чтобы удалить пользователя\nВ случае ввода иного символа произойдёт выход");
		//Цикл выбора опции до выбора выхода
		while(!stopMark)
		{
			System.out.println("Введите символ");
			String switched = scan.next();//Считываем
			switch(switched.substring(0,switched.length()-1))
			{ 
			case "1":
				{
					//Получаем список пользователей массивом строк и выводим
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
				System.out.println("Введите логин пользователя");
				
				String log = scan.next();
				if(conn.checkUserExistance(log))//Проверка существования пользователя 
				{conn.changeListenerToSpeaker(log);//Если существует, то меняем роль
				}
				else//В противном случае - сообщение об ошибке
				{
					System.out.println("Данного пользователя не существует");
				}
				break;
			}
			case "2":
			{
				System.out.println("Введите логин нового пользователя");//Считываем
				String login = scan.next();
				if(!conn.checkUserExistance(login))//Проверяем существование пользователя
				{
					System.out.println("Введите пароль нового пользователя");
					String password=scan.next();
					System.out.println("Введите роль нового пользователя: a-admin, s-speaker, другой символ - listener");
					String roleMarker = scan.next();
					switch(roleMarker.charAt(0))//Вызываем метод регистрации
					{	
						case 'a':{conn.userRegistration(login, password, "admin",true);break;}
						case 's':{conn.userRegistration(login, password, "Speaker",true);break;}
						default:{conn.userRegistration(login, password, "Listener",true);break;}
					}
				}
				else System.out.println("Пользователь с таким именем уже существует");
				break;
			}
			case "4":
			{
				System.out.println("Введите логин удаляемого пользователя");
				String login = scan.next();
				if(conn.checkUserExistance(login))//Удаление после проверки существования
				conn.deleteUser(login);
				else
					System.out.println("Пользователь с таким логином не существует");
				break;
			}
			default:{
				stopMark=true;System.out.println("Выход");break;}
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
				System.out.println("Ожидайте начала конференции");
			}
		}
		
	}
	

	//Авторизация пользователя
	public static void userAuthorization()
	{
		System.out.println("Введите Логин");
		Scanner scanner = new Scanner(System.in).useDelimiter("\n");
		String login = scanner.next();
		System.out.println("Введите пароль");
		String password = scanner.next();
		
		if(!conn.checkUserExistance(login))//Проверка существования пользователя
		{
			System.out.println("Пользователь с данным логином не зарегистрирован");}
		else
		{if(conn.userAuthorization(login, password))//Если пароль подтверждён
		{
			login=conn.getLogin();
			currentUserRole=conn.getRole();
			System.out.println("Correct password\n");
			showSystemRequest();
		} else  System.out.println("Incorrect password");
		}
		scanner.close();
	}
	
	//Регистрация пользователя
	public static  void userRegistration()
	{
		//Ввод данных пользователя
		System.out.println("Введите Логин");
		//Делимитер для использования пробелов в логинах и тд
		Scanner scanner = new Scanner(System.in).useDelimiter("\n");
		String login = scanner.next();
		System.out.println("Введите пароль");
		String password = scanner.next();
		
		//сли пользователь существует
		if(conn.checkUserExistance(login))
		{
			//Сообщение об ошибке
			System.out.println("Пользователь с данным логином зарегистрирован");}
		else {
		//Регистрация вызовом метода DBConnection
			//По умолчанию пользователь регистрируется как слушатель
		conn.userRegistration(login, password, "Listener",false);
		System.out.println("Регистрация успешна");
		//Сохранение логина и пароля для дальнейшей работы
		login=conn.getLogin();
		currentUserRole=conn.getRole();
		scanner.close();
		//Вывод на экран опций пользователя
		showSystemRequest();
		}
	}

	public static void main(String[] args) 
	{
		
		conn = new DBConnection();//Создание подключения к БД
		System.out.println("Введите 1, чтобы зарегистрироваться\nВведите 2, чтобы войти\n"
				+ "Любая другая комбинация приведёт к завершению");
		
		Scanner scanner = new Scanner(System.in);
		String inputedVariant = scanner.next();
		//выбор варианта работы
		switch(inputedVariant)
		{
		case "1":
		{ 
			System.out.println("Регистрация");
			userRegistration();//Метод для регистрации
			break;
			}
		case "2":
		{ 
			System.out.println("Вход");
			userAuthorization();//Метод для авторизации
			break;
			}
		default:
		{
			System.out.println("Выход");break;
			}
		}

	}

}
