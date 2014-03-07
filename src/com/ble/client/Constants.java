package com.ble.client;

public class Constants {
	
	public static String SERVICE_UUID 										= "00005301-0000-0041-4c50-574953450000";
	
//	 8  － 不具备读属性，可以写成功，但是返回值是发送的报文。 Property_write
	public static String Characteristic_UUID_WRITE							= "00005302-0000-0041-4c50-574953450000"; // 系统维护命令
	
//	48  － 不具备读属性，写失败   Property_notify
	public static String Characteristic_UUID_NOTIFY 						= "00005303-0000-0041-4c50-574953450000"; // 智能卡命令
	
	public static String CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb";
	
	public final static String ACTION_GATT_CONNECTED 						= "ACTION_GATT_CONNECTED";
	public final static String ACTION_GATT_DISCONNECTED 					= "ACTION_GATT_DISCONNECTED";
	public final static String ACTION_GATT_SERVICES_DISCOVERED 				= "ACTION_GATT_SERVICES_DISCOVERED";
	public final static String ACTION_GATT_DESCRIPTOR_WRITED				= "ACTION_GATT_DESCRIPTOR_WRITED";
	public final static String ACTION_DATA_AVAILABLE 						= "ACTION_DATA_AVAILABLE";
}
