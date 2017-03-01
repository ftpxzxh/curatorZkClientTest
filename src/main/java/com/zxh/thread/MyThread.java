package com.zxh.thread;

import java.io.IOException;

import com.zxh.mybatis.StockLockTest;

public class MyThread implements Runnable{

	public void run() {
		StockLockTest test= new StockLockTest();
		try {
			test.updateUserTest();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
