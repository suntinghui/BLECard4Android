package com.ble.ex.client;

public enum BLETransferTypeEnum {
	
	TRANSFER_UNDEFINE(0, (byte)0x00),
	
	TRANSFER_DISCONNECT(1, (byte)0x01),
	TRANSFER_QUERYBALANCE(2, (byte)0x04),
	TRANSFER_RECHARGE(3, (byte)0x04),
	TRANSFER_QUERYHISTORY(4, (byte)0x04),
	
	TRANSFER_AUTH_INNER(84, (byte)0x01),
	TRANSFER_AUTH_OUTER(85, (byte)0x01);
	
	private int id;
	private byte type;

	private BLETransferTypeEnum(int id, byte type) {
		this.id = id;
		this.type = type;
	}

	public int getId() {
		return this.id;
	}

	public byte getType() {
		if (!Constants.SECURITY_VERSION){
			if (this.type == 0x04){
				return 0x02;
			}
		}
		
		return this.type;
	}
	
}
